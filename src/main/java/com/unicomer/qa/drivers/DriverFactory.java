package com.unicomer.qa.drivers;

import com.unicomer.qa.config.ConfigManager;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Crea sesiones WebDriver según la configuración. Selenium Manager resuelve el driver de cada navegador.
 */
public final class DriverFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    public static WebDriver createDriver() {
        BrowserType browser = ConfigManager.browser();
        boolean headless = ConfigManager.isHeadless();
        LOG.info("Iniciando {} (headless={})", browser, headless);

        WebDriver driver = switch (browser) {
            case CHROME -> new ChromeDriver(configureChromium(new ChromeOptions(), headless));
            case EDGE -> new EdgeDriver(configureChromium(new EdgeOptions(), headless));
            case FIREFOX -> new FirefoxDriver(firefoxOptions(headless));
        };

        try {
            driver.manage().timeouts().pageLoadTimeout(ConfigManager.pageLoadTimeout());
            driver.manage().window().setSize(new Dimension(ConfigManager.windowWidth(), ConfigManager.windowHeight()));
            return driver;
        } catch (RuntimeException e) {
            // Evita dejar un navegador abierto si falla su configuración.
            driver.quit();
            throw e;
        }
    }

    private static <T extends ChromiumOptions<T>> T configureChromium(T options, boolean headless) {
        // El sitio carga scripts de terceros; la sincronización se hace con esperas explícitas.
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        options.addArguments("--disable-notifications");
        if (headless) {
            options.addArguments("--headless=new");
        }
        if (Platform.getCurrent().is(Platform.LINUX)) {
            // Requerido en runners Linux de CI sin sandbox de usuario ni /dev/shm suficiente.
            options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        }
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        if (headless) {
            options.addArguments("-headless");
        }
        return options;
    }
}
