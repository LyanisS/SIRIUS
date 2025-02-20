package edu.ezip.ing1.pds.gui;

import java.awt.BorderLayout;
import java.time.LocalDateTime;

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
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.ScheduleService;

public class ScheduleTableFrame extends JFrame {

    private final JTable table;
    private final DefaultTableModel tableModel;
    private final ScheduleService scheduleService;

    public ScheduleTableFrame() throws Exception {
        super("Schedule Management System");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Changer la fermeture
        setSize(800, 400);

        // Initialisation du modèle de table avec des noms de colonnes correspondant à Schedule DTO
        String[] columnNames = {
            "Schedule ID",
            "Trip ID",
            "Track Element ID",
            "Schedule DateTime",
            "Schedule Stop"
        };
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);

        // Ajouter la table dans un JScrollPane
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Créer le panel pour les boutons
        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshScheduleData());
        buttonPanel.add(refreshButton);

        // Ajouter le bouton "Ajouter un horaire"
        JButton addScheduleButton = new JButton("Ajouter un horaire");
        addScheduleButton.addActionListener(e -> openAddScheduleDialog());
        buttonPanel.add(addScheduleButton);

        // Ajouter le bouton de retour
        JButton backButton = new JButton("Retour");
        backButton.addActionListener(e -> {
            dispose();
        });
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Initialiser le service Schedule
        NetworkConfig networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, "network.yaml");
        scheduleService = new ScheduleService(networkConfig);

        // Charger les données initiales
        refreshScheduleData();
    }

    private void refreshScheduleData() {
        try {
            // Effacer les données existantes
            tableModel.setRowCount(0);

            // Récupérer les horaires
            Schedules schedules = scheduleService.selectSchedules();

            if (schedules != null && schedules.getSchedules() != null) {
                // Ajouter chaque horaire dans la table
                for (Schedule schedule : schedules.getSchedules()) {
                    Object[] row = {
                        schedule.getScheduleId(),
                        schedule.getTripId(),
                        schedule.getTrackElementId(),
                        schedule.getScheduleDatetime(),
                        schedule.getScheduleStop()
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading schedule data: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddScheduleDialog() {
        String scheduleId = JOptionPane.showInputDialog(this, "Schedule ID:");
        String tripId = JOptionPane.showInputDialog(this, "Trip ID:");
        String trackElementId = JOptionPane.showInputDialog(this, "Track Element ID:");
        String scheduleDatetime = JOptionPane.showInputDialog(this, "Schedule DateTime (AAAA-MM-JJTHH:mm:ss):");
        String scheduleStop = JOptionPane.showInputDialog(this, "Schedule Stop (true/false):");

        // Validation simple des entrées
        if (scheduleId != null && tripId != null && trackElementId != null && scheduleDatetime != null && scheduleStop != null) {
            try {
                // Créer un nouvel objet Schedule avec les données saisies
                Schedule newSchedule = new Schedule(Integer.parseInt(scheduleId), Integer.parseInt(tripId), Integer.parseInt(trackElementId),
                        LocalDateTime.parse(scheduleDatetime), Boolean.parseBoolean(scheduleStop));

                // Rafraîchir les données affichées
                refreshScheduleData();

                // Message de confirmation
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
                        "Error starting application: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
