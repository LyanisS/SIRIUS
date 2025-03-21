package edu.ezip.ing1.pds.gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainInterfaceFrame extends JFrame {

    private final static Logger logger = LoggerFactory.getLogger("FrontEnd - MainInterfaceFrame");

    private JPanel tablePanel;
    private NetworkConfig networkConfig;
    private JLabel headerTitleLabel;
    private JLabel headerSubtitleLabel;
    private JPanel actionPanel;

    public static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    public static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    public static final Color ACCENT_COLOR = new Color(231, 76, 60);
    public static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    public static final Color TEXT_COLOR = new Color(44, 62, 80);
    public static final Color TABLE_HEADER_COLOR = new Color(52, 73, 94);
    public static final Color TABLE_ALTERNATE_ROW = new Color(245, 245, 245);
    public static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    public static final Color REFRESH_BTN_COLOR = MainInterfaceFrame.TABLE_HEADER_COLOR;

    public MainInterfaceFrame() {
        super("PCC");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(600, 400);
        this.networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, "network.yaml");

        UIManager.put("Button.background", BACKGROUND_COLOR);
        UIManager.put("Button.foreground", TEXT_COLOR);
        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);

        // header
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, MainInterfaceFrame.PRIMARY_COLOR, getWidth(), 0, MainInterfaceFrame.SECONDARY_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 60));

        this.headerTitleLabel = new JLabel();
        this.headerTitleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        this.headerTitleLabel.setForeground(Color.WHITE);
        this.headerTitleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        headerPanel.add(this.headerTitleLabel, BorderLayout.WEST);

        this.headerSubtitleLabel = new JLabel();
        this.headerSubtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        this.headerSubtitleLabel.setForeground(new Color(255, 255, 255, 200));
        this.headerSubtitleLabel.setBorder(new EmptyBorder(0, 0, 0, 20));
        headerPanel.add(this.headerSubtitleLabel, BorderLayout.EAST);

        // tabs
        JPanel tabsButtonsPanel = new JPanel();
        JButton btnTrain = MainInterfaceFrame.createTabButton("Trains");
        btnTrain.addActionListener(e -> new TrainTableView(this));
        JButton btnSchedule = MainInterfaceFrame.createTabButton("Planning");
        btnSchedule.addActionListener(e -> new ScheduleTableView(this));
        JButton btnAlerts = MainInterfaceFrame.createTabButton("Alarmes");
        btnAlerts.addActionListener(e -> new AlertTableView(this));

        tabsButtonsPanel.add(btnTrain);
        tabsButtonsPanel.add(btnSchedule);
        tabsButtonsPanel.add(btnAlerts);

        this.add(tabsButtonsPanel, BorderLayout.SOUTH);

        // actions
        JPanel gradientPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(245, 245, 245), 0, getHeight(), MainInterfaceFrame.BACKGROUND_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        gradientPanel.setLayout(new BorderLayout());
        gradientPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        this.actionPanel = new JPanel();
        this.actionPanel.setOpaque(false);
        this.actionPanel.setLayout(new BoxLayout(this.actionPanel, BoxLayout.X_AXIS));
        this.actionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        gradientPanel.add(this.actionPanel, BorderLayout.CENTER);

        // table
        this.tablePanel = new JPanel(new BorderLayout());
        this.tablePanel.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
        this.tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(MainInterfaceFrame.PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
            )
        );

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(this.tablePanel, BorderLayout.CENTER);
        mainPanel.add(gradientPanel, BorderLayout.SOUTH);

        this.add(mainPanel, BorderLayout.CENTER);

        this.registerJButtons(null);
        this.setVisible(true);
        btnTrain.doClick();
    }

    public NetworkConfig getNetworkConfig() {
        return this.networkConfig;
    }

    public JPanel getTableJPanel() {
        return this.tablePanel;
    }

    public void registerJButtons(List<JButton> buttons) {
        this.actionPanel.removeAll();

        if (buttons != null) {
            boolean first = true;
            for (JButton btn : buttons) {
                if (!first) {
                    this.actionPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                } else {
                    first = false;
                }
                this.actionPanel.add(btn);
            }
            this.actionPanel.add(Box.createHorizontalGlue());
        }

        this.actionPanel.revalidate();
    }

    @Override
    public void setTitle(String title) {
        this.setTitle(title, "");
    }

    public void setTitle(String title, String subtitle) {
        super.setTitle(title + " - PCC");
        this.headerTitleLabel.setText(title);
        this.headerSubtitleLabel.setText(subtitle);
    }

    public static void styleDialogUIComponents() {

        UIManager.put("OptionPane.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);
        UIManager.put("OptionPane.messageFont", new Font("Arial", Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", new Font("Arial", Font.BOLD, 12));

        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("Label.font", new Font("Arial", Font.PLAIN, 12));
        UIManager.put("Label.foreground", TEXT_COLOR);

        UIManager.put("Button.background", PRIMARY_COLOR);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 12));
        UIManager.put("Button.focus", new Color(0, 0, 0, 0));

        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", TEXT_COLOR);
        UIManager.put("TextField.caretForeground", PRIMARY_COLOR);
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 7, 5, 7)));

        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", TEXT_COLOR);
        UIManager.put("ComboBox.selectionBackground", PRIMARY_COLOR);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        UIManager.put("ComboBox.border", BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)));
    }

    public static void resetDialogUIComponents() {

        UIManager.put("OptionPane.background", null);
        UIManager.put("OptionPane.messageForeground", null);
        UIManager.put("OptionPane.messageFont", null);
        UIManager.put("OptionPane.buttonFont", null);

        UIManager.put("Panel.background", null);
        UIManager.put("Label.font", null);
        UIManager.put("Label.foreground", null);

        UIManager.put("Button.background", null);
        UIManager.put("Button.foreground", null);
        UIManager.put("Button.font", null);
        UIManager.put("Button.focus", null);

        UIManager.put("TextField.background", null);
        UIManager.put("TextField.foreground", null);
        UIManager.put("TextField.caretForeground", null);
        UIManager.put("TextField.border", null);

        UIManager.put("ComboBox.background", null);
        UIManager.put("ComboBox.foreground", null);
        UIManager.put("ComboBox.selectionBackground", null);
        UIManager.put("ComboBox.selectionForeground", null);
        UIManager.put("ComboBox.border", null);
    }

    public static JButton createTabButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(MainInterfaceFrame.TEXT_COLOR);
        button.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, MainInterfaceFrame.PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(225, 225, 225));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
            }
        });

        return button;
    }

    public static JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(color.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(color.brighter());
                } else {
                    g2d.setColor(color);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                super.paintComponent(g);
            }
        };

        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));

        return button;
    }

    public void showErrorDialog(Exception e, String title, String baseMessage) {

        MainInterfaceFrame.styleDialogUIComponents();

        String errorMessage = e.getMessage();
        String userFriendlyMessage;

        if (errorMessage != null && errorMessage.contains("Connection")) {
            userFriendlyMessage = "Erreur de connexion au serveur.\n\n" +
                    "Veuillez vérifier votre connexion réseau et réessayer.";
        } else if (errorMessage != null && errorMessage.contains("timeout")) {
            userFriendlyMessage = "Le serveur ne répond pas. Veuillez réessayer plus tard.";
        } else {
            userFriendlyMessage = baseMessage + ": " + errorMessage;
        }

        JOptionPane.showMessageDialog(this,
                userFriendlyMessage,
                title,
                JOptionPane.ERROR_MESSAGE);

        MainInterfaceFrame.resetDialogUIComponents();

        logger.error("{}: {}", title, e.getMessage());
    }

    public void showWarningDialog(String title, String message) {
        MainInterfaceFrame.styleDialogUIComponents();

        JOptionPane.showMessageDialog(this,
                message,
                title,
                JOptionPane.WARNING_MESSAGE);

        MainInterfaceFrame.resetDialogUIComponents();
    }

    public void showSuccessDialog(String title, String message) {
        MainInterfaceFrame.styleDialogUIComponents();

        JOptionPane.showMessageDialog(this,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE);

        MainInterfaceFrame.resetDialogUIComponents();
    }

    public boolean showConfirmDialog(String title, String message) {
        MainInterfaceFrame.styleDialogUIComponents();

        int result = JOptionPane.showConfirmDialog(this,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        MainInterfaceFrame.resetDialogUIComponents();

        return result == JOptionPane.YES_OPTION;
    }
}
