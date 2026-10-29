
package com.bammwise.maven;

import com.bammwise.model.DependencyInfo;
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


    public List<DependencyInfo> resolveDependencyInfos(
            Project intellijProject,
            File pomFile
    ) {
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

        if (mavenProject == null) {
            throw new IllegalStateException(
                    "The pom.xml is not registered as a Maven project. "
                            + "Reload Maven projects and try again."
            );
        }

        List<DependencyInfo> dependencies = new ArrayList<>();

        for (MavenArtifact artifact : mavenProject.getDependencies()) {
            String groupId = artifact.getGroupId();
            String artifactId = artifact.getArtifactId();

            if (groupId == null || artifactId == null) {
                continue;
            }

            String resolvedVersion = artifact.getVersion();

            String managedVersion =
                    mavenProject.findManagedDependencyVersion(
                            groupId,
                            artifactId
                    );

            DependencyInfo dependency = new DependencyInfo(
                    groupId,
                    artifactId,
                    resolvedVersion,
                    artifact.getScope(),
                    managedVersion
            );

            dependencies.add(dependency);
        }

        System.out.println(
                "[SMG DEBUG] Structured Maven dependencies: "
                        + dependencies.size()
        );

        long managedCount = dependencies.stream()
                .filter(DependencyInfo::hasManagedVersion)
                .count();

        long overrideCount = dependencies.stream()
                .filter(DependencyInfo::hasVersionOverride)
                .count();

        System.out.println(
                "[SMG DEBUG] Dependencies with managed versions: "
                        + managedCount
        );

        System.out.println(
                "[SMG DEBUG] Dependencies differing from managed versions: "
                        + overrideCount
        );

        dependencies.stream()
                .filter(DependencyInfo::hasVersionOverride)
                .forEach(dependency -> System.out.println(
                        "[SMG DEBUG] Version override: "
                                + dependency.groupId()
                                + ":"
                                + dependency.artifactId()
                                + " | resolved="
                                + dependency.version()
                                + " | managed="
                                + dependency.managedVersion()
                ));

        return dependencies;
    }


    /**
     * Backward-compatible method for existing callers.
     */
    public List<String> resolveDependencies(
            Project intellijProject,
            File pomFile) {

        return resolveDependencyInfos(intellijProject, pomFile)
                .stream()
                .map(DependencyInfo::toDisplayString)
                .toList();
    }
}
