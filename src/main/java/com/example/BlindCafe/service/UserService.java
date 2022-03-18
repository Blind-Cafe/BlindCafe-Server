package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.*;
import com.example.BlindCafe.dto.request.AddUserInfoRequest;
import com.example.BlindCafe.domain.*;
import com.example.BlindCafe.domain.type.status.UserStatus;
import com.example.BlindCafe.dto.request.EditInterestRequest;
import com.example.BlindCafe.dto.request.EditProfileRequest;
import com.example.BlindCafe.dto.request.UploadAvatarRequest;
import com.example.BlindCafe.dto.response.AvatarListResponse;
import com.example.BlindCafe.dto.response.UserDetailResponse;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.*;
import com.example.BlindCafe.domain.type.status.CommonStatus;
import com.example.BlindCafe.util.AmazonS3Connector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.BlindCafe.exception.CodeAndMessage.*;
import static com.example.BlindCafe.domain.type.Gender.N;
import static com.example.BlindCafe.domain.type.ReasonType.FOR_RETIRED;
import static com.example.BlindCafe.domain.type.status.CommonStatus.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    private final ReasonRepository reasonRepository;

    private final AmazonS3Connector amazonS3Connector;

    private static final int USER_INTEREST_LENGTH = 3;
    private static final int USER_AVATAR_MAX_LENGTH = 3;

    /**
     * 사용자 추가 정보 입력
     */
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
        user.addRequiredInfo(request.getAge(), request.getMyGender(), request.getPhone(), request.getNickname(), request.getPartnerGender());

        // 관심사 저장
        updateInterest(user, request.getInterests());
    }

    // 사용자 추가 정보 입력 요청값 유효성 검사
    private void validateAddUserInfo(AddUserInfoRequest request) {
        if (request.getMyGender().equals(N))
            throw new BlindCafeException(BAD_REQUEST);

        Pattern pattern = Pattern.compile("^\\d{3}[- .]?\\d{4}[- .]?\\d{4}$");
        Matcher matcher = pattern.matcher(request.getPhone());
        if (!matcher.matches())
            throw new BlindCafeException(INVALID_PHONE_NUMBER);

        userRepository.findByNickname(request.getNickname())
                .ifPresent(u -> { throw new BlindCafeException(DUPLICATED_PHONE_NUMBER); });
    }

    /**
     * 마이페이지 (사용자 정보 조회)
     */
    public UserDetailResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        return UserDetailResponse.fromEntity(user);
    }

    /**
     * 사용자 프로필 수정
     */
    @Transactional
    public UserDetailResponse editProfile(Long userId, EditProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        user.updateProfile(
                new Address(request.getState(), request.getRegion()),
                request.getPartnerGender(),
                request.getMbti());
        return UserDetailResponse.fromEntity(user);
    }

    /**
     * 사용자 관심사 수정
     */
    @Transactional
    public void editInterest(Long userId, EditInterestRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        updateInterest(user, request.getInterests());
    }

    // 사용자 관심사 수정
    private void updateInterest(User user, List<Long> interestIds) {
        List<Interest> interests = interestRepository.findByIdIn(interestIds);
        if (interests.size() != USER_INTEREST_LENGTH)
            throw new BlindCafeException(INVALID_MAIN_INTEREST);
        user.updateInterest(interests);
    }

    /**
     * 프로필 이미지 리스트 조회
     */
    public AvatarListResponse getAvatars(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        return AvatarListResponse.fromEntity(user);
    }

    /**
     * 프로필 이미지 업로드/수정
     */
    @Transactional
    public void uploadAvatar(Long userId, UploadAvatarRequest request) {
        int sequence = request.getSequence();
        validateAvatarSequence(sequence);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        // S3에 이미지 업로드
        String src = amazonS3Connector.uploadAvatar(request.getImage(), userId);
        user.updateAvatar(src, sequence);
    }

    /**
     * 프로필 이미지 삭제
     */
    @Transactional
    public void deleteAvatar(Long userId, int sequence) {
        validateAvatarSequence(sequence);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        user.deleteAvatar(sequence);
    }

    private void validateAvatarSequence(int sequence) {
        if (sequence < 1 && sequence > USER_AVATAR_MAX_LENGTH)
            throw new BlindCafeException(INVALID_PROFILE_IMAGE_SEQUENCE);
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
