package com.example.BlindCafe.dto;

import com.example.BlindCafe.exception.CodeAndMessage;
import com.example.BlindCafe.type.status.MatchingStatus;
import lombok.*;

public class CreateMatchingDto {

    @Getter
    @Setter
    public static class Response extends ApiResponse {

        private MatchingStatus matchingStatus;
        private Long matchingId;
        private Long partnerId;
        private String partnerNickname;

        @Builder(builderMethodName = "matchingBuilder")
        public Response(
                CodeAndMessage codeAndMessage,
                MatchingStatus matchingStatus,
                Long matchingId,
                Long partnerId,
                String partnerNickname
        ) {
            super(codeAndMessage.getCode(), codeAndMessage.getMessage());
            this.matchingStatus = matchingStatus;
            this.matchingId = matchingId;
            this.partnerId = partnerId;
            this.partnerNickname = partnerNickname;
        }

        @Builder(builderMethodName = "noneMatchingBuilder")
        public Response(
                CodeAndMessage codeAndMessage,
                MatchingStatus matchingStatus
        ) {
            super(codeAndMessage.getCode(), codeAndMessage.getMessage());
            this.matchingStatus = matchingStatus;
        }
    }
}
