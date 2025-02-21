package edu.ezip.ing1.pds.gui;

import edu.ezip.ing1.pds.business.dto.Alert;
import edu.ezip.ing1.pds.business.dto.Alerts;
import edu.ezip.ing1.pds.services.AlertService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

public class AlertTableView {
    private MainInterfaceFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private AlertService service;

    public AlertTableView(MainInterfaceFrame frame) {
        this.frame = frame;
        this.frame.setTitle("Gestion des trains");
        this.frame.getMainJPanel().removeAll();

        String[] columnNames = { "ID Alarme", "Message", "Date/Heure", "Gravité", "Train" };
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        this.frame.getMainJPanel().add(scrollPane);

        List<JButton> buttons = new ArrayList<>();

        JButton refreshButton = new JButton("Actualiser");
        refreshButton.addActionListener(e -> refreshAlertData());
        buttons.add(refreshButton);

        this.frame.registerJButtons(buttons);

        this.service = new AlertService(this.frame.getNetworkConfig());

        this.refreshAlertData();
    }

    private void refreshAlertData() {
        try {
            tableModel.setRowCount(0);

            Alerts alerts = this.service.selectAlerts();

            if (alerts != null && alerts.getAlerts() != null) {
                for (Alert alert : alerts.getAlerts()) {
                    Object[] row = {
                            alert.getId(),
                            alert.getMessage(),
                            alert.getTimestamp().toString(),
                            alert.getGravity().getType(),
                            alert.getTrain().getId()
                    };
                    tableModel.addRow(row);
                }
            }
            this.frame.repaint();
            this.frame.revalidate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this.frame,
                    "Erreur dans le chargement des donnèes: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
