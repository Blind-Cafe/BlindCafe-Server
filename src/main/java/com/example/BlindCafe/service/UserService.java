package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.UserProfileResponse;
import com.example.BlindCafe.dto.request.*;
import com.example.BlindCafe.domain.*;
import com.example.BlindCafe.domain.type.status.UserStatus;
import com.example.BlindCafe.dto.response.AvatarListResponse;
import com.example.BlindCafe.dto.response.DeleteUserResponse;
import com.example.BlindCafe.dto.response.UserDetailResponse;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.repository.*;
import com.example.BlindCafe.utils.AmazonS3Connector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.BlindCafe.exception.CodeAndMessage.*;
import static com.example.BlindCafe.domain.type.Gender.N;
import static com.example.BlindCafe.domain.type.ReasonType.FOR_RETIRED;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RetiredUserRepository retiredUserRepository;
    private final InterestRepository interestRepository;
    private final ReasonRepository reasonRepository;
    private final SuggestionRepository suggestionRepository;

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
    public UserDetailResponse editProfile(Long userId, UpdateProfileRequest request) {
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
    public void editInterest(Long userId, UpdateInterestRequest request) {
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
    public void updateAvatar(Long userId, UpdateAvatarRequest request) {
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
        if (sequence < 1 || sequence > USER_AVATAR_MAX_LENGTH)
            throw new BlindCafeException(INVALID_PROFILE_IMAGE_SEQUENCE);
    }

    /**
     * 사용자 목소리 설정하기
     */
    @Transactional
    public void updateVoice(Long userId, UpdateVoiceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        user.updateVoice(request.getVoice());
    }

    /**
     * 사용자 목소리 삭제하기
     */
    @Transactional
    public void deleteVoice(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        user.deleteVoice();
    }

    /**
     * 사용자 탈퇴하기
     */
    @Transactional
    public DeleteUserResponse deleteUser(Long userId, Long reasonId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));

        Reason reason = reasonRepository.findByReasonTypeAndNum(FOR_RETIRED, reasonId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_REASON));

        RetiredUser retiredUser = RetiredUser.create(user, reason.getText());
        retiredUserRepository.save(retiredUser);
        userRepository.delete(user);
        return DeleteUserResponse.fromEntity(retiredUser);
    }

    /**
     * 사용자 프로필 조회
     */
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        return UserProfileResponse.fromEntity(user);
    }

    /**
     * 건의사항 접수하기
     */
    @Transactional
    public void suggest(Long userId, SuggestionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(EMPTY_USER));
        
        // 건의사항 저장하기
        Suggestion suggestion = Suggestion.create(user, request.getContent());
        suggestionRepository.save(suggestion);
        String image = amazonS3Connector.uploadSuggestion(request.getImages(), suggestion.getId());
        suggestion.updateImage(image);
        
        // 건의사항 이메일로 전송하기
    }
}
