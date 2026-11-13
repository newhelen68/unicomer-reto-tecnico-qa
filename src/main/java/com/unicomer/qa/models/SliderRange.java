package com.unicomer.qa.models;

import java.math.BigDecimal;

public record SliderRange(BigDecimal min, BigDecimal max) {

    public boolean contains(BigDecimal value) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }
}
