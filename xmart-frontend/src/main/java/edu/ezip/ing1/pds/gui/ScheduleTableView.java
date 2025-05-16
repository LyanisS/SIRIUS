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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
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
    private Integer filteredTrainId = null;

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
        
        if (filteredTrainId != null) {
            JButton clearFilterButton = MainInterfaceFrame.createButton("Effacer filtre", new Color(255, 165, 0)); // Orange
            clearFilterButton.addActionListener(e -> clearFilter());
            buttons.add(clearFilterButton);
            
            JPanel filterIndicator = new JPanel(new BorderLayout());
            filterIndicator.setBackground(Color.WHITE);
            JLabel filterLabel = new JLabel("Affichage filtré pour le train " + filteredTrainId);
            filterLabel.setFont(new Font("Arial", Font.BOLD, 14));
            filterLabel.setForeground(new Color(255, 165, 0));
            filterIndicator.add(filterLabel, BorderLayout.NORTH);
            tablePanel.add(filterIndicator, BorderLayout.NORTH);
        }

        this.frame.registerJButtons(buttons);
        
        this.service = new ScheduleService(this.frame.getNetworkConfig());
        this.tripService = new TripService(this.frame.getNetworkConfig());
        this.refreshScheduleData();
    }

    private void createStyledTable() {
        String[] columnNames = {
            "Numéro Train",
            "Numéro Trajet",
            "Stations desservies",
            "Heures d'arrivée",
            "Dernière station"
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
        header.setForeground(Color.BLUE);
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

            if (filteredTrainId != null) {
                // Si un filtre est actif, on applique le filtre
                applyTrainFilter(filteredTrainId);
                return;
            }

            Schedules schedules = this.service.selectSchedules();
            Map<Integer, List<Schedule>> schedulesByTrip = new HashMap<>();

            
            if (schedules != null && schedules.getSchedules() != null) {
                
                Set<Integer> uniqueTripIds = new HashSet<>();
                
                for (Schedule schedule : schedules.getSchedules()) {
                    int tripId = schedule.getTrip().getId();
                    uniqueTripIds.add(tripId);
                    
                    if (!schedulesByTrip.containsKey(tripId)) {
                        schedulesByTrip.put(tripId, new ArrayList<>());
                    }
                    schedulesByTrip.get(tripId).add(schedule);
                }
                
                
                for (Integer tripId : uniqueTripIds) {
                    List<Schedule> tripSchedules = schedulesByTrip.get(tripId);
                    
                    
                    tripSchedules.sort(Comparator.comparing(Schedule::getId).reversed());
                    
                   
                    Map<String, Schedule> uniqueStations = new LinkedHashMap<>();
                    for (Schedule schedule : tripSchedules) {
                        String stationName = schedule.getStation().getName();
                        if (!uniqueStations.containsKey(stationName)) {
                            uniqueStations.put(stationName, schedule);
                        }
                    }
                    
                    
                    List<Schedule> uniqueSchedules = new ArrayList<>(uniqueStations.values());
                    
                    uniqueSchedules.sort(Comparator.comparing(Schedule::getTimeArrival));
                    schedulesByTrip.put(tripId, uniqueSchedules);
                }
            }
            
            Trips trips = this.tripService.selectTrips();
            
            if (trips != null && trips.getTrips() != null) {
                for (Trip trip : trips.getTrips()) {
                    int trainId = trip.getTrain() != null ? trip.getTrain().getId() : -1;
                    int tripId = trip.getId();
                    
                    String direction = "Non défini";
                    String stationsStr = "À définir";
                    String timesStr = "Non planifié";
                    
                    List<Schedule> tripSchedules = schedulesByTrip.get(tripId);
                    if (tripSchedules != null && !tripSchedules.isEmpty()) {
                        tripSchedules.sort((s1, s2) -> s1.getTimeArrival().compareTo(s2.getTimeArrival()));
                        
                        StringBuilder stationsBuilder = new StringBuilder();
                        StringBuilder timesBuilder = new StringBuilder();
                        
                        Set<String> addedStations = new HashSet<>();
                        
                        for (Schedule sch : tripSchedules) {
                            String stationName = sch.getStation().getName();
                            
                            if (!addedStations.contains(stationName)) {
                                if (stationsBuilder.length() > 0) {
                                    stationsBuilder.append(" → ");
                                    timesBuilder.append(", ");
                                }
                                stationsBuilder.append(stationName);
                                addedStations.add(stationName);
                                
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                                String arrivalTime = sch.getTimeArrival() != null ? 
                                        timeFormat.format(sch.getTimeArrival()) : "?";
                                timesBuilder.append(arrivalTime);
                            }
                        }
                        
                        stationsStr = stationsBuilder.toString();
                        timesStr = timesBuilder.toString();
                        
                        if (tripSchedules.size() >= 1) {
                            direction = tripSchedules.get(tripSchedules.size() - 1).getStation().getName();
                        }
                    } else {
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
                            
                            if (stations.size() >= 1) {
                                direction = stations.get(stations.size() - 1).getName();
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
        panel.setPreferredSize(new Dimension(500, 600));
        
        JLabel trainLabel = createFormLabel("Train:");
        panel.add(trainLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JComboBox<String> trainComboBox = new JComboBox<>();
        trainComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        trainComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        trainComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        
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
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JLabel stationsLabel = createFormLabel("Stations desservies:");
        panel.add(stationsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel stationsMainPanel = new JPanel();
        stationsMainPanel.setLayout(new BoxLayout(stationsMainPanel, BoxLayout.Y_AXIS));
        stationsMainPanel.setBackground(Color.WHITE);
        stationsMainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane stationsScrollPane = new JScrollPane(stationsMainPanel);
        stationsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        stationsScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        stationsScrollPane.setPreferredSize(new Dimension(450, 350));
        stationsScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        List<Station> stationsList = getStations();
        List<JCheckBox> stationCheckboxes = new ArrayList<>();
        Map<JCheckBox, JPanel> stationTimesPanels = new HashMap<>();
        Map<JCheckBox, JSpinner> arrivalTimeSpinners = new HashMap<>();
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date initDate = cal.getTime();
        SpinnerDateModel timeModel = new SpinnerDateModel(initDate, null, null, Calendar.MINUTE);
        
        for (Station station : stationsList) {
            JPanel stationPanel = new JPanel();
            stationPanel.setLayout(new BoxLayout(stationPanel, BoxLayout.Y_AXIS));
            stationPanel.setBackground(Color.WHITE);
            stationPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            stationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JCheckBox checkbox = new JCheckBox(station.getName());
            checkbox.setBackground(Color.WHITE);
            checkbox.setFont(new Font("Arial", Font.PLAIN, 14));
            checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
            stationCheckboxes.add(checkbox);
            
            stationPanel.add(checkbox);
            
            JPanel timePanel = new JPanel();
            timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
            timePanel.setBackground(Color.WHITE);
            timePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            timePanel.setVisible(false);
            stationTimesPanels.put(checkbox, timePanel);
            
            JLabel arrivalLabel = new JLabel("Heure d'arrivée: ");
            arrivalLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            timePanel.add(arrivalLabel);
            
            JSpinner arrivalSpinner = new JSpinner(new SpinnerDateModel(initDate, null, null, Calendar.MINUTE));
            JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(arrivalSpinner, "HH:mm");
            arrivalSpinner.setEditor(timeEditor);
            arrivalSpinner.setPreferredSize(new Dimension(80, 25));
            arrivalSpinner.setMaximumSize(new Dimension(80, 25));
            timePanel.add(arrivalSpinner);
            arrivalTimeSpinners.put(checkbox, arrivalSpinner);
            
            timePanel.add(Box.createHorizontalStrut(10));
            
            stationPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            stationPanel.add(timePanel);
            stationPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            
            stationsMainPanel.add(stationPanel);
            
            checkbox.addActionListener(e -> {
                timePanel.setVisible(checkbox.isSelected());
                stationsMainPanel.revalidate();
                stationsMainPanel.repaint();
            });
        }
        
        panel.add(stationsScrollPane);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JButton autoScheduleButton = new JButton("Calculer horaires automatiquement");
        autoScheduleButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        autoScheduleButton.setFont(new Font("Arial", Font.PLAIN, 14));
        autoScheduleButton.addActionListener(e -> {
            List<JCheckBox> selectedCheckboxes = stationCheckboxes.stream()
                    .filter(JCheckBox::isSelected)
                    .collect(Collectors.toList());
            if (selectedCheckboxes.size() > 0) {
                Date firstTime = (Date) arrivalTimeSpinners.get(selectedCheckboxes.get(0)).getValue();
                Calendar baseCal = Calendar.getInstance();
                baseCal.setTime(firstTime);
                for (int i = 1; i < selectedCheckboxes.size(); i++) {
                    JCheckBox cb = selectedCheckboxes.get(i);
                    JSpinner spinner = arrivalTimeSpinners.get(cb);
                    baseCal.add(Calendar.MINUTE, 5);
                    spinner.setValue(baseCal.getTime());
                }
            }
        });

        JButton reverseButton = new JButton("Inverser le sens");
        reverseButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        reverseButton.setFont(new Font("Arial", Font.PLAIN, 14));
        reverseButton.addActionListener(e -> {
           
            List<Integer> selectedIndices = new ArrayList<>();
            List<Date> selectedTimes = new ArrayList<>();
            for (int i = 0; i < stationCheckboxes.size(); i++) {
                JCheckBox cb = stationCheckboxes.get(i);
                if (cb.isSelected()) {
                    selectedIndices.add(i);
                    selectedTimes.add((Date) arrivalTimeSpinners.get(cb).getValue());
                }
            }
           
            for (JCheckBox cb : stationCheckboxes) {
                cb.setSelected(false);
            }
            
            for (int i = 0; i < selectedIndices.size(); i++) {
                int idx = selectedIndices.get(selectedIndices.size() - 1 - i);
                Date time = selectedTimes.get(selectedTimes.size() - 1 - i);
                JCheckBox cb = stationCheckboxes.get(idx);
                cb.setSelected(true);
                arrivalTimeSpinners.get(cb).setValue(time);
            }
            
            List<Component> components = new ArrayList<>();
            for (int i = 0; i < stationsMainPanel.getComponentCount(); i++) {
                components.add(stationsMainPanel.getComponent(i));
            }
            stationsMainPanel.removeAll();
            for (int i = components.size() - 1; i >= 0; i--) {
                stationsMainPanel.add(components.get(i));
            }
            stationsMainPanel.revalidate();
            stationsMainPanel.repaint();
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonPanel.add(autoScheduleButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(reverseButton);
        panel.add(buttonPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        boolean done = false;
        while (!done) {
            JOptionPane optionPane = new JOptionPane(
                    panel,
                    JOptionPane.PLAIN_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION);
            JDialog dialog = optionPane.createDialog(this.frame, "Ajouter un trajet");
            dialog.setMinimumSize(new Dimension(550, 700));
            dialog.setResizable(true);
            dialog.setVisible(true);
            Integer result = (Integer) optionPane.getValue();
            if (result != null && result == JOptionPane.OK_OPTION) {
                int selectedTrainIndex = trainComboBox.getSelectedIndex();
                List<Station> selectedStations = new ArrayList<>();
                Map<Station, Date> stationArrivalTimes = new HashMap<>();
                for (int i = 0; i < stationCheckboxes.size(); i++) {
                    JCheckBox checkbox = stationCheckboxes.get(i);
                    if (checkbox.isSelected()) {
                        Station station = stationsList.get(i);
                        selectedStations.add(station);
                        Date arrivalTime = (Date) arrivalTimeSpinners.get(checkbox).getValue();
                        stationArrivalTimes.put(station, arrivalTime);
                    }
                }
                if (selectedTrainIndex != -1 && !selectedStations.isEmpty()) {
                    try {
                    
                        int trainId = trainList.get(selectedTrainIndex).getId();
                        
                       
                        Date firstStationTime = null;
                        Date lastStationTime = null;
                        
                        for (int i = 0; i < stationCheckboxes.size(); i++) {
                            JCheckBox checkbox = stationCheckboxes.get(i);
                            if (checkbox.isSelected()) {
                                Date arrivalTime = (Date) arrivalTimeSpinners.get(checkbox).getValue();
                                if (firstStationTime == null || arrivalTime.before(firstStationTime)) {
                                    firstStationTime = arrivalTime;
                                }
                                if (lastStationTime == null || arrivalTime.after(lastStationTime)) {
                                    lastStationTime = arrivalTime;
                                }
                            }
                        }
                        
                        Time startTime = new Time(firstStationTime.getTime());
                        Time endTime = new Time(lastStationTime.getTime());
                        
                        if (checkTrainTimeConflict(trainId, null, startTime, endTime)) {
                            JOptionPane.showMessageDialog(panel, 
                                "Ce train est déjà associé à un autre trajet à cette horaire. Veuillez choisir un autre train! ou modifier les horaires!", 
                                "Conflit !!", 
                                JOptionPane.WARNING_MESSAGE);
                            continue;
                        }
                        
                        Trip trip = new Trip();
                        trip.setTrain(trainList.get(selectedTrainIndex));
                        
                       
                        String direction = selectedStations.get(selectedStations.size() - 1).getName();

                        this.tripService.insertTrip(trip);
                        
                        System.out.println("ID du trajet généré: " + trip.getId());
                        
                        if (trip.getId() <= 0) {
                            JOptionPane.showMessageDialog(panel, "Erreur: L'ID du trajet n'a pas été correctement généré", "Erreur!", JOptionPane.ERROR_MESSAGE);
                            continue;
                        }

                        Schedules schedules = new Schedules();
                        System.out.println("Nombre de stations à associer: " + selectedStations.size());
                        
                        for (Station station : selectedStations) {
                            Schedule stationSchedule = new Schedule();
                            Calendar arrivalCal = Calendar.getInstance();
                            arrivalCal.setTime(stationArrivalTimes.get(station));
                            Time arrivalTime = new Time(arrivalCal.getTimeInMillis());
                            stationSchedule.setTimeArrival(arrivalTime);
                            Calendar depCal = (Calendar) arrivalCal.clone();
                            depCal.add(Calendar.MINUTE, 2);
                            Time departureTime = new Time(depCal.getTimeInMillis());
                            stationSchedule.setTimeDeparture(departureTime);
                            stationSchedule.setTrip(trip);
                            stationSchedule.setStation(station);
                            
                            System.out.println("Schedule pour station " + station.getName() + 
                                " avec TripID=" + stationSchedule.getTrip().getId());
                            
                            schedules.add(stationSchedule);
                        }
                        this.service.insertSchedules(schedules);
                        tripStations.put(trip.getId(), selectedStations);
                        refreshScheduleData();
                        this.frame.showSuccessDialog("Bravo!", "Le trajet a été ajouté avec succès!");
                        done = true;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(panel, "Vous avez une erreur lors de l'ajout du trajet : " + ex.getMessage(), "Erreur!!", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(panel, "Il faut remplir tous les champs et sélectionner au moins une station!", "Erreur!", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                done = true;
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
        panel.setPreferredSize(new Dimension(500, 600));

        JLabel trainLabel = createFormLabel("Train:");
        panel.add(trainLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JComboBox<String> trainComboBox = new JComboBox<>();
        trainComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        trainComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        trainComboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        
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
        
        
        trainComboBox.setEnabled(false);
        
        panel.add(trainComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel tripIdLabel = createFormLabel("Numéro Trajet:");
        panel.add(tripIdLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField tripIdField = new JTextField(String.valueOf(tripId));
        tripIdField.setEditable(false);
        tripIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        tripIdField.setAlignmentX(Component.LEFT_ALIGNMENT);
        tripIdField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(tripIdField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel stationsLabel = createFormLabel("Stations desservies:");
        panel.add(stationsLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel stationsMainPanel = new JPanel();
        stationsMainPanel.setLayout(new BoxLayout(stationsMainPanel, BoxLayout.Y_AXIS));
        stationsMainPanel.setBackground(Color.WHITE);
        stationsMainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JScrollPane stationsScrollPane = new JScrollPane(stationsMainPanel);
        stationsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        stationsScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        stationsScrollPane.setPreferredSize(new Dimension(450, 350));
        stationsScrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        List<Station> stationsList = getStations();
        List<JCheckBox> stationCheckboxes = new ArrayList<>();
        Map<JCheckBox, JSpinner> arrivalTimeSpinners = new HashMap<>();
        
        List<String> selectedStationNames = new ArrayList<>();
        if (stationsStr != null && !stationsStr.equals("À définir")) {
            String[] stationNames = stationsStr.split(" → ");
            for (String name : stationNames) {
                selectedStationNames.add(name.trim());
            }
        }
        
        for (Station station : stationsList) {
            JPanel stationPanel = new JPanel();
            stationPanel.setLayout(new BoxLayout(stationPanel, BoxLayout.Y_AXIS));
            stationPanel.setBackground(Color.WHITE);
            stationPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
            stationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            JCheckBox checkbox = new JCheckBox(station.getName());
            checkbox.setBackground(Color.WHITE);
            checkbox.setFont(new Font("Arial", Font.PLAIN, 14));
            checkbox.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            boolean isSelected = selectedStationNames.contains(station.getName());
            checkbox.setSelected(isSelected);
            
            stationCheckboxes.add(checkbox);
            stationPanel.add(checkbox);
            
            JPanel timePanel = new JPanel();
            timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.X_AXIS));
            timePanel.setBackground(Color.WHITE);
            timePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
            timePanel.setVisible(isSelected);
            
            JLabel arrivalLabel = new JLabel("Heure d'arrivée: ");
            arrivalLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            timePanel.add(arrivalLabel);
            
            Calendar cal = Calendar.getInstance();
            
            if (stationArrivalTimes.containsKey(station.getName())) {
                cal.setTime(stationArrivalTimes.get(station.getName()));
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 8);
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
            
            stationPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            stationPanel.add(timePanel);
            stationPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            
            stationsMainPanel.add(stationPanel);
            
            checkbox.addActionListener(e -> {
                timePanel.setVisible(checkbox.isSelected());
                stationsMainPanel.revalidate();
                stationsMainPanel.repaint();
            });
        }
        
        panel.add(stationsScrollPane);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton autoScheduleButton = new JButton("Recalculer horaires automatiquement");
        autoScheduleButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        autoScheduleButton.setFont(new Font("Arial", Font.PLAIN, 14));
        autoScheduleButton.addActionListener(e -> {
            List<JCheckBox> selectedCheckboxes = stationCheckboxes.stream()
                    .filter(JCheckBox::isSelected)
                    .collect(Collectors.toList());
            
            if (selectedCheckboxes.size() > 0) {
                Date firstTime = (Date) arrivalTimeSpinners.get(selectedCheckboxes.get(0)).getValue();
                Calendar baseCal = Calendar.getInstance();
                baseCal.setTime(firstTime);
                
                for (int i = 1; i < selectedCheckboxes.size(); i++) {
                    JCheckBox cb = selectedCheckboxes.get(i);
                    JSpinner spinner = arrivalTimeSpinners.get(cb);
                    
                    baseCal.add(Calendar.MINUTE, 5);
                    spinner.setValue(baseCal.getTime());
                }
            }
        });
        
        buttonPanel.add(autoScheduleButton);
        panel.add(buttonPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        boolean done = false;
        while (!done) {
            JOptionPane optionPane = new JOptionPane(
                    panel,
                    JOptionPane.PLAIN_MESSAGE,
                    JOptionPane.OK_CANCEL_OPTION);
            JDialog dialog = optionPane.createDialog(this.frame, "Modifier un trajet");
            dialog.setMinimumSize(new Dimension(550, 700));
            dialog.setResizable(true);
            dialog.setVisible(true);
            Integer result = (Integer) optionPane.getValue();
            if (result != null && result == JOptionPane.OK_OPTION) {
                int selectedTrainIdxNew = trainComboBox.getSelectedIndex();
                List<Station> selectedStations = new ArrayList<>();
                Map<Station, Date> stationArrivalTimes2 = new HashMap<>();
                for (int i = 0; i < stationCheckboxes.size(); i++) {
                    JCheckBox checkbox = stationCheckboxes.get(i);
                    if (checkbox.isSelected()) {
                        Station station = stationsList.get(i);
                        selectedStations.add(station);
                        Date arrivalTime = (Date) arrivalTimeSpinners.get(checkbox).getValue();
                        stationArrivalTimes2.put(station, arrivalTime);
                    }
                }
                if (selectedTrainIdxNew != -1 && !selectedStations.isEmpty()) {
                    try {
                        
                        Date firstStationTime = null;
                        Date lastStationTime = null;
                        
                        for (int i = 0; i < stationCheckboxes.size(); i++) {
                            JCheckBox checkbox = stationCheckboxes.get(i);
                            if (checkbox.isSelected()) {
                                Date arrivalTime = (Date) arrivalTimeSpinners.get(checkbox).getValue();
                                if (firstStationTime == null || arrivalTime.before(firstStationTime)) {
                                    firstStationTime = arrivalTime;
                                }
                                if (lastStationTime == null || arrivalTime.after(lastStationTime)) {
                                    lastStationTime = arrivalTime;
                                }
                            }
                        }
                        
                        Time startTime = new Time(firstStationTime.getTime());
                        Time endTime = new Time(lastStationTime.getTime());
                        
                        if (checkTrainTimeConflict(trainId, tripId, startTime, endTime)) {
                            JOptionPane.showMessageDialog(panel, 
                                "Ce train est déjà programmé pour un autre trajet à cette horaire!! Veuillez modifier les horaires.", 
                                "Conflit d'horaires!!", 
                                JOptionPane.WARNING_MESSAGE);
                            continue;
                        }
                        
                        Trip trip = new Trip(tripId, trainList.get(selectedTrainIdxNew));
                        
                       
                        Schedules allSchedules = this.service.selectSchedules();
                        
                        if (allSchedules != null && allSchedules.getSchedules() != null) {
                            List<String> existingStationNames = new ArrayList<>();
                            
                            for (Schedule existing : allSchedules.getSchedules()) {
                                if (existing.getTrip().getId() == tripId) {
                                    existingStationNames.add(existing.getStation().getName());
                                }
                            }
                            
                            System.out.println("Stations existantes pour ce trajet: " + String.join(", ", existingStationNames));
                        }
                        
                       
                        this.service.deleteSchedulesByTripId(tripId);
                        
                        
                        Schedules schedules = new Schedules();
                        for (Station station : selectedStations) {
                            Schedule stationSchedule = new Schedule();
                            Calendar arrivalCal = Calendar.getInstance();
                            arrivalCal.setTime(stationArrivalTimes2.get(station));
                            Time arrivalTime = new Time(arrivalCal.getTimeInMillis());
                            stationSchedule.setTimeArrival(arrivalTime);
                            Calendar depCal = (Calendar) arrivalCal.clone();
                            depCal.add(Calendar.MINUTE, 2);
                            Time departureTime = new Time(depCal.getTimeInMillis());
                            stationSchedule.setTimeDeparture(departureTime);
                            stationSchedule.setTrip(trip);
                            stationSchedule.setStation(station);
                            
                            System.out.println("Schedule pour station " + station.getName() + 
                                " avec TripID=" + stationSchedule.getTrip().getId());
                            
                            schedules.add(stationSchedule);
                        }
                        
                        this.service.insertSchedules(schedules);
                        
                        tripStations.put(tripId, selectedStations);
                        
                        refreshScheduleData();
                        this.frame.showSuccessDialog("Modification réussie!", "Les horaires du trajet ont été modifiés avec succès!");
                        done = true;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(panel, "Vous avez une erreur lors de la modification du trajet: " + ex.getMessage(), "Erreur!", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(panel, "Veuillez sélectionner un train et au moins une station.", "Erreur", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                done = true;
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
                "Voulez-vous vraiment supprimer le trajet " + tripId + " ?")) {
            try {
                this.service.deleteSchedule(tripId);
                
                this.tripService.deleteTrip(tripId);
                
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
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(MainInterfaceFrame.TEXT_COLOR);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    
    private boolean isSameHourMinute(Date d1, Time t2) {
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(t2);
        return c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY)
            && c1.get(Calendar.MINUTE) == c2.get(Calendar.MINUTE);
    }
    
    public void filterByTrainId(int trainId) {
        this.filteredTrainId = trainId;
        applyTrainFilter(trainId);
    }
    
    public void clearFilter() {
        this.filteredTrainId = null;
        if (frame instanceof MainInterfaceFrame) {
            ((MainInterfaceFrame) frame).setPlanningFilterTrainId(null);
        }
        refreshScheduleData();
    }
    
    public Integer getFilteredTrainId() {
        return filteredTrainId;
    }
    
    private void applyTrainFilter(int trainId) {
        try {
            tableModel.setRowCount(0);

            Schedules schedules = this.service.selectSchedules();
            Map<Integer, List<Schedule>> schedulesByTrip = new HashMap<>();

        
            if (schedules != null && schedules.getSchedules() != null) {
                for (Schedule schedule : schedules.getSchedules()) {
                    if (schedule.getTrip().getTrain() != null && 
                        schedule.getTrip().getTrain().getId() == trainId) {
                        int tripId = schedule.getTrip().getId();
                        if (!schedulesByTrip.containsKey(tripId)) {
                            schedulesByTrip.put(tripId, new ArrayList<>());
                        }
                        schedulesByTrip.get(tripId).add(schedule);
                    }
                }
            }
            
        
            if (!schedulesByTrip.isEmpty()) {
                for (Map.Entry<Integer, List<Schedule>> entry : schedulesByTrip.entrySet()) {
                    int tripId = entry.getKey();
                    List<Schedule> tripSchedules = entry.getValue();
                    
                    String stationsStr = "À définir";
                    String timesStr = "Non planifié";
                    String direction = "Non défini";
                    
                    if (tripSchedules != null && !tripSchedules.isEmpty()) {
                        tripSchedules.sort((s1, s2) -> s1.getTimeArrival().compareTo(s2.getTimeArrival()));
                        
                        StringBuilder stationsBuilder = new StringBuilder();
                        StringBuilder timesBuilder = new StringBuilder();
                        
                        Set<String> addedStations = new HashSet<>();
                        
                        for (Schedule sch : tripSchedules) {
                            String stationName = sch.getStation().getName();
                            
                            if (!addedStations.contains(stationName)) {
                                if (stationsBuilder.length() > 0) {
                                    stationsBuilder.append(" → ");
                                    timesBuilder.append(", ");
                                }
                                stationsBuilder.append(stationName);
                                addedStations.add(stationName);
                                
                                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                                String arrivalTime = sch.getTimeArrival() != null ? 
                                        timeFormat.format(sch.getTimeArrival()) : "?";
                                timesBuilder.append(arrivalTime);
                            }
                        }
                        
                        stationsStr = stationsBuilder.toString();
                        timesStr = timesBuilder.toString();
                        
                        if (tripSchedules.size() >= 1) {
                            direction = tripSchedules.get(tripSchedules.size() - 1).getStation().getName();
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
            
            if (tableModel.getRowCount() == 0) {
                this.frame.showWarningDialog("Aucun trajet trouvé", 
                        "Aucun trajet n'est associé au train " + trainId);
            }
            
            this.frame.repaint();
            this.frame.revalidate();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this.frame,
                    "Erreur dans le filtrage des données: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

   
    private boolean checkTrainTimeConflict(int trainId, Integer tripIdToIgnore, Time startTime, Time endTime) {
        try {
            Schedules schedules = this.service.selectSchedules();
            if (schedules != null && schedules.getSchedules() != null) {
                
                Map<Integer, List<Schedule>> schedulesByTrip = new HashMap<>();
                
                for (Schedule schedule : schedules.getSchedules()) {
                    if (schedule.getTrip().getTrain() != null && 
                        schedule.getTrip().getTrain().getId() == trainId &&
                        (tripIdToIgnore == null || schedule.getTrip().getId() != tripIdToIgnore)) {
                        
                        int tripId = schedule.getTrip().getId();
                        if (!schedulesByTrip.containsKey(tripId)) {
                            schedulesByTrip.put(tripId, new ArrayList<>());
                        }
                        schedulesByTrip.get(tripId).add(schedule);
                    }
                }
                
                for (Map.Entry<Integer, List<Schedule>> entry : schedulesByTrip.entrySet()) {
                    List<Schedule> tripSchedules = entry.getValue();
                    
                    if (tripSchedules != null && !tripSchedules.isEmpty()) {
                        
                        tripSchedules.sort((s1, s2) -> s1.getTimeArrival().compareTo(s2.getTimeArrival()));
                        
                       
                        Time existingStartTime = tripSchedules.get(0).getTimeArrival();
                        Time existingEndTime = tripSchedules.get(tripSchedules.size() - 1).getTimeArrival();
                        
                        
                        if ((startTime.before(existingEndTime) && endTime.after(existingStartTime)) ||
                            startTime.equals(existingStartTime) || endTime.equals(existingEndTime)) {
                            return true; 
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false; 
    }
}
