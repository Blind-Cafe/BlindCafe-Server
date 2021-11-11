package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.*;
import com.example.BlindCafe.dto.CreateUserInfoDto;
import com.example.BlindCafe.entity.*;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.exception.CodeAndMessage;
import com.example.BlindCafe.repository.*;
import com.example.BlindCafe.type.Social;
import com.example.BlindCafe.type.status.CommonStatus;
import com.example.BlindCafe.type.status.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.BlindCafe.exception.CodeAndMessage.*;
import static com.example.BlindCafe.auth.jwt.JwtUtils.createToken;
import static com.example.BlindCafe.type.Gender.N;
import static com.example.BlindCafe.type.Social.APPLE;
import static com.example.BlindCafe.type.Social.KAKAO;
import static com.example.BlindCafe.type.status.CommonStatus.*;
import static com.example.BlindCafe.type.status.MatchingStatus.*;
import static com.example.BlindCafe.auth.SocialUtils.*;
import static com.example.BlindCafe.type.status.UserStatus.*;
import static com.example.BlindCafe.type.status.UserStatus.NORMAL;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RetiredUserRepository retiredUserRepository;
    private final InterestRepository interestRepository;
    private final UserInterestRepository userInterestRepository;
    private final InterestOrderRepository interestOrderRepository;
    private final ProfileImageRepository profileImageRepository;

    private final static int USER_INTEREST_LENGTH = 3;
    private static int[][] interestOrderArr = new int[USER_INTEREST_LENGTH][2];
    private static int index;
    private static int editIndex;

    @Transactional
    public LoginDto.Response signin(LoginDto.Request request, Social social) {
        LoginDto.SocialResponse socialResponse = getInfoByToken(request.getToken(), social);
        String deviceId = request.getDeviceId();

        Optional<User> userOptional = userRepository.findBySocialId(socialResponse.getSocialId());
        boolean isRegistered = userOptional.isPresent();

         if (isRegistered) {
             User user = userOptional.get();
             // 신고 유저
             if (user.getStatus().equals(SUSPENDED))
                 throw new BlindCafeException(SUSPENDED_USER);
             else {
                 // 디바이스 토큰 업데이트
                 if (!user.getDeviceId().equals(deviceId)) {
                     user.setDeviceId(deviceId);
                     userRepository.save(user);
                 }
                 if (user.getStatus().equals(NOT_REQUIRED_INFO)) {
                     // 필수 정보가 입력되지 않은 경우
                     return getLoginResponse(user, SIGN_IN_NOT_REQUIRED_INFO);
                 } else {
                     // 필수 정보가 입력된 경우 정상 로그인
                     return getLoginResponse(user, SIGN_IN);
                 }
             }
         } else {
             // 회원가입
             User user = User.builder()
                     .socialId(socialResponse.getSocialId())
                     .socialType(socialResponse.getSocialType())
                     .deviceId(deviceId)
                     .status(UserStatus.NOT_REQUIRED_INFO)
                     .build();
             userRepository.save(user);
             return getLoginResponse(user, SIGN_UP);
         }
    }

    /**
     * 엑세스 토큰으로 유저 정보 얻기
     * @param token
     * @return 유저 정보
     */
    private LoginDto.SocialResponse getInfoByToken(String token, Social social) {
        if (social.equals(KAKAO)) {
            // 카카오 로그인
            return LoginDto.SocialResponse.builder()
                    .socialId(verifyKakaoToken(token))
                    .socialType(KAKAO)
                    .build();
        } else if (social.equals(APPLE)) {
            return LoginDto.SocialResponse.builder()
                    .socialId(verifyAppleToken(token))
                    .socialType(APPLE)
                    .build();
        } else {
            throw new BlindCafeException(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 토큰 생성 및 반환하기
     */
    private LoginDto.Response getLoginResponse(User user, CodeAndMessage codeAndMessage) {
        String token = createToken(user);
        String nickname = user.getNickname() != null ? user.getNickname() : null;
        return LoginDto.Response.builder()
                .codeAndMessage(codeAndMessage)
                .jwt(token)
                .id(user.getId())
                .nickname(nickname)
                .build();
    }

    @Transactional
    public CreateUserInfoDto.Response addUserInfo(User user, CreateUserInfoDto.Request request) {
        validateAddUserInfo(request);

        // 유저 정보 저장
        user.setAge(request.getAge());
        user.setMyGender(request.getMyGender());
        user.setNickname(request.getNickname());
        user.setPartnerGender(request.getPartnerGender());
        user.setStatus(NORMAL);
        userRepository.save(user);

        // 관심사 저장
        index = 0;
        request.getInterests().forEach(interest -> {
            // 메인
            Interest mainInterest = interestRepository.findById(interest.getMain())
                    .orElseThrow(()-> new BlindCafeException(INVALID_MAIN_INTEREST));

            UserInterest userInterest = UserInterest.builder()
                    .user(user)
                    .interest(mainInterest)
                    .build();
            userInterestRepository.save(userInterest);

            // 세부
            interest.getSub().forEach(sub -> {
                UserInterest subInterest = UserInterest.builder()
                        .user(user)
                        .interest(mainInterest.getChild()
                                .stream()
                                .filter(si -> si.getName().equals(sub))
                                .findAny().orElseThrow(() -> new BlindCafeException(INVALID_SUB_INTEREST))
                        )
                        .build();
                userInterestRepository.save(subInterest);
            });

            interestOrderArr[index][0] = interest.getMain().intValue();
            interestOrderArr[index][1] = interest.getSub().size();
            index++;
        });

        // 관심사 순위 저장
        sortBySubInterestCount();
        for (int priority=0; priority<USER_INTEREST_LENGTH; priority++) {
            InterestOrder interestOrder = InterestOrder.builder()
                    .user(user)
                    .interest(
                            interestRepository.findById(
                                    Long.valueOf(interestOrderArr[priority][0])
                            ).orElseThrow(()-> new BlindCafeException(INVALID_MAIN_INTEREST)))
                    .priority(priority+1)
                    .build();
            interestOrderRepository.save(interestOrder);
        }

        return CreateUserInfoDto.Response.builder()
                .codeAndMessage(SUCCESS).build();
    }

    private void validateAddUserInfo(CreateUserInfoDto.Request request) {
        if (request.getMyGender().equals(N))
            throw new BlindCafeException(INVALID_REQUEST);
    }

    private void sortBySubInterestCount() {
        Arrays.sort(interestOrderArr, new Comparator<int[]>() {
            @Override
            public int compare(int[] o1, int[] o2) {
                return o2[1] - o1[1];
        }});
    }

    @Transactional
    public UserHomeDto.Response userHome(Long userId) {

        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus().equals(NORMAL))
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        // 매칭 상태 확인
        List<UserMatching> matchings = user.getUserMatchings().stream()
                .filter(userMatching ->
                        userMatching.getStatus().equals(WAIT) ||
                        userMatching.getStatus().equals(FOUND) ||
                        userMatching.getStatus().equals(MATCHING))
                .collect(Collectors.toList());

        if (matchings.size() < 1) {
            // 요청 없음
            return UserHomeDto.Response.noneMatchingBuilder()
                    .codeAndMessage(SUCCESS)
                    .matchingStatus(NONE)
                    .build();
        } else {
            UserMatching validMatching = matchings.get(0);
            if (validMatching.getStatus().equals(WAIT)) {
                return UserHomeDto.Response.noneMatchingBuilder()
                        .codeAndMessage(SUCCESS)
                        .matchingStatus(WAIT)
                        .build();
            } else {
                Matching matching = validMatching.getMatching();
                List<UserMatching> userMatchings = matching.getUserMatchings()
                        .stream()
                        .filter(mat -> !mat.equals(validMatching))
                        .collect(Collectors.toList());
                UserMatching partnerMatching = userMatchings.get(0);

                LocalDateTime ldt = matching.getStartTime();
                Timestamp timestamp = Timestamp.valueOf(ldt);
                String startTime = String.valueOf(timestamp.getTime() / 1000);

                return UserHomeDto.Response.matchingBuilder()
                        .codeAndMessage(SUCCESS)
                        .matchingStatus(validMatching.getStatus())
                        .matchingId(matching.getId())
                        .partnerId(partnerMatching.getUser().getId())
                        .partnerNickname(partnerMatching.getUser().getNickname())
                        .startTime(startTime)
                        .build();
            }
        }
    }

    public UserDetailDto getUserDetail(Long userId) {
        return userRepository.findById(userId)
                .map(UserDetailDto::fromEntity)
                .orElseThrow(() -> new BlindCafeException(NO_USER));
    }

    /**
     * Todo
     * 지금은 예전 관심사 그냥 삭제하는데
     * 나중에는 예전 관심사 보관하기
     * + 유저 엔티티에서 연관관계 메소드로 현재 관심사만 가져오기
     */
    @Transactional
    public EditInterestDto.Response editInterest(User user, EditInterestDto.Request request) {
        userInterestRepository.deleteAllByUserId(user.getId());

        // 관심사 저장
        editIndex = 0;
        request.getInterests().forEach(interest -> {
            // 메인
            Interest mainInterest = interestRepository.findById(interest.getMain())
                    .orElseThrow(()-> new BlindCafeException(INVALID_MAIN_INTEREST));

            UserInterest userInterest = UserInterest.builder()
                    .user(user)
                    .interest(mainInterest)
                    .build();
            userInterestRepository.save(userInterest);

            // 세부
            interest.getSub().forEach(sub -> {
                UserInterest subInterest = UserInterest.builder()
                        .user(user)
                        .interest(mainInterest.getChild()
                                .stream()
                                .filter(si -> si.getName().equals(sub))
                                .findAny().orElseThrow(() -> new BlindCafeException(INVALID_SUB_INTEREST))
                        )
                        .build();
                userInterestRepository.save(subInterest);
            });

            interestOrderArr[editIndex][0] = interest.getMain().intValue();
            interestOrderArr[editIndex][1] = interest.getSub().size();
            editIndex++;
        });

        // 관심사 순위 저장
        sortBySubInterestCount();
        for (int priority=0; priority<USER_INTEREST_LENGTH; priority++) {
            InterestOrder interestOrder = InterestOrder.builder()
                    .user(user)
                    .interest(
                            interestRepository.findById(
                                    Long.valueOf(interestOrderArr[priority][0])
                            ).orElseThrow(()-> new BlindCafeException(INVALID_MAIN_INTEREST)))
                    .priority(priority+1)
                    .build();
            interestOrderRepository.save(interestOrder);
        }

        return EditInterestDto.Response.builder()
                .codeAndMessage(SUCCESS).build();
    }

    @Transactional
    public EditNicknameDto.Response editNickname(Long userId, EditNicknameDto.Request request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));
        user.setNickname(request.getNickname());
        return EditNicknameDto.Response.fromEntity(user);
    }

    @Transactional
    public EditAddressDto.Response editAddress(Long userId, EditAddressDto.Request request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));
        user.setAddress(new Address(request.getState(), request.getRegion()));
        return EditAddressDto.Response.fromEntity(user);
    }

    @Transactional
    public EditProfileImageDto.Response editProfileImage(
            Long userId,
            EditProfileImageDto.Request request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        ProfileImage profileImage = profileImageRepository
                .findByUserIdAndPriorityAndStatus(
                        userId, request.getPriority(), CommonStatus.NORMAL)
                .orElse(null);

        if (profileImage != null) {
            user.getProfileImages().remove(profileImage);
            profileImage.setStatus(DELETED);
        }
        ProfileImage newProfileImage = ProfileImage.builder()
                .user(user)
                .src(request.getSrc())
                .priority(request.getPriority())
                .status(CommonStatus.NORMAL)
                .build();
        profileImageRepository.save(newProfileImage);
        user.getProfileImages().add(newProfileImage);
        return EditProfileImageDto.Response.fromEntity(user);
    }

    @Transactional
    public DeleteUserDto.Response deleteUser(Long userId, Long reasonType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        RetiredUser retiredUser = RetiredUser.builder()
                .nickname(user.getNickname())
                .socialId(user.getSocialId())
                .socialType(user.getSocialType())
                .reasonType(reasonType)
                .build();

        retiredUserRepository.save(retiredUser);
        userRepository.delete(user);

        return DeleteUserDto.Response.builder()
                .codeAndMessage(SUCCESS)
                .nickname(retiredUser.getNickname())
                .build();
    }
}
