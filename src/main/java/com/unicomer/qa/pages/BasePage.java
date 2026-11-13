package com.unicomer.qa.pages;

import com.unicomer.qa.config.ConfigManager;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Acciones y esperas comunes a todas las páginas. Solo utiliza esperas explícitas.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, ConfigManager.explicitWait());
    }

    protected WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected List<WebElement> waitForAllVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    protected void click(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    protected void replaceText(By locator, String text) {
        WebElement field = wait.until(ExpectedConditions.elementToBeClickable(locator));
        field.click();
        field.sendKeys(Keys.chord(selectAllModifier(), "a"), Keys.DELETE);
        if (!text.isEmpty()) {
            field.sendKeys(text);
        }
        // El blur dispara el formato y las validaciones de la aplicación.
        field.sendKeys(Keys.TAB);
    }

    protected String getText(By locator) {
        return waitForVisible(locator).getText().trim();
    }

    // Para contenedores que pueden estar vacíos y sin dimensiones visibles.
    protected String getTextIfPresent(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator)).getText().trim();
    }

    protected boolean isDisplayed(By locator) {
        return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    protected String getInputValue(By locator) {
        String value = waitForVisible(locator).getDomProperty("value");
        return value == null ? "" : value;
    }

    private static Keys selectAllModifier() {
        return Platform.getCurrent().is(Platform.MAC) ? Keys.COMMAND : Keys.CONTROL;
    }
}
