package com.example.BlindCafe.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@AllArgsConstructor
public class SuggestionRequest {
    private List<MultipartFile> images;
    private String content;
}
