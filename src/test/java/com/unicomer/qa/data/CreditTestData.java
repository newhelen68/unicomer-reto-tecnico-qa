package com.unicomer.qa.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Datos de prueba de la calculadora, cargados desde {@code testdata/credit-calculator.json}.
 */
public record CreditTestData(
        List<CreditProduct> products,
        List<CreditScenario> calculationScenarios,
        DynamicUpdateScenario dynamicUpdateScenario,
        PaymentAlertScenario paymentAlertScenario,
        StaleAlertScenario staleAlertScenario) {

    private static final String RESOURCE = "testdata/credit-calculator.json";
    private static final CreditTestData INSTANCE = load();

    public static CreditTestData get() {
        return INSTANCE;
    }

    public CreditProduct product(String creditType) {
        return products.stream()
                .filter(product -> product.creditType().equals(creditType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de crédito sin datos: " + creditType));
    }

    private static CreditTestData load() {
        try (InputStream stream = CreditTestData.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("No se encontró el archivo de datos " + RESOURCE);
            }
            return new ObjectMapper().readValue(stream, CreditTestData.class);
        } catch (IOException e) {
            throw new UncheckedIOException("No fue posible leer " + RESOURCE, e);
        }
    }

    public record DynamicUpdateScenario(
            CreditScenario initial,
            BigDecimal expectedPaymentAtMaxTerm,
            BigDecimal expectedPaymentAtMaxRate) {
    }

    public record PaymentAlertScenario(CreditScenario initial, BigDecimal adjustedMonthlyIncome) {
    }

    public record StaleAlertScenario(CreditScenario initial, BigDecimal invalidRequestedAmount) {
    }
}
