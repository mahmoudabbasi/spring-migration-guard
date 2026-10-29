
package com.bammwise.compatibility;

import com.bammwise.model.ProjectEnvironment;
import com.bammwise.model.SpringCloudCompatibilityRule;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompatibilityValidator {

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?.*$");

    private static final Pattern JAVA_VERSION_PATTERN =
            Pattern.compile("^(?:1\\.)?(\\d+)(?:[.+_-].*)?$");

    private final CompatibilityRuleEngine ruleEngine =
            new CompatibilityRuleEngine(
                    DefaultCompatibilityRules.all()
            );

    private final List<SpringCloudCompatibilityRule> springCloudRules =
            DefaultSpringCloudCompatibilityRules.all();

    public List<String> validate(ProjectEnvironment environment) {
        List<String> issues = new ArrayList<>();

        if (environment == null) {
            issues.add("[ERROR] Project environment is missing.");
            return issues;
        }

        String bootVersion = environment.getSpringBootVersion();
        String javaVersion = environment.getJavaVersion();
        String cloudVersion = environment.getSpringCloudVersion();

        if (isBlank(bootVersion)) {
            issues.add(
                    "[WARNING] Spring Boot version could not be detected."
            );
        }

        if (isBlank(javaVersion)) {
            issues.add(
                    "[WARNING] Java version could not be detected."
            );
        }

        Integer bootMajor = parseMajorVersion(bootVersion);
        Integer javaMajor = parseJavaMajorVersion(javaVersion);

        if (!isBlank(bootVersion) && bootMajor == null) {
            issues.add(
                    "[WARNING] Could not parse Spring Boot version: "
                            + bootVersion
            );
        }

        if (!isBlank(javaVersion) && javaMajor == null) {
            issues.add(
                    "[WARNING] Could not parse Java version: "
                            + javaVersion
            );
        }

        if (bootMajor != null && javaMajor != null) {
            issues.addAll(ruleEngine.evaluate(environment));

            if (bootMajor < 2 || bootMajor > 4) {
                issues.add(
                        "[WARNING] No Java compatibility rule is configured "
                                + "for Spring Boot " + bootVersion + "."
                );
            }

            if ("2.7.1".equals(bootVersion) && javaMajor > 18) {
                issues.add(
                        "[WARNING] Spring Boot 2.7.1 officially supports Java "
                                + "up to 18, but the project declares Java "
                                + javaVersion
                                + ". Consider upgrading Spring Boot or using "
                                + "a supported Java version."
                );
            }
        }
        if (hasSpringCloudDependency(environment)) {
            validateSpringCloudCompatibility(
                    bootVersion,
                    cloudVersion,
                    issues
            );
        }

        if (issues.isEmpty()) {
            issues.add(
                    "[OK] No compatibility issue was found by the "
                            + "current validation rules."
            );
        }

        return issues;
    }


    private void validateSpringCloudCompatibility(
            String bootVersion,
            String cloudVersion,
            List<String> issues) {

        if (isBlank(bootVersion)) {
            return;
        }

        if (isBlank(cloudVersion)) {
            issues.add(
                    "[WARNING] Spring Cloud dependencies were detected, "
                            + "but the Spring Cloud release train version "
                            + "could not be determined."
            );
            return;
        }

        Integer bootMajor = parseMajorVersion(bootVersion);
        Integer bootMinor = parseMinorVersion(bootVersion);
        Integer cloudMajor = parseMajorVersion(cloudVersion);
        Integer cloudMinor = parseMinorVersion(cloudVersion);

        if (bootMajor == null || bootMinor == null
                || cloudMajor == null || cloudMinor == null) {
            issues.add(
                    "[WARNING] Could not verify Spring Boot / "
                            + "Spring Cloud compatibility."
            );
            return;
        }

        SpringCloudCompatibilityRule matchedRule =
                springCloudRules.stream()
                        .filter(rule -> rule.matchesBootVersion(
                                bootMajor, bootMinor))
                        .findFirst()
                        .orElse(null);

        if (matchedRule == null) {
            issues.add(
                    "[WARNING] No Spring Cloud compatibility rule is "
                            + "configured for Spring Boot " + bootVersion
                            + ". Verify the official compatibility matrix."
            );
            return;
        }

        if (!matchedRule.matchesCloudVersion(cloudMajor, cloudMinor)) {
            issues.add(
                    "[WARNING] Spring Boot " + bootVersion
                            + " is expected to use Spring Cloud "
                            + matchedRule.cloudMajor() + "."
                            + matchedRule.cloudMinor()
                            + ". Detected Spring Cloud " + cloudVersion
                            + ". Verify the official compatibility matrix."
            );
            return;
        }

        issues.add(
                "[INFO] Spring Boot " + bootVersion
                        + " and Spring Cloud " + cloudVersion
                        + " match a configured release-family rule. "
                        + "Verify the minimum release and support status."
        );
    }


    private boolean hasSpringCloudDependency(
            ProjectEnvironment environment) {

        return environment.getDependencies().stream()
                .filter(dependency -> dependency != null)
                .anyMatch(dependency ->
                        dependency.startsWith("org.springframework.cloud:")
                );
    }

    private Integer parseMajorVersion(String version) {
        return parseVersionPart(version, 1);
    }

    private Integer parseMinorVersion(String version) {
        return parseVersionPart(version, 2);
    }

    private Integer parseVersionPart(String version, int group) {
        if (isBlank(version)) {
            return null;
        }

        Matcher matcher = VERSION_PATTERN.matcher(version.trim());

        if (!matcher.matches()) {
            return null;
        }

        String value = matcher.group(group);

        if (value == null) {
            return group == 2 ? 0 : null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer parseJavaMajorVersion(String version) {
        if (isBlank(version)) {
            return null;
        }

        Matcher matcher = JAVA_VERSION_PATTERN.matcher(version.trim());

        if (!matcher.matches()) {
            return null;
        }

        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
