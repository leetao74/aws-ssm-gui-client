package jp.co.tac.gui;

import jp.co.tac.model.AwsInstance;
import jp.co.tac.ssm.SSMSession;
import jp.co.tac.aws.AWSCredentialManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.datatransfer.StringSelection;

public class SessionPanel extends JPanel {
    private final SSMSession ssmSession;
    private final AwsInstance instance;

    private JTextArea sessionInfoArea;
    private JButton startButton;
    private JButton terminateButton;
    private JButton copyCommandButton;

    public SessionPanel(SSMSession ssmSession, AwsInstance instance) {
        this.ssmSession = ssmSession;
        this.instance = instance;

        initComponents();
        layoutComponents();
        updateButtonStates();
    }

    private void initComponents() {
        sessionInfoArea = new JTextArea(10, 50);
        sessionInfoArea.setEditable(false);
        sessionInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        startButton = new JButton("Start Session");
        startButton.setToolTipText("Start SSM session with selected instance");
        startButton.addActionListener(this::startSession);

        terminateButton = new JButton("Terminate Session");
        terminateButton.setToolTipText("Terminate current session");
        terminateButton.addActionListener(this::terminateSession);

        copyCommandButton = new JButton("Copy CLI Command");
        copyCommandButton.setToolTipText("Copy AWS CLI command to clipboard");
        copyCommandButton.addActionListener(this::copyCliCommand);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 信息区域
        add(new JScrollPane(sessionInfoArea), BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(startButton);
        buttonPanel.add(terminateButton);
        buttonPanel.add(copyCommandButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void updateButtonStates() {
        boolean hasSession = ssmSession != null;
        startButton.setEnabled(hasSession);
        terminateButton.setEnabled(false);
        copyCommandButton.setEnabled(false);
    }

    private void startSession(ActionEvent e) {
        // if (!AWSCredentialManager.isLoggedIn(ssmSession.getProfileName())) {
        if (!AWSCredentialManager.isSessionValid(ssmSession.getProfile())) {
            JOptionPane.showMessageDialog(this,
                    "Please login with AWS SSO first",
                    "Login Required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            SSMSession.SessionInfo sessionInfo = ssmSession.startSession(instance.getInstanceId());

            sessionInfoArea.setText("Session started successfully:\n");
            sessionInfoArea.append("Instance: " + instance.getName() + "\n");
            sessionInfoArea.append("Instance ID: " + sessionInfo.instanceId + "\n");
            sessionInfoArea.append("Session ID: " + sessionInfo.sessionId + "\n");
            sessionInfoArea.append("Region: " + instance.getRegion() + "\n");
            sessionInfoArea.append("Profile: " + ssmSession.getProfileName() + "\n\n");

            String cliCommand = String.format("aws ssm start-session --target %s --region %s --profile %s",
                    sessionInfo.instanceId,
                    instance.getRegion(),
                    ssmSession.getProfileName());

            sessionInfoArea.append("To connect using AWS CLI, run:\n");
            sessionInfoArea.append(cliCommand + "\n");

            startButton.setEnabled(false);
            terminateButton.setEnabled(true);
            copyCommandButton.setEnabled(true);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to start session: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void terminateSession(ActionEvent e) {
        try {
            // 在实际应用中，这里应该跟踪活动会话ID
            // 为简化演示，我们只是重置UI
            sessionInfoArea.setText("");
            startButton.setEnabled(true);
            terminateButton.setEnabled(false);
            copyCommandButton.setEnabled(false);

            JOptionPane.showMessageDialog(this,
                    "Session terminated (simulated)",
                    "Info",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to terminate session: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copyCliCommand(ActionEvent e) {
        String command = sessionInfoArea.getText()
                .lines()
                .filter(line -> line.startsWith("aws ssm start-session"))
                .findFirst()
                .orElse("");

        if (!command.isEmpty()) {
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(command), null);

            JOptionPane.showMessageDialog(this,
                    "CLI command copied to clipboard",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}