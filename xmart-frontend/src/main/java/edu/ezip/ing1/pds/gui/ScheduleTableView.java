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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
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
import edu.ezip.ing1.pds.business.dto.Trips;
import edu.ezip.ing1.pds.services.ScheduleService;
import edu.ezip.ing1.pds.services.StationService;
import edu.ezip.ing1.pds.services.TrainService;
import edu.ezip.ing1.pds.services.TripService;

public class ScheduleTableView {

    private MainInterfaceFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private final ScheduleService service;
    private final TripService tripService;
    private Map<Integer, List<Station>> tripStations = new HashMap<>();

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
        this.tripService = new TripService(this.frame.getNetworkConfig());
        this.refreshScheduleData();
    }

    private void createStyledTable() {
        String[] columnNames = {
            "ID Train",
            "ID Trajet",
            "Stations desservies",
            "Heures d'arrivée",
            "Direction"
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

                ((JComponent) comp).setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(200, 200, 200)),
                    BorderFactory.createEmptyBorder(2, 5, 2, 5)
                ));

                return comp;
            }
        };

        table.setRowHeight(35);
        table.setIntercellSpacing(new Dimension(2, 2));
        table.setGridColor(new Color(180, 180, 180));
        table.setSelectionBackground(
                new Color(MainInterfaceFrame.PRIMARY_COLOR.getRed(), MainInterfaceFrame.PRIMARY_COLOR.getGreen(), MainInterfaceFrame.PRIMARY_COLOR.getBlue(), 100));
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(true);
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

        if (table.getColumnCount() >= 5) {
            table.getColumnModel().getColumn(0).setPreferredWidth(70); 
            table.getColumnModel().getColumn(1).setPreferredWidth(70); 
            table.getColumnModel().getColumn(2).setPreferredWidth(150); 
            table.getColumnModel().getColumn(3).setPreferredWidth(150);
            table.getColumnModel().getColumn(4).setPreferredWidth(80);
        }

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    }

    private void refreshScheduleData() {
        try {
            tableModel.setRowCount(0);

            // Récupérer les schedules pour avoir les horaires
            Schedules schedules = this.service.selectSchedules();
            Map<Integer, List<Schedule>> schedulesByTrip = new HashMap<>();

            if (schedules != null && schedules.getSchedules() != null) {
                for (Schedule schedule : schedules.getSchedules()) {
                    int tripId = schedule.getTrip().getId();
                    if (!schedulesByTrip.containsKey(tripId)) {
                        schedulesByTrip.put(tripId, new ArrayList<>());
                    }
                    schedulesByTrip.get(tripId).add(schedule);
                }
            }
            
            // Récupérer tous les Trip directement
            Trips trips = this.tripService.selectTrips();
            
            if (trips != null && trips.getTrips() != null) {
                for (Trip trip : trips.getTrips()) {
                    int trainId = trip.getTrain() != null ? trip.getTrain().getId() : -1;
                    int tripId = trip.getId();
                    
                    // Déterminer la direction (aller/retour) en fonction de l'ordre des stations
                    String direction = "Non défini";
                    
                    // Récupérer les stations et les heures d'arrivée
                    String stationsStr = "À définir";
                    String timesStr = "Non planifié";
                    
                    List<Schedule> tripSchedules = schedulesByTrip.get(tripId);
                    if (tripSchedules != null && !tripSchedules.isEmpty()) {
                        // Trier les schedules par heure d'arrivée
                        tripSchedules.sort((s1, s2) -> s1.getTimeArrival().compareTo(s2.getTimeArrival()));
                        
                        // Construire la liste des stations
                        StringBuilder stationsBuilder = new StringBuilder();
                        StringBuilder timesBuilder = new StringBuilder();
                        
                        for (Schedule sch : tripSchedules) {
                            if (stationsBuilder.length() > 0) {
                                stationsBuilder.append(" → ");
                                timesBuilder.append(", ");
                            }
                            stationsBuilder.append(sch.getStation().getName());
                            
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                            String arrivalTime = sch.getTimeArrival() != null ? 
                                    timeFormat.format(sch.getTimeArrival()) : "?";
                            timesBuilder.append(arrivalTime);
                        }
                        
                        stationsStr = stationsBuilder.toString();
                        timesStr = timesBuilder.toString();
                        
                        // Déterminer la direction en fonction de l'ordre des stations
                        if (tripSchedules.size() >= 2) {
                            Station firstStation = tripSchedules.get(0).getStation();
                            Station lastStation = tripSchedules.get(tripSchedules.size() - 1).getStation();
                            
                            // Logique simple: si la première station est alphabétiquement avant la dernière, c'est aller
                            if (firstStation.getName().compareTo(lastStation.getName()) < 0) {
                                direction = "Aller";
                            } else {
                                direction = "Retour";
                            }
                        }
                    } else {
                        // Utiliser les données locales si disponibles
                        List<Station> stations = tripStations.get(tripId);
                        if (stations != null && !stations.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (Station station : stations) {
                                if (sb.length() > 0) {
                                    sb.append(" → ");
                                }
                                sb.append(station.getName());
                            }
                            stationsStr = sb.toString();
                            
                            // Déterminer la direction en fonction de l'ordre des stations
                            if (stations.size() >= 2) {
                                Station firstStation = stations.get(0);
                                Station lastStation = stations.get(stations.size() - 1);
                                
                                if (firstStation.getName().compareTo(lastStation.getName()) < 0) {
                                    direction = "Aller";
                                } else {
                                    direction = "Retour";
                                }
                            }
                        }
                    }

                    Object[] row = {
                        trainId,
                        tripId,
                        stationsStr,
                        timesStr,
                        direction
                    };
                    tableModel.addRow(row);
                }
            }
            
            this.frame.repaint();
            this.frame.revalidate();
        } catch (Exception e) {
            e.printStackTrace();
            this.frame.showErrorDialog(e, "Erreur de chargement",
                    "Erreur lors de la récupération des données des trajets");
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
                this.frame.showWarningDialog("Attention", "Aucune station dans la base!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            this.frame.showErrorDialog(ex, "Erreur", "Erreur lors de la récupération des stations ");
        }
        return stationsList;
    }

    private JPanel createFormPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        return panel;
    }

    private void openAddScheduleDialog() {
        JPanel panel = createFormPanel("Ajouter un nouveau trajet");
        panel.setPreferredSize(new Dimension(500, 600)); // Agrandir le formulaire
        
        JLabel trainLabel = createFormLabel("Train:");
        panel.add(trainLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JComboBox<String> trainComboBox = new JComboBox<>();
        trainComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        trainComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        trainComboBox.setFont(new Font("Arial", Font.PLAIN, 14)); // Augmenter la police
        
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
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Plus d'espace
        
        JLabel directionLabel = createFormLabel("Sens de circulation:");
        panel.add(directionLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel directionPanel = new JPanel();
        directionPanel.setLayout(new BoxLayout(directionPanel, BoxLayout.X_AXIS));
        directionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35)); // Plus grand
        directionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        directionPanel.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
        
        ButtonGroup directionGroup = new ButtonGroup();
        JRadioButton allerRadio = new JRadioButton("Aller");
        JRadioButton retourRadio = new JRadioButton("Retour");
        allerRadio.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
        retourRadio.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
        allerRadio.setFont(new Font("Arial", Font.PLAIN, 14)); // Augmenter la police
        retourRadio.setFont(new Font("Arial", Font.PLAIN, 14)); // Augmenter la police
        allerRadio.setSelected(true);  // Par défaut sélectionné
        
        directionGroup.add(allerRadio);
        directionGroup.add(retourRadio);
        directionPanel.add(allerRadio);
        directionPanel.add(Box.createRigidArea(new Dimension(50, 0))); // Plus d'espace
        directionPanel.add(retourRadio);
        
        panel.add(directionPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Plus d'espace
        
        JLabel tripLabel = createFormLabel("ID Trajet:");
        panel.add(tripLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField tripField = new JTextField();
        tripField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35)); // Plus grand
        tripField.setAlignmentX(Component.LEFT_ALIGNMENT);
        tripField.setFont(new Font("Arial", Font.PLAIN, 14)); // Augmenter la police
        panel.add(tripField);
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Plus d'espace
        
        JLabel stationsLabel = createFormLabel("Stations desservies:");
        panel.add(stationsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Création d'un panel pour contenir la liste des stations et leurs horaires
        JPanel stationsMainPanel = new JPanel();
        stationsMainPanel.setLayout(new BoxLayout(stationsMainPanel, BoxLayout.Y_AXIS));
        stationsMainPanel.setBackground(Color.WHITE);
        stationsMainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane stationsScrollPane = new JScrollPane(stationsMainPanel);
        stationsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        stationsScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350)); // Plus grand pour accommoder les horaires
        stationsScrollPane.setPreferredSize(new Dimension(450, 350)); // Définir une taille préférée
        stationsScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        List<Station> stationsList = getStations();
        List<JCheckBox> stationCheckboxes = new ArrayList<>();
        Map<JCheckBox, JPanel> stationTimesPanels = new HashMap<>(); // Pour stocker les panels d'horaires par station
        Map<JCheckBox, JSpinner> arrivalTimeSpinners = new HashMap<>(); // Pour stocker les spinners d'heure d'arrivée
        
        // Créer un modèle de temps par défaut pour les spinners
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 8); // Commencer à 8h00 par défaut
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date initDate = cal.getTime();
        SpinnerDateModel timeModel = new SpinnerDateModel(initDate, null, null, Calendar.MINUTE);
        
        for (Station station : stationsList) {
            // Panel pour chaque station avec checkbox et horaires
            JPanel stationPanel = new JPanel();
            stationPanel.setLayout(new BoxLayout(stationPanel, BoxLayout.Y_AXIS));
            stationPanel.setBackground(Color.WHITE);
            stationPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            stationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Checkbox pour la station
            JCheckBox checkbox = new JCheckBox(station.getName());
            checkbox.setBackground(Color.WHITE);
            checkbox.setFont(new Font("Arial", Font.PLAIN, 14));
            checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
            stationCheckboxes.add(checkbox);
            
            stationPanel.add(checkbox);
            
            // Panel pour les horaires (initialement invisible)
            JPanel timePanel = new JPanel();
            timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
            timePanel.setBackground(Color.WHITE);
            timePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            timePanel.setVisible(false); // Caché par défaut
            stationTimesPanels.put(checkbox, timePanel);
            
            // Label pour l'heure d'arrivée
            JLabel arrivalLabel = new JLabel("Heure d'arrivée: ");
            arrivalLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            timePanel.add(arrivalLabel);
            
            // Spinner pour l'heure d'arrivée
            JSpinner arrivalSpinner = new JSpinner(new SpinnerDateModel(initDate, null, null, Calendar.MINUTE));
            JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(arrivalSpinner, "HH:mm");
            arrivalSpinner.setEditor(timeEditor);
            arrivalSpinner.setPreferredSize(new Dimension(80, 25));
            arrivalSpinner.setMaximumSize(new Dimension(80, 25));
            timePanel.add(arrivalSpinner);
            arrivalTimeSpinners.put(checkbox, arrivalSpinner);
            
            // Ajouter un peu d'espace entre les éléments
            timePanel.add(Box.createHorizontalStrut(10));
            
            // Ajouter le panel d'horaires au panel de la station
            stationPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            stationPanel.add(timePanel);
            stationPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            
            // Ajouter le panel de la station au panel principal
            stationsMainPanel.add(stationPanel);
            
            // Ajouter un listener pour afficher/masquer les horaires
            checkbox.addActionListener(e -> {
                timePanel.setVisible(checkbox.isSelected());
                stationsMainPanel.revalidate();
                stationsMainPanel.repaint();
            });
        }
        
        panel.add(stationsScrollPane);
        panel.add(Box.createRigidArea(new Dimension(0, 20))); // Plus d'espace
        
        // Bouton pour calculer automatiquement les horaires
        JButton autoScheduleButton = new JButton("Calculer horaires automatiquement");
        autoScheduleButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        autoScheduleButton.setFont(new Font("Arial", Font.PLAIN, 14));
        autoScheduleButton.addActionListener(e -> {
            // Trouver toutes les stations sélectionnées
            List<JCheckBox> selectedCheckboxes = stationCheckboxes.stream()
                    .filter(JCheckBox::isSelected)
                    .collect(Collectors.toList());
            
            if (selectedCheckboxes.size() > 0) {
                // Récupérer l'heure de la première station comme base
                Date firstTime = (Date) arrivalTimeSpinners.get(selectedCheckboxes.get(0)).getValue();
                Calendar baseCal = Calendar.getInstance();
                baseCal.setTime(firstTime);
                
                // Calculer les horaires pour les autres stations (espacés de 5 minutes)
                for (int i = 1; i < selectedCheckboxes.size(); i++) {
                    JCheckBox cb = selectedCheckboxes.get(i);
                    JSpinner spinner = arrivalTimeSpinners.get(cb);
                    
                    baseCal.add(Calendar.MINUTE, 5); // Ajouter 5 minutes
                    spinner.setValue(baseCal.getTime());
                }
            }
        });
        panel.add(autoScheduleButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10))); // Plus d'espace

        JOptionPane optionPane = new JOptionPane(
                panel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        
        JDialog dialog = optionPane.createDialog(this.frame, "Ajouter un trajet");
        dialog.setMinimumSize(new Dimension(550, 700)); // Définir une taille minimum plus grande
        dialog.setResizable(true); // Permettre le redimensionnement
        dialog.setVisible(true);
        
        Integer result = (Integer) optionPane.getValue();
        if (result != null && result == JOptionPane.OK_OPTION) {
            int selectedTrainIndex = trainComboBox.getSelectedIndex();
            String tripId = tripField.getText();

            // Récupérer les stations sélectionnées avec leurs horaires
            List<Station> selectedStations = new ArrayList<>();
            Map<Station, Date> stationArrivalTimes = new HashMap<>();
            
            for (int i = 0; i < stationCheckboxes.size(); i++) {
                JCheckBox checkbox = stationCheckboxes.get(i);
                if (checkbox.isSelected()) {
                    Station station = new Station(checkbox.getText());
                    selectedStations.add(station);
                    
                    // Récupérer l'heure d'arrivée
                    Date arrivalTime = (Date) arrivalTimeSpinners.get(checkbox).getValue();
                    stationArrivalTimes.put(station, arrivalTime);
                }
            }

            if (selectedTrainIndex != -1 && !tripId.isEmpty() && !selectedStations.isEmpty()) {
                try {
                    int tripIdInt = Integer.parseInt(tripId);
                    Trip trip = new Trip(tripIdInt, trainList.get(selectedTrainIndex));
                    String direction = allerRadio.isSelected() ? "Aller" : "Retour";
                    
                    // Vérifier que le Trip n'existe pas déjà
                    Trips existingTrips = this.tripService.selectTrips();
                    boolean tripExists = false;
                    
                    if (existingTrips != null && existingTrips.getTrips() != null) {
                        for (Trip existingTrip : existingTrips.getTrips()) {
                            if (existingTrip.getId() == trip.getId()) {
                                tripExists = true;
                                break;
                            }
                        }
                    }
                    
                    if (tripExists) {
                        this.frame.showWarningDialog("Attention", "Un trajet avec cet ID existe déjà. Veuillez choisir un autre ID.");
                        return;
                    }
                    
                    // Insérer le Trip
                    this.tripService.insertTrip(trip);
                    
                    // Créer un schedule pour chaque station avec l'horaire défini
                    Schedules schedules = new Schedules();
                    
                    for (Station station : selectedStations) {
                        Schedule stationSchedule = new Schedule();
                        
                        // Utiliser l'heure d'arrivée définie par l'utilisateur
                        Calendar arrivalCal = Calendar.getInstance();
                        arrivalCal.setTime(stationArrivalTimes.get(station));
                        Time arrivalTime = new Time(arrivalCal.getTimeInMillis());
                        stationSchedule.setTimeArrival(arrivalTime);
                        
                        // Heure de départ = heure d'arrivée + 2 minutes
                        Calendar depCal = (Calendar) arrivalCal.clone();
                        depCal.add(Calendar.MINUTE, 2);
                        Time departureTime = new Time(depCal.getTimeInMillis());
                        stationSchedule.setTimeDeparture(departureTime);
                        
                        stationSchedule.setTrip(trip);
                        stationSchedule.setStation(station);
                        schedules.add(stationSchedule);
                    }
                    
                    // Insérer les schedules dans la base de données
                    this.service.insertSchedules(schedules);
                    
                    // Stocker aussi localement pour l'affichage
                    tripStations.put(tripIdInt, selectedStations);

                    refreshScheduleData();
                    this.frame.showSuccessDialog("Bravo!", "Le trajet a été ajouté avec succès!");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    this.frame.showErrorDialog(ex, "Erreur!!", "Vous avez une erreur lors de l'ajout du trajet : " + ex.getMessage());
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

        int trainId = (int) tableModel.getValueAt(selectedRow, 0);
        int tripId = (int) tableModel.getValueAt(selectedRow, 1);
        String stationsStr = (String) tableModel.getValueAt(selectedRow, 2);
        String timesStr = (String) tableModel.getValueAt(selectedRow, 3);

        // Récupérer les horaires existants pour ce trajet
        Map<String, Time> stationArrivalTimes = new HashMap<>();
        try {
            Schedules schedules = this.service.selectSchedules();
            if (schedules != null && schedules.getSchedules() != null) {
                for (Schedule schedule : schedules.getSchedules()) {
                    if (schedule.getTrip().getId() == tripId) {
                        stationArrivalTimes.put(schedule.getStation().getName(), schedule.getTimeArrival());
                    }
                }
            }
        } catch (Exception ex) {
            this.frame.showErrorDialog(ex, "Erreur", "Erreur lors de la récupération des horaires");
        }

        JPanel panel = createFormPanel("Modifier le trajet");
        panel.setPreferredSize(new Dimension(500, 600)); // Agrandir le formulaire

        JLabel trainLabel = createFormLabel("Train:");
        panel.add(trainLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JComboBox<String> trainComboBox = new JComboBox<>();
        trainComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        trainComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        trainComboBox.setFont(new Font("Arial", Font.PLAIN, 14)); // Augmenter la police
        
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

        JLabel tripIdLabel = createFormLabel("ID Trajet:");
        panel.add(tripIdLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField tripIdField = new JTextField(String.valueOf(tripId));
        tripIdField.setEditable(false);
        tripIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        tripIdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        tripIdField.setFont(new Font("Arial", Font.PLAIN, 14)); // Augmenter la police
        panel.add(tripIdField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel stationsLabel = createFormLabel("Stations desservies:");
        panel.add(stationsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        // Création d'un panel pour contenir la liste des stations et leurs horaires
        JPanel stationsMainPanel = new JPanel();
        stationsMainPanel.setLayout(new BoxLayout(stationsMainPanel, BoxLayout.Y_AXIS));
        stationsMainPanel.setBackground(Color.WHITE);
        stationsMainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane stationsScrollPane = new JScrollPane(stationsMainPanel);
        stationsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        stationsScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350)); // Plus grand pour accommoder les horaires
        stationsScrollPane.setPreferredSize(new Dimension(450, 350)); // Définir une taille préférée
        stationsScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        List<Station> stationsList = getStations();
        List<JCheckBox> stationCheckboxes = new ArrayList<>();
        Map<JCheckBox, JSpinner> arrivalTimeSpinners = new HashMap<>();
        
        // Extraire les noms des stations déjà sélectionnées
        List<String> selectedStationNames = new ArrayList<>();
        if (stationsStr != null && !stationsStr.equals("À définir")) {
            String[] stationNames = stationsStr.split(" → ");
            for (String name : stationNames) {
                selectedStationNames.add(name.trim());
            }
        }
        
        for (Station station : stationsList) {
            // Panel pour chaque station avec checkbox et horaires
            JPanel stationPanel = new JPanel();
            stationPanel.setLayout(new BoxLayout(stationPanel, BoxLayout.Y_AXIS));
            stationPanel.setBackground(Color.WHITE);
            stationPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            stationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Checkbox pour la station
            JCheckBox checkbox = new JCheckBox(station.getName());
            checkbox.setBackground(Color.WHITE);
            checkbox.setFont(new Font("Arial", Font.PLAIN, 14));
            checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Vérifier si la station est déjà sélectionnée
            boolean isSelected = selectedStationNames.contains(station.getName());
            checkbox.setSelected(isSelected);
            
            stationCheckboxes.add(checkbox);
            stationPanel.add(checkbox);
            
            // Panel pour les horaires
            JPanel timePanel = new JPanel();
            timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
            timePanel.setBackground(Color.WHITE);
            timePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            timePanel.setVisible(isSelected); // Visible si la station est sélectionnée
            
            // Label pour l'heure d'arrivée
            JLabel arrivalLabel = new JLabel("Heure d'arrivée: ");
            arrivalLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            timePanel.add(arrivalLabel);
            
            // Spinner pour l'heure d'arrivée
            Calendar cal = Calendar.getInstance();
            
            // Utiliser l'horaire existant si disponible
            if (stationArrivalTimes.containsKey(station.getName())) {
                cal.setTime(stationArrivalTimes.get(station.getName()));
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 8); // Heure par défaut
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
            }
            
            Date initDate = cal.getTime();
            JSpinner arrivalSpinner = new JSpinner(new SpinnerDateModel(initDate, null, null, Calendar.MINUTE));
            JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(arrivalSpinner, "HH:mm");
            arrivalSpinner.setEditor(timeEditor);
            arrivalSpinner.setPreferredSize(new Dimension(80, 25));
            arrivalSpinner.setMaximumSize(new Dimension(80, 25));
            timePanel.add(arrivalSpinner);
            arrivalTimeSpinners.put(checkbox, arrivalSpinner);
            
            // Ajouter le panel d'horaires au panel de la station
            stationPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            stationPanel.add(timePanel);
            stationPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            
            // Ajouter le panel de la station au panel principal
            stationsMainPanel.add(stationPanel);
            
            // Ajouter un listener pour afficher/masquer les horaires
            checkbox.addActionListener(e -> {
                timePanel.setVisible(checkbox.isSelected());
                stationsMainPanel.revalidate();
                stationsMainPanel.repaint();
            });
        }
        
        panel.add(stationsScrollPane);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Bouton pour calculer automatiquement les horaires
        JButton autoScheduleButton = new JButton("Recalculer horaires automatiquement");
        autoScheduleButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        autoScheduleButton.setFont(new Font("Arial", Font.PLAIN, 14));
        autoScheduleButton.addActionListener(e -> {
            // Trouver toutes les stations sélectionnées
            List<JCheckBox> selectedCheckboxes = stationCheckboxes.stream()
                    .filter(JCheckBox::isSelected)
                    .collect(Collectors.toList());
            
            if (selectedCheckboxes.size() > 0) {
                // Récupérer l'heure de la première station comme base
                Date firstTime = (Date) arrivalTimeSpinners.get(selectedCheckboxes.get(0)).getValue();
                Calendar baseCal = Calendar.getInstance();
                baseCal.setTime(firstTime);
                
                // Calculer les horaires pour les autres stations (espacés de 5 minutes)
                for (int i = 1; i < selectedCheckboxes.size(); i++) {
                    JCheckBox cb = selectedCheckboxes.get(i);
                    JSpinner spinner = arrivalTimeSpinners.get(cb);
                    
                    baseCal.add(Calendar.MINUTE, 5); // Ajouter 5 minutes
                    spinner.setValue(baseCal.getTime());
                }
            }
        });
        panel.add(autoScheduleButton);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JOptionPane optionPane = new JOptionPane(
                panel,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        
        JDialog dialog = optionPane.createDialog(this.frame, "Modifier un trajet");
        dialog.setMinimumSize(new Dimension(550, 700)); // Définir une taille minimum plus grande
        dialog.setResizable(true); // Permettre le redimensionnement
        dialog.setVisible(true);
        
        Integer result = (Integer) optionPane.getValue();
        if (result != null && result == JOptionPane.OK_OPTION) {
            int selectedTrainIdxNew = trainComboBox.getSelectedIndex();
            
            // Récupérer les stations sélectionnées avec leurs horaires
            List<Station> selectedStations = new ArrayList<>();
            Map<Station, Date> stationArrivalTimes2 = new HashMap<>();
            
            for (int i = 0; i < stationCheckboxes.size(); i++) {
                JCheckBox checkbox = stationCheckboxes.get(i);
                if (checkbox.isSelected()) {
                    Station station = new Station(checkbox.getText());
                    selectedStations.add(station);
                    
                    // Récupérer l'heure d'arrivée
                    Date arrivalTime = (Date) arrivalTimeSpinners.get(checkbox).getValue();
                    stationArrivalTimes2.put(station, arrivalTime);
                }
            }

            if (selectedTrainIdxNew != -1 && !selectedStations.isEmpty()) {
                try {
                    Trip trip = new Trip(tripId, trainList.get(selectedTrainIdxNew));
                    
                    // Supprimer les anciens schedules
                    this.service.deleteSchedule(tripId);
                    
                    // Créer de nouveaux schedules avec les horaires mis à jour
                    Schedules schedules = new Schedules();
                    
                    for (Station station : selectedStations) {
                        Schedule stationSchedule = new Schedule();
                        
                        // Utiliser l'heure d'arrivée définie par l'utilisateur
                        Calendar arrivalCal = Calendar.getInstance();
                        arrivalCal.setTime(stationArrivalTimes2.get(station));
                        Time arrivalTime = new Time(arrivalCal.getTimeInMillis());
                        stationSchedule.setTimeArrival(arrivalTime);
                        
                        // Heure de départ = heure d'arrivée + 2 minutes
                        Calendar depCal = (Calendar) arrivalCal.clone();
                        depCal.add(Calendar.MINUTE, 2);
                        Time departureTime = new Time(depCal.getTimeInMillis());
                        stationSchedule.setTimeDeparture(departureTime);
                        
                        stationSchedule.setTrip(trip);
                        stationSchedule.setStation(station);
                        schedules.add(stationSchedule);
                    }
                    
                    // Insérer les nouveaux schedules
                    this.service.insertSchedules(schedules);
                    
                    // Mettre à jour le trajet
                    this.tripService.insertTrip(trip);
                    
                    // Stocker aussi localement pour l'affichage
                    tripStations.put(tripId, selectedStations);

                    refreshScheduleData();
                    this.frame.showSuccessDialog("Modification réussie!", "Le trajet a été modifié avec succès!");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    this.frame.showErrorDialog(ex, "Erreur!", "Vous avez une erreur lors de la modification du trajet: " + ex.getMessage());
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
                
                tripStations.remove(tripId);

                refreshScheduleData();

                this.frame.showSuccessDialog("Suppression réussie", "Le trajet a été supprimé avec succès!");

            } catch (Exception ex) {
                this.frame.showErrorDialog(ex, "Erreur", "Erreur lors de la suppression du trajet");
            }
        }
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14)); // Police plus grande et en gras
        label.setForeground(MainInterfaceFrame.TEXT_COLOR);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
}
