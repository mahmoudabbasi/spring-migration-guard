
package com.bammwise.model;

import java.util.Objects;

/**
 * Defines a Spring Boot minor-version range and its expected
 * Spring Cloud release family.
 */
public record SpringCloudCompatibilityRule(
        int bootMajor,
        int minBootMinor,
        int maxBootMinor,
        int cloudMajor,
        int cloudMinor
) {
    public SpringCloudCompatibilityRule {
        if (bootMajor < 1
                || minBootMinor < 0
                || maxBootMinor < minBootMinor
                || cloudMajor < 1
                || cloudMinor < 0) {
            throw new IllegalArgumentException(
                    "Invalid Spring Cloud compatibility rule"
            );
        }
    }

    public boolean matchesBootVersion(int major, int minor) {
        return bootMajor == major
                && minor >= minBootMinor
                && minor <= maxBootMinor;
    }

    public boolean matchesCloudVersion(int major, int minor) {
        return cloudMajor == major && cloudMinor == minor;
    }
}
