package com.example.BlindCafe.dto;

import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InterestDto {
    private List<Interest> interests;

    @Getter
    @AllArgsConstructor
    public static class Interest {
        private Long main;
        private List<String> sub;
    }
}
