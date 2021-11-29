package com.example.BlindCafe.service;

import com.example.BlindCafe.dto.FcmMessageDto;
import com.example.BlindCafe.dto.FirestoreDto;
import com.example.BlindCafe.dto.MessageDto;
import com.example.BlindCafe.entity.Matching;
import com.example.BlindCafe.entity.Message;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.firebase.FirebaseCloudMessageService;
import com.example.BlindCafe.firebase.FirebaseService;
import com.example.BlindCafe.repository.MatchingRepository;
import com.example.BlindCafe.repository.MessageRepository;
import com.example.BlindCafe.repository.UserRepository;
import com.example.BlindCafe.type.MessageType;
import com.example.BlindCafe.type.status.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static com.example.BlindCafe.exception.CodeAndMessage.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final UserRepository userRepository;
    private final MatchingRepository matchingRepository;
    private final MessageRepository messageRepository;

    private final FirebaseService firebaseService;
    private final FirebaseCloudMessageService fcmService;

    @Transactional
    public void sendMessage(Long userId, Long matchingId, MessageDto request) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getStatus().equals(UserStatus.NORMAL))
                .orElseThrow(() -> new BlindCafeException(NO_USER));
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new BlindCafeException(NO_MATCHING));

        MessageType messageType = getType(request.getType());

        // 메세지 db에 저장
        Message message = new Message();
        message.setMatching(matching);
        message.setUser(user);
        message.setContents(request.getContents());
        message.setType(messageType);
        Message savedMessage = messageRepository.save(message);

        // 메세지 firestore 저장
        User partner = matching.getUserMatchings().stream()
                .filter(um -> !um.getUser().equals(user))
                .map(um -> um.getUser() )
                .findAny()
                .orElseThrow(() -> new BlindCafeException(INVALID_MATCHING));

        LocalDateTime ldt = savedMessage.getCreatedAt();
        Timestamp timestamp = Timestamp.valueOf(ldt);

        FirestoreDto firestoreDto = FirestoreDto.builder()
                .roomId(matchingId)
                .targetToken(partner.getDeviceId())
                .message(new FirestoreDto.FirestoreMessage(
                        Long.toString(savedMessage.getId()),
                        Long.toString(user.getId()),
                        user.getNickname(),
                        savedMessage.getContents(),
                        request.getType(),
                        timestamp
                ))
                .build();
        FcmMessageDto.Request req = firebaseService.insertMessage(firestoreDto);

        // 푸쉬
        fcmService.sendMessageTo(
                firestoreDto.getTargetToken(),
                req.getTitle(),
                req.getBody(),
                req.getPath(),
                "1",
                req.getMatchingId()
        );
    }

    private MessageType getType(int type) {
        if (type == 1)
            return MessageType.TEXT;
        else if (type == 2)
            return MessageType.IMAGE;
        else if (type == 3)
            return MessageType.AUDIO;
        else
            throw new BlindCafeException(NO_MESSAGE_TYPE);
    }
}
