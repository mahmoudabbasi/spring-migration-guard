
package com.bammwise.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class MigrationGuardToolWindowFactory
        implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(
            @NotNull Project project,
            @NotNull ToolWindow toolWindow) {

        CompatibilityReportPanel panel =
                new CompatibilityReportPanel();

        Content content = ContentFactory.getInstance()
                .createContent(panel, "", false);

        toolWindow.getContentManager().addContent(content);
    }
}
