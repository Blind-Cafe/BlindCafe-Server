package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.response.HomeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainService {

    private final MatchingService matchingService;
    private final NoticeService noticeService;

    // 메인 화면
    public HomeResponse home(Long userId) {
        boolean status = matchingService.isMatchingRequest(userId);
        int ticketCount = matchingService.getTicketCount(userId);
        boolean unreceivedNotice = noticeService.isUnreceivedNotice(userId);
        return HomeResponse.builder()
                .status(status)
                .tickets(ticketCount)
                .notice(unreceivedNotice)
                .build();
    }
}
