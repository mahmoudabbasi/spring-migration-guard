
package com.bammwise.model;

import java.util.Objects;

public record DependencyCompatibilityIssue(
        String severity,
        String title,
        String dependency,
        String currentVersion,
        String managedVersion,
        String reason,
        String suggestedFix
) {
    public DependencyCompatibilityIssue {
        severity = requireText(severity, "severity");
        title = requireText(title, "title");
        dependency = requireText(dependency, "dependency");
        currentVersion = requireText(currentVersion, "currentVersion");
        managedVersion = requireText(managedVersion, "managedVersion");
        reason = requireText(reason, "reason");
        suggestedFix = requireText(suggestedFix, "suggestedFix");
    }

    private static String requireText(String value, String field) {
        Objects.requireNonNull(value, field + " cannot be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " cannot be blank"
            );
        }

        return value.trim();
    }

    public String toDisplayString() {
        return "[" + severity + "] " + title
                + "\nDependency: " + dependency
                + "\nCurrent version: " + currentVersion
                + "\nManaged version: " + managedVersion
                + "\nReason: " + reason
                + "\nSuggested fix: " + suggestedFix;
    }
}
