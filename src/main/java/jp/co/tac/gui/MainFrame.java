package jp.co.tac.gui;

import jp.co.tac.model.AwsInstance;
import jp.co.tac.model.ConfigManager;
import jp.co.tac.ssm.SSMSession;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.List;
import software.amazon.awssdk.regions.Region;
import jp.co.tac.config.ProfileConfig.AwsProfile;

public class MainFrame extends JFrame {
    private List<AwsInstance> instances;
    private InstanceTableModel tableModel;
    private JTable instanceTable;
    private SessionPanel sessionPanel;
    private SSMSession ssmSession;
    private AWSConfigPanel awsConfigPanel;

    public MainFrame() {
        super("AWS SSM Client with SSO");

        // 设置外观
        FlatLightLaf.setup();

        // 加载配置
        loadConfiguration();

        // 初始化组件
        initComponents();
        layoutComponents();

        // 配置窗口
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
    }

    private void loadConfiguration() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Configuration File");

            // 添加默认配置按钮
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(fileChooser, BorderLayout.CENTER);

            JButton defaultBtn = new JButton("Use Default");
            defaultBtn.addActionListener(e -> {
                instances = ConfigManager.loadDefaultConfiguration();
                SwingUtilities.getWindowAncestor(panel).dispose();
            });
            panel.add(defaultBtn, BorderLayout.SOUTH);

            int result = fileChooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                instances = ConfigManager.loadConfiguration(selectedFile.getAbsolutePath());
            } else {
                instances = ConfigManager.loadDefaultConfiguration();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load configuration: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            instances = Collections.emptyList();
        }
    }

    private void initComponents() {
        awsConfigPanel = new AWSConfigPanel();

        tableModel = new InstanceTableModel(instances);
        instanceTable = new JTable(tableModel);
        instanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        instanceTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = instanceTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        AwsInstance selectedInstance = tableModel.getInstanceAt(selectedRow);
                        updateSessionPanel(selectedInstance);
                    }
                }
            }
        });

        // 初始化会话面板为空
        sessionPanel = new SessionPanel(null, null);

        // 菜单栏
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem reloadItem = new JMenuItem("Reload Configuration");
        reloadItem.addActionListener(e -> {
            loadConfiguration();
            tableModel = new InstanceTableModel(instances);
            instanceTable.setModel(tableModel);
        });

        fileMenu.add(reloadItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        // 顶部面板 - AWS配置
        add(awsConfigPanel, BorderLayout.NORTH);

        // 中心面板 - 主内容
        JPanel centerPanel = new JPanel(new BorderLayout());

        // 左侧 - 实例表格
        JScrollPane tableScrollPane = new JScrollPane(instanceTable);
        tableScrollPane.setPreferredSize(new Dimension(500, 0));
        centerPanel.add(tableScrollPane, BorderLayout.WEST);

        // 右侧 - 会话面板
        centerPanel.add(sessionPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private void updateSessionPanel(AwsInstance instance) {
        if (ssmSession != null) {
            ssmSession.close();
        }

        try {
            AwsProfile selectedProfile = awsConfigPanel.getSelectedProfile();
            ssmSession = new SSMSession(selectedProfile);

            remove(sessionPanel);
            sessionPanel = new SessionPanel(ssmSession, instance);

            // 重新添加到布局
            ((BorderLayout) getContentPane().getLayout()).getLayoutComponent(BorderLayout.CENTER);
            add(sessionPanel, BorderLayout.CENTER);

            revalidate();
            repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error creating SSM session: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void dispose() {
        if (awsConfigPanel != null) {
            awsConfigPanel.cleanup();
        }
        super.dispose();
    }

}