package com.example.BlindCafe.type;

import com.example.BlindCafe.exception.BlindCafeException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.example.BlindCafe.exception.CodeAndMessage.INVALID_REQUEST;

@Getter
@AllArgsConstructor
public enum Gender {

    M("남성"),
    F("여성"),
    N("상관없음");

    private final String description;

    public static Gender getGender(String gender) {
        if (gender.equals("male"))
            return M;
        else if (gender.equals("female"))
            return F;
        else if (gender.equals("none"))
            return N;
        else
            throw new BlindCafeException(INVALID_REQUEST);
    }
}
