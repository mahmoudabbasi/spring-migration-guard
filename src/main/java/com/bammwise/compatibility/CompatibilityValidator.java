package com.bammwise.compatibility;

import com.bammwise.model.ProjectEnvironment;

import java.util.ArrayList;
import java.util.List;

public class CompatibilityValidator {
    public List<String> validate(ProjectEnvironment environment) {
        List<String> issues = new ArrayList<>();

        String springBootVersion = environment.getSpringBootVersion();
        String javaVersion = environment.getJavaVersion();

        if (springBootVersion == null || springBootVersion.isBlank()) {
            issues.add(
                    "[WARNING] Spring Boot version could not be detected."
            );
            return issues;
        }

        if (javaVersion == null || javaVersion.isBlank()) {
            issues.add(
                    "[WARNING] Java version could not be detected."
            );
            return issues;
        }

        int bootMajor = getMajorVersion(springBootVersion);
        int javaMajor = getJavaMajorVersion(javaVersion);

        if (bootMajor < 0 || javaMajor < 0) {
            issues.add(
                    "[WARNING] Could not parse Spring Boot or Java version."
            );
            return issues;
        }

        if (bootMajor >= 3 && javaMajor < 17) {
            issues.add(
                    "[ERROR] Spring Boot " + springBootVersion
                            + " requires Java 17 or later, but the project "
                            + "declares Java " + javaVersion + "."
            );
        }

        if (bootMajor == 2 && javaMajor < 8) {
            issues.add(
                    "[ERROR] Spring Boot 2.x requires Java 8 or later."
            );
        }

        if (issues.isEmpty()) {
            issues.add(
                    "[OK] No Java/Spring Boot compatibility issue "
                            + "was found by the current rules."
            );
        }

        return issues;
    }

    private int getMajorVersion(String version) {
        try {
            String[] parts = version.split("[.-]");
            return Integer.parseInt(parts[0]);
        } catch (Exception exception) {
            return -1;
        }
    }

    private int getJavaMajorVersion(String version) {
        try {
            String normalized = version.trim();

            if (normalized.startsWith("1.")) {
                return Integer.parseInt(normalized.substring(2, 3));
            }

            String[] parts = normalized.split("[.-]");
            return Integer.parseInt(parts[0]);
        } catch (Exception exception) {
            return -1;
        }
    }
}
