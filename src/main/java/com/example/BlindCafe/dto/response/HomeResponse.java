package com.example.BlindCafe.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeResponse {
    private boolean status;
    private int tickets;
    private boolean notice;
}
