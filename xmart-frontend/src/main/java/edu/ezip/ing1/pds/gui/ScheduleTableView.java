package edu.ezip.ing1.pds.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import edu.ezip.ing1.pds.business.dto.Schedule;
import edu.ezip.ing1.pds.business.dto.Schedules;
import edu.ezip.ing1.pds.business.dto.TrackElement;
import edu.ezip.ing1.pds.business.dto.Trip;
import edu.ezip.ing1.pds.services.ScheduleService;

public class ScheduleTableView {

    private MainInterfaceFrame frame;
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final ScheduleService service;

    public ScheduleTableView(MainInterfaceFrame frame) {
        this.frame = frame;
        this.frame.setTitle("Gestion du planning des trains");
        this.frame.getMainJPanel().removeAll();

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
        this.frame.getMainJPanel().add(scrollPane);

        List<JButton> buttons = new ArrayList<>();

        JButton refreshButton = new JButton("Actualiser");
        refreshButton.addActionListener(e -> refreshScheduleData());
        buttons.add(refreshButton);

        JButton addScheduleButton = new JButton("Ajouter un horaire");
        addScheduleButton.addActionListener(e -> openAddScheduleDialog());
        buttons.add(addScheduleButton);

        this.frame.registerJButtons(buttons);

        this.service = new ScheduleService(this.frame.getNetworkConfig());

        this.refreshScheduleData();
    }

    private void refreshScheduleData() {
        try {
            tableModel.setRowCount(0);

            Schedules schedules = this.service.selectSchedules();

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
            this.frame.repaint();
            this.frame.revalidate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this.frame,
                    "Erreur lors de la récupération des données du planning : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddScheduleDialog() {
        String scheduleDatetime = JOptionPane.showInputDialog(this.frame, "Date/heure (yyyy-MM-dd hh:mm:ss) :");
        String scheduleStop = JOptionPane.showInputDialog(this.frame, "Arrêt ? (Oui/Non):");
        String trackElementId = JOptionPane.showInputDialog(this.frame, "ID CDV :");
        String tripId = JOptionPane.showInputDialog(this.frame, "ID Trajet:");

        if (tripId != null && trackElementId != null && scheduleDatetime != null && scheduleStop != null) {
            try {
                Schedule newSchedule = new Schedule();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                newSchedule.setTimestamp(new java.sql.Timestamp(dateFormat.parse(scheduleDatetime).getTime()));
                newSchedule.setStop(Boolean.parseBoolean(scheduleStop));
                newSchedule.setTrackElement(new TrackElement(Integer.parseInt(trackElementId)));
                newSchedule.setTrip(new Trip(Integer.parseInt(tripId)));

                Schedules schedules = new Schedules();
                schedules.add(newSchedule);

                this.service.insertSchedules(schedules);

                refreshScheduleData();

                JOptionPane.showMessageDialog(this.frame, "L'horaire a été bien ajoutée!", "Bien ajoutée",
                        JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this.frame, "Erreur lors de l'ajout de l'horaire : " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this.frame, "Il faut remplir tous les champs.", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
