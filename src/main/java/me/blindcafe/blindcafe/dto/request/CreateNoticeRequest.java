package me.blindcafe.blindcafe.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateNoticeRequest {

    private Long userId;

    @NotNull
    private String title;

    @NotNull
    private String content;
}
