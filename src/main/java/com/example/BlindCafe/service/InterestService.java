package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.InterestDto;
import com.example.BlindCafe.entity.Interest;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.exception.CodeAndMessage;
import com.example.BlindCafe.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterestService {

    private final InterestRepository interestRepository;

    public InterestDto getSubInterest(List<Long> interestIds) {

        InterestDto interestDto = new InterestDto();
        for (Long interestId: interestIds) {
            Interest interest = interestRepository.findByIdAndParentId(interestId, interestId)
                    .orElseThrow(() -> new BlindCafeException(CodeAndMessage.INVALID_MAIN_INTEREST));

            interestDto.getInterests().add(new InterestDto.Interest(
                    interest.getId(),
                    interest.getChild().stream()
                            .filter(i -> !i.equals(i.getParent()))
                            .map(sub -> sub.getName())
                            .collect(Collectors.toList())
            ));
        }
        return interestDto;
    }
}
