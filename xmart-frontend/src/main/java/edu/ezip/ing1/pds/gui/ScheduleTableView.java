package edu.ezip.ing1.pds.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import edu.ezip.ing1.pds.business.dto.Schedule;
import edu.ezip.ing1.pds.business.dto.Schedules;
import edu.ezip.ing1.pds.business.dto.Station;
import edu.ezip.ing1.pds.business.dto.Stations;
import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.business.dto.Trains;
import edu.ezip.ing1.pds.business.dto.Trip;
import edu.ezip.ing1.pds.services.ScheduleService;
import edu.ezip.ing1.pds.services.StationService;
import edu.ezip.ing1.pds.services.TrainService;

public class ScheduleTableView {

    private MainInterfaceFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private final ScheduleService service;


    public ScheduleTableView(MainInterfaceFrame frame) {
        this.frame = frame;
        this.frame.setTitle("Gestion des trajets de trains");
        this.frame.getMainContentPanel().removeAll();

        createStyledTable();

        JPanel tablePanel = this.frame.getMainContentPanel();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        List<JButton> buttons = new ArrayList<>();

        JButton addButton = MainInterfaceFrame.createButton("Ajouter un trajet", MainInterfaceFrame.SUCCESS_COLOR);
        addButton.addActionListener(e -> openAddScheduleDialog());
        buttons.add(addButton);

        JButton editButton = MainInterfaceFrame.createButton("Modifier trajet", MainInterfaceFrame.PRIMARY_COLOR);
        editButton.addActionListener(e -> openEditScheduleDialog());
        buttons.add(editButton);

        JButton deleteButton = MainInterfaceFrame.createButton("Supprimer trajet", MainInterfaceFrame.ACCENT_COLOR);
        deleteButton.addActionListener(e -> deleteSchedule());
        buttons.add(deleteButton);

        JButton refreshButton = MainInterfaceFrame.createButton("Actualiser", MainInterfaceFrame.REFRESH_BTN_COLOR);
        refreshButton.addActionListener(e -> refreshScheduleData());
        buttons.add(refreshButton);

        this.frame.registerJButtons(buttons);

        this.service = new ScheduleService(this.frame.getNetworkConfig());
        this.refreshScheduleData();
    }

