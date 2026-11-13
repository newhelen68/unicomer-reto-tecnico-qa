package com.unicomer.qa.listeners;

import com.unicomer.qa.config.ConfigManager;
import com.unicomer.qa.drivers.DriverManager;
import io.qameta.allure.Allure;
import io.qameta.allure.AttachmentOptions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Registra el resultado de cada prueba, adjunta capturas de las pruebas UI y publica el ambiente en Allure.
 */
public class TestListener implements ITestListener, ISuiteListener {

    private static final Logger LOG = LoggerFactory.getLogger(TestListener.class);
    private static final String RESULTS_DIRECTORY_PROPERTY = "allure.results.directory";
    private static final String DEFAULT_RESULTS_DIRECTORY = "target/allure-results";

    @Override
    public void onStart(ISuite suite) {
        writeAllureEnvironment();
    }

    @Override
    public void onTestStart(ITestResult result) {
        LOG.info("Inicio: {}", testName(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOG.info("Aprobada: {} ({} ms)", testName(result), duration(result));
        attachScreenshot("Captura final");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOG.error("Fallida: {} ({} ms) - {}", testName(result), duration(result), failureMessage(result));
        attachScreenshot("Captura del fallo");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOG.warn("Omitida: {} - {}", testName(result), failureMessage(result));
    }

    // Solo las pruebas UI tienen una sesión de navegador activa en el hilo.
    private void attachScreenshot(String name) {
        DriverManager.currentDriver()
                .filter(TakesScreenshot.class::isInstance)
                .map(TakesScreenshot.class::cast)
                .ifPresent(driver -> {
                    try {
                        byte[] screenshot = driver.getScreenshotAs(OutputType.BYTES);
                        Allure.attachment(name, "image/png",
                                new ByteArrayInputStream(screenshot), AttachmentOptions.withFileExtension(".png"));
                    } catch (WebDriverException e) {
                        LOG.warn("No fue posible capturar la pantalla: {}", e.getMessage());
                    }
                });
    }

    // Formato clave=valor sin escapes de Properties.store para el lector de Allure 3.
    private void writeAllureEnvironment() {
        Map<String, String> environment = new LinkedHashMap<>();
        // Se registra sin validar: la suite API no debe depender de la configuración del navegador.
        environment.put("Navegador", ConfigManager.browserName());
        environment.put("Headless", String.valueOf(ConfigManager.isHeadless()));
        environment.put("Web", ConfigManager.webBaseUrl());
        environment.put("API", ConfigManager.apiBaseUrl());
        environment.put("Java", System.getProperty("java.version"));
        environment.put("SistemaOperativo", System.getProperty("os.name"));
        List<String> lines = environment.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .toList();

        Path directory = Path.of(System.getProperty(RESULTS_DIRECTORY_PROPERTY, DEFAULT_RESULTS_DIRECTORY));
        try {
            Files.createDirectories(directory);
            Files.write(directory.resolve("environment.properties"), lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.warn("No fue posible escribir el ambiente de Allure: {}", e.getMessage());
        }
    }

    private static String testName(ITestResult result) {
        String description = result.getMethod().getDescription();
        String name = description == null || description.isBlank()
                ? result.getMethod().getMethodName()
                : description;
        Object[] parameters = result.getParameters();
        return parameters.length == 0 ? name : name + " " + Arrays.toString(parameters);
    }

    private static long duration(ITestResult result) {
        return result.getEndMillis() - result.getStartMillis();
    }

    private static String failureMessage(ITestResult result) {
        Throwable throwable = result.getThrowable();
        return throwable == null ? "sin detalle" : throwable.getMessage();
    }
}
