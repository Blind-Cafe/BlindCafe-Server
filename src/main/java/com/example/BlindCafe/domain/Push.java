package com.example.BlindCafe.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
public class Push {
    private boolean push_matching;
    private boolean push_matching_open;
    private boolean push_one_day;
    private boolean push_two_days;
    private boolean push_end_of_one_hour;
    private boolean push_three_days;
    private boolean push_profile_open;
    private boolean push_matching_continue;
    private boolean push_last_chat;

    public Push() {
        this.push_matching = false;
        this.push_matching_open = false;
        this.push_one_day = false;
        this.push_two_days = false;
        this.push_end_of_one_hour = false;
        this.push_three_days = false;
        this.push_profile_open = false;
        this.push_matching_continue = false;
        this.push_last_chat = false;
    }
}
