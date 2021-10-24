package com.example.BlindCafe.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Drink {
    DRINK1("음료1"),
    DRINK2("음료2"),
    DRINK3("음료3"),
    DRINK4("음료4"),
    DRINK5("음료5"),
    DRINK6("음료6"),
    DRINK7("음료7"),
    DRINK8("음료8"),
    DRINK9("음료9");

    private final String description;
}
