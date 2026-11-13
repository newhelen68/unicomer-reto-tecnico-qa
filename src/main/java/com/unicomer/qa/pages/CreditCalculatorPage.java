package com.unicomer.qa.pages;

import com.unicomer.qa.config.ConfigManager;
import com.unicomer.qa.models.SliderRange;
import com.unicomer.qa.utils.CurrencyUtils;
import io.qameta.allure.Step;
import java.math.BigDecimal;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;

/**
 * Calculadora de créditos de Banco CUSCATLAN (aplicación Angular sin atributos id).
 */
public class CreditCalculatorPage extends BasePage {

    private static final String CALCULATOR_PATH = "/creditos/calculadora-creditos";
    private static final String SELECTED_CLASS = "btn-selected";

    private static final String OPTIONS_CARD = "//app-card-options-credit-options";
    private static final String INCOME_FIELD = OPTIONS_CARD + "//div[p[normalize-space()='Ingresos mensuales']]";
    private static final String AMOUNT_FIELD = OPTIONS_CARD
            + "//div[p[normalize-space()='Monto solicitado' or normalize-space()='Precio de venta']]";
    private static final String TERM_FIELD = OPTIONS_CARD + "//div[p[normalize-space()='Plazo']]";
    private static final String RATE_FIELD = OPTIONS_CARD + "//div[p[normalize-space()='Tasa']]";

    private static final By TITLE = By.cssSelector("app-home-calculadora .main-title");
    private static final By CREDIT_TYPE_BUTTONS = By.cssSelector("app-card-options-credit-options .first-block button");
    private static final By PURPOSE_BUTTONS = By.cssSelector("app-card-options-credit-options .second-block button");

    private static final By INCOME_INPUT = By.xpath(INCOME_FIELD + "/input");
    private static final By INCOME_MESSAGE = By.xpath(INCOME_FIELD + "/div[contains(@class,'messages')]");
    private static final By AMOUNT_LABEL = By.xpath(AMOUNT_FIELD + "/p");
    private static final By AMOUNT_INPUT = By.xpath(AMOUNT_FIELD + "/input");
    private static final By AMOUNT_MESSAGE = By.xpath(AMOUNT_FIELD + "/div[contains(@class,'messages')]");

    private static final By TERM_SLIDER = By.xpath(TERM_FIELD + "//mat-slider");
    private static final By TERM_LABEL = By.xpath(TERM_FIELD + "/div[contains(@class,'slider')]/p");
    private static final By RATE_SLIDER = By.xpath(RATE_FIELD + "//mat-slider");
    private static final By RATE_LABEL = By.xpath(RATE_FIELD + "/div[contains(@class,'slider')]/p");

    private static final By RESULT_CARD = By.cssSelector("app-card-calculation-result");
    private static final By MAX_FINANCING = By.cssSelector("app-card-calculation-result .total-amount .mount");
    private static final By MONTHLY_PAYMENT = By.cssSelector("app-card-calculation-result .fee p.font-700");
    private static final By RESULT_RATE = By.cssSelector("app-card-calculation-result .interest-rate p.font-700");
    private static final By PAYMENT_ALERT = By.cssSelector("app-card-calculation-result .messages p");

    public CreditCalculatorPage(WebDriver driver) {
        super(driver);
    }

    @Step("Abrir la calculadora de créditos")
    public CreditCalculatorPage navigateToCalculator() {
        driver.get(ConfigManager.webBaseUrl() + CALCULATOR_PATH);
        waitForAllVisible(CREDIT_TYPE_BUTTONS);
        waitForVisible(MAX_FINANCING);
        return this;
    }

    public boolean isCalculatorDisplayed() {
        return isDisplayed(TITLE)
                && isDisplayed(CREDIT_TYPE_BUTTONS)
                && isDisplayed(PURPOSE_BUTTONS)
                && isDisplayed(INCOME_INPUT)
                && isDisplayed(AMOUNT_INPUT)
                && isDisplayed(TERM_SLIDER)
                && isDisplayed(RATE_SLIDER)
                && isFinancingResultDisplayed();
    }

    public String getPageTitle() {
        return getText(TITLE);
    }

    public List<String> getCreditTypes() {
        return texts(waitForAllVisible(CREDIT_TYPE_BUTTONS));
    }

    public String getSelectedCreditType() {
        return selectedOption(CREDIT_TYPE_BUTTONS);
    }

    @Step("Seleccionar tipo de crédito: {creditType}")
    public CreditCalculatorPage selectCreditType(String creditType) {
        selectOption(CREDIT_TYPE_BUTTONS, creditType, "Tipo de crédito");
        return this;
    }

    public List<String> getCreditPurposes() {
        return texts(waitForAllVisible(PURPOSE_BUTTONS));
    }

    public String getSelectedCreditPurpose() {
        return selectedOption(PURPOSE_BUTTONS);
    }

    @Step("Seleccionar destino del crédito: {purpose}")
    public CreditCalculatorPage selectCreditPurpose(String purpose) {
        selectOption(PURPOSE_BUTTONS, purpose, "Destino del crédito");
        return this;
    }

    @Step("Registrar ingresos mensuales: {amount}")
    public CreditCalculatorPage enterMonthlyIncome(String amount) {
        replaceText(INCOME_INPUT, amount);
        return this;
    }

