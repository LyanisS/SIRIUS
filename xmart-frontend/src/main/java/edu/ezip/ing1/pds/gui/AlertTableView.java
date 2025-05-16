package edu.ezip.ing1.pds.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import edu.ezip.ing1.pds.business.dto.Alert;
import edu.ezip.ing1.pds.business.dto.Alerts;
import edu.ezip.ing1.pds.services.AlertService;

public class AlertTableView {
    private MainInterfaceFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private AlertService service;
    private Integer filteredTrainId = null;

    public AlertTableView(MainInterfaceFrame frame) {
        this.frame = frame;
        this.frame.setTitle("Gestion des alarmes");
        this.frame.getMainContentPanel().removeAll();

        createStyledTable();

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

        contentPanel.add(mainPanel, BorderLayout.CENTER);

        List<JButton> buttons = new ArrayList<>();

        JButton addButton = MainInterfaceFrame.createButton("Ajouter une alerte", MainInterfaceFrame.SUCCESS_COLOR);
        addButton.addActionListener(e -> showAddAlertDialog());
        buttons.add(addButton);

        JButton deleteButton = MainInterfaceFrame.createButton("Supprimer", MainInterfaceFrame.ACCENT_COLOR);
        deleteButton.addActionListener(e -> deleteSelectedAlert());
        buttons.add(deleteButton);
        
        if (filteredTrainId != null) {
            JButton clearFilterButton = MainInterfaceFrame.createButton("Effacer filtre", new Color(255, 165, 0));
            clearFilterButton.addActionListener(e -> clearFilter());
            buttons.add(clearFilterButton);
            
            JPanel filterIndicator = new JPanel(new BorderLayout());
            filterIndicator.setBackground(Color.WHITE);
            JLabel filterLabel = new JLabel("Affichage filtré pour le train " + filteredTrainId);
            filterLabel.setFont(new Font("Arial", Font.BOLD, 14));
            filterLabel.setForeground(new Color(255, 165, 0));
            filterIndicator.add(filterLabel, BorderLayout.NORTH);
            mainPanel.add(filterIndicator, BorderLayout.NORTH);
        }

        this.frame.registerJButtons(buttons);

        this.service = new AlertService(this.frame.getNetworkConfig());

        this.refreshAlertData();
    }

    private void createStyledTable() {
        String[] columnNames = { "Numéro d'alarme", "Message", "Heure", "Gravité", "Numéro de train", "Impact sur le planning" };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.setFont(new Font("Arial", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(52, 152, 219));
        table.getTableHeader().setForeground(Color.BLUE);
    }


    private void refreshAlertData() {
        try {
            tableModel.setRowCount(0);

            if (filteredTrainId != null) {
                applyTrainFilter(filteredTrainId);
                return;
            }

            Alerts alerts = this.service.selectAlerts();

            if (alerts != null && alerts.getAlerts() != null) {
                for (Alert alert : alerts.getAlerts()) {
                    Object[] row = {
                            alert.getId(),
                            alert.getMessage(),
                            alert.getTime().toString(),
                            alert.getGravity().getType(),
                            alert.getTrain().getId(),
                            AlertTableView.getDurationString(alert.getDuration())
                    };
                    tableModel.addRow(row);
                    System.out.println("Alert: " + alert.getId() + " " + alert.getMessage() + " " + alert.getTime() + " " + alert.getGravity().getType() + " " + alert.getTrain().getId() + " " + alert.getDuration());
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this.frame,
                    "Erreur dans le chargement des donnèes: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }

        if (tableModel.getRowCount() == 0) {
            String message = filteredTrainId != null ? "Aucune alerte trouvée pour le train n°" + filteredTrainId : "Aucune alerte disponible";
            JLabel emptyLabel = new JLabel(message, SwingConstants.CENTER);
            emptyLabel.setFont(new Font("Arial", Font.BOLD, 16));
            emptyLabel.setForeground(new Color(128, 128, 128));
            this.frame.getMainContentPanel().add(emptyLabel, BorderLayout.NORTH);
        }

        this.frame.revalidate();
        this.frame.repaint();
        System.out.println("revalidate/repaint");
    }

    private static String getDurationString(int duration_seconds) {
        int duration_minutes = 0;
        while (duration_seconds >= 60) {
            duration_minutes++;
            duration_seconds -= 60;
        }
        String duration = "Aucun";
        if (duration_seconds > 0 && duration_minutes > 0) {
            duration = duration_minutes + " min " + duration_seconds;
        } else if (duration_minutes > 0 && duration_seconds == 0) {
            duration = duration_minutes + " min";
        } else if (duration_minutes == 0 && duration_seconds > 0) {
            duration = duration_seconds + " sec";
        }
        return duration;
    }

    private void showAddAlertDialog() {
        AddAlertStyle dialog = new AddAlertStyle(this.frame, this.service);

        if (dialog.showDialog()) {
            dialog.addAlert();
            this.refreshAlertData();
        }
    }

    private void deleteSelectedAlert() {
        int selectedRow = this.table.getSelectedRow();
        if (selectedRow == -1) {
            this.frame.showWarningDialog("Aucune alerte sélectionnée",
                    "Veuillez sélectionner une alerte à supprimer.");
            return;
        }

        try {
            int alertId = (int) this.tableModel.getValueAt(selectedRow, 0);

            if (this.frame.showConfirmDialog("Confirmer la suppression",
                    "Êtes-vous sûr de vouloir supprimer l'alerte' #" + alertId + " ?")) {
                try {

                    this.service.deleteAlert(alertId);

                    refreshAlertData();

                    this.frame.showSuccessDialog("Suppression réussie",
                            "L'alerte #" + alertId + " a été supprimée avec succès.");
                } catch (Exception e) {
                    handleDeleteError(e, alertId);
                }
            }
        } catch (Exception e) {
            handleDeleteError(e, -1);
        }
    }

    private void handleDeleteError(Exception e, int alertId) {
        this.frame.showErrorDialog(e, "Erreur de suppression",
                "Erreur lors de la suppression de l'alerte " + alertId );
    }
    
    public void filterByTrainId(int trainId) {
        this.filteredTrainId = trainId;
        applyTrainFilter(trainId);
    }
    
    public void clearFilter() {
        this.filteredTrainId = null;
        if (frame != null) {
            frame.setAlarmsFilterTrainId(null);
        }
        refreshAlertData();
    }
    
    public Integer getFilteredTrainId() {
        return filteredTrainId;
    }
    
    private void applyTrainFilter(int trainId) {
        try {
            tableModel.setRowCount(0);
            
            Alerts alerts = this.service.selectAlerts();
            
            if (alerts != null && alerts.getAlerts() != null) {
                for (Alert alert : alerts.getAlerts()) {
                    if (alert.getTrain() != null && alert.getTrain().getId() == trainId) {
                        Object[] row = {
                                alert.getId(),
                                alert.getMessage(),
                                alert.getTime().toString(),
                                alert.getGravity().getType(),
                                alert.getTrain().getId(),
                                AlertTableView.getDurationString(alert.getDuration())
                        };
                        tableModel.addRow(row);
                    }
                }
            }
            
            if (tableModel.getRowCount() == 0) {
                this.frame.showWarningDialog("Aucune alerte trouvée", 
                        "Aucune alerte n'est associée au train " + trainId);
            }
            
            this.frame.repaint();
            this.frame.revalidate();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this.frame,
                    "Erreur dans le filtrage des données: " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
