package com.bammwise.action;

import com.bammwise.compatibility.CompatibilityValidator;
import com.bammwise.detector.ProjectEnvironmentDetector;
import com.bammwise.model.ProjectEnvironment;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class CheckCompatibilityAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();

        if (project == null || project.isDisposed()) {
            return;
        }

        try {
            // Step 1: Detect project environment
            ProjectEnvironment environment =
                    new ProjectEnvironmentDetector().detect(project);

            // Step 2: Validate compatibility
            CompatibilityValidator validator =
                    new CompatibilityValidator();

            List<String> issues = validator.validate(environment);

            // Step 3: Build report
            StringBuilder report = new StringBuilder();

            report.append("Spring Boot: ")
                    .append(environment.getSpringBootVersion())
                    .append("\n");

            report.append("Java: ")
                    .append(environment.getJavaVersion())
                    .append("\n\n");

            report.append("Compatibility Analysis:\n");

            for (String issue : issues) {
                report.append("\n")
                        .append(issue);
            }

            report.append("Dependencies:\n");

            if (environment.getDependencies().isEmpty()) {
                report.append("No direct dependencies found.\n");
            } else {
                for (String dependency : environment.getDependencies()) {
                    report.append("- ")
                            .append(dependency)
                            .append("\n");
                }
            }

            report.append("\nCompatibility Analysis:\n");

            // Step 4: Display results
            Messages.showInfoMessage(
                    project,
                    report.toString(),
                    "Spring Migration Guard"
            );

        } catch (Exception exception) {
            Messages.showErrorDialog(
                    project,
                    "Could not analyze project:\n"
                            + exception.getMessage(),
                    "Spring Migration Guard Error"
            );
        }
    }

}