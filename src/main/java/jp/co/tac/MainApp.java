package jp.co.tac;

import jp.co.tac.gui.MainFrame;
import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                showFatalError("Application failed to start", e);
            }
        });
    }

    private static void showFatalError(String message, Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(null,
                message + ": " + e.getMessage(),
                "Fatal Error",
                JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }
}