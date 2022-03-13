package com.example.BlindCafe.dto;

import com.example.BlindCafe.entity.Interest;
import com.example.BlindCafe.entity.InterestOrder;
import com.example.BlindCafe.entity.ProfileImage;
import com.example.BlindCafe.entity.User;
import com.example.BlindCafe.entity.type.status.CommonStatus;
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
        this.images = user.getProfileImages().stream()
                .filter(profileImage -> profileImage.getStatus().equals(CommonStatus.NORMAL))
                .sorted(Comparator.comparing(ProfileImage::getPriority))
                .map(ProfileImage::getSrc)
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
