package me.blindcafe.blindcafe.domain;

import lombok.*;

import javax.persistence.Embeddable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    private String state;
    private String region;

    @Override
    public String toString() {
        if (state != null && region != null)
            return state + " " + region;
        else
            return null;
    }

    public static Address create(String state, String region) {
        Address address = new Address();
        address.setState(state);
        address.setRegion(region);
        return address;
    }
}
