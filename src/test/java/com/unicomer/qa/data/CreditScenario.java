package com.unicomer.qa.data;

import java.math.BigDecimal;

public record CreditScenario(
        String creditType,
        String purpose,
        BigDecimal monthlyIncome,
        BigDecimal requestedAmount,
        int termYears,
        BigDecimal interestRate,
        BigDecimal expectedMaximumFinancing,
        BigDecimal expectedMonthlyPayment) {

    @Override
    public String toString() {
        return creditType + " - " + purpose;
    }
}
