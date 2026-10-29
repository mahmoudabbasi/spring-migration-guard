
package com.bammwise.compatibility;

import com.bammwise.model.CompatibilityRule;
import com.bammwise.model.ProjectEnvironment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompatibilityRuleEngineTest {

/*
    private final CompatibilityRuleEngine engine =
            new CompatibilityRuleEngine(List.of(
                    new CompatibilityRule(
                            "spring-boot",
                            "3.x",
                            17,
                            null,
                            "ERROR",
                            "This Spring Boot version requires a newer Java version."
                    )
            ));
*/

    private final CompatibilityRuleEngine engine =
            new CompatibilityRuleEngine(
                    DefaultCompatibilityRules.all()
            );

    @Test
    void shouldReportErrorForBoot3WithJava8() {
        ProjectEnvironment environment = environment("3.1.0", "8");

        List<String> issues = engine.evaluate(environment);

        assertTrue(issues.stream().anyMatch(
                issue -> issue.contains("[ERROR]")
                        && issue.contains("Java 17 or later")
        ));
    }

    @Test
    void shouldAcceptBoot3WithJava17() {
        ProjectEnvironment environment = environment("3.1.0", "17");

        List<String> issues = engine.evaluate(environment);

        assertTrue(issues.isEmpty());
    }

    @Test
    void shouldIgnoreBoot2ForBoot3Rule() {
        ProjectEnvironment environment = environment("2.7.1", "8");

        List<String> issues = engine.evaluate(environment);

        assertTrue(issues.isEmpty());
    }

    @Test
    void shouldNotCrashWhenEnvironmentIsNull() {
        assertFalse(engine.evaluate(null).isEmpty());
    }

    private ProjectEnvironment environment(
            String bootVersion,
            String javaVersion) {

        ProjectEnvironment environment = new ProjectEnvironment();
        environment.setSpringBootVersion(bootVersion);
        environment.setJavaVersion(javaVersion);
        return environment;
    }
}
