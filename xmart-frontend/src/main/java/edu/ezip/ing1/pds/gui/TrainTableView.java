package edu.ezip.ing1.pds.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.JCheckBox;

import edu.ezip.ing1.pds.business.dto.Schedule;
import edu.ezip.ing1.pds.business.dto.Schedules;
import edu.ezip.ing1.pds.business.dto.Station;
import edu.ezip.ing1.pds.business.dto.Stations;
import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.business.dto.Trains;
import edu.ezip.ing1.pds.services.ScheduleService;
import edu.ezip.ing1.pds.services.StationService;
import edu.ezip.ing1.pds.services.TrainService;

public class TrainTableView {
    private MainInterfaceFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private TrainService trainService;
    private ScheduleService scheduleService;
    private Map<Integer, List<Schedule>> trainSchedules;

    private static boolean SIMULATION_ACTIVE = false;
    private static Date SIMULATION_TIME = null;
    private static final String SIMULATION_LABEL_TEXT = "HEURE SIMULÉE: ";

    public TrainTableView(MainInterfaceFrame frame) {
        this.frame = frame;
        this.frame.setTitle("Gestion des trains");
        this.trainService = new TrainService(this.frame.getNetworkConfig());
        this.scheduleService = new ScheduleService(this.frame.getNetworkConfig());
        this.trainSchedules = new HashMap<>();
        
        loadScheduleData();
        initializeTable();
        displayTable();
        setupTrainButtons();
    }
    
