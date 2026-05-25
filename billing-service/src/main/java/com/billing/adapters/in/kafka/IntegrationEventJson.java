package com.billing.adapters.in.kafka;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

final class IntegrationEventJson {

    private IntegrationEventJson() {}

    static JsonNode read(ObjectMapper objectMapper, String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid integration event JSON", e);
        }
    }

    static String text(JsonNode root, String field) {
        JsonNode node = root.get(field);
        return node == null || node.isNull() ? null : node.asText();
    }

    static long longVal(JsonNode root, String field) {
        JsonNode node = root.get(field);
        return node == null || node.isNull() ? 0L : node.asLong();
    }
}
