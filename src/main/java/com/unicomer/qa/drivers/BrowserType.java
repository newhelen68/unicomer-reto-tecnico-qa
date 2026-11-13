package com.unicomer.qa.drivers;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public enum BrowserType {
    CHROME,
    FIREFOX,
    EDGE;

    public static BrowserType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Navegador no soportado: " + value + ". Opciones: " + supportedValues()));
    }

    private static String supportedValues() {
        return Arrays.stream(values())
                .map(type -> type.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(", "));
    }
}
