package com.bammwise.maven;


import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MavenEffectiveModelResolver {
    public List<String> resolveDependencies(File pomFile)
            throws Exception {

        if (pomFile == null || !pomFile.isFile()) {
            throw new IllegalArgumentException(
                    "Valid pom.xml file is required."
            );
        }

        File outputFile = File.createTempFile(
                "spring-migration-effective-",
                ".xml"
        );

        File logFile = File.createTempFile(
                "spring-migration-maven-",
                ".log"
        );

        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "mvn",
                    "-B",
                    "-f",
                    pomFile.getAbsolutePath(),
                    "help:effective-pom",
                    "-Doutput=" + outputFile.getAbsolutePath()
            );

            builder.directory(pomFile.getParentFile());
            builder.redirectErrorStream(true);
            builder.redirectOutput(logFile);

            Process process = builder.start();

            boolean finished = process.waitFor(
                    2, TimeUnit.MINUTES
            );

            if (!finished) {
                process.destroyForcibly();

                throw new IllegalStateException(
                        "Maven effective POM resolution timed out."
                );
            }

            if (process.exitValue() != 0 || !outputFile.isFile()
                    || outputFile.length() == 0) {

                String log = Files.readString(logFile.toPath());

                throw new IllegalStateException(
                        "Maven could not build the effective POM:\n"
                                + log
                );
            }

            return parseDependencies(outputFile);

        } finally {
            Files.deleteIfExists(outputFile.toPath());
            Files.deleteIfExists(logFile.toPath());
        }
    }

    private List<String> parseDependencies(File effectivePom)
            throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );
        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );
        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );
        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_DTD, ""
        );
        factory.setAttribute(
                XMLConstants.ACCESS_EXTERNAL_SCHEMA, ""
        );

        Document document = factory.newDocumentBuilder()
                .parse(effectivePom);

        Element root = document.getDocumentElement();
        Element dependenciesElement =
                firstChild(root, "dependencies");

        List<String> dependencies = new ArrayList<>();

        if (dependenciesElement == null) {
            return dependencies;
        }

        NodeList children = dependenciesElement.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (!(node instanceof Element dependency)
                    || !"dependency".equals(
                    dependency.getTagName())) {
                continue;
            }

            String groupId = childText(dependency, "groupId");
            String artifactId = childText(dependency, "artifactId");
            String version = childText(dependency, "version");
            String scope = childText(dependency, "scope");

            if (groupId == null || artifactId == null) {
                continue;
            }

            StringBuilder entry = new StringBuilder()
                    .append(groupId)
                    .append(":")
                    .append(artifactId)
                    .append(":")
                    .append(
                            version != null
                                    ? version
                                    : "(version unresolved)"
                    );

            if (scope != null) {
                entry.append(" [scope: ")
                        .append(scope)
                        .append("]");
            }

            dependencies.add(entry.toString());
        }

        return dependencies;
    }

    private Element firstChild(Element parent, String name) {
        NodeList children = parent.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (node instanceof Element element
                    && name.equals(element.getTagName())) {
                return element;
            }
        }

        return null;
    }

    private String childText(Element parent, String name) {
        Element child = firstChild(parent, name);

        if (child == null) {
            return null;
        }

        String value = child.getTextContent().trim();

        return value.isEmpty() ? null : value;
    }
}
