
package com.bammwise.ui;

import com.bammwise.model.ProjectEnvironment;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.awt.event.ActionListener;
import com.bammwise.model.DependencyCompatibilityIssue;

public class CompatibilityReportPanel extends JPanel {

    private final JLabel bootVersionLabel = new JLabel("Not detected");
    private final JLabel javaVersionLabel = new JLabel("Not detected");
    private final JLabel cloudVersionLabel = new JLabel("Not detected");

    private final JPanel issuesPanel = new JPanel();
    private final JTextArea dependenciesArea = new JTextArea();

    public CompatibilityReportPanel() {
        super(new BorderLayout(0, 12));

        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Spring Migration Guard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JLabel subtitle = new JLabel("Spring project compatibility report");
        subtitle.setForeground(new Color(120, 120, 120));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        add(header, BorderLayout.NORTH);

        JButton analyzeAgainButton = new JButton("Analyze Again");

        analyzeAgainButton.addActionListener(event -> onAnalyzeAgain.run());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(analyzeAgainButton);

        add(footer, BorderLayout.SOUTH);


        issuesPanel.setLayout(new BoxLayout(issuesPanel, BoxLayout.Y_AXIS));

        dependenciesArea.setEditable(false);
        dependenciesArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        dependenciesArea.setLineWrap(false);
        dependenciesArea.setMargin(new Insets(8, 8, 8, 8));

        JPanel overview = createOverviewPanel();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Overview", new JScrollPane(overview));
        tabs.addTab("Issues", new JScrollPane(issuesPanel));
        tabs.addTab("Dependencies", new JScrollPane(dependenciesArea));

        add(tabs, BorderLayout.CENTER);


        showEmptyState();
    }


    public void setOnAnalyzeAgain(Runnable callback) {
        this.onAnalyzeAgain = callback != null ? callback : () -> {};
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(12, 4, 12, 4));

        panel.add(createVersionCard("Spring Boot", bootVersionLabel));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createVersionCard("Java", javaVersionLabel));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createVersionCard("Spring Cloud", cloudVersionLabel));

        return panel;
    }


    private Runnable onAnalyzeAgain = () -> {};

    private JPanel createVersionCard(String name, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(8, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(130, 130, 130, 90)),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(new Color(120, 120, 120));

        valueLabel.setFont(
                valueLabel.getFont().deriveFont(Font.BOLD, 14f));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(nameLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(valueLabel);

        card.add(textPanel, BorderLayout.CENTER);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        return card;
    }

    public void updateReport(
            ProjectEnvironment environment,
            List<String> issues) {

        bootVersionLabel.setText(display(environment.getSpringBootVersion()));
        javaVersionLabel.setText(display(environment.getJavaVersion()));
        cloudVersionLabel.setText(display(environment.getSpringCloudVersion()));

        issuesPanel.removeAll();

        if (issues == null || issues.isEmpty()) {
            addIssue("[OK] No compatibility issues reported.");
        } else {
            for (String issue : issues) {
                addIssue(issue);
            }
        }

        StringBuilder dependencies = new StringBuilder();
        List<String> items = environment.getDependencies();

        if (items.isEmpty()) {
            dependencies.append("No dependencies found.");
        } else {
            for (String item : items) {
                dependencies.append("• ")
                        .append(item)
                        .append('\n');
            }
        }

        dependenciesArea.setText(dependencies.toString());
        dependenciesArea.setCaretPosition(0);

        issuesPanel.revalidate();
        issuesPanel.repaint();
        revalidate();
        repaint();
    }

    private void addIssue(String issue) {
        Color color = colorFor(issue);

        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, color),
                new EmptyBorder(10, 12, 10, 8)
        ));

        JLabel message = new JLabel(
                "<html><div style='width: 500px;'>"
                        + escapeHtml(issue)
                        + "</div></html>"
        );
        message.setForeground(color);

        card.add(message, BorderLayout.CENTER);
        card.setBackground(UIManager.getColor("Panel.background"));

        issuesPanel.add(card);
        issuesPanel.add(Box.createVerticalStrut(6));
    }

    private Color colorFor(String issue) {
        String value = issue == null ? "" : issue.toUpperCase();

        if (value.startsWith("[ERROR]")) {
            return new Color(220, 65, 65);
        }

        if (value.startsWith("[WARNING]")) {
            return new Color(205, 130, 25);
        }

        if (value.startsWith("[OK]")) {
            return new Color(35, 155, 90);
        }

        return new Color(55, 135, 210);
    }

    private String display(String value) {
        return value == null || value.isBlank()
                ? "Not detected"
                : value;
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }


    public void updateStructuredReport(
            ProjectEnvironment environment,
            List<String> issues,
            List<DependencyCompatibilityIssue> dependencyIssues) {

        updateReport(environment, issues);

        if (dependencyIssues == null || dependencyIssues.isEmpty()) {
            return;
        }

        for (DependencyCompatibilityIssue issue : dependencyIssues) {
            addDependencyIssue(issue);
        }

        issuesPanel.revalidate();
        issuesPanel.repaint();
    }

    private void addDependencyIssue(DependencyCompatibilityIssue issue) {
        Color color = switch (issue.severity().toUpperCase()) {
            case "ERROR" -> new Color(220, 65, 65);
            case "WARNING" -> new Color(205, 130, 25);
            default -> new Color(55, 135, 210);
        };

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, color),
                new EmptyBorder(10, 12, 10, 8)
        ));
        card.setBackground(UIManager.getColor("Panel.background"));

        JLabel title = new JLabel(
                "[" + issue.severity() + "] " + issue.title()
        );
        title.setFont(title.getFont().deriveFont(Font.BOLD, 13f));
        title.setForeground(color);

        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(createDetailLabel("Dependency", issue.dependency()));
        card.add(createDetailLabel("Current version", issue.currentVersion()));
        card.add(createDetailLabel("Managed version", issue.managedVersion()));
        card.add(createDetailLabel("Reason", issue.reason()));
        card.add(createDetailLabel("Suggested fix", issue.suggestedFix()));

        issuesPanel.add(card);
        issuesPanel.add(Box.createVerticalStrut(8));
    }

    private JLabel createDetailLabel(String label, String value) {
        String html = "<html><div style='width: 550px;'>"
                + "<b>" + escapeHtml(label) + ":</b> "
                + escapeHtml(value)
                + "</div></html>";

        JLabel result = new JLabel(html);
        result.setAlignmentX(Component.LEFT_ALIGNMENT);
        result.setBorder(new EmptyBorder(2, 0, 2, 0));
        return result;
    }


    private void showEmptyState() {
        addIssue("[INFO] Run Analyze Spring Boot Compatibility to inspect this project.");
    }
}
