package edu.ezip.ing1.pds.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
import edu.ezip.ing1.pds.business.dto.Station;
import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.business.dto.Trains;
import edu.ezip.ing1.pds.business.dto.Trip;
import edu.ezip.ing1.pds.services.ScheduleService;
import edu.ezip.ing1.pds.services.TrainService;

public class ScheduleTableView {

    private MainInterfaceFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private final ScheduleService service;
    private JPanel mainPanel;

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
        this.frame.setTitle("Planning");
        this.frame.getTableJPanel().removeAll();

        createStyledTable();

        JPanel tablePanel = this.frame.getTableJPanel();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        List<JButton> buttons = new ArrayList<>();

        JButton addButton = MainInterfaceFrame.createActionButton("Ajouter trajet", SUCCESS_COLOR);
        addButton.addActionListener(e -> {
            System.out.println("Clic sur Ajouter Trajet, instance scheduleTableView = " + this);
            if (this != null) {
                this.openAddScheduleDialog();
            }
        });
        buttons.add(addButton);

        JButton editButton = MainInterfaceFrame.createActionButton("Modifier", PRIMARY_COLOR);
        editButton.addActionListener(e -> openEditScheduleDialog());
        buttons.add(editButton);

        JButton deleteButton = MainInterfaceFrame.createActionButton("Supprimer", ACCENT_COLOR);
        deleteButton.addActionListener(e -> deleteSchedule());
        buttons.add(deleteButton);

        JButton refreshButton = MainInterfaceFrame.createActionButton("Actualiser", MainInterfaceFrame.REFRESH_BTN_COLOR);
        refreshButton.addActionListener(e -> refreshScheduleData());
        buttons.add(refreshButton);

        this.frame.registerJButtons(buttons);

