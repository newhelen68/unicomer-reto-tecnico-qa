package com.unicomer.qa.data;

import java.util.List;
import org.testng.annotations.DataProvider;

public final class CreditDataProvider {

    public static final String CALCULATION_SCENARIOS = "calculationScenarios";
    public static final String CREDIT_PRODUCTS = "creditProducts";

    private CreditDataProvider() {
    }

    @DataProvider(name = CALCULATION_SCENARIOS, propagateFailureAsTestFailure = true)
    public static Object[][] calculationScenarios() {
        return toRows(CALCULATION_SCENARIOS, CreditTestData.get().calculationScenarios());
    }

    @DataProvider(name = CREDIT_PRODUCTS, propagateFailureAsTestFailure = true)
    public static Object[][] creditProducts() {
        return toRows(CREDIT_PRODUCTS, CreditTestData.get().products());
    }

    // Sin filas, TestNG omitiría la prueba y la suite terminaría en verde.
    private static Object[][] toRows(String name, List<?> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("El conjunto de datos '" + name + "' está vacío");
        }
        return items.stream().map(item -> new Object[] {item}).toArray(Object[][]::new);
    }
}
