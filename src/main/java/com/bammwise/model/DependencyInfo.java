
package com.bammwise.model;

public record DependencyInfo(
        String groupId,
        String artifactId,
        String version,
        String scope,
        String managedVersion
) {
    /**
     * Backward-compatible constructor for existing callers.
     */
    public DependencyInfo(
            String groupId,
            String artifactId,
            String version,
            String scope
    ) {
        this(groupId, artifactId, version, scope, null);
    }

    public DependencyInfo {
        if (groupId == null || groupId.isBlank()) {
            throw new IllegalArgumentException(
                    "Dependency groupId must not be blank."
            );
        }

        if (artifactId == null || artifactId.isBlank()) {
            throw new IllegalArgumentException(
                    "Dependency artifactId must not be blank."
            );
        }

        groupId = groupId.trim();
        artifactId = artifactId.trim();
        version = normalize(version);
        scope = normalize(scope);
        managedVersion = normalize(managedVersion);
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    public boolean hasManagedVersion() {
        return managedVersion != null;
    }

    public boolean hasVersionOverride() {
        return version != null
                && managedVersion != null
                && !version.equals(managedVersion);
    }

    public String toDisplayString() {
        StringBuilder result = new StringBuilder()
                .append(groupId)
                .append(":")
                .append(artifactId)
                .append(":")
                .append(version != null
                        ? version
                        : "(version unresolved)");

        if (managedVersion != null) {
            result.append(" [managed: ")
                    .append(managedVersion)
                    .append("]");
        }

        if (scope != null) {
            result.append(" [scope: ")
                    .append(scope)
                    .append("]");
        }

        return result.toString();
    }
}
