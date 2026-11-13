package com.unicomer.qa.api;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.unicomer.qa.models.CatFact;
import com.unicomer.qa.models.CatFactsResponse;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

@Epic("Reto técnico QA Automation")
@Feature("API Cat Facts")
public class CatFactsApiTest {

    private static final int HTTP_OK = 200;
    // Umbral definido por el reto; no es configurable para no relajar el requisito.
    private static final long MAX_RESPONSE_TIME_MS = 3000;
    private static final String SCHEMA = "schemas/catfacts-schema.json";
    private static final int LIMIT = 5;
    private static final int MAX_LENGTH = 40;

    private CatFactsClient client;

    @BeforeClass(alwaysRun = true)
    public void createClient() {
        client = new CatFactsClient();
    }

    @Test(groups = "smoke", description = "TC-API-001 Código de estado 200")
    @Story("Disponibilidad")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verifica que GET /facts responda exactamente con HTTP 200.")
    public void shouldRespondWithStatus200() {
        Response response = client.getFacts();

        assertEquals(response.statusCode(), HTTP_OK, "Código de estado HTTP");
    }

    @Test(groups = "smoke", description = "TC-API-002 Respuesta no vacía")
    @Story("Contenido")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verifica que la respuesta contenga hechos con texto.")
    public void shouldReturnNonEmptyBody() {
        Response response = client.getFacts();

        assertFalse(response.asString().isBlank(), "El cuerpo de la respuesta no debe estar vacío");
        CatFactsResponse body = response.as(CatFactsResponse.class);
        assertNotNull(body.data(), "La respuesta debe incluir el arreglo data");
        assertFalse(body.data().isEmpty(), "El arreglo data debe contener al menos un hecho");
        assertTrue(body.data().stream().allMatch(fact -> fact.fact() != null && !fact.fact().isBlank()),
                "Cada elemento debe incluir el texto del hecho");
    }

    @Test(groups = "smoke", description = "TC-API-003 Tiempo de respuesta menor a 3000 ms")
    @Story("Rendimiento")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Mide una solicitud real hasta recibir el cuerpo completo y valida que tarde menos de 3000 ms.")
    public void shouldRespondWithinMaximumTime() {
        // getTimeIn solo cubre hasta los encabezados; se mide hasta leer el cuerpo completo.
        long start = System.nanoTime();
        Response response = client.getFacts();
        response.asByteArray();
        long totalTimeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        Allure.parameter("Umbral (ms)", MAX_RESPONSE_TIME_MS);
        Allure.parameter("Tiempo total (ms)", totalTimeMs, true);
        Allure.parameter("Tiempo hasta encabezados (ms)", response.getTimeIn(TimeUnit.MILLISECONDS), true);

        // Un error rápido no debe aprobar la medición de rendimiento.
        assertEquals(response.statusCode(), HTTP_OK, "Precondición: respuesta exitosa");
        assertTrue(totalTimeMs < MAX_RESPONSE_TIME_MS,
                "Tiempo de respuesta " + totalTimeMs + " ms; máximo permitido " + MAX_RESPONSE_TIME_MS + " ms");
    }

    @Test(description = "TC-API-004 Contrato de la respuesta")
    @Story("Contrato")
    @Severity(SeverityLevel.NORMAL)
    @Description("Valida Content-Type, JSON Schema, tipos de datos y coherencia del campo length.")
    public void shouldMatchResponseContract() {
        Response response = client.getFacts();

        assertEquals(response.statusCode(), HTTP_OK, "Código de estado HTTP");
        assertTrue(response.contentType().startsWith(ContentType.JSON.toString()),
                "Content-Type esperado application/json, obtenido " + response.contentType());
        assertThat("Estructura según el esquema JSON", response.asString(), matchesJsonSchemaInClasspath(SCHEMA));

        CatFactsResponse body = response.as(CatFactsResponse.class);
        SoftAssert softly = new SoftAssert();
        softly.assertEquals(body.data().size(), body.perPage().intValue(), "Cantidad de elementos según per_page");
        for (CatFact fact : body.data()) {
            softly.assertEquals(fact.length().intValue(), characterCount(fact.fact()),
                    "length debe coincidir con la longitud del hecho: " + fact.fact());
        }
        softly.assertAll();
    }

    @Test(description = "TC-API-005 Parámetros limit y max_length")
    @Story("Parámetros de consulta")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verifica que la respuesta respete los parámetros documentados limit y max_length.")
    public void shouldApplyLimitAndMaxLengthParameters() {
        Response response = client.getFacts(Map.of("limit", LIMIT, "max_length", MAX_LENGTH));

        assertEquals(response.statusCode(), HTTP_OK, "Código de estado HTTP");
        CatFactsResponse body = response.as(CatFactsResponse.class);
        List<CatFact> facts = body.data();
        assertEquals(body.perPage().intValue(), LIMIT, "per_page según limit");
        assertFalse(facts.isEmpty(), "Debe existir al menos un hecho con la longitud solicitada");
        assertTrue(facts.size() <= LIMIT, "Cantidad de hechos no mayor a limit: " + facts.size());
        // Se mide el texto real para no depender del campo length que devuelve el servicio.
        assertTrue(facts.stream().allMatch(fact -> characterCount(fact.fact()) <= MAX_LENGTH),
                "Todos los hechos deben tener como máximo " + MAX_LENGTH + " caracteres");
    }

    // Cuenta caracteres Unicode, no unidades UTF-16.
    private static int characterCount(String text) {
        return text.codePointCount(0, text.length());
    }
}
