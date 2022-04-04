package me.blindcafe.blindcafe.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileMessageDto {
    private String matchingId;
    private String senderId;
    private String senderName;
    private String type;
    private MultipartFile file;
}
