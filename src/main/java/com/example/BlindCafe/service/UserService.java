package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.*;
import com.example.BlindCafe.dto.CreateUserInfoDto;
import com.example.BlindCafe.entity.*;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.exception.CodeAndMessage;
import com.example.BlindCafe.repository.*;
import com.example.BlindCafe.type.ReasonType;
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
import static com.example.BlindCafe.type.Gender.N;
import static com.example.BlindCafe.type.ReasonType.FOR_RETIRED;
import static com.example.BlindCafe.type.status.CommonStatus.*;
import static com.example.BlindCafe.type.status.MatchingStatus.*;
import static com.example.BlindCafe.type.status.UserStatus.NORMAL;
import static com.example.BlindCafe.type.status.UserStatus.NOT_REQUIRED_INFO;

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
    private final ReasonRepository reasonRepository;

    private final static int USER_INTEREST_LENGTH = 3;
    private static int[][] interestOrderArr = new int[USER_INTEREST_LENGTH][2];
    private static int index;
    private static int editIndex;


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
                .filter(u -> u.getStatus().equals(NORMAL) || u.getStatus().equals(NOT_REQUIRED_INFO))
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
    public DeleteUserDto.Response deleteUser(Long userId, Long reasonNum) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        Reason reason = reasonRepository.findByReasonTypeAndNum(FOR_RETIRED, reasonNum)
                .orElseThrow(() -> new BlindCafeException(INVALID_REASON));

        RetiredUser retiredUser = RetiredUser.builder()
                .nickname(user.getNickname())
                .socialId(user.getSocialId())
                .socialType(user.getSocialType())
                .reason(reason)
                .build();

        retiredUserRepository.save(retiredUser);

        /**
         * Todo
         * userMatching 걸려있는 상대방 user matching들에 탈퇴로 채팅 종료 상태 변경
         */

        userRepository.delete(user);

        return DeleteUserDto.Response.builder()
                .codeAndMessage(SUCCESS)
                .nickname(retiredUser.getNickname())
                .build();
    }
}
