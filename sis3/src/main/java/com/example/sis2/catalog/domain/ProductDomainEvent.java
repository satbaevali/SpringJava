package com.example.sis2.catalog.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ProductDomainEvent(
        String eventId,
        String eventType,
        Long productId,
        String productName,
        BigDecimal price,
        int remainingStock,
        Instant occurredAt
) {

    public static ProductDomainEvent productCreated(Product product) {
        return new ProductDomainEvent(
                UUID.randomUUID().toString(),
                "PRODUCT_CREATED",
                product.getId(),
                product.getName(),
                product.getPrice().amount(),
                product.getStock(),
                Instant.now()
        );
    }

    public static ProductDomainEvent stockReduced(Product product) {
        return new ProductDomainEvent(
                UUID.randomUUID().toString(),
                "STOCK_REDUCED",
                product.getId(),
                product.getName(),
                product.getPrice().amount(),
                product.getStock(),
                Instant.now()
        );
    }

    public static ProductDomainEvent poisonEvent(Long productId) {
        return new ProductDomainEvent(
                UUID.randomUUID().toString(),
                "POISON_EVENT",
                productId,
                "CORRUPTED_PAYLOAD",
                new BigDecimal("0.00"),
                -999,
                Instant.now()
        );
    }

    public String aggregateKey() {
        return String.valueOf(productId);
    }

    public String toJson() {
        String safeName = productName == null ? "" : productName.replace("\"", "\\\"");
        return "{"
                + "\"eventId\":\"" + eventId + "\","
                + "\"eventType\":\"" + eventType + "\","
                + "\"productId\":" + productId + ","
                + "\"productName\":\"" + safeName + "\","
                + "\"price\":" + (price != null ? price.toPlainString() : "0.00") + ","
                + "\"remainingStock\":" + remainingStock + ","
                + "\"occurredAt\":\"" + (occurredAt != null ? occurredAt.toString() : Instant.now().toString()) + "\""
                + "}";
    }

    public static ProductDomainEvent fromJson(String json) {
        if (json == null || !json.trim().startsWith("{") || json.contains("RAW_POISON")) {
            throw new IllegalArgumentException("Poison message: malformed JSON payload: " + json);
        }
        String eventId = extractString(json, "eventId");
        String eventType = extractString(json, "eventType");
        Long productId = Long.parseLong(extractNumber(json, "productId"));
        String productName = extractString(json, "productName");
        BigDecimal price = new BigDecimal(extractNumber(json, "price"));
        int remainingStock = Integer.parseInt(extractNumber(json, "remainingStock"));
        String occurredAtStr = extractString(json, "occurredAt");
        Instant occurredAt = occurredAtStr.isEmpty() ? Instant.now() : Instant.parse(occurredAtStr);

        return new ProductDomainEvent(eventId, eventType, productId, productName, price, remainingStock, occurredAt);
    }

    private static String extractString(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + field + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private static String extractNumber(String json, String field) {
        Pattern pattern = Pattern.compile("\"" + field + "\"\\s*:\\s*(-?[0-9.]+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Poison message: missing numeric field " + field);
    }
}
