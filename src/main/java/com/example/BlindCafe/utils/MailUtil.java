package com.example.BlindCafe.utils;

import com.example.BlindCafe.exception.BlindCafeException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

import java.time.LocalDateTime;

import static com.example.BlindCafe.exception.CodeAndMessage.EMAIL_SEND_ERROR;
import static com.example.BlindCafe.exception.CodeAndMessage.INTERNAL_SERVER_ERROR;

@Component
@RequiredArgsConstructor
public class MailUtil {

    @Value("${email.from}")
    private String FROM;

    @Value("${email.to}")
    private String TO;

    private final JavaMailSender mailSender;

    public void sendMail(String nickname, String phone, String content, String images) {
        try {
            MimeMessage message = createMessage(nickname, phone, content, images);
            try {
                mailSender.send(message);
            } catch(MailException e){
                throw new BlindCafeException(EMAIL_SEND_ERROR);
            }
        } catch (Exception e) {
            throw new BlindCafeException(INTERNAL_SERVER_ERROR);
        }
    }

    private MimeMessage createMessage(String nickname, String phone, String content, String images) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);

            helper.setFrom(FROM);
            helper.setTo(TO);

            String subject = "[BlindCafe] 사용자 건의사항입니다.";
            helper.setSubject(subject);

            StringBuilder msg = new StringBuilder();
            msg.append("<div style='margin:100px;'>");
            msg.append("<h3> 신고접수 : ").append(LocalDateTime.now()).append("</h3>");
            msg.append("<h3> 사용자명 : ").append(nickname).append("</h3>");
            msg.append("<h3> 전화번호 : ").append(phone).append("</h3>");
            msg.append("<br>");
            msg.append(content);
            msg.append("<br>");
            for (String image: images.split(","))
                msg.append("<img src='").append(image).append("'><br>");
            msg.append("</div>");
            message.setText(msg.toString(), "utf-8", "html");

            return message;
        } catch (Exception e) {
            throw new BlindCafeException(EMAIL_SEND_ERROR);
        }
    }
}
