package com.bammwise.model;


import java.util.Objects;


public record CompatibilityRule(
        String component,
        String versionRange,
        int minJavaVersion,
        Integer maxJavaVersion,
        String severity,
        String message
) {
    public CompatibilityRule {
        component = requireText(component, "component");
        versionRange = requireText(versionRange, "versionRange");
        severity = requireText(severity, "severity");
        message = requireText(message, "message");

        if (minJavaVersion < 0) {
            throw new IllegalArgumentException(
                    "minJavaVersion cannot be negative"
            );
        }

        if (maxJavaVersion != null
                && maxJavaVersion < minJavaVersion) {
            throw new IllegalArgumentException(
                    "maxJavaVersion cannot be lower than minJavaVersion"
            );
        }
    }

    private static String requireText(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " cannot be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }

        return value;
    }
}