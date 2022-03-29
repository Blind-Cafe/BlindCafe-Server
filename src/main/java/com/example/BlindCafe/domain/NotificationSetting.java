package com.example.BlindCafe.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "notification_setting")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_setting_id")
    private Long id;

    @JsonIgnore
    @OneToOne(mappedBy = "notification_setting", fetch = FetchType.LAZY)
    private User user;

    private boolean isAll;

    private String off;

    public void setUser(User user) {
        this.user = user;
        user.setNotificationSetting(this);
    }

    public static NotificationSetting create(User user) {
        NotificationSetting setting = new NotificationSetting();
        setting.setUser(user);
        setting.setAll(true);
        setting.setOff("");
        return setting;
    }

    // 전체 ON/OFF 설정
    public void setAll(boolean isActivate) {
        this.isAll = isActivate;
    }

    // 채팅방별 ON/OFF 설정
    public void setRoom(String mid, boolean isActivate) {
        String[] rooms = this.off.split(":");

        if (isActivate) {
            for (String room: rooms) {
                    if (room.equals(mid)) return;
            }
            this.setOff(this.off + mid + ":");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (String room: rooms) {
            if (!room.equals(mid) && !room.equals("")) {
                sb.append(room).append(":");
            }
        }
        this.setOff(sb.toString());
    }

    public String getInactivateRooms() {
        return this.off.substring(0, this.off.length()-1);
    }
}
