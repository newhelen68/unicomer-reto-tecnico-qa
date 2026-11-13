package com.unicomer.qa.drivers;

import java.util.Optional;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gestiona una sesión WebDriver independiente por hilo; prepara el framework para ejecución paralela.
 */
public final class DriverManager {

    private static final Logger LOG = LoggerFactory.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    public static void startDriver() {
        if (DRIVER.get() != null) {
            throw new IllegalStateException("Ya existe una sesión WebDriver activa en este hilo");
        }
        DRIVER.set(DriverFactory.createDriver());
    }

    public static WebDriver getDriver() {
        return currentDriver().orElseThrow(
                () -> new IllegalStateException("No hay una sesión WebDriver activa en este hilo"));
    }

    public static Optional<WebDriver> currentDriver() {
        return Optional.ofNullable(DRIVER.get());
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            return;
        }
        try {
            driver.quit();
        } catch (RuntimeException e) {
            LOG.warn("No fue posible cerrar el navegador: {}", e.getMessage());
        } finally {
            DRIVER.remove();
        }
    }
}
