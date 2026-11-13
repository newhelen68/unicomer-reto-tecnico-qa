package com.unicomer.qa.base;

import com.unicomer.qa.drivers.DriverManager;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Ciclo de vida del navegador para pruebas UI: una sesión nueva por prueba.
 */
public abstract class BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        DriverManager.startDriver();
    }

    // alwaysRun garantiza el cierre aunque la prueba falle.
    @AfterMethod(alwaysRun = true)
    public void closeBrowser() {
        DriverManager.quitDriver();
    }

    protected WebDriver driver() {
        return DriverManager.getDriver();
    }
}
