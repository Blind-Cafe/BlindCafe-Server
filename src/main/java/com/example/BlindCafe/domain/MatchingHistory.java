package com.example.BlindCafe.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "matching_history")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_history_id")
    private Long id;

    @JsonIgnore
    @OneToOne(mappedBy = "matching_history", fetch = FetchType.LAZY)
    private User user;

    private String partners;

    public void setUser(User user) {
        this.user = user;
        user.setMatchingHistory(this);
    }

    public static MatchingHistory create(User user) {
        MatchingHistory history = new MatchingHistory();
        history.setUser(user);
        history.setPartners(user.getId().toString());
        return history;
    }

    // 매칭 히스토리에 상대방 추가
    public void update(Long partnerId) {
        this.setPartners(this.getPartners() + "," + partnerId.toString());
    }

    // 매칭 히스토리 불러오기
    public List<Long> getMatchingPartners() {
        return Arrays.stream(this.partners.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}