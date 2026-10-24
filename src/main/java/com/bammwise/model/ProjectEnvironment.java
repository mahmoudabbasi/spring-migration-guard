package com.bammwise.model;

import java.util.ArrayList;
import java.util.List;

public class ProjectEnvironment {
    private String springBootVersion;
    private String javaVersion;
    private final List<String> dependencies = new ArrayList<>();

    public String getSpringBootVersion() {
        return springBootVersion;
    }

    public void setSpringBootVersion(String springBootVersion) {
        this.springBootVersion = springBootVersion;
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
}
