
package com.bammwise.compatibility;

import com.bammwise.model.CompatibilityRule;

import java.util.List;

/**
 * Provides the default compatibility rules for Spring Boot.
 *
 * These rules are data definitions. CompatibilityRuleEngine
 * evaluates them independently.
 */
public final class DefaultCompatibilityRules {

    private DefaultCompatibilityRules() {
        // Utility class
    }

    public static List<CompatibilityRule> all() {
        return List.of(
                springBoot2JavaRule(),
                springBoot3JavaRule(),
                springBoot4JavaRule()
        );
    }

    private static CompatibilityRule springBoot2JavaRule() {
        return new CompatibilityRule(
                "spring-boot",
                "2.x",
                8,
                null,
                "ERROR",
                "Spring Boot 2.x requires Java 8 or later."
        );
    }

    private static CompatibilityRule springBoot3JavaRule() {
        return new CompatibilityRule(
                "spring-boot",
                "3.x",
                17,
                null,
                "ERROR",
                "Spring Boot 3.x requires Java 17 or later."
        );
    }

    private static CompatibilityRule springBoot4JavaRule() {
        return new CompatibilityRule(
                "spring-boot",
                "4.x",
                17,
                null,
                "ERROR",
                "Spring Boot 4.x requires Java 17 or later."
        );
    }
}
