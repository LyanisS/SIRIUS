package edu.ezip.ing1.pds.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.business.dto.TrainStatus;
import edu.ezip.ing1.pds.business.dto.Trains;
import edu.ezip.ing1.pds.services.TrainService;

public class TrainTableView {
    private MainInterfaceFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private final TrainService service;

    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color ACCENT_COLOR = new Color(231, 76, 60);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color TABLE_HEADER_COLOR = new Color(52, 73, 94);
    private static final Color TABLE_ALTERNATE_ROW = new Color(245, 245, 245);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);

    public TrainTableView(MainInterfaceFrame frame) {
        this.frame = frame;
        this.frame.setTitle("Gestion des Trains - Système de Contrôle");
        this.frame.getMainJPanel().removeAll();

        UIManager.put("Button.background", BACKGROUND_COLOR);
        UIManager.put("Button.foreground", TEXT_COLOR);
        UIManager.put("Panel.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.background", BACKGROUND_COLOR);
        UIManager.put("OptionPane.messageForeground", TEXT_COLOR);

        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setBackground(BACKGROUND_COLOR);
        container.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = createHeaderPanel();
        container.add(headerPanel, BorderLayout.NORTH);

        createStyledTable();

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BACKGROUND_COLOR);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        container.add(tablePanel, BorderLayout.CENTER);

        JPanel actionPanel = createActionPanel();
        container.add(actionPanel, BorderLayout.SOUTH);

        this.frame.getMainJPanel().setLayout(new BorderLayout());
        this.frame.getMainJPanel().add(container, BorderLayout.CENTER);

        List<JButton> buttons = createToolbarButtons();
        this.frame.registerJButtons(buttons);

        this.service = new TrainService(this.frame.getNetworkConfig());
        this.refreshTrainData();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, getWidth(), 0, SECONDARY_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 60));

        JLabel titleLabel = new JLabel("Gestion des Trains");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel subtitleLabel = new JLabel("Système de Contrôle Ferroviaire");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        subtitleLabel.setBorder(new EmptyBorder(0, 0, 0, 20));
        headerPanel.add(subtitleLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private void createStyledTable() {
        String[] columnNames = {
                "ID Train",
                "Statut",
                "Position (ID CDV)",
                "Station"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);

                if (!isRowSelected(row)) {
                    comp.setBackground(row % 2 == 0 ? Color.WHITE : TABLE_ALTERNATE_ROW);
                } else {

                    comp.setBackground(
                            new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 80));
                    comp.setForeground(TEXT_COLOR.darker());
                }

                if (column == 1 && !isRowSelected(row)) {
                    String status = (String) getValueAt(row, column);
                    if ("EN_PANNE".equals(status)) {
                        comp.setForeground(ACCENT_COLOR);
                    } else if ("EN_MARCHE".equals(status)) {
                        comp.setForeground(SUCCESS_COLOR);
                    } else {
                        comp.setForeground(TEXT_COLOR);
                    }
                }

                return comp;
            }
        };

        table.setRowHeight(30);
        table.setIntercellSpacing(new Dimension(10, 5));
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(
                new Color(PRIMARY_COLOR.getRed(), PRIMARY_COLOR.getGreen(), PRIMARY_COLOR.getBlue(), 100));
        table.setSelectionForeground(TEXT_COLOR.darker());
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                }
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER_COLOR);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setBorder(null);
        header.setPreferredSize(new Dimension(0, 35));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    private JPanel createActionPanel() {
        JPanel gradientPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(245, 245, 245), 0, getHeight(), BACKGROUND_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        gradientPanel.setLayout(new BorderLayout());
        gradientPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JPanel actionPanel = new JPanel();
        actionPanel.setOpaque(false);
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
        actionPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton addButton = createStyledButton("Ajouter un train", SUCCESS_COLOR);
        addButton.addActionListener(e -> showAddTrainDialog());

        JButton deleteButton = createStyledButton("Supprimer", ACCENT_COLOR);
        deleteButton.addActionListener(e -> deleteSelectedTrain());

        JButton updateStatusButton = createStyledButton("Mettre à jour le statut", PRIMARY_COLOR);
        updateStatusButton.addActionListener(e -> updateSelectedTrainStatus());

        actionPanel.add(addButton);
        actionPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        actionPanel.add(deleteButton);
        actionPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        actionPanel.add(updateStatusButton);
        actionPanel.add(Box.createHorizontalGlue());

        gradientPanel.add(actionPanel, BorderLayout.CENTER);
        return gradientPanel;
    }

    private JButton createStyledButton(String text, Color color) {
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

    private List<JButton> createToolbarButtons() {
        List<JButton> buttons = new ArrayList<>();

        JButton refreshButton = createToolbarButton("Actualiser");
        refreshButton.addActionListener(e -> refreshTrainData());
        buttons.add(refreshButton);

        JButton planningButton = createToolbarButton("Planning");
        planningButton.addActionListener(e -> showPlanningView());
        buttons.add(planningButton);

        JButton alarmsButton = createToolbarButton("Alarmes");
        alarmsButton.addActionListener(e -> showAlarmsView());
        buttons.add(alarmsButton);

        return buttons;
    }

    private JButton createToolbarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(TEXT_COLOR);
        button.setBackground(BACKGROUND_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_COLOR),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(225, 225, 225));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(BACKGROUND_COLOR);
            }
        });

        return button;
    }

    private void refreshTrainData() {
        try {
            tableModel.setRowCount(0);

            Trains trains = this.service.selectTrains();

            if (trains != null && trains.getTrains() != null) {
                for (Train train : trains.getTrains()) {
                    String stationName = train.getTrackElement().getStation() != null
                            ? train.getTrackElement().getStation().getName()
                            : "";

                    Object[] row = {
                            train.getId(),
                            train.getStatus().getName(),
                            train.getTrackElement().getId(),
                            train.getTrackElement().getStation() != null
                                    ? train.getTrackElement().getStation().getName()
                                    : ""
                    };
                    tableModel.addRow(row);
                }
            }
            this.frame.repaint();
            this.frame.revalidate();
        } catch (Exception e) {
            showErrorDialog(e, "Erreur de chargement",
                    "Erreur dans le chargement des données du train");
        }
    }

    private void deleteSelectedTrain() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("Aucun train sélectionné",
                    "Veuillez sélectionner un train à supprimer.");
            return;
        }

        try {
            int trainId = (int) tableModel.getValueAt(selectedRow, 0);
            String stationName = (String) tableModel.getValueAt(selectedRow, 3);

            if (!"POSE".equals(stationName) && !"MAMO".equals(stationName)) {
                showWarningDialog("Suppression impossible",
                        "Impossible de supprimer ce train.\n\n" +
                                "Seuls les trains situés dans les stations POSE ou MAMO peuvent être supprimés.");
                return;
            }

            if (showConfirmDialog("Confirmer la suppression",
                    "Êtes-vous sûr de vouloir supprimer le train #" + trainId + " ?")) {
                try {

                    this.service.deleteTrain(trainId);

                    refreshTrainData();

                    showSuccessDialog("Suppression réussie",
                            "Le train #" + trainId + " a été supprimé avec succès.");
                } catch (Exception e) {
                    handleDeleteError(e, trainId);
                }
            }
        } catch (Exception e) {
            handleDeleteError(e, -1);
        }
    }

    private void handleDeleteError(Exception e, int trainId) {
        String errorMessage = e.getMessage();
        String userFriendlyMessage;

        if (errorMessage != null && errorMessage.contains("Connection")) {
            userFriendlyMessage = "Erreur de connexion au serveur.\n\n" +
                    "Veuillez vérifier votre connexion réseau et réessayer.";
        } else if (errorMessage != null && errorMessage.contains("Unrecognized token")) {

            userFriendlyMessage = "Le train a probablement été supprimé, mais une erreur est survenue lors du traitement de la réponse.\n\n"
                    +
                    "Veuillez actualiser la liste pour vérifier.";

            try {
                refreshTrainData();
            } catch (Exception refreshError) {

            }
        } else {
            userFriendlyMessage = "Une erreur est survenue lors de la suppression du train" +
                    (trainId > 0 ? " #" + trainId : "") + ":\n\n" + errorMessage;
        }

        showErrorDialog(e, "Erreur", userFriendlyMessage);
    }

    private void showAddTrainDialog() {

        styleDialogUIComponents();

        AddTrainDialog dialog = new AddTrainDialog(this.frame, this.service);

        if (dialog.showDialog()) {
            dialog.addTrain();
            refreshTrainData();
        }

        resetDialogUIComponents();
    }

    private void styleDialogUIComponents() {

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

    private void resetDialogUIComponents() {

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

    private void updateSelectedTrainStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showWarningDialog("Aucun train sélectionné",
                    "Veuillez sélectionner un train pour mettre à jour son statut.");
            return;
        }

        try {
            int trainId = (int) tableModel.getValueAt(selectedRow, 0);
            String currentStatus = (String) tableModel.getValueAt(selectedRow, 1);

            JPanel panel = new JPanel();
            panel.setBackground(BACKGROUND_COLOR);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel titleLabel = new JLabel("Mettre à jour le statut du train #" + trainId);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            titleLabel.setForeground(TEXT_COLOR);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(titleLabel);

            panel.add(Box.createRigidArea(new Dimension(0, 15)));

            JLabel statusLabel = new JLabel("Nouveau statut:");
            statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            statusLabel.setForeground(TEXT_COLOR);
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(statusLabel);

            panel.add(Box.createRigidArea(new Dimension(0, 5)));

            JComboBox<String> statusComboBox = new JComboBox<>();
            statusComboBox.setBackground(Color.WHITE);
            statusComboBox.setForeground(TEXT_COLOR);
            statusComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
            statusComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            statusComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

            for (TrainStatus status : TrainStatus.values()) {
                statusComboBox.addItem(status.getName());
            }

            statusComboBox.setSelectedItem(currentStatus);
            panel.add(statusComboBox);

            int result = JOptionPane.showConfirmDialog(this.frame, panel,
                    "Mise à jour du statut",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String newStatusName = (String) statusComboBox.getSelectedItem();

                TrainStatus newStatus = null;
                for (TrainStatus status : TrainStatus.values()) {
                    if (status.getName().equals(newStatusName)) {
                        newStatus = status;
                        break;
                    }
                }

                if (newStatus != null) {

                    this.service.updateTrainStatus(trainId, newStatus.getId());

                    refreshTrainData();

                    showSuccessDialog("Mise à jour réussie",
                            "Le statut du train #" + trainId + " a été mis à jour avec succès.");
                }
            }
        } catch (Exception e) {
            showErrorDialog(e, "Erreur",
                    "Une erreur est survenue lors de la mise à jour du statut du train");
        }
    }

    private void showErrorDialog(Exception e, String title, String baseMessage) {

        styleDialogUIComponents();

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

        JOptionPane.showMessageDialog(this.frame,
                userFriendlyMessage,
                title,
                JOptionPane.ERROR_MESSAGE);

        resetDialogUIComponents();

        System.err.println(title + ": " + e.getMessage());
        e.printStackTrace();
    }

    private void showWarningDialog(String title, String message) {
        styleDialogUIComponents();

        JOptionPane.showMessageDialog(this.frame,
                message,
                title,
                JOptionPane.WARNING_MESSAGE);

        resetDialogUIComponents();
    }

    private void showSuccessDialog(String title, String message) {
        styleDialogUIComponents();

        JOptionPane.showMessageDialog(this.frame,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE);

        resetDialogUIComponents();
    }

    private boolean showConfirmDialog(String title, String message) {
        styleDialogUIComponents();

        int result = JOptionPane.showConfirmDialog(this.frame,
                message,
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        resetDialogUIComponents();

        return result == JOptionPane.YES_OPTION;
    }

    private void showPlanningView() {

        JOptionPane.showMessageDialog(this.frame,
                "La vue de planning sera implémentée dans une future version.",
                "Fonctionnalité à venir",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAlarmsView() {

        JOptionPane.showMessageDialog(this.frame,
                "La vue des alarmes sera implémentée dans une future version.",
                "Fonctionnalité à venir",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
