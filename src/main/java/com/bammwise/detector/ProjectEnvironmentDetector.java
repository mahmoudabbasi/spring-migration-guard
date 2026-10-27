package com.bammwise.detector;

import com.bammwise.maven.MavenEffectiveModelResolver;
import com.bammwise.model.ProjectEnvironment;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VfsUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class ProjectEnvironmentDetector {
    private static final Pattern PROPERTY_PATTERN =
            Pattern.compile("\\$\\{([^}]+)}");

    public ProjectEnvironment detect(Project project) throws Exception {
        VirtualFile baseDir = project.getBaseDir();

        if (baseDir == null) {
            throw new IllegalStateException(
                    "Project base directory not found."
            );
        }

        VirtualFile pomFile = baseDir.findChild("pom.xml");

        if (pomFile == null) {
            throw new IllegalStateException(
                    "pom.xml not found in: " + baseDir.getPath()
            );
        }

        String xml = VfsUtil.loadText(pomFile);

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
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        Document document = factory.newDocumentBuilder().parse(
                new ByteArrayInputStream(
                        xml.getBytes(StandardCharsets.UTF_8)
                )
        );


        Element root = document.getDocumentElement();

        ProjectEnvironment environment = new ProjectEnvironment();

        Properties properties = readProperties(root);

        String bootVersion = findSpringBootVersion(root);
        String javaVersion = findJavaVersion(root);



        environment.setSpringBootVersion(
                resolveProperties(bootVersion, properties)
        );

        environment.setJavaVersion(
                resolveProperties(javaVersion, properties)
        );

        extractDependencies(root, properties, environment);

        File pomFileOnDisk = new File(pomFile.getPath());

        MavenEffectiveModelResolver resolver =
                new MavenEffectiveModelResolver();

        try {
            List<String> effectiveDependencies =
                    resolver.resolveDependencies(project, pomFileOnDisk);
            System.err.println("[SMG DEBUG] ProjectEnvironmentDetector is running");

            if (!effectiveDependencies.isEmpty()) {
                environment.getDependencies().clear();
                environment.getDependencies().addAll(effectiveDependencies);
            }
        } catch (Exception exception) {
            System.err.println(
                    "Spring Migration Guard: Failed to resolve effective Maven dependencies."
            );
            exception.printStackTrace();
        }
        return environment;
    }

    private Properties readProperties(Element root) {
        Properties properties = new Properties();

        Element propertiesElement = firstChild(root, "properties");

        if (propertiesElement == null) {
            return properties;
        }

        NodeList children = propertiesElement.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (node instanceof Element element) {
                properties.setProperty(
                        element.getTagName(),
                        element.getTextContent().trim()
                );
            }
        }

        return properties;
    }

    private void extractDependencies(
            Element root,
            Properties properties,
            ProjectEnvironment environment) {

        Element dependenciesElement =
                firstChild(root, "dependencies");

        if (dependenciesElement == null) {
            return;
        }

        NodeList children = dependenciesElement.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);

            if (!(node instanceof Element dependency)
                    || !"dependency".equals(dependency.getTagName())) {
                continue;
            }

            String groupId = resolveProperties(
                    childText(dependency, "groupId"),
                    properties
            );

            String artifactId = resolveProperties(
                    childText(dependency, "artifactId"),
                    properties
            );

            String version = resolveProperties(
                    childText(dependency, "version"),
                    properties
            );

            String scope = resolveProperties(
                    childText(dependency, "scope"),
                    properties
            );

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
                                    : "(managed or unspecified)"
                    );

            if (scope != null) {
                entry.append(" [scope: ")
                        .append(scope)
                        .append("]");
            }

            environment.getDependencies().add(entry.toString());
        }
    }

    private String findSpringBootVersion(Element root) {
        Element parent = firstChild(root, "parent");

        if (parent != null) {
            String groupId = childText(parent, "groupId");
            String artifactId = childText(parent, "artifactId");

            if ("org.springframework.boot".equals(groupId)
                    && "spring-boot-starter-parent".equals(artifactId)) {
                return childText(parent, "version");
            }
        }

        Element properties = firstChild(root, "properties");

        if (properties != null) {
            String version = childText(properties, "spring-boot.version");

            if (version != null) {
                return version;
            }
        }

        return null;
    }

    private String findJavaVersion(Element root) {
        Element properties = firstChild(root, "properties");

        if (properties == null) {
            return null;
        }

        String version = childText(properties, "java.version");

        if (version == null) {
            version = childText(properties, "maven.compiler.release");
        }

        if (version == null) {
            version = childText(properties, "maven.compiler.source");
        }

        return version;
    }

    private String resolveProperties(
            String value,
            Properties properties) {

        if (value == null) {
            return null;
        }

        String result = value.trim();

        // Resolve simple project properties, including references
        // from one property to another.
        for (int i = 0; i < 10; i++) {
            Matcher matcher = PROPERTY_PATTERN.matcher(result);
            StringBuffer resolved = new StringBuffer();
            boolean found = false;

            while (matcher.find()) {
                found = true;

                String propertyValue =
                        properties.getProperty(matcher.group(1));

                if (propertyValue == null) {
                    matcher.appendReplacement(
                            resolved,
                            Matcher.quoteReplacement(matcher.group())
                    );
                } else {
                    matcher.appendReplacement(
                            resolved,
                            Matcher.quoteReplacement(propertyValue)
                    );
                }
            }

            matcher.appendTail(resolved);
            result = resolved.toString();

            if (!found) {
                break;
            }
        }

        return result;
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
