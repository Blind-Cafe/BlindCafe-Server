package com.example.BlindCafe.controller;

import com.example.BlindCafe.dto.SubInterestDto;
import com.example.BlindCafe.service.InterestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interest")
public class interestController {

    private final InterestService interestService;

    @GetMapping("{interestId}")
    public SubInterestDto getSubInterest(@PathVariable Long interestId) {
        log.info("GET /api/interest/{}", interestId);
        return interestService.getSubInterest(interestId);
    }
}
