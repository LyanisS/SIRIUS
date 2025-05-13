package edu.ezip.ing1.pds.gui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.awt.event.*;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.TrainService;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;

public class MainTemplate extends JFrame {
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color BUTTON_COLOR = new Color(52, 152, 219);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color ALERT_COLOR = new Color(231, 76, 60);
    

    private JPanel topPanel;
    private JPanel mainContentPanel;
    private JPanel bottomNavPanel;
    private JPanel rightBottomNavPanel;
    

    private JButton alertButton;
    private JPanel alertCard;
    private boolean isAlertCardVisible = false;
    

    private JButton trafficBtn;
    private JButton scheduleBtn;
    private JButton alertsBtn;
    

    private JLabel timeLabel;
    
    private JButton refreshButton;
    
    public MainTemplate() {
        setTitle("Système de Contrôle Ferroviaire");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        createTopPanel();
        createMainContentPanel();
        createBottomNavigation();
        createAlertCard();
        
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isAlertCardVisible && !isClickInCard(e.getPoint())) {
                    hideAlertCard();
                }
            }
        });
    
        startTimeUpdate();
        
        //@ Ajout du timer d'actualisation automatique toutes les 40s
        Timer autoRefreshTimer = new Timer(40000, e -> refreshAll());
        autoRefreshTimer.start();
        
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }
    
    private void createTopPanel() {
        topPanel = new JPanel(new BorderLayout()) {
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
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        timeLabel.setForeground(Color.WHITE);
        
        leftSection.add(timeLabel);
        
    
        JPanel rightSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 15));
        rightSection.setOpaque(false);
        
        // @ bouton Actualiser
        refreshButton = new JButton("\u27F3 Actualiser");
        refreshButton.setFont(new Font("Dialog", Font.BOLD, 14));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBackground(new Color(41, 128, 185));
        refreshButton.setFocusPainted(false);
        refreshButton.setBorderPainted(false);
        refreshButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        refreshButton.setHorizontalTextPosition(SwingConstants.RIGHT);
        refreshButton.setIconTextGap(8);
        refreshButton.addActionListener(e -> refreshAll());
        rightSection.add(refreshButton);
        
        topPanel.add(leftSection, BorderLayout.WEST);
        topPanel.add(rightSection, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
    }
    
    private void createAlertCard() {
        alertCard = new JPanel();
        alertCard.setLayout(new BoxLayout(alertCard, BoxLayout.Y_AXIS));
        alertCard.setBackground(Color.WHITE);
        alertCard.setBorder(BorderFactory.createCompoundBorder(
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
        
        alertCard.add(headerPanel);
        alertCard.add(Box.createVerticalStrut(10));
        
    
   
        
        alertCard.setVisible(false);
    }
    


    
    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setForeground(BUTTON_COLOR);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(PRIMARY_COLOR);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(BUTTON_COLOR);
            }
        });
        
        return button;
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
        mainContentPanel = new JPanel();
        mainContentPanel.setBackground(BACKGROUND_COLOR);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(mainContentPanel, BorderLayout.CENTER);
    }
    
    private void createBottomNavigation() {
        JPanel bottomContainer = new JPanel(new BorderLayout());
        bottomContainer.setBackground(new Color(236, 240, 241)); 
        bottomContainer.setPreferredSize(new Dimension(0, 60));

        bottomNavPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        bottomNavPanel.setBackground(new Color(236, 240, 241));
        
        trafficBtn = createNavButton("Trafic");
        scheduleBtn = createNavButton("Horaires");
        alertsBtn = createNavButton("Alarmes");
        
        bottomNavPanel.add(trafficBtn);
        bottomNavPanel.add(scheduleBtn);
        bottomNavPanel.add(alertsBtn);
        
    
        rightBottomNavPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        rightBottomNavPanel.setBackground(new Color(236, 240, 241));
        
        bottomContainer.add(bottomNavPanel, BorderLayout.WEST);
        bottomContainer.add(rightBottomNavPanel, BorderLayout.EAST);
        
        add(bottomContainer, BorderLayout.SOUTH);
        

        setupNavigationListeners();
    }
    
    private JButton createNavButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(TEXT_COLOR);
        button.setBackground(BUTTON_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        

        int width = text.equals("Supprimer Train") ? 160 : 150;
        button.setPreferredSize(new Dimension(width, 40));
        
        return button;
    }
    
    private void setupNavigationListeners() {
        trafficBtn.addActionListener(e -> {
            updateRightNavigation("traffic");
            new TrainTableView(this);
        });
        scheduleBtn.addActionListener(e -> updateRightNavigation("schedule"));
        alertsBtn.addActionListener(e -> updateRightNavigation("alerts"));
    }
    
    private void updateRightNavigation(String section) {
        rightBottomNavPanel.removeAll();
        
        switch (section) {
            case "traffic":
                JButton addTrainBtn = createNavButton("Ajouter Train");
                JButton showRoutesBtn = createNavButton("Afficher Trajets");
                JButton deleteTrainBtn = createNavButton("Supprimer Train");
                
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
                                    showErrorDialog(ex, "Erreur", 
                                        "Une erreur est survenue lors de la suppression du train");
                                }
                            }
                        } else {
                            showWarningDialog("Aucun train sélectionné", 
                                "Veuillez sélectionner un train à supprimer");
                        }
                    }
                });
                
                rightBottomNavPanel.add(addTrainBtn);
                rightBottomNavPanel.add(showRoutesBtn);
                rightBottomNavPanel.add(deleteTrainBtn);
                break;
            case "schedule":
                rightBottomNavPanel.add(createNavButton("Ajouter Horaire"));
                rightBottomNavPanel.add(createNavButton("Modifier Horaire"));
                rightBottomNavPanel.add(createNavButton("Supprimer Horaire"));
                break;
            case "alerts":
                rightBottomNavPanel.add(createNavButton("Afficher Alarmes"));
                break;
        }
        
        rightBottomNavPanel.revalidate();
        rightBottomNavPanel.repaint();
    }
    
    private JTable findTrainTable() {
        Component[] components = mainContentPanel.getComponents();
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
    
    private void showErrorDialog(Exception e, String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
    
    private void showWarningDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }
    
    private void showSuccessDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private boolean showConfirmDialog(String title, String message) {
        return JOptionPane.showConfirmDialog(this, message, title,
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    
    private void startTimeUpdate() {
        Timer timer = new Timer(1000, e -> {
            LocalTime currentTime = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            timeLabel.setText(currentTime.format(formatter));
        });
        timer.start();
    }
    
    public JPanel getMainContentPanel() {
        return mainContentPanel;
    }
    
    public void refreshTrainTable() {
        if (mainContentPanel.getComponents().length > 0) {
            new TrainTableView(this);
        }
    }
    
    public void refreshAll() {
        refreshTrainTable();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            MainTemplate template = new MainTemplate();
            template.setVisible(true);
        });
    }
} 