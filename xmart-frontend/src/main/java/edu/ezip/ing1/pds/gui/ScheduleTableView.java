package edu.ezip.ing1.pds.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import edu.ezip.ing1.pds.business.dto.Schedule;
import edu.ezip.ing1.pds.business.dto.Schedules;
import edu.ezip.ing1.pds.business.dto.Trip;
import edu.ezip.ing1.pds.services.ScheduleService;

public class ScheduleTableView {

    private MainInterfaceFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private final ScheduleService service;


    public ScheduleTableView(MainInterfaceFrame frame) {
        this.frame = frame;
        this.frame.setTitle("Gestion du planning des trains");
        this.frame.getMainContentPanel().removeAll();

        createStyledTable();

        JPanel tablePanel = this.frame.getMainContentPanel();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        List<JButton> buttons = new ArrayList<>();

        JButton addButton = MainInterfaceFrame.createButton("Ajouter un horaire", MainInterfaceFrame.SUCCESS_COLOR);
        addButton.addActionListener(e -> openAddScheduleDialog());
        buttons.add(addButton);

        JButton editButton = MainInterfaceFrame.createButton("Modifier", MainInterfaceFrame.PRIMARY_COLOR);
        editButton.addActionListener(e -> openEditScheduleDialog());
        buttons.add(editButton);

        JButton deleteButton = MainInterfaceFrame.createButton("Supprimer", MainInterfaceFrame.ACCENT_COLOR);
        deleteButton.addActionListener(e -> deleteSchedule());
        buttons.add(deleteButton);

        JButton refreshButton = MainInterfaceFrame.createButton("Actualiser", MainInterfaceFrame.REFRESH_BTN_COLOR);
        refreshButton.addActionListener(e -> refreshScheduleData());
        buttons.add(refreshButton);

        this.frame.registerJButtons(buttons);

        this.service = new ScheduleService(this.frame.getNetworkConfig());
        this.refreshScheduleData();
    }

