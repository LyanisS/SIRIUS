package edu.ezip.ing1.pds.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import edu.ezip.ing1.pds.business.dto.Schedule;
import edu.ezip.ing1.pds.business.dto.Schedules;
import edu.ezip.ing1.pds.business.dto.TrackElement;
import edu.ezip.ing1.pds.business.dto.Trip;
import edu.ezip.ing1.pds.services.ScheduleService;

public class ScheduleTableView {

    private MainInterfaceFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private final ScheduleService service;

    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    private static final Color ACCENT_COLOR = new Color(231, 76, 60);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color TABLE_HEADER_COLOR = new Color(52, 73, 94);
    private static final Color TABLE_ALTERNATE_ROW = new Color(245, 245, 245);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);

    public ScheduleTableView(MainInterfaceFrame frame) {
        this.frame = frame;
        this.frame.setTitle("Gestion du planning des trains - Système de Contrôle");
        this.frame.getTableJPanel().removeAll();

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

        this.service = new ScheduleService(this.frame.getNetworkConfig());
        this.refreshScheduleData();
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

        JLabel titleLabel = new JLabel("Gestion du Planning");
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
            "ID Horaire",
            "ID Trajet",
            "ID CDV",
            "Date/Heure",
            "Arrêt?"
        };

        JScrollPane scrollPane = new JScrollPane(table);
        this.frame.getTableJPanel().add(scrollPane);

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

        JButton addButton = createStyledButton("Ajouter un horaire", SUCCESS_COLOR);
        addButton.addActionListener(e -> openAddScheduleDialog());

        JButton editButton = createStyledButton("Modifier", PRIMARY_COLOR);
        editButton.addActionListener(e -> openEditScheduleDialog());

        JButton deleteButton = createStyledButton("Supprimer", ACCENT_COLOR);
        deleteButton.addActionListener(e -> deleteSchedule());

        actionPanel.add(addButton);
        actionPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        actionPanel.add(editButton);
        actionPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        actionPanel.add(deleteButton);
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

        JButton addScheduleButton = MainInterfaceFrame.createActionButton("Ajouter un horaire", MainInterfaceFrame.SUCCESS_COLOR);
        addScheduleButton.addActionListener(e -> openAddScheduleDialog());
        buttons.add(addScheduleButton);

        JButton refreshButton = MainInterfaceFrame.createActionButton("Actualiser", MainInterfaceFrame.REFRESH_BTN_COLOR);
        refreshButton.addActionListener(e -> refreshScheduleData());
        buttons.add(refreshButton);

        this.frame.registerJButtons(buttons);

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

    private void refreshScheduleData() {
        try {
            tableModel.setRowCount(0);

            Schedules schedules = this.service.selectSchedules();

            if (schedules != null && schedules.getSchedules() != null) {
                for (Schedule schedule : schedules.getSchedules()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = schedule.getTimestamp() != null
                            ? dateFormat.format(schedule.getTimestamp())
                            : "";

                    Object[] row = {
                        schedule.getId(),
                        schedule.getTrip().getId(),
                        schedule.getTrackElement().getId(),
                        formattedDate,
                        schedule.getStop() ? "Oui" : "Non"
                    };
                    tableModel.addRow(row);
                }
            }
            this.frame.repaint();
            this.frame.revalidate();
        } catch (Exception e) {
            showErrorDialog(e, "Erreur de chargement",
                    "Erreur lors de la récupération des données du planning");
        }
    }

    private void openAddScheduleDialog() {
        styleDialogUIComponents();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Ajouter un nouvel horaire");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel datetimeLabel = new JLabel("Date/heure (yyyy-MM-dd HH:mm:ss):");
        datetimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        datetimeLabel.setForeground(TEXT_COLOR);
        datetimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(datetimeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField datetimeField = new JTextField();
        datetimeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        datetimeField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(datetimeField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel stopLabel = new JLabel("Arrêt ?");
        stopLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stopLabel.setForeground(TEXT_COLOR);
        stopLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(stopLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JComboBox<String> stopComboBox = new JComboBox<>(new String[]{"Oui", "Non"});
        stopComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        stopComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(stopComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel cdvLabel = new JLabel("ID CDV:");
        cdvLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        cdvLabel.setForeground(TEXT_COLOR);
        cdvLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cdvLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField cdvField = new JTextField();
        cdvField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cdvField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cdvField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel tripLabel = new JLabel("ID Trajet:");
        tripLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        tripLabel.setForeground(TEXT_COLOR);
        tripLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(tripLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField tripField = new JTextField();
        tripField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        tripField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(tripField);

        int result = JOptionPane.showConfirmDialog(
                this.frame,
                panel,
                "Ajouter un horaire",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String scheduleDatetime = datetimeField.getText();
            String scheduleStop = stopComboBox.getSelectedItem().toString();
            String trackElementId = cdvField.getText();
            String tripId = tripField.getText();

            if (!scheduleDatetime.isEmpty() && !trackElementId.isEmpty() && !tripId.isEmpty()) {
                try {
                    Schedule newSchedule = new Schedule();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    newSchedule.setTimestamp(new java.sql.Timestamp(dateFormat.parse(scheduleDatetime).getTime()));

                    newSchedule.setStop("Oui".equals(scheduleStop));

                    newSchedule.setTrackElement(new TrackElement(Integer.parseInt(trackElementId)));
                    newSchedule.setTrip(new Trip(Integer.parseInt(tripId)));

                    Schedules schedules = new Schedules();
                    schedules.add(newSchedule);

                    this.service.insertSchedules(schedules);

                    refreshScheduleData();

                    showSuccessDialog("Parfait!", "L'horaire a été ajouté avec succès!");

                } catch (Exception ex) {
                    showErrorDialog(ex, "Erreur!!", "Vous avez une erreur lors de l'ajout de l'horaire!!");
                }
            } else {
                showWarningDialog("Erreur!", "Il faut remplir tous les champs!");
            }
        }

        resetDialogUIComponents();
    }

    private void openEditScheduleDialog() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            showWarningDialog("Aucun horaire sélectionné!",
                    "Veuillez sélectionner un horaire à modifier.");
            return;
        }

        styleDialogUIComponents();

        int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentDatetime = (String) tableModel.getValueAt(selectedRow, 3);
        boolean currentStop = "Oui".equals(tableModel.getValueAt(selectedRow, 4));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Modifier l'horaire #" + scheduleId);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel datetimeLabel = new JLabel("Date/heure (yyyy-MM-dd HH:mm:ss):");
        datetimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        datetimeLabel.setForeground(TEXT_COLOR);
        datetimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(datetimeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField datetimeField = new JTextField(currentDatetime);
        datetimeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        datetimeField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(datetimeField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel stopLabel = new JLabel("Arrêt ?");
        stopLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stopLabel.setForeground(TEXT_COLOR);
        stopLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(stopLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JComboBox<String> stopComboBox = new JComboBox<>(new String[]{"Oui", "Non"});
        stopComboBox.setSelectedItem(currentStop ? "Oui" : "Non");
        stopComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        stopComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(stopComboBox);

        int result = JOptionPane.showConfirmDialog(
                this.frame,
                panel,
                "Modifier un horaire",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String scheduleDatetime = datetimeField.getText();
            String scheduleStop = stopComboBox.getSelectedItem().toString();

            if (!scheduleDatetime.isEmpty()) {
                try {
                    Schedule updatedSchedule = new Schedule();
                    updatedSchedule.setId(scheduleId);
                    String res = "Oui";
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    updatedSchedule.setTimestamp(new java.sql.Timestamp(dateFormat.parse(scheduleDatetime).getTime()));
                    updatedSchedule.setStop(res.equals(scheduleStop));
                    System.out.println("------------------------------------------------------------------------------");
                    System.out.println(updatedSchedule.getStop());
                    System.out.println("------------------------------------------------------------------------");

                    this.service.UpdateSchedule(scheduleId, updatedSchedule.getStop());

                    refreshScheduleData();

                    showSuccessDialog("Modification réussie!", "L'horaire a été modifié avec succès!");

                } catch (Exception ex) {
                    showErrorDialog(ex, "Erreur!", "Vous avez une erreur! ");
                }
            } else {
                showWarningDialog("Erreur", "La date/heure ne peut pas être vide.");
            }
        }

        resetDialogUIComponents();
    }

    private void deleteSchedule() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            showWarningDialog("Aucun horaire sélectionné",
                    "Veuillez sélectionner un horaire à supprimer.");
            return;
        }

        int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);

        if (showConfirmDialog("Confirmer la suppression",
                "Voulez-vous vraiment supprimer l'horaire #" + scheduleId + " ?")) {
            try {
                this.service.deleteSchedule(scheduleId);

                refreshScheduleData();

                showSuccessDialog("Suppression réussie", "L'horaire a été supprimé avec succès!");

            } catch (Exception ex) {
                showErrorDialog(ex, "Erreur", "Erreur lors de la suppression de l'horaire");
            }
        }
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

    private void showErrorDialog(Exception e, String title, String baseMessage) {
        styleDialogUIComponents();

        String errorMessage = e.getMessage();
        String userFriendlyMessage;

        if (errorMessage != null && errorMessage.contains("Connection")) {
            userFriendlyMessage = "Erreur de connexion au serveur.\n\n"
                    + "Veuillez vérifier votre connexion réseau et réessayer.";
        } else if (errorMessage != null && errorMessage.contains("timeout")) {
            userFriendlyMessage = "Le serveur ne répond pas. Veuillez réessayer plus tard.";
        } else {
            userFriendlyMessage = baseMessage + ": " + errorMessage;
        }

        JOptionPane.showMessageDialog(this.frame,
                userFriendlyMessage,
                title,
                JOptionPane.ERROR_MESSAGE
        );

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

    private void showWarningDialog(String title, String message) {
        styleDialogUIComponents();

        JOptionPane.showMessageDialog(this.frame,
                message,
                title,
                JOptionPane.WARNING_MESSAGE);

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

    private void showTrainView() {
        TrainTableView trainTableView = new TrainTableView(this.frame);
    }

    private void showStatisticsView() {

    }
}
