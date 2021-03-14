package com.challenge.forexrate.model;

import lombok.Getter;

@Getter
public enum Trend {

    ASCENDING("ascending"),
    DESCENDING("descending"),
    CONSTANT("constant"),
    UNDEFINED("undefined");

    private final String trendName;

    Trend(String trendName) {
        this.trendName = trendName;
    }
}
