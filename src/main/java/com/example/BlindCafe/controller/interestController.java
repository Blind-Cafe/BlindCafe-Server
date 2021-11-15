package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.InterestDto;
import com.example.BlindCafe.service.InterestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interest")
public class interestController {

    private final InterestService interestService;

    @GetMapping()
    public InterestDto getSubInterest(
            Authentication authentication,
            @RequestParam(value="id") List<Long> interestIds
    ) {
        log.info("GET /api/interest");
        return interestService.getSubInterest(interestIds);
    }
}
