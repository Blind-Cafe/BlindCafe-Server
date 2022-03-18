package com.example.BlindCafe.firebase;

import com.example.BlindCafe.dto.FcmMessageDto;
import com.example.BlindCafe.dto.FirestoreDto;
import com.example.BlindCafe.exception.BlindCafeException;
import com.example.BlindCafe.exception.CodeAndMessage;
import com.example.BlindCafe.domain.type.FcmMessage;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebaseService {
    private static final String ROOM_COLLECTION = "Rooms";
    private static final String MESSAGE_COLLECTION = "Messages";

    private static final String IMAGE_DEFAULT_MESSAGE = "사진이 전송되었습니다.";
    private static final String AUDIO_DEFAULT_MESSAGE = "음성 메세지가 전송되었습니다.";

    public FcmMessageDto.Request insertMessage(FirestoreDto firestoreDto) {
        try {
            Firestore firestore = FirestoreClient.getFirestore();
            ApiFuture<WriteResult> apiFuture =
                    firestore.collection(ROOM_COLLECTION)
                            .document(Long.toString(firestoreDto.getRoomId()))
                            .collection(MESSAGE_COLLECTION)
                            .document(firestoreDto.getMessage().getId())
                            .set(firestoreDto.getMessage());
            return makeRequestFormat(firestoreDto);
        } catch (Exception e) {
            throw new BlindCafeException(CodeAndMessage.FIREBASE_INSERT_ERROR);
        }
    }

    private FcmMessageDto.Request makeRequestFormat(FirestoreDto firestoreDto) {
        FcmMessageDto.Request request = new FcmMessageDto.Request();
        FirestoreDto.FirestoreMessage message = firestoreDto.getMessage();

        request.setTitle(message.getSenderName());
        request.setMatchingId(firestoreDto.getRoomId());
        request.setPath(FcmMessage.ONE_DAY.getPath());

        if (message.getType() == 1) {
            // text
            request.setBody(message.getContents());
        } else if (message.getType() == 2) {
            // image
            request.setBody(IMAGE_DEFAULT_MESSAGE);
            request.setImage(message.getContents());
        } else {
            // audio
            request.setBody(AUDIO_DEFAULT_MESSAGE);
        }

        return request;
    }
}
