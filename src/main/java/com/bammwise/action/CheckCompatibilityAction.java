
package com.bammwise.action;

import com.bammwise.compatibility.CompatibilityValidator;
import com.bammwise.compatibility.DependencyCompatibilityAnalyzer;
import com.bammwise.detector.ProjectEnvironmentDetector;
import com.bammwise.model.DependencyCompatibilityIssue;
import com.bammwise.model.ProjectEnvironment;
import com.bammwise.ui.CompatibilityReportPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CheckCompatibilityAction extends AnAction {

    private static final String TOOL_WINDOW_ID =
            "Spring Migration Guard";

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();

        if (project == null || project.isDisposed()) {
            return;
        }

        analyzeProject(project);
    }

    private void analyzeProject(Project project) {
        try {
            // Step 1: Detect project environment
            ProjectEnvironment environment =
                    new ProjectEnvironmentDetector().detect(project);

            // Step 2: Run existing compatibility rules
            List<String> issues =
                    new CompatibilityValidator().validate(environment);

            // Step 3: Run structured dependency analysis
            List<DependencyCompatibilityIssue> dependencyIssues =
                    new DependencyCompatibilityAnalyzer()
                            .analyzeIssues(environment);

            // Step 4: Find the Tool Window
            ToolWindow toolWindow =
                    ToolWindowManager.getInstance(project)
                            .getToolWindow(TOOL_WINDOW_ID);

            if (toolWindow == null) {
                Messages.showErrorDialog(
                        project,
                        "Spring Migration Guard Tool Window is not registered.",
                        "Spring Migration Guard"
                );
                return;
            }

            // Step 5: Show the Tool Window
            toolWindow.show();

            // Step 6: Update the report panel
            for (Content content :
                    toolWindow.getContentManager().getContents()) {

                if (content.getComponent()
                        instanceof CompatibilityReportPanel panel) {

                    panel.setOnAnalyzeAgain(
                            () -> analyzeProject(project)
                    );

                    panel.updateStructuredReport(
                            environment,
                            issues,
                            dependencyIssues
                    );
                    return;
                }
            }

            Messages.showErrorDialog(
                    project,
                    "Could not find the compatibility report panel.",
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