        this.service = new ScheduleService(this.frame.getNetworkConfig());
        this.refreshScheduleData();
    }

    public ScheduleTableView(MainTemplate template) {
        this.frame = null;
        this.service = new ScheduleService(template.getNetworkConfig());
        this.mainPanel = new JPanel(new BorderLayout());
        this.mainPanel.setBackground(BACKGROUND_COLOR);
        this.mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        createStyledTable();
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
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

        JLabel titleLabel = new JLabel("Planning");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JLabel subtitleLabel = new JLabel("");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        subtitleLabel.setBorder(new EmptyBorder(0, 0, 0, 20));
        headerPanel.add(subtitleLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private void createStyledTable() {
        String[] columnNames = {
            "ID Train",
            "ID Trajet",
            "Date Départ",
            "Date Arrivée",
            "Stations Desservies"
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
        header.setForeground(new Color(0, 51, 102));
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

    private void refreshScheduleData() {
        try {
            tableModel.setRowCount(0);

            Schedules schedules = this.service.selectSchedules();

            if (schedules != null && schedules.getSchedules() != null) {
                for (Schedule schedule : schedules.getSchedules()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String departureDate = schedule.getTimeDeparture() != null
                            ? dateFormat.format(schedule.getTimeDeparture())
                            : "";
                    String arrivalDate = schedule.getTimeArrival() != null
                            ? dateFormat.format(schedule.getTimeArrival())
                            : "";

                    Object[] row = {
                        schedule.getTrip().getTrain().getId(),
                        schedule.getTrip().getId(),
                        departureDate,
                        arrivalDate,
                        schedule.getStation().getName()
                    };
                    tableModel.addRow(row);
                }
            }
            if (this.frame != null) {
                this.frame.repaint();
                this.frame.revalidate();
            } else if (this.mainPanel != null) {
                this.mainPanel.repaint();
                this.mainPanel.revalidate();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur lors de la récupération des données du planning : " + e.getMessage(), "Erreur de chargement", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void openAddScheduleDialog() {
        System.out.println("Méthode openAddScheduleDialog appelée !");
        MainInterfaceFrame.styleDialogUIComponents();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel trainIdLabel = new JLabel("Train:");
        trainIdLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        trainIdLabel.setForeground(TEXT_COLOR);
        trainIdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(trainIdLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JComboBox<String> trainComboBox = new JComboBox<>();
        trainComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        trainComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(trainComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        try {
            TrainService trainService;
            if (this.frame != null) {
                trainService = new TrainService(this.frame.getNetworkConfig());
            } else {
                trainService = new TrainService(this.service.getNetworkConfig());
            }
            
            Trains trains = trainService.selectTrains();
            if (trains != null && trains.getTrains() != null) {
                for (Train train : trains.getTrains()) {
                    trainComboBox.addItem("Train #" + train.getId());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Erreur lors du chargement des trains : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }

        JLabel tripIdLabel = new JLabel("ID Trajet:");
        tripIdLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        tripIdLabel.setForeground(TEXT_COLOR);
        tripIdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(tripIdLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField tripIdField = new JTextField();
        tripIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        tripIdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(tripIdField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel departureTimeLabel = new JLabel("Heure de départ (HH:mm):");
        departureTimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        departureTimeLabel.setForeground(TEXT_COLOR);
        departureTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(departureTimeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField departureTimeField = new JTextField();
        departureTimeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        departureTimeField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(departureTimeField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel arrivalTimeLabel = new JLabel("Heure d'arrivée (HH:mm):");
        arrivalTimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        arrivalTimeLabel.setForeground(TEXT_COLOR);
        arrivalTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(arrivalTimeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField arrivalTimeField = new JTextField();
        arrivalTimeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        arrivalTimeField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(arrivalTimeField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel stationsLabel = new JLabel("Stations desservies:");
        stationsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stationsLabel.setForeground(TEXT_COLOR);
        stationsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(stationsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        String[] stations = {"POSE", "JASM", "TROC", "BONO", "STSD", "NATN", "MAMO"};
        
        JPanel checkBoxPanel = new JPanel(new GridLayout(0, 1));
        checkBoxPanel.setBackground(Color.WHITE);
        
        Map<String, JCheckBox> checkBoxMap = new HashMap<>();
        
        for (String station : stations) {
            JCheckBox checkBox = new JCheckBox(station);
            checkBox.setBackground(Color.WHITE);
            checkBoxPanel.add(checkBox);
            checkBoxMap.put(station, checkBox);
        }
        
        JScrollPane scrollPane = new JScrollPane(checkBoxPanel);
        scrollPane.setPreferredSize(new Dimension(200, 150));
        
        JButton stationsButton = new JButton("Sélectionner");
        stationsButton.setBackground(PRIMARY_COLOR);
        stationsButton.setForeground(Color.BLACK);
        stationsButton.setFocusPainted(false);
        stationsButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        stationsButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(scrollPane);
        
        stationsButton.addActionListener(e -> {
            popupMenu.show(stationsButton, 0, stationsButton.getHeight());
        });
        
        panel.add(stationsButton);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Boutons en gris foncé
        UIManager.put("Button.background", new Color(70, 70, 70));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 12));
        
        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Ajouter un trajet",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
                
        // Restaurer UI
        UIManager.put("Button.background", null);
        UIManager.put("Button.foreground", null);
        UIManager.put("Button.font", null);

        if (result == JOptionPane.OK_OPTION) {
            String selectedTrain = (String) trainComboBox.getSelectedItem();
            String tripId = tripIdField.getText();
            String departureTime = departureTimeField.getText();
            String arrivalTime = arrivalTimeField.getText();

            List<String> selectedStations = new ArrayList<>();
            for (Map.Entry<String, JCheckBox> entry : checkBoxMap.entrySet()) {
                if (entry.getValue().isSelected()) {
                    selectedStations.add(entry.getKey());
                }
            }

            if (selectedTrain != null && !tripId.isEmpty() && !departureTime.isEmpty() && !arrivalTime.isEmpty() && !selectedStations.isEmpty()) {
                try {
                    int trainId = Integer.parseInt(selectedTrain.replace("Train #", ""));
                    
                    Schedules schedules = new Schedules();
                    
                    for (String stationName : selectedStations) {
                        Schedule newSchedule = new Schedule();
                        newSchedule.setTrip(new Trip(Integer.parseInt(tripId), new Train(trainId)));
                        newSchedule.setStation(new Station(stationName));

                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        java.util.Date departureDate = timeFormat.parse(departureTime);
                        java.util.Date arrivalDate = timeFormat.parse(arrivalTime);
                        newSchedule.setTimeDeparture(new Time(departureDate.getTime()));
                        newSchedule.setTimeArrival(new Time(arrivalDate.getTime()));
                        
                        schedules.add(newSchedule);
                    }

                    service.insertSchedules(schedules);
                    
                    if (this.frame != null) {
                        refreshScheduleData();
                    }
                    
                    JOptionPane.showMessageDialog(null,
                            "Horaires ajoutés avec succès pour " + selectedStations.size() + " station(s)",
                            "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Erreur lors de l'ajout des horaires : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                String message = "Veuillez remplir tous les champs";
                if (selectedStations.isEmpty()) {
                    message = "Veuillez sélectionner au moins une station";
                }
                JOptionPane.showMessageDialog(null,
                        message,
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

        MainInterfaceFrame.resetDialogUIComponents();
    }

    private void openEditScheduleDialog() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null,
                    "Veuillez sélectionner un horaire à modifier.",
                    "Aucun horaire sélectionné!",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        MainInterfaceFrame.styleDialogUIComponents();

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

        // Boutons en gris foncé
        UIManager.put("Button.background", new Color(70, 70, 70));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 12));
        
        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Modifier un horaire",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
                
        // Restaurer UI
        UIManager.put("Button.background", null);
        UIManager.put("Button.foreground", null);
        UIManager.put("Button.font", null);

        if (result == JOptionPane.OK_OPTION) {
            String scheduleDatetime = datetimeField.getText();
            String scheduleStop = stopComboBox.getSelectedItem().toString();

            if (!scheduleDatetime.isEmpty()) {
                try {
                    Schedule updatedSchedule = new Schedule();
                    updatedSchedule.setId(scheduleId);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    updatedSchedule.setTimeArrival(new Time(dateFormat.parse(scheduleDatetime).getTime()));

                    boolean isStop = "Oui".equals(scheduleStop);
                    this.service.UpdateSchedule(scheduleId, isStop);

                    refreshScheduleData();

                    JOptionPane.showMessageDialog(null, "L'horaire a été modifié avec succès!", "Modification réussie!", JOptionPane.INFORMATION_MESSAGE);

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Vous avez une erreur! : " + ex.getMessage(), "Erreur!", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(null, "La date/heure ne peut pas être vide.", "Erreur", JOptionPane.WARNING_MESSAGE);
            }
        }

        MainInterfaceFrame.resetDialogUIComponents();
    }

    private void deleteSchedule() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null,
                    "Veuillez sélectionner un horaire à supprimer.",
                    "Aucun horaire sélectionné",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);

        boolean confirmed = false;
        if (this.frame != null) {
            confirmed = this.frame.showConfirmDialog("Confirmer la suppression",
                    "Voulez-vous vraiment supprimer l'horaire #" + scheduleId + " ?");
        } else {
            confirmed = JOptionPane.showConfirmDialog(null,
                    "Voulez-vous vraiment supprimer l'horaire #" + scheduleId + " ?",
                    "Confirmer la suppression",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }

        if (confirmed) {
            try {
                this.service.deleteSchedule(scheduleId);

                refreshScheduleData();

                JOptionPane.showMessageDialog(null, "L'horaire a été supprimé avec succès!", "Suppression réussie", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Erreur lors de la suppression de l'horaire : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }
}
