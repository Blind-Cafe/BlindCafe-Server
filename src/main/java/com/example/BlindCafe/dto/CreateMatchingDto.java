package com.example.BlindCafe.dto;

import com.example.BlindCafe.domain.type.status.MatchingStatus;
import lombok.*;

public class CreateMatchingDto {

    @Getter
    @Setter
    public static class Response {

        private MatchingStatus matchingStatus;
        private Long matchingId;
        private Long partnerId;
        private String partnerNickname;


        @Builder(builderMethodName = "matchingBuilder")
        public Response(
                MatchingStatus matchingStatus,
                Long matchingId,
                Long partnerId,
                String partnerNickname
        ) {
            this.matchingStatus = matchingStatus;
            this.matchingId = matchingId;
            this.partnerId = partnerId;
            this.partnerNickname = partnerNickname;
        }

        @Builder(builderMethodName = "noneMatchingBuilder")
        public Response(
                MatchingStatus matchingStatus
        ) {
            this.matchingStatus = matchingStatus;
        }
    }
}
