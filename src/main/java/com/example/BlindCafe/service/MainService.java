package com.example.BlindCafe.service;

import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.dto.response.HomeResponse;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.exception.CodeAndMessage;
import com.example.BlindCafe.repository.UserRepository;
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
        int ticketCount = matchingService.getTicketCount(user);
        boolean unreceivedNotice = noticeService.isUnreceivedNotice(userId);
        return HomeResponse.builder()
                .request(request)
                .tickets(ticketCount)
                .notice(unreceivedNotice)
                .build();
    }
}
