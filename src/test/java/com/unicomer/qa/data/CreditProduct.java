package com.unicomer.qa.data;

import com.unicomer.qa.models.SliderRange;
import java.math.BigDecimal;
import java.util.List;

/**
 * Reglas visibles de un tipo de crédito, verificadas en la aplicación.
 */
public record CreditProduct(
        String creditType,
        List<String> purposes,
        String amountLabel,
        BigDecimal minimumIncome,
        BigDecimal minimumAmount,
        SliderRange termYears,
        SliderRange interestRate) {

    @Override
    public String toString() {
        return creditType;
    }
}
