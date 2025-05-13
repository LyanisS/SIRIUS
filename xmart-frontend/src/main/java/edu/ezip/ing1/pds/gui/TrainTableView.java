package edu.ezip.ing1.pds.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.business.dto.Trains;
import edu.ezip.ing1.pds.services.TrainService;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;

public class TrainTableView {
    private MainTemplate mainTemplate;
    private JTable table;
    private DefaultTableModel tableModel;
    private TrainService trainService;

    public TrainTableView(MainTemplate template) {
        this.mainTemplate = template;
        NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, "network.yaml");
        this.trainService = new TrainService(networkConfig);
        initializeTable();
        displayTable();
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
        table.getTableHeader().setForeground(Color.WHITE);
    
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
            JOptionPane.showMessageDialog(mainTemplate,
                "Erreur lors du chargement des trains: " + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void displayTable() {
        JPanel contentPanel = mainTemplate.getMainContentPanel();
        contentPanel.removeAll();
        contentPanel.setLayout(new BorderLayout());

      
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        if (tableModel.getRowCount() == 0) {
            JLabel emptyLabel = new JLabel("Aucun train disponible", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.BOLD, 16));
            emptyLabel.setForeground(new Color(128, 128, 128));
            contentPanel.add(emptyLabel, BorderLayout.NORTH);
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
