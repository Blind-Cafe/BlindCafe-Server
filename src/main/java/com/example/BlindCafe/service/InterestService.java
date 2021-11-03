package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.SubInterestDto;
import com.example.BlindCafe.entity.Interest;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.exception.CodeAndMessage;
import com.example.BlindCafe.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestService {

    private final InterestRepository interestRepository;

    public SubInterestDto getSubInterest(Long interestId) {

        Interest interest = interestRepository.findByIdAndParentId(interestId, interestId)
                .orElseThrow(() -> new BlindCafeException(CodeAndMessage.INVALID_MAIN_INTEREST));

        return SubInterestDto.builder()
                .sub(interest.getChild()
                        .stream()
                        .filter(i -> !i.equals(i.getParent()))
                        .map(sub -> sub.getName()).collect(Collectors.toList()))
                .build();
    }
}