    private void loadScheduleData() {
        try {
          
            Schedules schedules = this.scheduleService.selectSchedules();
            
            if (schedules != null && schedules.getSchedules() != null && !schedules.getSchedules().isEmpty()) {
               
                for (Schedule schedule : schedules.getSchedules()) {
                    int trainId = schedule.getTrip().getTrain().getId();
                    if (!trainSchedules.containsKey(trainId)) {
                        trainSchedules.put(trainId, new ArrayList<>());
                    }
                    trainSchedules.get(trainId).add(schedule);
                }
                
            
                for (List<Schedule> trainScheduleList : trainSchedules.values()) {
                    trainScheduleList.sort((s1, s2) -> s1.getTimeArrival().compareTo(s2.getTimeArrival()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du chargement des schedules: " + e.getMessage());
        }
    }
    
    private void setupTrainButtons() {
        List<JButton> buttons = new ArrayList<>();
        
        JButton addTrainBtn = MainInterfaceFrame.createButton("Ajouter Train", MainInterfaceFrame.SUCCESS_COLOR);
        addTrainBtn.addActionListener(e -> {
            AddTrainStyle dialog = new AddTrainStyle(this.frame);
            dialog.showDialog();
            dialog.addTrain();
        });
        buttons.add(addTrainBtn);
        
        JButton showRoutesBtn = MainInterfaceFrame.createButton("Afficher Trajets", MainInterfaceFrame.PRIMARY_COLOR);
        buttons.add(showRoutesBtn);
        
        JButton deleteTrainBtn = MainInterfaceFrame.createButton("Supprimer Train", MainInterfaceFrame.ACCENT_COLOR);
        deleteTrainBtn.addActionListener(e -> {
            TrainService service = new TrainService(this.frame.getNetworkConfig());
            
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int trainId = (int) table.getValueAt(selectedRow, 0);
                if (frame.showConfirmDialog("Confirmer la suppression",
                        "Êtes-vous sûr de vouloir supprimer le train " + trainId + " ?")) {
                    try {
                        service.deleteTrain(trainId);
                        loadTrainData();
                        frame.showSuccessDialog("Suppression effectuèe", 
                                "Le train a été supprimé avec succès");
                    } catch (Exception ex) {
                        frame.showErrorDialog(ex, "Erreur lors de la suppression",
                                "Impossible de supprimer le train : " + ex.getMessage());
                    }
                }
            } else {
                frame.showWarningDialog("Sélection requise", 
                        "Veuillez sélectionner un train à supprimer");
            }
        });
        buttons.add(deleteTrainBtn);
        
        // simulation button
        JButton simulateTimeBtn = MainInterfaceFrame.createButton("Simuler Heure", new Color(150, 80, 200));
        simulateTimeBtn.addActionListener(e -> SimulateTimeDialog());
        buttons.add(simulateTimeBtn);
        
        this.frame.registerJButtons(buttons);
    }

    private void initializeTable() {
        String[] columnNames = {"Numèro Train", "Position", "Heure d'arrivée"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
      
        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(52, 152, 219));
        table.getTableHeader().setForeground(Color.BLUE);
    
        loadTrainData();
    }

    private void loadTrainData() {
    
        tableModel.setRowCount(0);
        
        try {
            Trains trains = trainService.selectTrains();
            if (trains != null && trains.getTrains() != null && !trains.getTrains().isEmpty()) {
                for (Train train : trains.getTrains()) {
                
                    TrainPosition position = calculateTrainPosition(train.getId());
                    
                    tableModel.addRow(new Object[]{
                        train.getId(), 
                        position.getStation(), 
                        position.getArrivalTime()
                    });
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this.frame,
                "Erreur lors du chargement des trains: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void displayTable() {
        JPanel contentPanel = this.frame.getMainContentPanel();
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
    
        
        if (SIMULATION_ACTIVE && SIMULATION_TIME != null) {
            JLabel simulationLabel = new JLabel(SIMULATION_LABEL_TEXT + new SimpleDateFormat("HH:mm").format(SIMULATION_TIME));
            simulationLabel.setFont(new Font("Arial", Font.BOLD, 16));
            simulationLabel.setForeground(new Color(150, 80, 200));
            simulationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            simulationLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            mainPanel.add(simulationLabel);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        mainPanel.add(scrollPane);
        
    
        mainPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        
        JPanel stationsPanel = new JPanel();
        stationsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        stationsPanel.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
        stationsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0)); 
        stationsPanel.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        
        try {
            StationService stationService = new StationService(this.frame.getNetworkConfig());
            Stations stationsObj = stationService.selectStations();
            if (stationsObj != null && stationsObj.getStations() != null) {
                for (Station station : stationsObj.getStations()) {
                    //colors of stations
                    Color stationColor;
                    switch(station.getName()) {
                        case "POSE":
                            stationColor = new Color(46, 204, 113); // Vert
                            break;
                        case "JASM":
                            stationColor = new Color(52, 152, 219); // Bleu
                            break;
                        case "TROC":
                            stationColor = new Color(155, 89, 182); // Violet
                            break;
                        case "BONO":
                            stationColor = new Color(231, 76, 60); // Rouge
                            break;
                        case "STSD":
                            stationColor = new Color(241, 196, 15); // Jaune
                            break;
                        case "NATN":
                            stationColor = new Color(230, 126, 34); // Orange
                            break;
                        case "MAMO":
                            stationColor = new Color(26, 188, 156); // Turquoise
                            break;
                        default:
                            stationColor = new Color(52, 73, 94); // Gris
                    }
                    
                    JButton btn = createStationButton(station.getName(), stationColor);
                    final String stationName = station.getName();
                    btn.addActionListener(e -> showTrainsAtStation(stationName, stationColor));
                    stationsPanel.add(btn);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la récupération des stations : " + e.getMessage());
        }
        
        mainPanel.add(stationsPanel);
        
        mainPanel.add(Box.createVerticalGlue());
        
        contentPanel.add(mainPanel, BorderLayout.CENTER);

        if (tableModel.getRowCount() == 0) {
            JLabel emptyLabel = new JLabel("Aucun train disponible", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.BOLD, 16));
            emptyLabel.setForeground(new Color(128, 128, 128));
            contentPanel.add(emptyLabel, BorderLayout.NORTH);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private JButton createStationButton(String text, Color color) {
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
        };
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(90, 40));
        return button;
    }
    
    private JTable findTrainTable() {
        for (Component comp : this.frame.getMainContentPanel().getComponents()) {
            if (comp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) comp;
                Component view = scrollPane.getViewport().getView();
                if (view instanceof JTable) {
                    return (JTable) view;
                }
            }
        }
        return null;
    }

    public void refreshTrainTable() {
    
        loadScheduleData();
        
    
        loadTrainData();
        displayTable();
    }

    private void showTrainsAtStation(String stationName, Color stationColor) {
        try {
            
            List<Train> trainsAtStation = new ArrayList<>();
            
            Trains trains = trainService.selectTrains();
            if (trains != null && trains.getTrains() != null && !trains.getTrains().isEmpty()) {
                for (Train train : trains.getTrains()) {
               
                    if (trainSchedules.containsKey(train.getId()) && !trainSchedules.get(train.getId()).isEmpty()) {
                      
                        List<Schedule> scheduleList = trainSchedules.get(train.getId());
                        Schedule lastSchedule = scheduleList.get(scheduleList.size() - 1);
                        
                       
                        if (lastSchedule.getStation().getName().equals(stationName)) {
                            trainsAtStation.add(train);
                        }
                    }
                }
            }
            
      
            if (trainsAtStation.isEmpty()) {
                JOptionPane.showMessageDialog(this.frame,
                    "Aucun train n'est actuellement présent à la station " + stationName,
                    "Trains à la station " + stationName,
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
            
                JPanel popupPanel = new JPanel();
                popupPanel.setLayout(new BoxLayout(popupPanel, BoxLayout.Y_AXIS));
                popupPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                
        
                JLabel titleLabel = new JLabel("Trains à la station " + stationName);
                titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
                titleLabel.setForeground(stationColor.darker());
                titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                popupPanel.add(titleLabel);
                
                popupPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                
                
                for (Train train : trainsAtStation) {
                    String arrivalTime = "Non associé à un trajet";
                    
                    
                    if (trainSchedules.containsKey(train.getId()) && !trainSchedules.get(train.getId()).isEmpty()) {
                        List<Schedule> scheduleList = trainSchedules.get(train.getId());
                        Schedule lastSchedule = scheduleList.get(scheduleList.size() - 1);
                        
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        arrivalTime = lastSchedule.getTimeArrival() != null ? 
                                timeFormat.format(lastSchedule.getTimeArrival()) : "non associé à un trajet";
                    }
                    
                    JPanel trainPanel = new JPanel();
                    trainPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
                    trainPanel.setBackground(new Color(245, 245, 245));
                    trainPanel.setBorder(BorderFactory.createLineBorder(stationColor, 1));
                    
                    JLabel trainLabel = new JLabel("Train " + train.getId() + " - Arrivé à " + arrivalTime);
                    trainLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                    trainPanel.add(trainLabel);
                    
                    trainPanel.setMaximumSize(new Dimension(300, 40));
                    trainPanel.setPreferredSize(new Dimension(300, 40));
                    
                    popupPanel.add(trainPanel);
                    popupPanel.add(Box.createRigidArea(new Dimension(0, 5)));
                }
                
                JOptionPane.showMessageDialog(this.frame,
                    popupPanel,
                    "Trains à la station " + stationName,
                    JOptionPane.PLAIN_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this.frame,
                "Erreur lors de la récupération des trains: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private class TrainPosition {
        private String station;
        private String arrivalTime;
        
        public TrainPosition(String station, String arrivalTime) {
            this.station = station;
            this.arrivalTime = arrivalTime;
        }
        
        public String getStation() {
            return station;
        }
        
        public String getArrivalTime() {
            return arrivalTime;
        }
    }
    
    private void SimulateTimeDialog() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Simuler une heure différente");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JPanel timePanel = new JPanel();
        timePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        timePanel.setBackground(Color.WHITE);
        timePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel timeLabel = new JLabel("Heure (HH:mm): ");
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        timePanel.add(timeLabel);
        
        Calendar cal = Calendar.getInstance();
        if (SIMULATION_ACTIVE && SIMULATION_TIME != null) {
            cal.setTime(SIMULATION_TIME);
        }
        Date initDate = cal.getTime();
        JSpinner timeSpinner = new JSpinner(new SpinnerDateModel(initDate, null, null, Calendar.MINUTE));
        JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
        timeSpinner.setEditor(timeEditor);
        timeSpinner.setPreferredSize(new Dimension(80, 25));
        timePanel.add(timeSpinner);
        
        panel.add(timePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JCheckBox useRealTimeCheckbox = new JCheckBox("Utiliser l'heure réelle");
        useRealTimeCheckbox.setFont(new Font("Arial", Font.PLAIN, 14));
        useRealTimeCheckbox.setBackground(Color.WHITE);
        useRealTimeCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        useRealTimeCheckbox.setSelected(!SIMULATION_ACTIVE);
        panel.add(useRealTimeCheckbox);
        
        JLabel noteLabel = new JLabel("Note: La simulation restera active jusqu'à ce que vous la désactiviez !");
        noteLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(noteLabel);
        
        int result = JOptionPane.showConfirmDialog(this.frame, panel, "Simuler l'heure",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            SIMULATION_ACTIVE = !useRealTimeCheckbox.isSelected();
            if (SIMULATION_ACTIVE) {
                SIMULATION_TIME = (Date) timeSpinner.getValue();
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                System.out.println("Heure simulée: " + timeFormat.format(SIMULATION_TIME));
                
            
                JOptionPane.showMessageDialog(
                    this.frame,
                    "Simulation activée avec l'heure: " + timeFormat.format(SIMULATION_TIME),
                    "Simulation d'heure",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                SIMULATION_TIME = null;
                System.out.println("Utilisation de l'heure réelle");
                JOptionPane.showMessageDialog(
                    this.frame,
                    "Retour à l'heure réelle",
                    "Simulation d'heure",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
            
            loadScheduleData();
            loadTrainData();
            displayTable();
        }
    }

    private TrainPosition calculateTrainPosition(int trainId) {
        String station = "Garage";
        String arrivalTime = "Non associé à un trajet";
        
        try {
        
            if (trainSchedules.containsKey(trainId) && !trainSchedules.get(trainId).isEmpty()) {
                List<Schedule> scheduleList = trainSchedules.get(trainId);
                SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                
                //utile for debug
                Date currentTime = getCurrentTime();
                System.out.println("Heure actuelle: " + timeFormat.format(currentTime));
                System.out.println("Train #" + trainId + " - Horaires:");
                
        
                for (Schedule s : scheduleList) {
                    System.out.println("  Station: " + s.getStation().getName() + 
                                    ", Arrivée: " + (s.getTimeArrival() != null ? timeFormat.format(s.getTimeArrival()) : "non associé à un trajet") + 
                                    ", Départ: " + (s.getTimeDeparture() != null ? timeFormat.format(s.getTimeDeparture()) : "Non associé à un trajet"));
                }
                
        
                scheduleList.sort((s1, s2) -> s1.getTimeArrival().compareTo(s2.getTimeArrival()));
                
            
                Schedule lastVisitedSchedule = null;
                Schedule nextSchedule = null;
                
                Calendar currentCal = Calendar.getInstance();
                currentCal.setTime(currentTime);
                int currentHour = currentCal.get(Calendar.HOUR_OF_DAY);
                int currentMinute = currentCal.get(Calendar.MINUTE);
                
                for (Schedule schedule : scheduleList) {
                    if (schedule.getTimeArrival() != null) {
                        Calendar scheduleCal = Calendar.getInstance();
                        scheduleCal.setTime(schedule.getTimeArrival());
                        int scheduleHour = scheduleCal.get(Calendar.HOUR_OF_DAY);
                        int scheduleMinute = scheduleCal.get(Calendar.MINUTE);
                        
                        
                        if (scheduleHour < currentHour || 
                            (scheduleHour == currentHour && scheduleMinute <= currentMinute)) {
                            lastVisitedSchedule = schedule;
                        } else if (nextSchedule == null) {
                            nextSchedule = schedule;
                            break;
                        }
                    }
                }
                
                System.out.println("Dernière station visitée: " + 
                                (lastVisitedSchedule != null ? lastVisitedSchedule.getStation().getName() : "aucune"));
                System.out.println("Prochaine station: " + 
                                (nextSchedule != null ? nextSchedule.getStation().getName() : "aucune"));
                
            
                if (lastVisitedSchedule != null) {
                    
                    if (nextSchedule != null) {
                    
                        Calendar depCal = Calendar.getInstance();
                        depCal.setTime(lastVisitedSchedule.getTimeDeparture());
                        int depHour = depCal.get(Calendar.HOUR_OF_DAY);
                        int depMinute = depCal.get(Calendar.MINUTE);
                        
                        if (depHour > currentHour || 
                            (depHour == currentHour && depMinute > currentMinute)) {
                            station = lastVisitedSchedule.getStation().getName();
                            arrivalTime = "Départ à " + timeFormat.format(lastVisitedSchedule.getTimeDeparture());
                        } else {
                            // THE TRAIN IS BETWEEN 2 STATIONS
                            station = "En route: " + lastVisitedSchedule.getStation().getName() + 
                                    " → " + nextSchedule.getStation().getName();
                            arrivalTime = "Arrivée prévue à " + timeFormat.format(nextSchedule.getTimeArrival());
                        }
                    } else {
                        // train is in the last station (TERMINUS)
                        station = lastVisitedSchedule.getStation().getName() + " (Terminus)";
                        arrivalTime = "Arrivé à " + timeFormat.format(lastVisitedSchedule.getTimeArrival());
                    }
                } else if (nextSchedule != null) {
                    // the train is in his first station, waiting for the departure
                    station = "En attente à " + scheduleList.get(0).getStation().getName();
                    arrivalTime = "Départ prévu à " + timeFormat.format(scheduleList.get(0).getTimeArrival());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors du calcul de la position du train: " + e.getMessage());
        }
        
        return new TrainPosition(station, arrivalTime);
    }


    private Date getCurrentTime() {
        if (SIMULATION_ACTIVE && SIMULATION_TIME != null) {
            Calendar simCal = Calendar.getInstance();
            Calendar currentCal = Calendar.getInstance();
            
            simCal.setTime(SIMULATION_TIME);
            currentCal.set(Calendar.HOUR_OF_DAY, simCal.get(Calendar.HOUR_OF_DAY));
            currentCal.set(Calendar.MINUTE, simCal.get(Calendar.MINUTE));
            currentCal.set(Calendar.SECOND, 0);
            
            return currentCal.getTime();
        }
        return new Date();
    }
}
