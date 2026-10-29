
package com.bammwise.compatibility;

import com.bammwise.model.CompatibilityRule;
import com.bammwise.model.ProjectEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Evaluates compatibility rules against a detected project environment.
 */
public class CompatibilityRuleEngine {

    private static final Pattern VERSION_PATTERN =
            Pattern.compile("^(\\d+)(?:\\.(\\d+))?.*$");

    private final List<CompatibilityRule> rules;

    public CompatibilityRuleEngine(List<CompatibilityRule> rules) {
        Objects.requireNonNull(rules, "rules cannot be null");

        this.rules = List.copyOf(rules);
    }

    /**
     * Evaluates Java compatibility rules for the project's Spring Boot version.
     *
     * This first version supports rules whose component is "spring-boot".
     * It does not yet replace the existing CompatibilityValidator.
     */
    public List<String> evaluate(ProjectEnvironment environment) {
        if (environment == null) {
            return List.of("[ERROR] Project environment is missing.");
        }

        String bootVersion = environment.getSpringBootVersion();
        String javaVersion = environment.getJavaVersion();

        if (isBlank(bootVersion) || isBlank(javaVersion)) {
            return List.of();
        }

        int[] bootParts = parseVersion(bootVersion);
        Integer javaMajor = parseJavaMajor(javaVersion);

        if (bootParts == null || javaMajor == null) {
            return List.of();
        }

        List<String> issues = new ArrayList<>();

        for (CompatibilityRule rule : rules) {
            if (!"spring-boot".equalsIgnoreCase(rule.component())) {
                continue;
            }

            if (!matchesVersionRange(bootParts, rule.versionRange())) {
                continue;
            }

            if (javaMajor < rule.minJavaVersion()) {
                issues.add(formatIssue(rule, bootVersion,
                        "Java " + rule.minJavaVersion() + " or later"));
            } else if (rule.maxJavaVersion() != null
                    && javaMajor > rule.maxJavaVersion()) {
                issues.add(formatIssue(rule, bootVersion,
                        "Java " + rule.maxJavaVersion() + " or earlier"));
            }
        }

        return List.copyOf(issues);
    }

    private String formatIssue(
            CompatibilityRule rule,
            String bootVersion,
            String requirement) {

        return "[" + rule.severity().toUpperCase() + "] "
                + "Spring Boot " + bootVersion + ": "
                + rule.message()
                + " Required range: " + requirement + ".";
    }

    private boolean matchesVersionRange(
            int[] version,
            String range) {

        String normalized = range.trim();

        if (normalized.endsWith(".x")) {
            String prefix = normalized.substring(
                    0, normalized.length() - 2);
            int[] rangeParts = parseVersion(prefix);

            return rangeParts != null
                    && version[0] == rangeParts[0]
                    && (rangeParts.length < 2
                    || version[1] == rangeParts[1]);
        }

        int[] exactParts = parseVersion(normalized);

        if (exactParts == null) {
            return false;
        }

        return version[0] == exactParts[0]
                && (exactParts.length < 2
                || version[1] == exactParts[1]);
    }

    private int[] parseVersion(String version) {
        Matcher matcher = VERSION_PATTERN.matcher(version);

        if (!matcher.matches()) {
            return null;
        }

        try {
            int major = Integer.parseInt(matcher.group(1));

            if (matcher.group(2) == null) {
                return new int[]{major};
            }

            int minor = Integer.parseInt(matcher.group(2));
            return new int[]{major, minor};
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer parseJavaMajor(String version) {
        String normalized = version.trim();

        if (normalized.startsWith("1.")) {
            normalized = normalized.substring(2);
        }

        Matcher matcher = Pattern.compile("^(\\d+)").matcher(normalized);

        if (!matcher.find()) {
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
