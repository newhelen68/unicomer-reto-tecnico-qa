package com.unicomer.qa.ui;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.unicomer.qa.base.BaseTest;
import com.unicomer.qa.data.CreditDataProvider;
import com.unicomer.qa.data.CreditProduct;
import com.unicomer.qa.data.CreditScenario;
import com.unicomer.qa.data.CreditTestData;
import com.unicomer.qa.data.CreditTestData.PaymentAlertScenario;
import com.unicomer.qa.data.CreditTestData.StaleAlertScenario;
import com.unicomer.qa.models.SliderRange;
import com.unicomer.qa.pages.CreditCalculatorPage;
import com.unicomer.qa.utils.CurrencyUtils;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import java.math.BigDecimal;
import java.util.List;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

@Epic("Reto técnico QA Automation")
@Feature("Calculadora de créditos")
public class CreditCalculatorTest extends BaseTest {

    private static final String CALCULATOR_TITLE = "Calcula tu Crédito";
    private static final String DEFAULT_CREDIT_TYPE = "Consumo";
    private static final String PAYMENT_ALERT = "La cuota supera tu ingreso";
    private static final String ZERO_AMOUNT = "$0.00";
    private static final BigDecimal ONE_CENT = new BigDecimal("0.01");

    private final CreditTestData data = CreditTestData.get();

    @Test(groups = {"ui", "smoke", "regression"},
            description = "TC-UI-001 Carga de la calculadora y estado inicial")
    @Story("Disponibilidad")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verifica que la calculadora cargue con sus controles visibles y el estado por defecto.")
    public void shouldLoadCalculatorWithDefaultState() {
        CreditCalculatorPage calculator = openCalculator();
        List<String> expectedCreditTypes = data.products().stream().map(CreditProduct::creditType).toList();

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(calculator.isCalculatorDisplayed(), "Controles principales visibles");
        softly.assertEquals(calculator.getPageTitle(), CALCULATOR_TITLE, "Título de la página");
        softly.assertEquals(calculator.getCreditTypes(), expectedCreditTypes, "Tipos de crédito disponibles");
        softly.assertEquals(calculator.getSelectedCreditType(), DEFAULT_CREDIT_TYPE, "Tipo de crédito por defecto");
        softly.assertEquals(calculator.getCreditPurposes(), data.product(DEFAULT_CREDIT_TYPE).purposes(),
                "Destinos del tipo por defecto");
        softly.assertEquals(calculator.getMonthlyIncomeValue(), "", "Ingresos mensuales vacíos");
        softly.assertEquals(calculator.getRequestedAmountValue(), "", "Monto solicitado vacío");
        softly.assertEquals(calculator.getMaximumFinancingText(), ZERO_AMOUNT, "Monto máximo inicial");
        softly.assertAll();
    }

    @Test(dataProvider = CreditDataProvider.CALCULATION_SCENARIOS, dataProviderClass = CreditDataProvider.class,
            groups = {"ui", "regression"},
            description = "TC-UI-002 Cálculo del monto máximo a financiar por tipo de crédito")
    @Story("Cálculo de financiamiento")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Ejecuta los ocho pasos del reto y valida cada selección y el resultado financiero.")
    public void shouldCalculateMaximumFinancingAmount(CreditScenario scenario) {
        CreditProduct product = data.product(scenario.creditType());
        CreditCalculatorPage calculator = openCalculator();

        calculator.selectCreditType(scenario.creditType());
        assertEquals(calculator.getSelectedCreditType(), scenario.creditType(), "Tipo de crédito seleccionado");
        assertEquals(calculator.getCreditPurposes(), product.purposes(), "Destinos disponibles para el tipo");

        calculator.selectCreditPurpose(scenario.purpose());
        assertEquals(calculator.getSelectedCreditPurpose(), scenario.purpose(), "Destino seleccionado");

        calculator.enterMonthlyIncome(scenario.monthlyIncome().toPlainString());
        assertEquals(calculator.getMonthlyIncomeValue(), CurrencyUtils.format(scenario.monthlyIncome()),
                "Ingresos mensuales registrados");

        assertEquals(calculator.getRequestedAmountLabel(), product.amountLabel(), "Etiqueta del monto");
        calculator.enterRequestedAmount(scenario.requestedAmount().toPlainString());
        assertEquals(calculator.getRequestedAmountValue(), CurrencyUtils.format(scenario.requestedAmount()),
                "Monto registrado");

        calculator.selectLoanTerm(scenario.termYears());
        assertDecimalEquals(calculator.getLoanTerm(), BigDecimal.valueOf(scenario.termYears()), "Plazo seleccionado");
        assertEquals(calculator.getLoanTermLabel(), scenario.termYears() + " años", "Etiqueta del plazo");

        calculator.selectInterestRate(scenario.interestRate());
        assertDecimalEquals(calculator.getInterestRate(), scenario.interestRate(), "Tasa seleccionada");
        assertEquals(calculator.getInterestRateLabel(), scenario.interestRate().stripTrailingZeros().toPlainString()
                + " %", "Etiqueta de la tasa");

        assertTrue(calculator.isFinancingResultDisplayed(), "Resultado del cálculo visible");
        String maximumFinancing = calculator.getMaximumFinancingText();
        assertTrue(CurrencyUtils.isValidCurrency(maximumFinancing),
                "Formato monetario del monto máximo: " + maximumFinancing);
        assertDecimalEquals(calculator.getMaximumFinancingAmount(), scenario.expectedMaximumFinancing(),
                "Monto máximo a financiar");
        // Cuota según la fórmula de amortización de la aplicación; tolerancia por redondeo.
        assertWithinOneCent(calculator.getMonthlyPayment(), scenario.expectedMonthlyPayment(), "Cuota mensual");
        assertDecimalEquals(calculator.getDisplayedInterestRate(), scenario.interestRate(), "Tasa del resultado");
        assertFalse(calculator.isPaymentAlertDisplayed(), "No debe mostrarse la alerta de capacidad de pago");
    }

