package com.example.BlindCafe.util;

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
import java.util.UUID;
import java.io.File;

import static com.example.BlindCafe.exception.CodeAndMessage.FILE_CONVERT_ERROR;
import static com.example.BlindCafe.exception.CodeAndMessage.FILE_EXTENSION_ERROR;

@Slf4j
@RequiredArgsConstructor
@Component
public class AmazonS3Connector {

    /**
     * Todo
     * AES256
     */

    private final AmazonS3Client amazonS3Client;
    private final Tika tika = new Tika();
    private final static String PROFILE_IMAGE_DIR = "users/profiles/";

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;

    @Value("${cloud.aws.cloudfront.url}")
    public String cloudfrontUrl;

    public String uploadProfileImage(MultipartFile multipartFile, Long userId) {
        File file = convertToFile(multipartFile);
        String fileName = PROFILE_IMAGE_DIR + userId + "/" + UUID.randomUUID() + extension(multipartFile);
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, file));
        file.delete();
        return cloudfrontUrl + fileName;
    }

    private File convertToFile(MultipartFile multipartFile) {
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
}
