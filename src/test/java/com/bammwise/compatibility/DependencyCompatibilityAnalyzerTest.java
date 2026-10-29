
package com.bammwise.compatibility;

import com.bammwise.model.DependencyInfo;
import com.bammwise.model.ProjectEnvironment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DependencyCompatibilityAnalyzerTest {

    private final DependencyCompatibilityAnalyzer analyzer =
            new DependencyCompatibilityAnalyzer();

    @Test
    void shouldWarnWhenBoot2UsesSpringSecurity6InsteadOfManaged5() {
        ProjectEnvironment environment = new ProjectEnvironment();
        environment.setSpringBootVersion("2.7.1");

        environment.addDependencyInfo(
                new DependencyInfo(
                        "org.springframework.security",
                        "spring-security-config",
                        "6.1.0",
                        "compile",
                        "5.7.2"
                )
        );

        List<String> issues = analyzer.analyze(environment);

        assertEquals(1, issues.size());
        assertTrue(
                issues.get(0).contains(
                        "Potential Spring Security version incompatibility"
                )
        );
        assertTrue(issues.get(0).contains("6.1.0"));
        assertTrue(issues.get(0).contains("5.7.2"));
    }

    @Test
    void shouldNotWarnWhenResolvedAndManagedVersionsMatch() {
        ProjectEnvironment environment = new ProjectEnvironment();
        environment.setSpringBootVersion("2.7.1");

        environment.addDependencyInfo(
                new DependencyInfo(
                        "org.springframework.security",
                        "spring-security-config",
                        "5.7.2",
                        "compile",
                        "5.7.2"
                )
        );

        assertTrue(analyzer.analyze(environment).isEmpty());
    }

    @Test
    void shouldNotWarnForBoot3WithSpringSecurity6() {
        ProjectEnvironment environment = new ProjectEnvironment();
        environment.setSpringBootVersion("3.1.0");

        environment.addDependencyInfo(
                new DependencyInfo(
                        "org.springframework.security",
                        "spring-security-config",
                        "6.1.0",
                        "compile",
                        "6.1.0"
                )
        );

        assertTrue(analyzer.analyze(environment).isEmpty());
    }
}
