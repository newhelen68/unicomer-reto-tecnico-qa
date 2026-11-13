package com.unicomer.qa.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CatFact(String fact, Integer length) {
}
