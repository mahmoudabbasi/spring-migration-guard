package com.bammwise.compatibility;



import com.bammwise.model.ProjectEnvironment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompatibilityValidatorTest {

    private final CompatibilityValidator validator =
            new CompatibilityValidator();

    @Test
    void shouldReportErrorWhenBoot31UsesJava8() {
        ProjectEnvironment environment = environment("3.1.0", "8");

        List<String> issues = validator.validate(environment);

        assertTrue(
                issues.stream().anyMatch(issue ->
                        issue.contains("[ERROR]")
                                && issue.contains("Java 17 or later")),
                "Spring Boot 3.1 should report an error for Java 8"
        );
    }

    @Test
    void shouldNotReportJavaCompatibilityErrorWhenBoot31UsesJava17() {
        ProjectEnvironment environment = environment("3.1.0", "17");

        List<String> issues = validator.validate(environment);

        assertTrue(
                issues.stream().noneMatch(issue ->
                        issue.contains("[ERROR]")
                                && issue.contains("Java")),
                "Spring Boot 3.1 should not report a Java error for Java 17"
        );
    }

    @Test
    void shouldWarnWhenBoot271UsesJava21() {
        ProjectEnvironment environment = environment("2.7.1", "21");

        List<String> issues = validator.validate(environment);

        assertTrue(
                issues.stream().anyMatch(issue ->
                        issue.contains("[WARNING]")
                                && issue.contains("Java")),
                "Spring Boot 2.7.1 should warn about Java 21"
        );
    }

    @Test
    void shouldWarnWhenJavaVersionIsMissing() {
        ProjectEnvironment environment = environment("3.1.0", null);

        List<String> issues = validator.validate(environment);

        assertTrue(
                issues.stream().anyMatch(issue ->
                        issue.contains("Java version could not be detected")),
                "A missing Java version should produce a warning"
        );
    }

    @Test
    void shouldReportErrorWhenEnvironmentIsNull() {
        List<String> issues = validator.validate(null);

        assertTrue(
                issues.stream().anyMatch(issue ->
                        issue.contains("[ERROR]")
                                && issue.contains("environment is missing")),
                "A missing project environment should produce an error"
        );
    }



    @Test
    void shouldUseDefaultRulesForBoot3WithJava8() {
        ProjectEnvironment environment = new ProjectEnvironment();
        environment.setSpringBootVersion("3.1.0");
        environment.setJavaVersion("8");

        CompatibilityValidator validator = new CompatibilityValidator();

        List<String> issues = validator.validate(environment);

        assertTrue(issues.stream().anyMatch(
                issue -> issue.contains("[ERROR]")
                        && issue.contains("Java 17 or later")
        ));
    }

    @Test
    void shouldUseDefaultRulesForBoot3WithJava17() {
        ProjectEnvironment environment = new ProjectEnvironment();
        environment.setSpringBootVersion("3.1.0");
        environment.setJavaVersion("17");

        CompatibilityValidator validator = new CompatibilityValidator();

        List<String> issues = validator.validate(environment);

        assertFalse(issues.stream().anyMatch(
                issue -> issue.contains("[ERROR]")
                        && issue.contains("Java 17 or later")
        ));
    }



    @Test
    void shouldReportInfoForBoot31AndCloud20220() {
        ProjectEnvironment environment = environment("3.1.0", "17");
        environment.setSpringCloudVersion("2022.0.3");
        environment.getDependencies().add(
                "org.springframework.cloud:spring-cloud-starter-gateway"
        );

        List<String> issues = validator.validate(environment);

        assertTrue(issues.stream().anyMatch(
                issue -> issue.contains("[INFO]")
                        && issue.contains("match a configured release-family rule")
        ));
    }

    @Test
    void shouldWarnForBoot31AndCloud20230() {
        ProjectEnvironment environment = environment("3.1.0", "17");
        environment.setSpringCloudVersion("2023.0.1");
        environment.getDependencies().add(
                "org.springframework.cloud:spring-cloud-starter-gateway"
        );

        List<String> issues = validator.validate(environment);

        assertTrue(issues.stream().anyMatch(
                issue -> issue.contains("[WARNING]")
                        && issue.contains("expected to use Spring Cloud 2022.0")
        ));
    }

    @Test
    void shouldWarnForUnconfiguredBootVersion() {
        ProjectEnvironment environment = environment("3.6.0", "17");
        environment.setSpringCloudVersion("2025.0.0");
        environment.getDependencies().add(
                "org.springframework.cloud:spring-cloud-starter-gateway"
        );

        List<String> issues = validator.validate(environment);

        assertTrue(issues.stream().anyMatch(
                issue -> issue.contains("[WARNING]")
                        && issue.contains("No Spring Cloud compatibility rule")
        ));
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