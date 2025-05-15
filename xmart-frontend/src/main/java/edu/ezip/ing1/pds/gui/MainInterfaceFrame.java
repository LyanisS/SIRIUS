package edu.ezip.ing1.pds.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;

public class MainInterfaceFrame extends JFrame {

    private final static Logger logger = LoggerFactory.getLogger("FrontEnd - MainInterfaceFrame");

    private NetworkConfig networkConfig;

    public static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    public static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color BUTTON_COLOR = new Color(52, 152, 219);
    public static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color ALERT_COLOR = new Color(231, 76, 60);
    public static final Color TABLE_HEADER_COLOR = new Color(52, 73, 94);
    public static final Color TABLE_ALTERNATE_ROW = new Color(245, 245, 245);
    public static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    public static final Color ACCENT_COLOR = new Color(231, 76, 60);
    public static final Color REFRESH_BTN_COLOR = MainInterfaceFrame.TABLE_HEADER_COLOR;

    private JPanel mainContentPanel;
    private JPanel rightBottomNavPanel;
    private JLabel timeLabel;
    private String currentView = "trains"; // by deffault we have traffic view
    
    public MainInterfaceFrame() {
        super("Système de Contrôle Ferroviaire");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1200, 800);
        this.setLocationRelativeTo(null);
        this.networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, "network.yaml");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        this.createTopPanel();
        this.createMainContentPanel();
        this.createBottomNavigation();

        this.startTimeUpdate();
        
    
        Timer autoRefreshTimer = new Timer(40000, e -> refreshAll());
        autoRefreshTimer.start();
        
        this.setVisible(true);
    }

    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, getWidth(), 0, BUTTON_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topPanel.setPreferredSize(new Dimension(0, 80));

        // the hour section
        JPanel leftSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        leftSection.setOpaque(false);
        this.timeLabel = new JLabel();
        this.timeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        this.timeLabel.setForeground(Color.WHITE);
        leftSection.add(this.timeLabel);


        JPanel rightSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        rightSection.setOpaque(false);
        
    
        JButton refreshButton = createButton("Actualiser", BUTTON_COLOR);
        refreshButton.setToolTipText("Actualiser");
        refreshButton.addActionListener(e -> refreshAll());
        rightSection.add(refreshButton);

        topPanel.add(leftSection, BorderLayout.WEST);
        topPanel.add(rightSection, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
    }
    
    public void refreshAll() {
        if (currentView.equals("trains")) {
            new TrainTableView(this);
        } else if (currentView.equals("planning")) {
            new ScheduleTableView(this);
        } else if (currentView.equals("alarmes")) {
            new AlertTableView(this);
        }
    }

    public NetworkConfig getNetworkConfig() {
        return this.networkConfig;
    }

    public void registerJButtons(List<JButton> buttons) {
        this.rightBottomNavPanel.removeAll();

        if (buttons != null) {
            boolean first = true;
            for (JButton btn : buttons) {
                if (!first) {
                    this.rightBottomNavPanel.add(Box.createRigidArea(new Dimension(10, 0)));
                } else {
                    first = false;
                }
                this.rightBottomNavPanel.add(btn);
            }
            this.rightBottomNavPanel.add(Box.createHorizontalGlue());
        }

        this.rightBottomNavPanel.revalidate();
        this.rightBottomNavPanel.repaint();
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title + " - Système de Contrôle Ferroviaire");
    }

    public static JButton createButton(String text, Color color) {
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
                g2d.setColor(Color.WHITE);
                String str = getText();
                g2d.setFont(getFont());
                int textWidth = g2d.getFontMetrics().stringWidth(str);
                int textHeight = g2d.getFontMetrics().getHeight();
                g2d.drawString(str, (getWidth() - textWidth) / 2,
                        (getHeight() + textHeight / 2) / 2);
            }
        };;
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setForeground(MainInterfaceFrame.TEXT_COLOR);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.setPreferredSize(new Dimension(150, 40));
        
        return button;
    }

    public static JButton createButton(String text) {
        return MainInterfaceFrame.createButton(text, MainInterfaceFrame.BUTTON_COLOR);
    }

    private void createMainContentPanel() {
        this.mainContentPanel = new JPanel();
        this.mainContentPanel.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
        this.mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        this.add(this.mainContentPanel, BorderLayout.CENTER);
    }

    private void createBottomNavigation() {
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBackground(new Color(236, 240, 241));
        bottomContainer.setPreferredSize(new Dimension(0, 70));

        JPanel bottomNavPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 15));
        bottomNavPanel.setBackground(new Color(236, 240, 241));

        JButton btnTrain = MainInterfaceFrame.createButton("Trains");
        btnTrain.addActionListener(e -> {
            currentView = "trains";
            new TrainTableView(this);
        });
        bottomNavPanel.add(btnTrain);

        JButton btnSchedule = MainInterfaceFrame.createButton("Planning");
        btnSchedule.addActionListener(e -> {
            currentView = "planning";
            new ScheduleTableView(this);
        });
        bottomNavPanel.add(btnSchedule);

        JButton btnAlerts = MainInterfaceFrame.createButton("Alarmes");
        btnAlerts.addActionListener(e -> {
            currentView = "alarmes";
            new AlertTableView(this);
        });
        bottomNavPanel.add(btnAlerts);


        this.rightBottomNavPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        this.rightBottomNavPanel.setBackground(new Color(236, 240, 241));

        bottomContainer.add(bottomNavPanel, BorderLayout.WEST);
        bottomContainer.add(this.rightBottomNavPanel, BorderLayout.EAST);

        this.add(bottomContainer, BorderLayout.SOUTH);
        btnTrain.doClick(); 
    }

    public JPanel getMainContentPanel() {
        return this.mainContentPanel;
    }

    public void showErrorDialog(Exception e, String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        logger.error("{}: {}", title, e.getMessage());
    }

    public void showWarningDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    public void showSuccessDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public boolean showConfirmDialog(String title, String message) {
        return JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void startTimeUpdate() {
        Timer timer = new Timer(1000, e -> {
            LocalTime currentTime = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            timeLabel.setText(currentTime.format(formatter));
        });
        timer.start();
    }
}