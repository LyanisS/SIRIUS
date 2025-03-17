package edu.ezip.ing1.pds.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.business.dto.Trains;
import edu.ezip.ing1.pds.services.TrainService;

public class TrainTableView {
    private MainInterfaceFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private final TrainService service;

    public TrainTableView(MainInterfaceFrame frame) {
        this.frame = frame;
        this.frame.setTitle("Gestion des trains");
        this.frame.getMainJPanel().removeAll();

        String[] columnNames = {
                "ID Train",
                "Statut",
                "Position (ID CDV)",
                "Station"
        };
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        this.frame.getMainJPanel().add(scrollPane);

        List<JButton> buttons = new ArrayList<>();

        JButton refreshButton = new JButton("Actualiser");
        refreshButton.addActionListener(e -> refreshTrainData());
        buttons.add(refreshButton);

        this.frame.registerJButtons(buttons);

        this.service = new TrainService(this.frame.getNetworkConfig());

        this.refreshTrainData();
    }

    private void refreshTrainData() {
        try {
            tableModel.setRowCount(0);

            Trains trains = this.service.selectTrains();

            if (trains != null && trains.getTrains() != null) {
                for (Train train : trains.getTrains()) {
                    Object[] row = {
                            train.getId(),
                            train.getStatus().getName(),
                            train.getTrackElement().getId(),
                            train.getTrackElement().getStation() != null
                                    ? train.getTrackElement().getStation().getName()
                                    : ""
                    };
                    tableModel.addRow(row);
                }
            }
            this.frame.repaint();
            this.frame.revalidate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this.frame,
                    "Erreur dans le chargement des donnèes du train: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
