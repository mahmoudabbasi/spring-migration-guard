
package com.bammwise.compatibility;

import com.bammwise.model.ProjectEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompatibilityValidator {

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("^(\\d+)\\.(\\d+)(?:\\.(\\d+))?.*$");

    private static final Pattern JAVA_VERSION_PATTERN =
            Pattern.compile("^(?:1\\.)?(\\d+)(?:[.+_-].*)?$");

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
            validateJavaCompatibility(
                    bootVersion,
                    bootMajor,
                    javaVersion,
                    javaMajor,
                    issues
            );
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

    private void validateJavaCompatibility(
            String bootVersion,
            int bootMajor,
            String javaVersion,
            int javaMajor,
            List<String> issues) {

        if (bootMajor == 3 && javaMajor < 17) {
            issues.add(
                    "[ERROR] Spring Boot " + bootVersion
                            + " requires Java 17 or later, but the project "
                            + "declares Java " + javaVersion + "."
            );
            return;
        }

        if (bootMajor >= 4 && javaMajor < 17) {
            issues.add(
                    "[ERROR] Spring Boot " + bootVersion
                            + " requires Java 17 or later, but the project "
                            + "declares Java " + javaVersion + "."
            );
            return;
        }

        if (bootMajor == 2 && javaMajor < 8) {
            issues.add(
                    "[ERROR] Spring Boot 2.x requires Java 8 or later."
            );
            return;
        }

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


        if (bootMajor == 3 && bootMinor == 1
                && cloudMajor == 2022 && cloudMinor == 0) {

            issues.add(
                    "[INFO] Spring Boot 3.1.x and Spring Cloud 2022.0.x "
                            + "are a compatible version family. "
                            + "Verify the minimum release and support status."
            );
            return;
        }

        if (bootMajor != 3 && bootMajor != 4) {
            issues.add(
                    "[WARNING] The current Spring Cloud compatibility "
                            + "rules cover Spring Boot 3.x and 4.x only."
            );
            return;
        }

        Integer expectedCloudMajor = null;
        Integer expectedCloudMinor = null;

        if (bootMajor == 3 && (bootMinor == 0 || bootMinor == 1)) {
            expectedCloudMajor = 2022;
            expectedCloudMinor = 0;
        } else if (bootMajor == 3 && (bootMinor == 2 || bootMinor == 3)) {
            expectedCloudMajor = 2023;
            expectedCloudMinor = 0;
        } else if (bootMajor == 3 && bootMinor == 4) {
            expectedCloudMajor = 2024;
            expectedCloudMinor = 0;
        } else if (bootMajor == 3 && bootMinor == 5) {
            expectedCloudMajor = 2025;
            expectedCloudMinor = 0;
        } else if (bootMajor == 4 && bootMinor == 0) {
            expectedCloudMajor = 2025;
            expectedCloudMinor = 1;
        }

        if (expectedCloudMajor == null) {
            issues.add(
                    "[WARNING] No Spring Cloud compatibility rule is "
                            + "configured for Spring Boot " + bootVersion
                            + ". Verify the official compatibility matrix."
            );
            return;
        }

        if (cloudMajor != expectedCloudMajor
                || cloudMinor != expectedCloudMinor) {
            issues.add(
                    "[WARNING] Spring Boot " + bootVersion
                            + " is expected to use Spring Cloud "
                            + expectedCloudMajor + "." + expectedCloudMinor
                            + ". Detected Spring Cloud " + cloudVersion
                            + ". Verify the official compatibility matrix."
            );
        }
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
