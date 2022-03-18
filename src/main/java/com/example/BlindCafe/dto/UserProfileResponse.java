package com.example.BlindCafe.dto;

import com.example.BlindCafe.domain.Interest;
import com.example.BlindCafe.domain.InterestOrder;
import com.example.BlindCafe.domain.Avatar;
import com.example.BlindCafe.domain.User;
import com.example.BlindCafe.domain.type.status.CommonStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class UserProfileResponse {
    private List<String> images;
    private String nickname;
    private int age;
    private String region;
    private List<String> interests;

    public UserProfileResponse(User user) {
        this.images = user.getAvatars().stream()
                .filter(profileImage -> profileImage.getStatus().equals(CommonStatus.NORMAL))
                .sorted(Comparator.comparing(Avatar::getPriority))
                .map(Avatar::getSrc)
                .collect(Collectors.toList());
        this.nickname = user.getNickname();
        this.age = user.getAge();
        this.region = user.getAddress() != null ?
                user.getAddress().toString() : null;
        this.interests = user.getInterestOrders().stream()
                .sorted(Comparator.comparing(InterestOrder::getPriority))
                .map(InterestOrder::getInterest)
                .map(Interest::getName)
                .collect(Collectors.toList());
    }
}
