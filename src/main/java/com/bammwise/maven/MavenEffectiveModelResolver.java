
package com.bammwise.maven;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MavenEffectiveModelResolver {

    public List<String> resolveDependencies(
            Project intellijProject,
            File pomFile) {

        if (intellijProject == null
                || pomFile == null
                || !pomFile.isFile()) {
            throw new IllegalArgumentException(
                    "A valid IntelliJ project and pom.xml are required."
            );
        }

        VirtualFile virtualPom = LocalFileSystem.getInstance()
                .findFileByIoFile(pomFile);

        if (virtualPom == null) {
            throw new IllegalStateException(
                    "IntelliJ could not locate the pom.xml file."
            );
        }

        MavenProjectsManager manager =
                MavenProjectsManager.getInstance(intellijProject);

        if (!manager.isInitialized()) {
            throw new IllegalStateException(
                    "Maven has not finished initializing. "
                            + "Reload the Maven project and try again."
            );
        }

        MavenProject mavenProject = manager.findProject(virtualPom);


        System.err.println(
                "[SMG DEBUG] Maven initialized: " + manager.isInitialized()
        );

        System.err.println(
                "[SMG DEBUG] pom path: " + pomFile.getAbsolutePath()
        );

        System.err.println(
                "[SMG DEBUG] Maven project found: " + (mavenProject != null)
        );

        if (mavenProject == null) {
            throw new IllegalStateException(
                    "The pom.xml is not registered as a Maven project. "
                            + "Reload Maven projects and try again."
            );
        }

        System.err.println(
                "[SMG DEBUG] Maven dependency count: "
                        + (mavenProject == null
                        ? -1
                        : mavenProject.getDependencies().size())
        );

        List<String> dependencies = new ArrayList<>();

        for (MavenArtifact artifact : mavenProject.getDependencies()) {
            System.out.printf(
                    "[SMG DEBUG] %s:%s | version=%s | scope=%s%n",
                    artifact.getGroupId(),
                    artifact.getArtifactId(),
                    artifact.getVersion(),
                    artifact.getScope()
            );


            String groupId = artifact.getGroupId();
            String artifactId = artifact.getArtifactId();
            String version = artifact.getVersion();
            String scope = artifact.getScope();

            if (groupId == null || artifactId == null) {
                continue;
            }

            StringBuilder entry = new StringBuilder()
                    .append(groupId)
                    .append(":")
                    .append(artifactId)
                    .append(":")
                    .append(version != null
                            ? version
                            : "(version unresolved)");

            if (scope != null && !scope.isBlank()) {
                entry.append(" [scope: ")
                        .append(scope)
                        .append("]");
            }

            dependencies.add(entry.toString());
        }

        System.out.println(
                "[SMG DEBUG] Resolved dependency count: "
                        + dependencies.size()
        );

        return dependencies;
    }
}