    private void createStyledTable() {
        String[] columnNames = {
            "ID Train",
            "ID Trajet",
            "Stations desservies",
            "Date/Heure"
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
                    comp.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 245));
                    comp.setForeground(MainInterfaceFrame.TEXT_COLOR);
                } else {
                    comp.setBackground(
                            new Color(MainInterfaceFrame.PRIMARY_COLOR.getRed(), MainInterfaceFrame.PRIMARY_COLOR.getGreen(), MainInterfaceFrame.PRIMARY_COLOR.getBlue(), 80));
                    comp.setForeground(Color.BLACK);
                }

                // Ajouter une bordure à la cellule pour plus de visibilité
                ((JComponent) comp).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(2, 5, 2, 5)
                ));

                return comp;
            }
        };

        table.setRowHeight(35); // Augmenter la hauteur des lignes
        table.setIntercellSpacing(new Dimension(2, 2)); // Espace entre les cellules
        table.setGridColor(new Color(180, 180, 180));
        table.setSelectionBackground(
                new Color(MainInterfaceFrame.PRIMARY_COLOR.getRed(), MainInterfaceFrame.PRIMARY_COLOR.getGreen(), MainInterfaceFrame.PRIMARY_COLOR.getBlue(), 100));
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(true); // Afficher la grille
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(true);
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
        header.setBackground(MainInterfaceFrame.TABLE_HEADER_COLOR);
        header.setForeground(Color.BLACK);
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.DARK_GRAY));
        header.setPreferredSize(new Dimension(0, 40));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setFont(new Font("Arial", Font.PLAIN, 12));

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Définir des largeurs spécifiques pour certaines colonnes
        if (table.getColumnCount() >= 4) {
            table.getColumnModel().getColumn(0).setPreferredWidth(80); // ID Train
            table.getColumnModel().getColumn(1).setPreferredWidth(80); // ID Trajet
            table.getColumnModel().getColumn(2).setPreferredWidth(150); // Stations desservies
            table.getColumnModel().getColumn(3).setPreferredWidth(170); // Date/Heure
        }

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private void refreshScheduleData() {
        try {
            tableModel.setRowCount(0);

            Schedules schedules = this.service.selectSchedules();

            if (schedules != null && schedules.getSchedules() != null) {
                for (Schedule schedule : schedules.getSchedules()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = schedule.getTimeArrival() != null
                            ? dateFormat.format(schedule.getTimeArrival())
                            : "";

                    String stationName = schedule.getStation() != null ? schedule.getStation().getName() : "-";
                    int trainId = schedule.getTrip() != null && schedule.getTrip().getTrain() != null ? 
                                 schedule.getTrip().getTrain().getId() : -1;

                    Object[] row = {
                        trainId,
                        schedule.getTrip().getId(),
                        stationName,
                        formattedDate
                    };
                    tableModel.addRow(row);
                }
            }
            this.frame.repaint();
            this.frame.revalidate();
        } catch (Exception e) {
            this.frame.showErrorDialog(e, "Erreur de chargement",
                    "Erreur lors de la récupération des données du planning");
        }
    }

    private List<Station> getStations() {
        List<Station> stationsList = new ArrayList<>();
        try {
            StationService stationService = new StationService(this.frame.getNetworkConfig());
            Stations stations = stationService.selectStations();
            if (stations != null && stations.getStations() != null && !stations.getStations().isEmpty()) {
                stationsList.addAll(stations.getStations());
               
            } else {
               
                this.frame.showWarningDialog("Attention", "Aucune station n'a été trouvée dans la base de données.");
            }
        } catch (Exception ex) {
            
            ex.printStackTrace();
            this.frame.showErrorDialog(ex, "Erreur", "Erreur lors de la récupération des stations depuis la base de données");
        }
        return stationsList;
    }

    private void openAddScheduleDialog() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Ajouter un nouveau trajet");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Liste déroulante des trains
        JLabel trainLabel = new JLabel("Train:");
        trainLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        trainLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        trainLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(trainLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JComboBox<String> trainComboBox = new JComboBox<>();
        trainComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        trainComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Récupération des trains
        List<Train> trainList = new ArrayList<>();
        try {
            TrainService trainService = new TrainService(this.frame.getNetworkConfig());
            Trains trains = trainService.selectTrains();
            if (trains != null && trains.getTrains() != null) {
                for (Train train : trains.getTrains()) {
                    trainList.add(train);
                    trainComboBox.addItem("Train " + train.getId());
                }
            }
        } catch (Exception ex) {
            this.frame.showErrorDialog(ex, "Erreur", "Erreur lors de la récupération des trains");
        }
        
        panel.add(trainComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Date/heure avec un calendrier
        JLabel datetimeLabel = new JLabel("Date/heure:");
        datetimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        datetimeLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        datetimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(datetimeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Utilisation d'un JSpinner avec un modèle de date
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd HH:mm:ss");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        dateSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(dateSpinner);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Stations desservies
        JLabel stationsLabel = new JLabel("Stations desservies:");
        stationsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stationsLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        stationsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(stationsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Liste des stations avec cases à cocher
        JPanel stationsPanel = new JPanel();
        stationsPanel.setLayout(new BoxLayout(stationsPanel, BoxLayout.Y_AXIS));
        stationsPanel.setBackground(Color.WHITE);
        stationsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        stationsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane stationsScrollPane = new JScrollPane(stationsPanel);
        stationsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        stationsScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        stationsScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        // Liste des stations à cocher
        List<JCheckBox> stationCheckboxes = new ArrayList<>();
        List<Station> stationsList = getStations();
        
        for (Station station : stationsList) {
            JCheckBox checkbox = new JCheckBox(station.getName());
            checkbox.setBackground(Color.WHITE);
            stationCheckboxes.add(checkbox);
            stationsPanel.add(checkbox);
        }
        
        panel.add(stationsScrollPane);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // ID Trajet
        JLabel tripLabel = new JLabel("ID Trajet:");
        tripLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        tripLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
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
                "Ajouter un trajet",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int selectedTrainIndex = trainComboBox.getSelectedIndex();
            Date selectedDate = dateModel.getDate();
            String tripId = tripField.getText();
            List<Station> selectedStations = new ArrayList<>();

            // Récupération des stations sélectionnées
            for (int i = 0; i < stationCheckboxes.size(); i++) {
                if (stationCheckboxes.get(i).isSelected()) {
                    Station station = new Station(stationCheckboxes.get(i).getText());
                    selectedStations.add(station);
                }
            }

            if (selectedTrainIndex != -1 && !tripId.isEmpty() && !selectedStations.isEmpty()) {
                try {
                    Schedule newSchedule = new Schedule();

                    // Définir l'heure d'arrivée
                    newSchedule.setTimeArrival(new Time(selectedDate.getTime()));
                    
                    // Définir le trajet avec le train sélectionné
                    Train selectedTrain = trainList.get(selectedTrainIndex);
                    Trip trip = new Trip(Integer.parseInt(tripId), selectedTrain);
                    newSchedule.setTrip(trip);
                    
                    // Pour chaque station sélectionnée, créer un horaire
                    Schedules schedules = new Schedules();
                    for (Station station : selectedStations) {
                        Schedule stationSchedule = new Schedule();
                        stationSchedule.setTimeArrival(new Time(selectedDate.getTime()));
                        stationSchedule.setTrip(trip);
                        stationSchedule.setStation(station);
                        schedules.add(stationSchedule);
                    }

                    this.service.insertSchedules(schedules);
                    refreshScheduleData();
                    this.frame.showSuccessDialog("Parfait!", "Le trajet a été ajouté avec succès!");

                } catch (Exception ex) {
                    this.frame.showErrorDialog(ex, "Erreur!!", "Vous avez une erreur lors de l'ajout du trajet!!");
                }
            } else {
                this.frame.showWarningDialog("Erreur!", "Il faut remplir tous les champs et sélectionner au moins une station!");
            }
        }
    }

    private void openEditScheduleDialog() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            this.frame.showWarningDialog("Aucun trajet sélectionné!",
                    "Veuillez sélectionner un trajet à modifier.");
            return;
        }

        // Les colonnes ont changé, donc nous récupérons les données différemment
        int trainId = (int) tableModel.getValueAt(selectedRow, 0);
        int tripId = (int) tableModel.getValueAt(selectedRow, 1);
        String stationName = (String) tableModel.getValueAt(selectedRow, 2);
        String currentDatetime = (String) tableModel.getValueAt(selectedRow, 3);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Modifier le trajet");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Liste déroulante des trains
        JLabel trainLabel = new JLabel("Train:");
        trainLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        trainLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        trainLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(trainLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JComboBox<String> trainComboBox = new JComboBox<>();
        trainComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        trainComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Récupération des trains
        List<Train> trainList = new ArrayList<>();
        int selectedTrainIndex = -1;
        try {
            TrainService trainService = new TrainService(this.frame.getNetworkConfig());
            Trains trains = trainService.selectTrains();
            if (trains != null && trains.getTrains() != null) {
                int index = 0;
                for (Train train : trains.getTrains()) {
                    trainList.add(train);
                    trainComboBox.addItem("Train " + train.getId());
                    if (train.getId() == trainId) {
                        selectedTrainIndex = index;
                    }
                    index++;
                }
                if (selectedTrainIndex >= 0) {
                    trainComboBox.setSelectedIndex(selectedTrainIndex);
                }
            }
        } catch (Exception ex) {
            this.frame.showErrorDialog(ex, "Erreur", "Erreur lors de la récupération des trains");
        }
        
        panel.add(trainComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Date/heure avec un calendrier
        JLabel datetimeLabel = new JLabel("Date/heure:");
        datetimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        datetimeLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        datetimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(datetimeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Utilisation d'un JSpinner avec un modèle de date
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd HH:mm:ss");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        dateSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Définir la date actuelle
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parsedDate = dateFormat.parse(currentDatetime);
            dateModel.setValue(parsedDate);
        } catch (Exception ex) {
            // En cas d'erreur, utiliser la date actuelle
            dateModel.setValue(new Date());
        }
        
        panel.add(dateSpinner);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Stations desservies
        JLabel stationsLabel = new JLabel("Stations desservies:");
        stationsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stationsLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        stationsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(stationsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Liste des stations avec cases à cocher
        JPanel stationsPanel = new JPanel();
        stationsPanel.setLayout(new BoxLayout(stationsPanel, BoxLayout.Y_AXIS));
        stationsPanel.setBackground(Color.WHITE);
        stationsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        stationsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane stationsScrollPane = new JScrollPane(stationsPanel);
        stationsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        stationsScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        stationsScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // Liste des stations à cocher
        List<JCheckBox> stationCheckboxes = new ArrayList<>();
        List<Station> stationsList = getStations();
        
        for (Station station : stationsList) {
            JCheckBox checkbox = new JCheckBox(station.getName());
            checkbox.setBackground(Color.WHITE);
            // Présélectionner la station actuelle
            if (station.getName().equals(stationName)) {
                checkbox.setSelected(true);
            }
            stationCheckboxes.add(checkbox);
            stationsPanel.add(checkbox);
        }
        
        panel.add(stationsScrollPane);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Champ pour ID Trajet
        JLabel tripIdLabel = new JLabel("ID Trajet:");
        tripIdLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        tripIdLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        tripIdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(tripIdLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        JTextField tripIdField = new JTextField(String.valueOf(tripId));
        tripIdField.setEditable(false);  // L'ID du trajet n'est pas modifiable
        tripIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        tripIdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(tripIdField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        int result = JOptionPane.showConfirmDialog(
                this.frame,
                panel,
                "Modifier un trajet",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int selectedTrainIdxNew = trainComboBox.getSelectedIndex();
            Date selectedDate = dateModel.getDate();
            List<Station> selectedStations = new ArrayList<>();

            // Récupération des stations sélectionnées
            for (int i = 0; i < stationCheckboxes.size(); i++) {
                if (stationCheckboxes.get(i).isSelected()) {
                    Station station = new Station(stationCheckboxes.get(i).getText());
                    selectedStations.add(station);
                }
            }

            if (selectedTrainIdxNew != -1 && !selectedStations.isEmpty()) {
                try {
                    // Définir l'horaire mis à jour
                    Schedule updatedSchedule = new Schedule();
                    updatedSchedule.setTimeArrival(new Time(selectedDate.getTime()));

                    // Récupérer le train sélectionné
                    Train selectedTrain = trainList.get(selectedTrainIdxNew);
                    Trip trip = new Trip(tripId, selectedTrain);
                    updatedSchedule.setTrip(trip);
                    
                    // Mettre à jour l'horaire pour chaque station sélectionnée
                    for (Station station : selectedStations) {
                        updatedSchedule.setStation(station);
                        this.service.UpdateSchedule(tripId, true); // Nous utilisons tripId comme identifiant
                    }

                    refreshScheduleData();
                    this.frame.showSuccessDialog("Modification réussie!", "Le trajet a été modifié avec succès!");

                } catch (Exception ex) {
                    this.frame.showErrorDialog(ex, "Erreur!", "Vous avez une erreur lors de la modification du trajet!");
                }
            } else {
                this.frame.showWarningDialog("Erreur", "Veuillez sélectionner un train et au moins une station.");
            }
        }
    }

    private void deleteSchedule() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            this.frame.showWarningDialog("Aucun trajet sélectionné",
                    "Veuillez sélectionner un trajet à supprimer.");
            return;
        }

        int tripId = (int) tableModel.getValueAt(selectedRow, 1);

        if (this.frame.showConfirmDialog("Confirmer la suppression",
                "Voulez-vous vraiment supprimer le trajet #" + tripId + " ?")) {
            try {
                this.service.deleteSchedule(tripId);

                refreshScheduleData();

                this.frame.showSuccessDialog("Suppression réussie", "Le trajet a été supprimé avec succès!");

            } catch (Exception ex) {
                this.frame.showErrorDialog(ex, "Erreur", "Erreur lors de la suppression du trajet");
            }
        }
    }
}
