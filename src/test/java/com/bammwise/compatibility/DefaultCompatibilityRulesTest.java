
package com.bammwise.compatibility;

import com.bammwise.model.CompatibilityRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DefaultCompatibilityRulesTest {

    @Test
    void shouldProvideDefaultRules() {
        List<CompatibilityRule> rules =
                DefaultCompatibilityRules.all();

        assertNotNull(rules);
        assertEquals(3, rules.size());
    }

    @Test
    void shouldDefineRulesForBoot2Boot3AndBoot4() {
        List<CompatibilityRule> rules =
                DefaultCompatibilityRules.all();

        assertFalse(rules.stream().noneMatch(
                rule -> rule.versionRange().equals("2.x")
        ));

        assertFalse(rules.stream().noneMatch(
                rule -> rule.versionRange().equals("3.x")
        ));

        assertFalse(rules.stream().noneMatch(
                rule -> rule.versionRange().equals("4.x")
        ));
    }

    @Test
    void shouldDefineJava17MinimumForBoot3AndBoot4() {
        List<CompatibilityRule> rules =
                DefaultCompatibilityRules.all();

        assertEquals(17, rules.stream()
                .filter(rule -> rule.versionRange().equals("3.x"))
                .findFirst()
                .orElseThrow()
                .minJavaVersion());

        assertEquals(17, rules.stream()
                .filter(rule -> rule.versionRange().equals("4.x"))
                .findFirst()
                .orElseThrow()
                .minJavaVersion());
    }
}
