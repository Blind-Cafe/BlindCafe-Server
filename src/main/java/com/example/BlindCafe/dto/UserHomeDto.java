package com.example.BlindCafe.dto;

import com.example.BlindCafe.exception.CodeAndMessage;
import com.example.BlindCafe.type.status.MatchingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

public class UserHomeDto {

    @Getter
    @Setter
    public static class Response extends ApiResponse {
        @Enumerated(EnumType.STRING)
        private MatchingStatus matchingStatus;
        private Long matchingId;
        private Long partnerId;
        private String partnerNickname;
        private String startTime;
        private String reason;

        @Builder(builderClassName = "matchingBuilder", builderMethodName = "matchingBuilder")
        public Response(
                CodeAndMessage codeAndMessage,
                MatchingStatus matchingStatus,
                Long matchingId,
                Long partnerId,
                String partnerNickname,
                String startTime
        ) {
            super(codeAndMessage.getCode(), codeAndMessage.getMessage());
            this.matchingStatus = matchingStatus;
            this.matchingId = matchingId;
            this.partnerId = partnerId;
            this.partnerNickname = partnerNickname;
            this.startTime = startTime;
        }

        @Builder(builderClassName = "outMatchingBuilder", builderMethodName = "outMatchingBuilder")
        public Response(
                CodeAndMessage codeAndMessage,
                MatchingStatus matchingStatus,
                String partnerNickname,
                String reason
        ) {
            super(codeAndMessage.getCode(), codeAndMessage.getMessage());
            this.matchingStatus = matchingStatus;
            this.partnerNickname = partnerNickname;
            this.reason = reason;
        }

        @Builder(builderClassName = "noneMatchingBuilder", builderMethodName = "noneMatchingBuilder")
        public Response(
                CodeAndMessage codeAndMessage,
                MatchingStatus matchingStatus
        ) {
            super(codeAndMessage.getCode(), codeAndMessage.getMessage());
            this.matchingStatus = matchingStatus;
        }
    }
}
