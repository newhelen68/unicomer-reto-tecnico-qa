# QA Automation - Reto técnico Grupo Unicomer

## Descripción

Framework de automatización para el reto técnico de QA Automation: pruebas UI de la
[calculadora de créditos de Banco CUSCATLAN](https://www.bancocuscatlan.com/creditos/calculadora-creditos)
con Selenium y pruebas del endpoint `GET https://catfact.ninja/facts` con REST Assured.

## Tecnologías

| Componente | Versión |
|---|---|
| Java (compilación) | 21 |
| Maven Wrapper / Maven | 3.3.4 / 3.9.16 |
| Selenium WebDriver (Selenium Manager) | 4.52.0 |
| TestNG | 7.12.0 |
| REST Assured + JSON Schema Validator | 6.3.0 |
| Allure Java / Allure Report | 3.1.1 / 3.17.0 |
| SLF4J / Logback | 2.0.20 / 1.6.6 |
| Checkstyle | 14.4.0 |
| Maven Surefire | 3.5.6 |

## Estructura

```text
.github/workflows/                    Pipelines de CI
config/checkstyle/checkstyle.xml      Reglas de calidad de código
suites/                               Suites TestNG (testng, ui, api, smoke, regression, known-defects)
src/main/java/com/unicomer/qa/
  api/          CatFactsClient
  config/       ConfigManager
  drivers/      BrowserType, DriverFactory, DriverManager
  models/       CatFact, CatFactsResponse, SliderRange
  pages/        BasePage, CreditCalculatorPage
  utils/        CurrencyUtils
src/test/java/com/unicomer/qa/
  api/          CatFactsApiTest
  base/         BaseTest
  data/         CreditDataProvider y modelos de datos
  listeners/    TestListener
  ui/           CreditCalculatorTest
src/test/resources/
  schemas/      catfacts-schema.json
  testdata/     credit-calculator.json
```

## Requisitos

- JDK 21 o superior.
- Google Chrome (por defecto), Microsoft Edge o Mozilla Firefox.
- Acceso a internet. Maven no es necesario: el proyecto incluye Maven Wrapper.

Selenium Manager descarga automáticamente el driver del navegador.

## Configuración

La configuración se resuelve en este orden de prioridad:

1. Propiedad JVM: `-Dbrowser=firefox`, `-Dheadless=true`, `-Dexplicit.wait=20`.
2. Variable de entorno: `BROWSER`, `HEADLESS`, `EXPLICIT_WAIT`.
3. Archivo `.env` en la raíz del proyecto (opcional).
4. Valores por defecto en `src/main/resources/config.properties`.

Para un ambiente local, copiar la plantilla y ajustar los valores:

```bash
cp .env.example .env
```

| Variable | Uso |
|---|---|
| `WEB_BASE_URL` / `API_BASE_URL` | URLs base del sitio y de la API |
| `BROWSER` | `chrome`, `firefox` o `edge` |
| `HEADLESS` | Ejecución sin interfaz gráfica |
| `WINDOW_WIDTH` / `WINDOW_HEIGHT` | Tamaño de la ventana |
| `EXPLICIT_WAIT` / `PAGE_LOAD_TIMEOUT` | Esperas en segundos |
| `API_REQUEST_TIMEOUT_MS` | Tiempo máximo de conexión y lectura HTTP |
| `API_MAX_RESPONSE_TIME_MS` | Umbral de la validación de rendimiento (3000 ms) |

## Instalación

```bash
git clone https://github.com/newhelen68/unicomer-reto-tecnico-qa.git
cd unicomer-reto-tecnico-qa
./mvnw test-compile
```

En Windows se usa `mvnw.cmd`. En PowerShell, los argumentos `-D` deben ir entre comillas:
`.\mvnw.cmd clean test "-DsuiteXmlFile=suites/api.xml"`.

Con Maven 3.9 o superior instalado, los mismos comandos funcionan con `mvn`.

## Ejecución

```bash
# Suite completa (UI + API)
./mvnw clean test

# Por tipo y por nivel
./mvnw clean test -DsuiteXmlFile=suites/ui.xml
./mvnw clean test -DsuiteXmlFile=suites/api.xml
./mvnw clean test -DsuiteXmlFile=suites/smoke.xml
./mvnw clean test -DsuiteXmlFile=suites/regression.xml

# Navegador y modo headless
./mvnw clean test -Dbrowser=chrome
./mvnw clean test -Dbrowser=edge -Dheadless=true
./mvnw clean test -Dbrowser=firefox -Dheadless=true

# Defectos conocidos (falla mientras el defecto D-01 siga abierto)
./mvnw clean test -DsuiteXmlFile=suites/known-defects.xml
```

| Suite | Contenido | Pruebas |
|---|---|---|
| `testng.xml` | UI + API, sin defectos conocidos | 14 |
| `ui.xml` | Calculadora de créditos | 9 |
| `api.xml` | Cat Facts | 5 |
| `smoke.xml` | Grupo `smoke` | 4 |
| `regression.xml` | Grupo `regression` | 14 |
| `known-defects.xml` | Grupo `known-defect` | 1 |

## Reportes

```bash
./mvnw allure:report
```

Genera `target/allure-report/index.html`, un archivo único que se abre directamente en el navegador.
Incluye pasos, parámetros, duración, intercambios HTTP de la API, ambiente de ejecución y
captura de pantalla de cada prueba UI fallida. La primera generación descarga un Node.js privado
en `.allure/` (excluido de Git).

Los resultados de TestNG quedan en `target/surefire-reports`.

## CI/CD

El workflow `qa-automation.yml` se ejecuta en push a `main`, en pull requests y de forma manual
(con selección de navegador). Ejecuta Checkstyle y compilación con Java 21, las suites API y UI en jobs
independientes, y publica como artifacts los resultados y el reporte Allure, incluso cuando hay fallos.

El workflow `known-defects.yml` ejecuta bajo demanda la suite de defectos conocidos y publica su reporte.

## Consideraciones

- Las pruebas dependen de servicios externos públicos; su disponibilidad y latencia afectan los resultados.
- `TC-UI-006` documenta el defecto D-01: en Vivienda y Vehículo, la alerta "La cuota supera tu ingreso"
  permanece visible cuando el monto se vuelve inválido. Falla de forma intencional y está excluido de las
  suites principales.
- Se usa Surefire 3.5.6 porque la rama 3.6 no admite suites XML de TestNG.
- La advertencia de Selenium sobre la versión CDP no afecta las pruebas: el framework no usa DevTools.
