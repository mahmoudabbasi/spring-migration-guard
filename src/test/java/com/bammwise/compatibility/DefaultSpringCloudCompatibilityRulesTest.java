
package com.bammwise.compatibility;

import com.bammwise.model.SpringCloudCompatibilityRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultSpringCloudCompatibilityRulesTest {

    @Test
    void shouldDefineExpectedReleaseFamilies() {
        List<SpringCloudCompatibilityRule> rules =
                DefaultSpringCloudCompatibilityRules.all();

        assertEquals(6, rules.size());

        assertTrue(rules.stream().anyMatch(
                rule -> rule.matchesBootVersion(3, 1)
                        && rule.matchesCloudVersion(2022, 0)
        ));

        assertTrue(rules.stream().anyMatch(
                rule -> rule.matchesBootVersion(3, 3)
                        && rule.matchesCloudVersion(2023, 0)
        ));

        assertTrue(rules.stream().anyMatch(
                rule -> rule.matchesBootVersion(4, 0)
                        && rule.matchesCloudVersion(2025, 1)
        ));
    }

    @Test
    void shouldNotMatchAnUnconfiguredBootVersion() {
        List<SpringCloudCompatibilityRule> rules =
                DefaultSpringCloudCompatibilityRules.all();

        assertFalse(rules.stream().anyMatch(
                rule -> rule.matchesBootVersion(3, 6)
        ));
    }
}
