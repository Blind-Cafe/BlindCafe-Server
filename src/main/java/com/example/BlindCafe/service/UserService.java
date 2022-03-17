package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.*;
import com.example.BlindCafe.dto.request.AddUserInfoRequest;
import com.example.BlindCafe.dto.request.EditInterestRequest;
import com.example.BlindCafe.entity.*;
import com.example.BlindCafe.entity.type.status.UserStatus;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.*;
import com.example.BlindCafe.entity.type.status.CommonStatus;
import com.example.BlindCafe.entity.type.status.MatchingStatus;
import com.example.BlindCafe.util.AmazonS3Connector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.BlindCafe.exception.CodeAndMessage.*;
import static com.example.BlindCafe.entity.type.Gender.N;
import static com.example.BlindCafe.entity.type.ReasonType.FOR_RETIRED;
import static com.example.BlindCafe.entity.type.status.CommonStatus.*;
import static com.example.BlindCafe.entity.type.status.MatchingStatus.*;

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
    private final AmazonS3Connector amazonS3Connector;

    private final static int USER_INTEREST_LENGTH = 3;
    private static int[][] interestOrderArr = new int[USER_INTEREST_LENGTH][2];
    private static int index;
    private static int editIndex;

    @Transactional
    public void addUserInfo(Long userId, AddUserInfoRequest request) {
        validateAddUserInfo(request);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));

        // 이미 추가 정보를 입력받은 경우 핸들링
        if (user.getStatus().equals(UserStatus.NORMAL))
            throw new BlindCafeException(ALREADY_REQUIRED_INFO);

        // 사용자 추가 정보 저장
        user.addRequiredInfo(request.getAge(), request.getMyGender(), request.getEmail(), request.getNickname(), request.getPartnerGender());

        // 관심사 조회
        Map<Long, List<String>> interestMap = new HashMap<>();
        // HashMap의 keySet()사용할 경우 main interest의 순서가 변경되기 때문에 별도의 list 활용
        List<Long> mainInterestIds = new ArrayList<>();
        request.getInterests().forEach(i -> {
            interestMap.put(i.getMain(), i.getSub());
            mainInterestIds.add(i.getMain());
        });

        List<Interest> mainInterests = interestRepository.findByIdIn(mainInterestIds);
        if (mainInterests.size() != USER_INTEREST_LENGTH)
            throw new BlindCafeException(INVALID_MAIN_INTEREST);

        // 관심사 저장
        List<Interest> userInterest = new ArrayList<>();
        mainInterests.forEach(mainInterest -> {
            // 메인관심사
            userInterest.add(mainInterest);
            // 세부관심사 저장
            interestMap.get(mainInterest).forEach(sub -> {
                Interest subInterest = mainInterest.getChild()
                        .stream()
                        .filter(si -> si.getName().equals(sub))
                        .findAny().orElseThrow(() -> new BlindCafeException(INVALID_SUB_INTEREST));
                userInterest.add(subInterest);
            });
        });
        user.updateInterest(userInterest);
    }

    private void validateAddUserInfo(AddUserInfoRequest request) {
        if (request.getMyGender().equals(N))
            throw new BlindCafeException(BAD_REQUEST);

        // TODO 지금은 닉네임 중복체크, 이후 변경 사항에 따라 수정 예정
        userRepository.findByNickname(request.getNickname())
                .ifPresent(u -> { throw new BlindCafeException(DUPLICATED_NICKNAME); });
    }

    @Transactional
    public UserHomeDto.Response userHome(Long userId) {

        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus().equals(NORMAL) || u.getStatus().equals(NOT_REQUIRED_INFO))
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        // 매칭 상태 확인
        UserMatching userMatching = user.getUserMatchings().stream()
                .filter(um -> validMatchingStatus(um))
                .findAny().orElse(null);

        if (userMatching == null) {
            // 요청 없음
            return UserHomeDto.Response.noneMatchingBuilder()
                    .codeAndMessage(SUCCESS)
                    .matchingStatus(NONE)
                    .build();
        }

        MatchingStatus status = userMatching.getStatus();
        Matching matching = userMatching.getMatching();
        updateMatchingStatus(matching);

        if (status.equals(WAIT)) {
            if (ChronoUnit.HOURS.between(userMatching.getCreatedAt(), LocalDateTime.now()) >= 24L) {
                // 24시간 초과로 인한 요청 취소
                userMatching.setStatus(CANCEL_REQUEST_EXPIRED);
                throw new BlindCafeException(REQUEST_EXPIRED);
            } else {
                return UserHomeDto.Response.noneMatchingBuilder()
                        .codeAndMessage(SUCCESS)
                        .matchingStatus(WAIT)
                        .build();
            }
        } else {
            UserMatching partnerMatching = matching.getUserMatchings().stream()
                    .filter(um -> !um.equals(userMatching))
                    .findAny().orElse(null);

            if (Objects.isNull(partnerMatching)) {
                userMatching.setStatus(OUT);
                throw new BlindCafeException(INVALID_MATCHING);
            }


            if (status.equals(FAILED_LEAVE_ROOM) ||
                    status.equals(FAILED_REPORT) ||
                    status.equals(FAILED_WONT_EXCHANGE)) {
                // 폭파
                UserHomeDto.Response response = UserHomeDto.Response.outMatchingBuilder()
                        .codeAndMessage(SUCCESS)
                        .matchingStatus(userMatching.getStatus())
                        .partnerNickname(partnerMatching.getUser().getNickname())
                        .reason(userMatching.getReason().getText())
                        .build();
                userMatching.setStatus(OUT);
                return response;
            }

            // 정상적인 매칭 상태
            LocalDateTime ldt = matching.getStartTime();
            Timestamp timestamp = Timestamp.valueOf(ldt);
            String startTime = String.valueOf(timestamp.getTime() / 1000);

            if (status.equals(MATCHING_CONTINUE_YET)) {
                userMatching.setStatus(MATCHING_CONTINUE);
            }

            return UserHomeDto.Response.matchingBuilder()
                    .codeAndMessage(SUCCESS)
                    .matchingStatus(userMatching.getStatus())
                    .matchingId(matching.getId())
                    .partnerId(partnerMatching.getUser().getId())
                    .partnerNickname(partnerMatching.getUser().getNickname())
                    .startTime(startTime)
                    .build();
        }
    }

    private boolean validMatchingStatus(UserMatching userMatching) {
        MatchingStatus status = userMatching.getStatus();
        if (status.equals(WAIT) ||
            status.equals(FOUND) ||
            status.equals(MATCHING) ||
            status.equals(FAILED_LEAVE_ROOM) ||
            status.equals(FAILED_REPORT) ||
            status.equals(FAILED_WONT_EXCHANGE) ||
            status.equals(PROFILE_OPEN) ||
            status.equals(PROFILE_READY) ||
            status.equals(PROFILE_ACCEPT) ||
            status.equals(MATCHING_CONTINUE_YET)
        ) return true;
        return false;
    }

    private void updateMatchingStatus(Matching matching) {
        if (Objects.isNull(matching))
            return;
        if (!matching.getStatus().equals(MATCHING))
            return;
        if (matching.getExpiryTime().isAfter(LocalDateTime.now()))
            return;
        List<UserMatching> userMatchings = matching.getUserMatchings();
        for (UserMatching userMatching: userMatchings) {
            userMatching.setStatus(PROFILE_OPEN);
        }
        matching.setStatus(PROFILE_EXCHANGE);
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
    public EditInterestDto.Response editInterest(Long userId, EditInterestDto.Request request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        userInterestRepository.deleteAllByUser(user);
        interestOrderRepository.deleteAllByUser(user);

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
    public void editProfileImage(
            Long userId,
            int priority,
            MultipartFile image
    ) {
        validatePriority(priority);

        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus().equals(NORMAL) || u.getStatus().equals(NOT_REQUIRED_INFO))
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        uploadProfileImage(user, priority, image);
    }

    private void uploadProfileImage(User user, int priority, MultipartFile image) {
        Avatar avatar = profileImageRepository
                .findByUserIdAndPriorityAndStatus(
                        user.getId(), priority, CommonStatus.NORMAL)
                .orElse(null);

        if (avatar != null) {
            user.getAvatars().remove(avatar);
            avatar.setStatus(DELETED);
        }

        if (Objects.isNull(image.getContentType())) {
            return;
        }

        String src = amazonS3Connector.uploadProfileImage(image, user.getId());

        Avatar newAvatar = Avatar.builder()
                .user(user)
                .src(src)
                .priority(priority)
                .status(CommonStatus.NORMAL)
                .build();
        profileImageRepository.save(newAvatar);
        user.getAvatars().add(newAvatar);
    }

    private void validatePriority(int priority) {
        if (priority < 1 && priority > 3)
            throw new BlindCafeException(INVALID_PROFILE_IMAGE_PRIORITY);
    }

    @Transactional
    public DeleteUserDto.Response deleteUser(Long userId, Long reasonNum) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));

        Reason reason = reasonRepository.findByReasonTypeAndNum(FOR_RETIRED, reasonNum)
                .orElseThrow(() -> new BlindCafeException(NO_REASON));

        RetiredUser retiredUser = RetiredUser.builder()
                .nickname(user.getNickname())
                .socialId(user.getSocialId())
                .socialType(user.getSocialType())
                .reason(reason)
                .build();

        retiredUserRepository.save(retiredUser);

        // userRepository.delete(user);
        user.setSocialId(UUID.randomUUID().toString());
        user.setStatus(RETIRED);

        return DeleteUserDto.Response.builder()
                .codeAndMessage(SUCCESS)
                .nickname(retiredUser.getNickname())
                .build();
    }

    @Transactional
    public void editPartnerGender(Long userId, EditPartnerGenderDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));
        user.setPartnerGender(request.getGender());
    }

    @Transactional
    public void updateDeviceToken(Long userId, EditDeviceToken request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));
        user.setDeviceId(request.getToken());
    }

    public EditUserProfileDto.Response getMyProfileForEdit(Long userId) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus().equals(NORMAL) || u.getStatus().equals(NOT_REQUIRED_INFO))
                .orElseThrow(() -> new BlindCafeException(NO_USER));
        return EditUserProfileDto.Response.fromEntity(user);
    }

    @Transactional
    public EditUserProfileDto.Response editProfile(Long userId, EditUserProfileDto.Request request) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus().equals(NORMAL) || u.getStatus().equals(NOT_REQUIRED_INFO))
                .orElseThrow(() -> new BlindCafeException(NO_USER));
        // 닉네임 수정
        user.setNickname(request.getNickname());
        // 매칭 상대방 수정
        user.setPartnerGender(request.getPartnerGender());
        // 지역 수정
        user.setAddress(new Address(request.getState(), request.getRegion()));
        return EditUserProfileDto.Response.fromEntity(user);
    }

    public ProfileImageListDto getProfileImages(Long userId) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus().equals(NORMAL) || u.getStatus().equals(NOT_REQUIRED_INFO))
                .orElseThrow(() -> new BlindCafeException(NO_USER));
        return ProfileImageListDto.builder()
                .images(user.getAvatars().stream()
                        .filter(profileImage -> profileImage.getStatus().equals(CommonStatus.NORMAL))
                        .sorted(Comparator.comparing(Avatar::getPriority))
                        .map(Avatar::getSrc)
                        .collect(Collectors.toList()))
                .build();
    }

    public ProfileImageListDto getProfileImagesForEdit(Long userId) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus().equals(NORMAL) || u.getStatus().equals(NOT_REQUIRED_INFO))
                .orElseThrow(() -> new BlindCafeException(NO_USER));
        String[] images = new String[3];
        Arrays.fill(images, "");
        ArrayList<Avatar> avatars = new ArrayList<>();
        avatars.addAll(user.getAvatars().stream()
                .filter(profileImage -> profileImage.getStatus().equals(CommonStatus.NORMAL))
                .sorted(Comparator.comparing(Avatar::getPriority))
                .collect(Collectors.toList()));
        for (Avatar pi: avatars) {
            images[pi.getPriority()-1] = pi.getSrc();
        }

        return ProfileImageListDto.builder()
                .images(Arrays.asList(images))
                .build();
    }

    @Transactional
    public void deleteProfileImage(Long userId, int priority) {
        validatePriority(priority);
        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus().equals(NORMAL) || u.getStatus().equals(NOT_REQUIRED_INFO))
                .orElseThrow(() -> new BlindCafeException(NO_USER));
        Avatar avatar = profileImageRepository
                .findByUserIdAndPriorityAndStatus(
                        user.getId(), priority, CommonStatus.NORMAL)
                .orElseThrow(() -> new BlindCafeException(NO_PROFILE_IMAGE));

        user.getAvatars().remove(avatar);
        avatar.setStatus(DELETED);
    }

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(NO_USER));
        return new UserProfileResponse(user);
    }
}
