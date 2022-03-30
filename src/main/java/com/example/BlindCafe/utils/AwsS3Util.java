package com.example.BlindCafe.utils;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.BlindCafe.exception.BlindCafeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.BlindCafe.exception.CodeAndMessage.FILE_CONVERT_ERROR;
import static com.example.BlindCafe.exception.CodeAndMessage.FILE_EXTENSION_ERROR;

@Slf4j
@RequiredArgsConstructor
@Component
public class AwsS3Util {

    public static String DEFAULT_IMAGE;

    private final AmazonS3Client amazonS3Client;
    private final Tika tika = new Tika();
    private final String PROFILE_IMAGE_DIR = "users/profiles/";
    public final String SUGGESTION_IMAGE_DIR = "suggestion/";
    private final String VOICE_DIR = "users/voice/";
    private final String MESSAGE_DIR = "chat/";
    private static String cloudfrontUrl;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;

    @Value("${cloud.aws.cloudfront.url}")
    public void setCloudfrontUrl(String value) {
        cloudfrontUrl = value;
        DEFAULT_IMAGE = value + "users/profiles/0/profile_default.png";
    }

    public String uploadAvatar(MultipartFile multipartFile, Long userId) {
        File file = convertToFile(multipartFile);
        String fileName = PROFILE_IMAGE_DIR + userId + "/" + UUID.randomUUID() + extension(multipartFile);
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, file));
        file.delete();
        return cloudfrontUrl + fileName;
    }

    public String uploadSuggestion(List<MultipartFile> multipartFiles, Long suggestionId) {
        StringBuilder sb = new StringBuilder();
        AtomicInteger index = new AtomicInteger(1);
        multipartFiles.forEach(multipartFile -> {
            File file = convertToFile(multipartFile);
            String fileName = SUGGESTION_IMAGE_DIR + suggestionId + "/" + index + extension(multipartFile);
            amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, file));
            sb.append(cloudfrontUrl + fileName + ",");
            file.delete();
            index.getAndIncrement();
        });
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public String uploadVoice(MultipartFile multipartFile, Long userId) {
        File file = convertToFile(multipartFile);
        String fileName = VOICE_DIR + userId + "/" + UUID.randomUUID() + extension(multipartFile);
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, file));
        file.delete();
        return cloudfrontUrl + fileName;
    }

    public String uploadFileFromMessage(MultipartFile multipartFile, String mid) {
        File file = convertToFile(multipartFile);
        String fileName = MESSAGE_DIR + mid + "/" + UUID.randomUUID() + extension(multipartFile);
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, file));
        file.delete();
        return cloudfrontUrl + fileName;
    }

    private File convertToFile(MultipartFile multipartFile) {
        if (Objects.isNull(multipartFile.getContentType()))
            throw new BlindCafeException(FILE_CONVERT_ERROR);

        File convertedFile = new File(multipartFile.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convertedFile)) {
            fos.write(multipartFile.getBytes());
        } catch (IOException e) {
            throw new BlindCafeException(FILE_CONVERT_ERROR);
        }
        return convertedFile;
    }

    /**
     * tika 활용해서 파일 확장자 확인
     * PPT, CSV ,PDF 등 다양한 형태의, 파일의 메타 데이터와 텍스트를 감지하고 추출하는 라이브러리
     */
    private String extension(MultipartFile multipartFile) {
        MimeTypes mTypes = MimeTypes.getDefaultMimeTypes();
        try {
            MimeType mimeType = mTypes.forName(
                    tika.detect(multipartFile.getBytes())
            );
            return mimeType.getExtension();
        } catch (MimeTypeException | IOException e) {
            throw new BlindCafeException(FILE_EXTENSION_ERROR);
        }
    }

    public static String getDefaultImage() {
        return cloudfrontUrl + DEFAULT_IMAGE;
    }
}
