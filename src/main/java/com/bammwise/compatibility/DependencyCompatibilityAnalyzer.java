
package com.bammwise.compatibility;

import com.bammwise.model.DependencyCompatibilityIssue;
import com.bammwise.model.DependencyInfo;
import com.bammwise.model.ProjectEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects known dependency compatibility risks.
 */
public class DependencyCompatibilityAnalyzer {

    private static final Pattern MAJOR_VERSION =
            Pattern.compile("^(\\d+)");

    /**
     * Structured analysis for UI, reporting and future rule extensions.
     */
    public List<DependencyCompatibilityIssue> analyzeIssues(
            ProjectEnvironment environment) {

        if (environment == null
                || environment.getSpringBootVersion() == null
                || environment.getDependencyInfos() == null) {
            return List.of();
        }

        Integer bootMajor =
                parseMajor(environment.getSpringBootVersion());

        if (bootMajor == null) {
            return List.of();
        }

        List<DependencyCompatibilityIssue> issues =
                new ArrayList<>();

        for (DependencyInfo dependency :
                environment.getDependencyInfos()) {

            if (dependency == null
                    || !isSpringSecurityDependency(dependency)) {
                continue;
            }

            // Only analyze dependencies with a known managed version.
            if (!dependency.hasManagedVersion()) {
                continue;
            }

            Integer resolvedMajor =
                    parseMajor(dependency.version());

            Integer managedMajor =
                    parseMajor(dependency.managedVersion());

            if (resolvedMajor == null || managedMajor == null) {
                continue;
            }

            if (bootMajor == 2
                    && resolvedMajor >= 6
                    && managedMajor < 6) {

                issues.add(new DependencyCompatibilityIssue(
                        "WARNING",
                        "Potential Spring Security version incompatibility",
                        dependency.groupId() + ":"
                                + dependency.artifactId(),
                        dependency.version(),
                        dependency.managedVersion(),
                        "Spring Boot "
                                + environment.getSpringBootVersion()
                                + " manages Spring Security "
                                + dependency.managedVersion()
                                + ", but this dependency resolves to "
                                + dependency.version()
                                + ". Mixing these major versions may cause "
                                + "runtime compatibility problems.",
                        "If the override is not intentional, remove the "
                                + "explicit <version> from this dependency "
                                + "in pom.xml and let Spring Boot manage it."
                ));
            }
        }

        return List.copyOf(issues);
    }

    /**
     * Backward-compatible textual API.
     */
    public List<String> analyze(ProjectEnvironment environment) {
        return analyzeIssues(environment)
                .stream()
                .map(DependencyCompatibilityIssue::toDisplayString)
                .toList();
    }

    private boolean isSpringSecurityDependency(
            DependencyInfo dependency) {

        return "org.springframework.security".equals(
                dependency.groupId()
        );
    }

    private Integer parseMajor(String version) {
        if (version == null || version.isBlank()) {
            return null;
        }

        Matcher matcher =
                MAJOR_VERSION.matcher(version.trim());

        if (!matcher.find()) {
            return null;
        }

        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
