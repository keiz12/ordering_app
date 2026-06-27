package com.example.orderingapp.dto.statistic;

import java.math.*;
import java.util.*;


public class StatisticResponse {

    private final Map<String, BigDecimal> rawDataMap = new TreeMap<>();

    private final Map<String, BigDecimal> percentageDataMap = new TreeMap<>();

    private final Map<String, Double> ratings = new TreeMap<>();

    public Map<String, BigDecimal> getRawDataMap() {
        return rawDataMap;
    }

    public Map<String, BigDecimal> getPercentageDataMap() {
        return percentageDataMap;
    }

    public Map<String, Double> getRatings() {
        return ratings;
    }
}
