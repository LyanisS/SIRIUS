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
        super("Train Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);

        // Initialize table model with column names
        String[] columnNames = {"Train ID", "Status ID", "Track Element ID"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);

        // Add table to scroll pane
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel();

        // Button to refresh train data
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshTrainData());
        buttonPanel.add(refreshButton);

        // Button to open ScheduleTableFrame
        JButton openScheduleButton = new JButton("Afficher Schedules");
        openScheduleButton.addActionListener(e -> openScheduleWindow());
        buttonPanel.add(openScheduleButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Initialize train service
        NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, "network.yaml");
        trainService = new TrainService(networkConfig);

        // Load initial data
        refreshTrainData();
    }

    private void refreshTrainData() {
        try {
            // Clear existing data
            tableModel.setRowCount(0);

            // Fetch trains
            Trains trains = trainService.selectTrains();

            if (trains != null && trains.getTrains() != null) {
                // Add each train to the table
                for (Train train : trains.getTrains()) {
                    Object[] row = {
                        train.getTrainId(),
                        train.getTrainStatusId(),
                        train.getTrackElementId()
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
