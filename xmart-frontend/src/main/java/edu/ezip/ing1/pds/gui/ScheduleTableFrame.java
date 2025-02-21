package edu.ezip.ing1.pds.gui;

import java.awt.BorderLayout;
import java.text.SimpleDateFormat;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import edu.ezip.ing1.pds.business.dto.Schedule;
import edu.ezip.ing1.pds.business.dto.Schedules;
import edu.ezip.ing1.pds.business.dto.TrackElement;
import edu.ezip.ing1.pds.business.dto.Trip;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.ScheduleService;

public class ScheduleTableFrame extends JFrame {

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final ScheduleService scheduleService;

    public ScheduleTableFrame() throws Exception {
        super("Gestion du planning - PCC");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 400);

        String[] columnNames = {
            "ID Horaire",
            "ID Trajet",
            "ID CDV",
            "Date/Heure",
            "Arrêt?"
        };
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("Actualiser");
        refreshButton.addActionListener(e -> refreshScheduleData());
        buttonPanel.add(refreshButton);

        JButton addScheduleButton = new JButton("Ajouter un horaire");
        addScheduleButton.addActionListener(e -> openAddScheduleDialog());
        buttonPanel.add(addScheduleButton);

        JButton backButton = new JButton("Retour");
        backButton.addActionListener(e -> {
            dispose();
        });
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);

        NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, "network.yaml");
        scheduleService = new ScheduleService(networkConfig);

        refreshScheduleData();
    }

    private void refreshScheduleData() {
        try {
            tableModel.setRowCount(0);

            Schedules schedules = scheduleService.selectSchedules();

            if (schedules != null && schedules.getSchedules() != null) {
                for (Schedule schedule : schedules.getSchedules()) {
                    Object[] row = {
                        schedule.getId(),
                        schedule.getTrip().getId(),
                        schedule.getTrackElement().getId(),
                        schedule.getTimestamp(),
                        schedule.getStop()
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la récupération des données du planning : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddScheduleDialog() {
        String scheduleDatetime = JOptionPane.showInputDialog(this, "Date/heure (yyyy-MM-dd hh:mm:ss) :");
        String scheduleStop = JOptionPane.showInputDialog(this, "Arrêt ? (true/false):");
        String trackElementId = JOptionPane.showInputDialog(this, "ID CDV :");
        String tripId = JOptionPane.showInputDialog(this, "ID Trajet:");

        if (tripId != null && trackElementId != null && scheduleDatetime != null && scheduleStop != null) {
            try {
                Schedule newSchedule = new Schedule();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                newSchedule.setTimestamp(new java.sql.Timestamp(dateFormat.parse(scheduleDatetime).getTime()));
                newSchedule.setStop(Boolean.parseBoolean(scheduleStop));
                newSchedule.setTrackElement(new TrackElement(Integer.parseInt(trackElementId)));
                newSchedule.setTrip(new Trip(Integer.parseInt(tripId)));

                refreshScheduleData();

                JOptionPane.showMessageDialog(this, "Horaire ajouté avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout de l'horaire : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.", "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                ScheduleTableFrame frame = new ScheduleTableFrame();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Erreur lors du lancement de l'application : " + e.getMessage(),
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
