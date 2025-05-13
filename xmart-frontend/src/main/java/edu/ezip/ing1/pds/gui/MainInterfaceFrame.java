package edu.ezip.ing1.pds.gui;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import edu.ezip.ing1.pds.business.dto.AlertGravity;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.TrainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private JButton alertButton;
    private JPanel alertCard;
    private boolean isAlertCardVisible = false;
    private JLabel timeLabel;

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
        this.createAlertCard();

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            if (isAlertCardVisible && !isClickInCard(e.getPoint())) {
                hideAlertCard();
            }
            }
        });

        this.startTimeUpdate();
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


        JPanel leftSection = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        leftSection.setOpaque(false);
        this.timeLabel = new JLabel();
        this.timeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        this.timeLabel.setForeground(Color.WHITE);

        JLabel profileLabel = new JLabel("Profile");
        profileLabel.setFont(new Font("Arial", Font.BOLD, 18));
        profileLabel.setForeground(Color.WHITE);

        leftSection.add(this.timeLabel);
        leftSection.add(profileLabel);


        JPanel rightSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        rightSection.setOpaque(false);

        this.alertButton = new JButton("Messages d'alarmes");
        this.alertButton.setFont(new Font("Arial", Font.BOLD, 16));
        this.alertButton.setForeground(Color.WHITE);
        this.alertButton.setContentAreaFilled(false);
        this.alertButton.setBorderPainted(false);
        this.alertButton.setFocusPainted(false);
        this.alertButton.setCursor(new Cursor(Cursor.HAND_CURSOR));


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.setOpaque(false);

        JPanel indicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(ALERT_COLOR);
                g2d.fillOval(0, 0, 10, 10);
            }
        };
        indicator.setPreferredSize(new Dimension(10, 10));
        indicator.setOpaque(false);

        buttonPanel.add(indicator);
        buttonPanel.add(this.alertButton);
        rightSection.add(buttonPanel);

        this.alertButton.addActionListener(e -> toggleAlertCard());

        topPanel.add(leftSection, BorderLayout.WEST);
        topPanel.add(rightSection, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
    }

    private void createAlertCard() {
        this.alertCard = new JPanel();
        this.alertCard.setLayout(new BoxLayout(alertCard, BoxLayout.Y_AXIS));
        this.alertCard.setBackground(Color.WHITE);
        this.alertCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));


        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        JLabel titleLabel = new JLabel("Messages d'alarmes");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);


        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Arial", Font.BOLD, 20));
        closeButton.setForeground(TEXT_COLOR);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> hideAlertCard());
        headerPanel.add(closeButton, BorderLayout.EAST);

        this.alertCard.add(headerPanel);
        this.alertCard.add(Box.createVerticalStrut(10));

        this.alertCard.setVisible(false);
    }

    private void addAlertItem(String message, AlertGravity alertGravity) {
        JPanel item = new JPanel(new BorderLayout(10, 0));
        item.setBackground(new Color(249, 249, 249));
        item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, alertGravity.getColor()),
                BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));


        JPanel leftContent = new JPanel(new BorderLayout(10, 5));
        leftContent.setOpaque(false);

        JLabel severityLabel = new JLabel(alertGravity.getType());
        severityLabel.setFont(new Font("Arial", Font.BOLD, 12));
        severityLabel.setForeground(alertGravity.getColor());
        severityLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(alertGravity.getColor()),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));


        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setForeground(TEXT_COLOR);

        leftContent.add(severityLabel, BorderLayout.NORTH);
        leftContent.add(messageLabel, BorderLayout.CENTER);

        JPanel rightContent = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JLabel timeLabel = new JLabel(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        timeLabel.setForeground(new Color(150, 150, 150));

        JButton detailsBtn = createButton("Détails");
        JButton acknowledgeBtn = createButton("Acquitter");

        rightContent.add(timeLabel);
        rightContent.add(detailsBtn);
        rightContent.add(acknowledgeBtn);

        item.add(leftContent, BorderLayout.CENTER);
        item.add(rightContent, BorderLayout.EAST);


        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(245, 245, 245));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(new Color(249, 249, 249));
            }
        });

        this.alertCard.add(item);
        this.alertCard.add(Box.createVerticalStrut(5));
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
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(MainInterfaceFrame.TEXT_COLOR);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JButton createButton(String text) {
        return MainInterfaceFrame.createButton(text, MainInterfaceFrame.BUTTON_COLOR);
    }

    private void toggleAlertCard() {
        if (!isAlertCardVisible) {
            showAlertCard();
        } else {
            hideAlertCard();
        }
    }

    private void showAlertCard() {
        if (alertCard != null && alertButton != null) {
            Point buttonLocation = alertButton.getLocationOnScreen();

            int x = buttonLocation.x - alertCard.getPreferredSize().width + alertButton.getWidth();
            int y = buttonLocation.y + alertButton.getHeight() + 5;

            if (x < 0) x = 0;
            if (x + alertCard.getPreferredSize().width > getWidth()) {
                x = getWidth() - alertCard.getPreferredSize().width;
            }

            alertCard.setLocation(x, y);
            alertCard.setVisible(true);
            isAlertCardVisible = true;

            JLayeredPane layeredPane = getRootPane().getLayeredPane();
            if (!layeredPane.isAncestorOf(alertCard)) {
                layeredPane.add(alertCard, JLayeredPane.POPUP_LAYER);
                alertCard.setSize(alertCard.getPreferredSize());
            }
        }
    }

    private void hideAlertCard() {
        if (alertCard != null) {
            alertCard.setVisible(false);
            isAlertCardVisible = false;
        }
    }

    private boolean isClickInCard(Point point) {
        if (alertCard != null && alertCard.isVisible()) {
            Point cardScreenLocation = alertCard.getLocationOnScreen();
            Rectangle cardBounds = new Rectangle(
                    cardScreenLocation.x,
                    cardScreenLocation.y,
                    alertCard.getWidth(),
                    alertCard.getHeight()
            );
            return cardBounds.contains(point);
        }
        return false;
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
        bottomContainer.setPreferredSize(new Dimension(0, 60));

        JPanel bottomNavPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        bottomNavPanel.setBackground(new Color(236, 240, 241));

        JButton btnTrain = MainInterfaceFrame.createButton("Trains");
        btnTrain.addActionListener(e -> new TrainTableView(this));
        bottomNavPanel.add(btnTrain);

        JButton btnSchedule = MainInterfaceFrame.createButton("Planning");
        btnSchedule.addActionListener(e -> new ScheduleTableView(this));
        bottomNavPanel.add(btnSchedule);

        JButton btnAlerts = MainInterfaceFrame.createButton("Alarmes");
        btnAlerts.addActionListener(e -> new AlertTableView(this));
        bottomNavPanel.add(btnAlerts);


        this.rightBottomNavPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        this.rightBottomNavPanel.setBackground(new Color(236, 240, 241));

        bottomContainer.add(bottomNavPanel, BorderLayout.WEST);
        bottomContainer.add(this.rightBottomNavPanel, BorderLayout.EAST);

        this.add(bottomContainer, BorderLayout.SOUTH);
        btnTrain.doClick();
        // TODO: remove this line when updateRightNavigation is moved to TrainTableView
        updateRightNavigation("traffic");
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

    @Deprecated // TODO: move this method to TrainTableView
    private void updateRightNavigation(String section) {
        if (section.equals("traffic")) {
            JButton addTrainBtn = MainInterfaceFrame.createButton("Ajouter Train");
            JButton modifyStatusBtn = MainInterfaceFrame.createButton("Modifier Statut");
            JButton showRoutesBtn = MainInterfaceFrame.createButton("Afficher Trajets");
            JButton deleteTrainBtn = MainInterfaceFrame.createButton("Supprimer Train");

            addTrainBtn.addActionListener(e -> {
                AddTrainStyle dialog = new AddTrainStyle(this);
                dialog.showDialog();
                dialog.addTrain();
            });

            deleteTrainBtn.addActionListener(e -> {
                TrainService service = new TrainService(ConfigLoader.loadConfig(NetworkConfig.class, "network.yaml"));
                JTable trainTable = findTrainTable();
                if (trainTable != null) {
                    int selectedRow = trainTable.getSelectedRow();
                    if (selectedRow != -1) {
                        int trainId = (int) trainTable.getValueAt(selectedRow, 0);
                        if (showConfirmDialog("Confirmer la suppression",
                                "Êtes-vous sûr de vouloir supprimer le train " + trainId + " ?")) {
                            try {
                                service.deleteTrain(trainId);
                                refreshTrainTable();
                                showSuccessDialog("Suppression réussie",
                                        "Le train " + trainId + " a été supprimé avec succès");
                            } catch (Exception ex) {
                                showErrorDialog(ex, "Erreur de suppression",
                                        "Une erreur est survenue lors de la suppression du train " + trainId);
                            }
                        }
                    } else {
                        showWarningDialog("Aucun train sélectionné",
                                "Veuillez sélectionner un train à supprimer");
                    }
                }
            });

            List<JButton> buttons = new ArrayList<JButton>();
            buttons.add(addTrainBtn);
            buttons.add(modifyStatusBtn);
            buttons.add(showRoutesBtn);
            buttons.add(deleteTrainBtn);

            this.registerJButtons(buttons);
        }
    }

    @Deprecated // TODO: move this method to TrainTableView
    private JTable findTrainTable() {
        Component[] components = this.mainContentPanel.getComponents();
        for (Component component : components) {
            if (component instanceof JScrollPane) {
                JViewport viewport = ((JScrollPane) component).getViewport();
                if (viewport.getView() instanceof JTable) {
                    return (JTable) viewport.getView();
                }
            }
        }
        return null;
    }

    @Deprecated // TODO: move this method to TrainTableView
    public void refreshTrainTable() {
        if (this.mainContentPanel.getComponents().length > 0) {
            new TrainTableView(this);
        }
    }
}