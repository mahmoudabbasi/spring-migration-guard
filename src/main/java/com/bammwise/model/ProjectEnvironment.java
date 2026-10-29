
package com.bammwise.model;

import java.util.ArrayList;
import java.util.List;

public class ProjectEnvironment {

    private String springBootVersion;
    private String springCloudVersion;
    private String javaVersion;

    // Preserved for backward compatibility with existing code.
    private final List<String> dependencies = new ArrayList<>();

    // Structured dependency data for compatibility analysis.
    private final List<DependencyInfo> dependencyInfos =
            new ArrayList<>();

    public String getSpringBootVersion() {
        return springBootVersion;
    }

    public void setSpringBootVersion(String springBootVersion) {
        this.springBootVersion = springBootVersion;
    }

    public String getSpringCloudVersion() {
        return springCloudVersion;
    }

    public void setSpringCloudVersion(String springCloudVersion) {
        this.springCloudVersion = springCloudVersion;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public List<DependencyInfo> getDependencyInfos() {
        return dependencyInfos;
    }

    public void addDependencyInfo(DependencyInfo dependencyInfo) {
        if (dependencyInfo == null) {
            throw new IllegalArgumentException(
                    "Dependency info must not be null."
            );
        }

        dependencyInfos.add(dependencyInfo);
    }

    public void clearDependencyInfos() {
        dependencyInfos.clear();
    }
}
