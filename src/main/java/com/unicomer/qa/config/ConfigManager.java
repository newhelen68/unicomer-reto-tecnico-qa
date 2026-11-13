package com.unicomer.qa.config;

import com.unicomer.qa.drivers.BrowserType;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Configuración centralizada del framework.
 *
 * <p>Prioridad: propiedad JVM ({@code -Dbrowser}), variable de entorno ({@code BROWSER}),
 * archivo {@code .env} y valores por defecto de {@code config.properties}.
 */
public final class ConfigManager {

    private static final String DEFAULTS_RESOURCE = "config.properties";
    private static final Path ENV_FILE = Path.of(".env");

    private static final Properties DEFAULTS = loadDefaults();
    private static final Map<String, String> ENV_FILE_VALUES = loadEnvFile();

    private ConfigManager() {
    }

    public static String webBaseUrl() {
        return get("WEB_BASE_URL");
    }

    public static String apiBaseUrl() {
        return get("API_BASE_URL");
    }

    public static BrowserType browser() {
        return BrowserType.from(get("BROWSER"));
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(get("HEADLESS"));
    }

    public static int windowWidth() {
        return getInt("WINDOW_WIDTH");
    }

    public static int windowHeight() {
        return getInt("WINDOW_HEIGHT");
    }

    public static Duration explicitWait() {
        return Duration.ofSeconds(getInt("EXPLICIT_WAIT"));
    }

    public static Duration pageLoadTimeout() {
        return Duration.ofSeconds(getInt("PAGE_LOAD_TIMEOUT"));
    }

    public static int apiRequestTimeoutMs() {
        return getInt("API_REQUEST_TIMEOUT_MS");
    }

    public static long apiMaxResponseTimeMs() {
        return getInt("API_MAX_RESPONSE_TIME_MS");
    }

    private static String get(String key) {
        return Stream.of(
                        System.getProperty(toPropertyName(key)),
                        System.getenv(key),
                        ENV_FILE_VALUES.get(key),
                        DEFAULTS.getProperty(key))
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Configuración requerida no definida: " + key));
    }

    private static int getInt(String key) {
        String value = get(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Valor numérico inválido para " + key + ": " + value, e);
        }
    }

    // BROWSER -> browser, EXPLICIT_WAIT -> explicit.wait
    private static String toPropertyName(String key) {
        return key.toLowerCase(Locale.ROOT).replace('_', '.');
    }

    private static Properties loadDefaults() {
        Properties properties = new Properties();
        try (InputStream stream = ConfigManager.class.getClassLoader().getResourceAsStream(DEFAULTS_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("No se encontró " + DEFAULTS_RESOURCE + " en el classpath");
            }
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException("No fue posible leer " + DEFAULTS_RESOURCE, e);
        }
    }

    private static Map<String, String> loadEnvFile() {
        Map<String, String> values = new HashMap<>();
        if (!Files.isRegularFile(ENV_FILE)) {
            return values;
        }
        try {
            for (String line : Files.readAllLines(ENV_FILE, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                int separator = trimmed.indexOf('=');
                if (trimmed.startsWith("#") || separator <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, separator).trim();
                String value = trimmed.substring(separator + 1).trim().replaceAll("^\"|\"$", "");
                values.put(key, value);
            }
            return values;
        } catch (IOException e) {
            throw new UncheckedIOException("No fue posible leer el archivo .env", e);
        }
    }
}