    @Test(groups = {"ui", "regression"}, description = "TC-UI-003 Recálculo al modificar plazo y tasa")
    @Story("Actualización dinámica")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que el resultado se recalcule al llevar plazo y tasa a sus valores máximos.")
    public void shouldRecalculateWhenTermAndRateChange() {
        CreditScenario scenario = data.dynamicUpdateScenario();
        CreditProduct product = data.product(scenario.creditType());
        CreditCalculatorPage calculator = fillCalculator(openCalculator(), scenario);

        assertRangeEquals(calculator.getLoanTermRange(), product.termYears(), "Rango del plazo");
        assertRangeEquals(calculator.getInterestRateRange(), product.interestRate(), "Rango de la tasa");
        BigDecimal maximumFinancing = calculator.getMaximumFinancingAmount();
        BigDecimal initialPayment = calculator.getMonthlyPayment();
        assertDecimalEquals(maximumFinancing, scenario.expectedMaximumFinancing(), "Monto máximo inicial");
        assertWithinOneCent(initialPayment, scenario.expectedMonthlyPayment(), "Cuota inicial");

        calculator.selectLoanTerm(product.termYears().max().intValueExact());
        BigDecimal longTermPayment = calculator.getMonthlyPayment();
        assertTrue(longTermPayment.compareTo(initialPayment) < 0,
                "La cuota debe disminuir al aumentar el plazo: " + initialPayment + " -> " + longTermPayment);
        assertDecimalEquals(calculator.getMaximumFinancingAmount(), maximumFinancing,
                "El monto máximo no depende del plazo");

        calculator.selectInterestRate(product.interestRate().max());
        BigDecimal highRatePayment = calculator.getMonthlyPayment();
        assertTrue(highRatePayment.compareTo(longTermPayment) > 0,
                "La cuota debe aumentar al subir la tasa: " + longTermPayment + " -> " + highRatePayment);
        assertDecimalEquals(calculator.getDisplayedInterestRate(), product.interestRate().max(),
                "Tasa del resultado actualizada");
        assertDecimalEquals(calculator.getMaximumFinancingAmount(), maximumFinancing,
                "El monto máximo no depende de la tasa");
    }

    @Test(dataProvider = CreditDataProvider.CREDIT_PRODUCTS, dataProviderClass = CreditDataProvider.class,
            groups = {"ui", "regression"},
            description = "TC-UI-004 Validación de ingreso y monto mínimos por tipo de crédito")
    @Story("Validaciones de entrada")
    @Severity(SeverityLevel.NORMAL)
    @Description("Valida campos vacíos, texto no numérico y valores límite de ingreso y monto.")
    public void shouldValidateMinimumIncomeAndAmount(CreditProduct product) {
        String incomeMessage = "Tu ingreso debe ser igual o mayor a " + CurrencyUtils.format(product.minimumIncome());
        String amountMessage = "Monto mínimo a financiar " + CurrencyUtils.format(product.minimumAmount());
        CreditCalculatorPage calculator = openCalculator().selectCreditType(product.creditType());

        assertEquals(calculator.getMonthlyIncomeMessage(), incomeMessage, "Mensaje con ingreso vacío");
        assertEquals(calculator.getRequestedAmountMessage(), amountMessage, "Mensaje con monto vacío");
        assertEquals(calculator.getMaximumFinancingText(), ZERO_AMOUNT, "Sin cálculo con campos vacíos");

        calculator.enterMonthlyIncome("abc");
        assertEquals(calculator.getMonthlyIncomeValue(), "", "El texto no numérico debe descartarse");
        assertEquals(calculator.getMonthlyIncomeMessage(), incomeMessage, "Mensaje con ingreso no numérico");

        calculator.enterMonthlyIncome(product.minimumIncome().subtract(ONE_CENT).toPlainString())
                .enterRequestedAmount(product.minimumAmount().toPlainString());
        assertEquals(calculator.getMonthlyIncomeMessage(), incomeMessage, "Mensaje con ingreso bajo el mínimo");
        assertEquals(calculator.getMaximumFinancingText(), ZERO_AMOUNT, "Sin cálculo con ingreso bajo el mínimo");

        calculator.enterMonthlyIncome(product.minimumIncome().toPlainString());
        assertEquals(calculator.getMonthlyIncomeMessage(), "", "Sin mensaje con el ingreso mínimo");
        assertEquals(calculator.getRequestedAmountMessage(), "", "Sin mensaje con el monto mínimo");
        assertTrue(calculator.getMaximumFinancingAmount().signum() > 0, "Cálculo con valores mínimos válidos");

        calculator.enterRequestedAmount(product.minimumAmount().subtract(ONE_CENT).toPlainString());
        assertEquals(calculator.getRequestedAmountMessage(), amountMessage, "Mensaje con monto bajo el mínimo");
        assertEquals(calculator.getMaximumFinancingText(), ZERO_AMOUNT, "Sin cálculo con monto bajo el mínimo");
    }

