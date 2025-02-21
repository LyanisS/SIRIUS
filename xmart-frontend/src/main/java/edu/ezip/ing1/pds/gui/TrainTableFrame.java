package edu.ezip.ing1.pds.gui;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.business.dto.Trains;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.TrainService;

public class TrainTableFrame extends JFrame {

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final TrainService trainService;

    public TrainTableFrame() throws Exception {
        super("Gestion des trains - PCC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        String[] columnNames = {"ID Train", "Statut", "Position (ID CDV)"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton refreshButton = new JButton("Actualiser");
        refreshButton.addActionListener(e -> refreshTrainData());
        buttonPanel.add(refreshButton);

        JButton openScheduleButton = new JButton("Afficher le planning");
        openScheduleButton.addActionListener(e -> openScheduleWindow());
        buttonPanel.add(openScheduleButton);

        add(buttonPanel, BorderLayout.SOUTH);

        NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, "network.yaml");
        trainService = new TrainService(networkConfig);

        refreshTrainData();
    }

    private void refreshTrainData() {
        try {
            tableModel.setRowCount(0);

            Trains trains = trainService.selectTrains();

            if (trains != null && trains.getTrains() != null) {
                for (Train train : trains.getTrains()) {
                    Object[] row = {
                        train.getId(),
                        train.getStatus().getName(),
                        train.getTrackElement().getId()
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading train data: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openScheduleWindow() {
        SwingUtilities.invokeLater(() -> {
            try {
                ScheduleTableFrame scheduleFrame = new ScheduleTableFrame();
                scheduleFrame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error opening Schedule window: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                TrainTableFrame frame = new TrainTableFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error starting application: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
