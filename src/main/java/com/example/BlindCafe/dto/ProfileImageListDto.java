package com.example.BlindCafe.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProfileImageListDto {
    List<String> images;
}