    public String getMonthlyIncomeValue() {
        return getInputValue(INCOME_INPUT);
    }

    public String getMonthlyIncomeMessage() {
        return getTextIfPresent(INCOME_MESSAGE);
    }

    public String getRequestedAmountLabel() {
        return getText(AMOUNT_LABEL);
    }

    @Step("Registrar monto solicitado: {amount}")
    public CreditCalculatorPage enterRequestedAmount(String amount) {
        replaceText(AMOUNT_INPUT, amount);
        return this;
    }

    public String getRequestedAmountValue() {
        return getInputValue(AMOUNT_INPUT);
    }

    public String getRequestedAmountMessage() {
        return getTextIfPresent(AMOUNT_MESSAGE);
    }

    @Step("Seleccionar plazo: {years} años")
    public CreditCalculatorPage selectLoanTerm(int years) {
        moveSlider(TERM_SLIDER, BigDecimal.valueOf(years));
        return this;
    }

    public BigDecimal getLoanTerm() {
        return sliderValue(waitForVisible(TERM_SLIDER));
    }

    public SliderRange getLoanTermRange() {
        return sliderRange(waitForVisible(TERM_SLIDER));
    }

    public String getLoanTermLabel() {
        return getText(TERM_LABEL);
    }

    @Step("Seleccionar tasa de interés: {rate} %")
    public CreditCalculatorPage selectInterestRate(BigDecimal rate) {
        moveSlider(RATE_SLIDER, rate);
        return this;
    }

    public BigDecimal getInterestRate() {
        return sliderValue(waitForVisible(RATE_SLIDER));
    }

    public SliderRange getInterestRateRange() {
        return sliderRange(waitForVisible(RATE_SLIDER));
    }

    public String getInterestRateLabel() {
        return getText(RATE_LABEL);
    }

    public boolean isFinancingResultDisplayed() {
        return isDisplayed(MAX_FINANCING) && isDisplayed(MONTHLY_PAYMENT);
    }

    public String getMaximumFinancingText() {
        scrollToResult();
        return getText(MAX_FINANCING);
    }

    @Step("Obtener el monto máximo a financiar")
    public BigDecimal getMaximumFinancingAmount() {
        return CurrencyUtils.parse(getMaximumFinancingText());
    }

    public String getMonthlyPaymentText() {
        return getText(MONTHLY_PAYMENT);
    }

    public BigDecimal getMonthlyPayment() {
        return CurrencyUtils.parse(getMonthlyPaymentText());
    }

    public BigDecimal getDisplayedInterestRate() {
        return new BigDecimal(getText(RESULT_RATE).replace("%", "").trim());
    }

    public boolean isPaymentAlertDisplayed() {
        scrollToResult();
        return isDisplayed(PAYMENT_ALERT);
    }

    public String getPaymentAlertText() {
        return getText(PAYMENT_ALERT);
    }

    // El sitio desplaza un contenedor interno; se muestra el resultado como lo vería el usuario.
    private void scrollToResult() {
        new Actions(driver).scrollToElement(waitForVisible(RESULT_CARD)).perform();
    }

    private void selectOption(By options, String text, String optionName) {
        List<WebElement> buttons = waitForAllVisible(options);
        WebElement option = buttons.stream()
                .filter(button -> button.getText().trim().equals(text))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        optionName + " no disponible: '" + text + "'. Opciones: " + texts(buttons)));
        click(option);
        wait.until(ExpectedConditions.attributeContains(option, "class", SELECTED_CLASS));
    }

    private String selectedOption(By options) {
        return waitForAllVisible(options).stream()
                .filter(button -> String.valueOf(button.getDomAttribute("class")).contains(SELECTED_CLASS))
                .map(button -> button.getText().trim())
                .findFirst()
                .orElse("");
    }

    // El mat-slider se opera por teclado, igual que un usuario, sin modificar el DOM con JavaScript.
    private void moveSlider(By locator, BigDecimal target) {
        WebElement slider = waitForVisible(locator);
        SliderRange range = sliderRange(slider);
        if (!range.contains(target)) {
            throw new IllegalArgumentException("Valor " + target + " fuera del rango permitido " + range);
        }
        slider.sendKeys(Keys.HOME);
        wait.until(d -> sliderValue(slider).compareTo(range.min()) == 0);
        while (sliderValue(slider).compareTo(target) < 0) {
            BigDecimal previous = sliderValue(slider);
            slider.sendKeys(Keys.ARROW_RIGHT);
            wait.until(d -> sliderValue(slider).compareTo(previous) > 0);
        }
        BigDecimal reached = sliderValue(slider);
        if (reached.compareTo(target) != 0) {
            throw new IllegalArgumentException(
                    "El valor " + target + " no coincide con un paso del control; valor alcanzado: " + reached);
        }
    }

    private static BigDecimal sliderValue(WebElement slider) {
        return new BigDecimal(slider.getDomAttribute("aria-valuenow"));
    }

    private static SliderRange sliderRange(WebElement slider) {
        return new SliderRange(
                new BigDecimal(slider.getDomAttribute("aria-valuemin")),
                new BigDecimal(slider.getDomAttribute("aria-valuemax")));
    }

    private static List<String> texts(List<WebElement> elements) {
        return elements.stream().map(element -> element.getText().trim()).toList();
    }
}
