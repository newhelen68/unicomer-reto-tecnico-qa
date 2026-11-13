package com.unicomer.qa.data;

import java.util.List;
import org.testng.annotations.DataProvider;

public final class CreditDataProvider {

    public static final String CALCULATION_SCENARIOS = "calculationScenarios";
    public static final String CREDIT_PRODUCTS = "creditProducts";

    private CreditDataProvider() {
    }

    @DataProvider(name = CALCULATION_SCENARIOS)
    public static Object[][] calculationScenarios() {
        return toRows(CreditTestData.get().calculationScenarios());
    }

    @DataProvider(name = CREDIT_PRODUCTS)
    public static Object[][] creditProducts() {
        return toRows(CreditTestData.get().products());
    }

    private static Object[][] toRows(List<?> items) {
        return items.stream().map(item -> new Object[] {item}).toArray(Object[][]::new);
    }
}
