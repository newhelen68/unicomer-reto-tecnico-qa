package com.unicomer.qa.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Respuesta paginada de {@code GET /facts}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CatFactsResponse(
        @JsonProperty("per_page") Integer perPage,
        List<CatFact> data) {
}