    private void createStyledTable() {
        String[] columnNames = {
            "ID Horaire",
            "ID Trajet",
            "ID CDV",
            "Date/Heure",
            "Arrêt?"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);

                if (!isRowSelected(row)) {
                    comp.setBackground(row % 2 == 0 ? Color.WHITE : MainInterfaceFrame.TABLE_ALTERNATE_ROW);
                } else {
                    comp.setBackground(
                            new Color(MainInterfaceFrame.PRIMARY_COLOR.getRed(), MainInterfaceFrame.PRIMARY_COLOR.getGreen(), MainInterfaceFrame.PRIMARY_COLOR.getBlue(), 80));
                    comp.setForeground(MainInterfaceFrame.TEXT_COLOR.darker());
                }

                return comp;
            }
        };

        table.setRowHeight(30);
        table.setIntercellSpacing(new Dimension(10, 5));
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(
                new Color(MainInterfaceFrame.PRIMARY_COLOR.getRed(), MainInterfaceFrame.PRIMARY_COLOR.getGreen(), MainInterfaceFrame.PRIMARY_COLOR.getBlue(), 100));
        table.setSelectionForeground(MainInterfaceFrame.TEXT_COLOR.darker());
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    table.setRowSelectionInterval(row, row);
                }
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setBackground(MainInterfaceFrame.TABLE_HEADER_COLOR);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setBorder(null);
        header.setPreferredSize(new Dimension(0, 35));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
    }

    private void refreshScheduleData() {
        try {
            tableModel.setRowCount(0);

            Schedules schedules = this.service.selectSchedules();

            if (schedules != null && schedules.getSchedules() != null) {
                for (Schedule schedule : schedules.getSchedules()) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String formattedDate = schedule.getTimeArrival() != null
                            ? dateFormat.format(schedule.getTimeArrival())
                            : "";

                    Object[] row = {
                        schedule.getId(),
                        schedule.getTrip().getId(),
                        "-",
                        formattedDate,
                        "-"
                    };
                    tableModel.addRow(row);
                }
            }
            this.frame.repaint();
            this.frame.revalidate();
        } catch (Exception e) {
            this.frame.showErrorDialog(e, "Erreur de chargement",
                    "Erreur lors de la récupération des données du planning");
        }
    }

    private void openAddScheduleDialog() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Ajouter un nouvel horaire");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel datetimeLabel = new JLabel("Date/heure (yyyy-MM-dd HH:mm:ss):");
        datetimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        datetimeLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        datetimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(datetimeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField datetimeField = new JTextField();
        datetimeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        datetimeField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(datetimeField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel stopLabel = new JLabel("Arrêt ?");
        stopLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stopLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        stopLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(stopLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JComboBox<String> stopComboBox = new JComboBox<>(new String[]{"Oui", "Non"});
        stopComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        stopComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(stopComboBox);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel cdvLabel = new JLabel("ID CDV:");
        cdvLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        cdvLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        cdvLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cdvLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField cdvField = new JTextField();
        cdvField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cdvField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(cdvField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel tripLabel = new JLabel("ID Trajet:");
        tripLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        tripLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        tripLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(tripLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField tripField = new JTextField();
        tripField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        tripField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(tripField);

        int result = JOptionPane.showConfirmDialog(
                this.frame,
                panel,
                "Ajouter un horaire",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String scheduleDatetime = datetimeField.getText();
            String scheduleStop = stopComboBox.getSelectedItem().toString();
            String trackElementId = cdvField.getText();
            String tripId = tripField.getText();

            if (!scheduleDatetime.isEmpty() && !trackElementId.isEmpty() && !tripId.isEmpty()) {
                try {
                    Schedule newSchedule = new Schedule();

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    newSchedule.setTimeArrival(new Time(dateFormat.parse(scheduleDatetime).getTime()));

                    newSchedule.setTrip(new Trip(Integer.parseInt(tripId)));

                    Schedules schedules = new Schedules();
                    schedules.add(newSchedule);

                    this.service.insertSchedules(schedules);

                    refreshScheduleData();

                    this.frame.showSuccessDialog("Parfait!", "L'horaire a été ajouté avec succès!");

                } catch (Exception ex) {
                    this.frame.showErrorDialog(ex, "Erreur!!", "Vous avez une erreur lors de l'ajout de l'horaire!!");
                }
            } else {
                this.frame.showWarningDialog("Erreur!", "Il faut remplir tous les champs!");
            }
        }
    }

    private void openEditScheduleDialog() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            this.frame.showWarningDialog("Aucun horaire sélectionné!",
                    "Veuillez sélectionner un horaire à modifier.");
            return;
        }

        int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentDatetime = (String) tableModel.getValueAt(selectedRow, 3);
        boolean currentStop = "Oui".equals(tableModel.getValueAt(selectedRow, 4));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Modifier l'horaire #" + scheduleId);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel datetimeLabel = new JLabel("Date/heure (yyyy-MM-dd HH:mm:ss):");
        datetimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        datetimeLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        datetimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(datetimeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JTextField datetimeField = new JTextField(currentDatetime);
        datetimeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        datetimeField.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(datetimeField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel stopLabel = new JLabel("Arrêt ?");
        stopLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        stopLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
        stopLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(stopLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));

        JComboBox<String> stopComboBox = new JComboBox<>(new String[]{"Oui", "Non"});
        stopComboBox.setSelectedItem(currentStop ? "Oui" : "Non");
        stopComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        stopComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(stopComboBox);

        int result = JOptionPane.showConfirmDialog(
                this.frame,
                panel,
                "Modifier un horaire",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String scheduleDatetime = datetimeField.getText();
            String scheduleStop = stopComboBox.getSelectedItem().toString();

            if (!scheduleDatetime.isEmpty()) {
                try {
                    Schedule updatedSchedule = new Schedule();
                    updatedSchedule.setId(scheduleId);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    updatedSchedule.setTimeArrival(new Time(dateFormat.parse(scheduleDatetime).getTime()));

                    this.service.UpdateSchedule(scheduleId, true);

                    refreshScheduleData();

                    this.frame.showSuccessDialog("Modification réussie!", "L'horaire a été modifié avec succès!");

                } catch (Exception ex) {
                    this.frame.showErrorDialog(ex, "Erreur!", "Vous avez une erreur! ");
                }
            } else {
                this.frame.showWarningDialog("Erreur", "La date/heure ne peut pas être vide.");
            }
        }
    }

    private void deleteSchedule() {
        int selectedRow = table.getSelectedRow();

        if (selectedRow == -1) {
            this.frame.showWarningDialog("Aucun horaire sélectionné",
                    "Veuillez sélectionner un horaire à supprimer.");
            return;
        }

        int scheduleId = (int) tableModel.getValueAt(selectedRow, 0);

        if (this.frame.showConfirmDialog("Confirmer la suppression",
                "Voulez-vous vraiment supprimer l'horaire #" + scheduleId + " ?")) {
            try {
                this.service.deleteSchedule(scheduleId);

                refreshScheduleData();

                this.frame.showSuccessDialog("Suppression réussie", "L'horaire a été supprimé avec succès!");

            } catch (Exception ex) {
                this.frame.showErrorDialog(ex, "Erreur", "Erreur lors de la suppression de l'horaire");
            }
        }
    }
}
