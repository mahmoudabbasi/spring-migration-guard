package com.bammwise.model;

public record CompatibilityRule(
        String component,
        String versionRange,
        int minJavaVersion,
        Integer maxJavaVersion,
        String severity,
        String message
) {}