package com.example.BlindCafe.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PushMessageDto {
    private String targetToken;
    private String title;
    private String body;
    private String path;
}