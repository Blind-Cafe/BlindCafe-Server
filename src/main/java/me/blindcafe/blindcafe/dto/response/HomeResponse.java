package me.blindcafe.blindcafe.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeResponse {
    private boolean request;
    private int tickets;
    private boolean notice;
}
