package me.blindcafe.blindcafe.service;

import me.blindcafe.blindcafe.domain.User;
import me.blindcafe.blindcafe.dto.response.HomeResponse;
import me.blindcafe.blindcafe.exception.BlindCafeException;
import me.blindcafe.blindcafe.exception.CodeAndMessage;
import me.blindcafe.blindcafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainService {

    private final UserRepository userRepository;
    private final MatchingService matchingService;
    private final NoticeService noticeService;

    // 메인 화면
    @Transactional
    public HomeResponse home(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BlindCafeException(CodeAndMessage.EMPTY_USER));

        boolean request = matchingService.isMatchingRequest(user);
        int ticketCount = user.getTicket().getCount();
        boolean unreceivedNotice = noticeService.isUnreceivedNotice(userId);
        return HomeResponse.builder()
                .request(request)
                .tickets(ticketCount)
                .notice(unreceivedNotice)
                .build();
    }
}