    @Test(groups = {"ui", "regression"},
            description = "TC-UI-005 Alerta cuando la cuota supera la capacidad de pago")
    @Story("Capacidad de pago")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que la alerta aparezca con una cuota alta y desaparezca al aumentar el ingreso.")
    public void shouldWarnWhenPaymentExceedsIncomeCapacity() {
        PaymentAlertScenario scenario = data.paymentAlertScenario();
        CreditCalculatorPage calculator = fillCalculator(openCalculator(), scenario.initial());

        assertTrue(calculator.isPaymentAlertDisplayed(), "La alerta debe mostrarse con una cuota alta");
        assertEquals(calculator.getPaymentAlertText(), PAYMENT_ALERT, "Texto de la alerta");

        calculator.enterMonthlyIncome(scenario.adjustedMonthlyIncome().toPlainString());
        assertFalse(calculator.isPaymentAlertDisplayed(), "La alerta debe ocultarse cuando el ingreso cubre la cuota");
        assertDecimalEquals(calculator.getMaximumFinancingAmount(), scenario.initial().expectedMaximumFinancing(),
                "El monto máximo se mantiene");
    }

    @Test(groups = {"ui", "known-defect"},
            description = "TC-UI-006 [D-01] Ocultar la alerta de capacidad de pago al invalidar el monto")
    @Story("Capacidad de pago")
    @Severity(SeverityLevel.MINOR)
    @Description("Defecto conocido D-01: en Vivienda y Vehículo la alerta permanece visible con un monto inválido.")
    public void shouldHidePaymentAlertWhenAmountBecomesInvalid() {
        StaleAlertScenario scenario = data.staleAlertScenario();
        CreditCalculatorPage calculator = fillCalculator(openCalculator(), scenario.initial());
        assertTrue(calculator.isPaymentAlertDisplayed(), "Precondición: alerta visible");

        calculator.enterRequestedAmount(scenario.invalidRequestedAmount().toPlainString());
        assertEquals(calculator.getMaximumFinancingText(), ZERO_AMOUNT, "Sin cálculo con monto inválido");
        assertFalse(calculator.isPaymentAlertDisplayed(), "D-01: la alerta no debe mostrarse sin un cálculo vigente");
    }

    private CreditCalculatorPage openCalculator() {
        return new CreditCalculatorPage(driver()).navigateToCalculator();
    }

    private static CreditCalculatorPage fillCalculator(CreditCalculatorPage calculator, CreditScenario scenario) {
        return calculator.selectCreditType(scenario.creditType())
                .selectCreditPurpose(scenario.purpose())
                .enterMonthlyIncome(scenario.monthlyIncome().toPlainString())
                .enterRequestedAmount(scenario.requestedAmount().toPlainString())
                .selectLoanTerm(scenario.termYears())
                .selectInterestRate(scenario.interestRate());
    }

    private static void assertDecimalEquals(BigDecimal actual, BigDecimal expected, String message) {
        assertTrue(actual.compareTo(expected) == 0, message + ": esperado " + expected + ", obtenido " + actual);
    }

    private static void assertWithinOneCent(BigDecimal actual, BigDecimal expected, String message) {
        assertTrue(actual.subtract(expected).abs().compareTo(ONE_CENT) <= 0,
                message + ": esperado " + expected + " ± " + ONE_CENT + ", obtenido " + actual);
    }

    private static void assertRangeEquals(SliderRange actual, SliderRange expected, String message) {
        assertDecimalEquals(actual.min(), expected.min(), message + " (mínimo)");
        assertDecimalEquals(actual.max(), expected.max(), message + " (máximo)");
    }
}
