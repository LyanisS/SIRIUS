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
import java.util.ArrayList;
import java.util.List;

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

import edu.ezip.ing1.pds.business.dto.Station;
import edu.ezip.ing1.pds.business.dto.Stations;
import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.business.dto.Trains;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.StationService;
import edu.ezip.ing1.pds.services.TrainService;

public class TrainTableView {
    private MainInterfaceFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private TrainService trainService;

    public TrainTableView(MainInterfaceFrame frame) {
        this.frame = frame;
        this.frame.setTitle("Gestion des trains");
        NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, "network.yaml");
        this.trainService = new TrainService(networkConfig);
        
        initializeTable();
        displayTable();
        setupTrainButtons();
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
            TrainService service = new TrainService(ConfigLoader.loadConfig(NetworkConfig.class, "network.yaml"));
            
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                int trainId = (int) table.getValueAt(selectedRow, 0);
                if (frame.showConfirmDialog("Confirmer la suppression",
                        "Êtes-vous sûr de vouloir supprimer le train " + trainId + " ?")) {
                    try {
                        service.deleteTrain(trainId);
                        loadTrainData();
                        frame.showSuccessDialog("Suppression réussie", 
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
        
        this.frame.registerJButtons(buttons);
    }

    private void initializeTable() {
        String[] columnNames = {"ID Train"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        
        table.getColumnModel().getColumn(0).setPreferredWidth(150);
      
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
                    tableModel.addRow(new Object[]{train.getId()});
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
            NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, "network.yaml");
            StationService stationService = new StationService(networkConfig);
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
                    btn.addActionListener(e -> System.out.println("Station sélectionnée : " + station.getName()));
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
        JTable trainTable = findTrainTable();
        if (trainTable != null) {
            DefaultTableModel model = (DefaultTableModel) trainTable.getModel();
            model.setRowCount(0);
            try {
                Trains trains = this.trainService.selectTrains();
                if (trains != null && trains.getTrains() != null && !trains.getTrains().isEmpty()) {
                    for (Train train : trains.getTrains()) {
                        model.addRow(new Object[]{train.getId()});
                    }
                }
            } catch (Exception e) {
                frame.showErrorDialog(e, "Erreur lors du rafraîchissement",
                        "Impossible de rafraîchir les données : " + e.getMessage());
            }
        }
    }
}
