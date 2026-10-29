
package com.bammwise.compatibility;

import com.bammwise.model.SpringCloudCompatibilityRule;

import java.util.List;

/**
 * Default Spring Boot / Spring Cloud release-family mappings.
 *
 * These mappings describe release families, not security support
 * or compatibility with every individual patch release.
 */
public final class DefaultSpringCloudCompatibilityRules {

    private DefaultSpringCloudCompatibilityRules() {
    }

    public static List<SpringCloudCompatibilityRule> all() {
        return List.of(
                new SpringCloudCompatibilityRule(2, 6, 7, 2021, 0),
                new SpringCloudCompatibilityRule(3, 0, 1, 2022, 0),
                new SpringCloudCompatibilityRule(3, 2, 3, 2023, 0),
                new SpringCloudCompatibilityRule(3, 4, 4, 2024, 0),
                new SpringCloudCompatibilityRule(3, 5, 5, 2025, 0),
                new SpringCloudCompatibilityRule(4, 0, 0, 2025, 1)
        );
    }
}
