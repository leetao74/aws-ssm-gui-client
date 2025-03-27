package jp.co.tac.gui;

import jp.co.tac.config.AWSConfigManager;
import jp.co.tac.config.ProfileConfig.AwsProfile;
import jp.co.tac.aws.AWSCredentialManager;
import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class AWSConfigPanel extends JPanel {
    private final AWSConfigManager configManager;
    private final JComboBox<AwsProfile> profileCombo = new JComboBox<>();
    private final JButton loginButton = new JButton("SSO Login");
    private final JLabel statusLabel = new JLabel("Not logged in");

    public AWSConfigPanel() {
        this.configManager = new AWSConfigManager();
        initUI();
        loadProfiles();
    }

    private void initUI() {
        setLayout(new GridLayout(1, 4, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Profile Combo Renderer
        profileCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof AwsProfile) {
                    AwsProfile p = (AwsProfile) value;
                    setText(String.format("%s (%s)", p.getName(), p.getDescription()));
                    setToolTipText(buildTooltip(p));
                }
                return this;
            }

            private String buildTooltip(AwsProfile p) {
                return p.isSsoProfile() ? String.format("SSO Profile\nAccount: %s\nRole: %s",
                        p.getSsoAccountId(), p.getSsoRoleName()) : "Credentials Profile";
            }
        });

        // Login Button
        loginButton.addActionListener(e -> {
            AwsProfile profile = profileCombo.getItemAt(profileCombo.getSelectedIndex());
            boolean success = AWSCredentialManager.loginWithSSO(profile);
            updateStatus(success, profile);
        });

        // Status Label
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        updateStatus(false, null);

        add(profileCombo);
        add(loginButton);
        add(new JLabel()); // Spacer
        add(statusLabel);
    }

    private void loadProfiles() {
        profileCombo.removeAllItems();
        configManager.getProfiles().forEach(profileCombo::addItem);

        // Select default profile
        AwsProfile defaultProfile = configManager.getDefaultProfile();
        if (defaultProfile != null) {
            profileCombo.setSelectedItem(defaultProfile);
            updateStatus(
                    AWSCredentialManager.isSessionValid(defaultProfile),
                    defaultProfile);
        }
    }

    private void updateStatus(boolean loggedIn, AwsProfile profile) {
        if (profile == null) {
            statusLabel.setText("No profile selected");
            statusLabel.setForeground(Color.GRAY);
            return;
        }

        if (loggedIn) {
            statusLabel.setText("Logged in: " + profile.getName());
            statusLabel.setForeground(new Color(0, 100, 0));
        } else {
            statusLabel.setText("Login required: " + profile.getName());
            statusLabel.setForeground(Color.RED);
        }
    }

    public void refresh() {
        configManager.loadConfig();
        loadProfiles();
    }

    public void shutdown() {
        configManager.shutdown();
    }

    public AwsProfile getSelectedProfile() {
        return (AwsProfile) profileCombo.getSelectedItem();
    }

    public void cleanup() {
        configManager.shutdown();
    }
}