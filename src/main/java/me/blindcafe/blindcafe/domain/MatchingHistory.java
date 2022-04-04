package me.blindcafe.blindcafe.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
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
    @OneToOne(mappedBy = "history", fetch = FetchType.LAZY)
    private User user;

    private String partners;

    public static MatchingHistory create() {
        MatchingHistory history = new MatchingHistory();
        history.setPartners("0");
        return history;
    }

    // 매칭 히스토리에 상대방 추가
    public void update(Long partnerId) {
        this.setPartners(this.getPartners() + "," + partnerId.toString());
    }

    // 매칭 히스토리 불러오기
    public List<Long> getMatchingPartners() {
        if (this.partners.equals("0")) return new ArrayList<>();
        return Arrays.stream(this.partners.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }
}
