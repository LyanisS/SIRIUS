package edu.ezip.ing1.pds.gui;

import edu.ezip.ing1.pds.business.dto.Alert;
import edu.ezip.ing1.pds.business.dto.Alerts;
import edu.ezip.ing1.pds.services.AlertService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class AlertTableView {
    private MainInterfaceFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private AlertService service;

    public AlertTableView(MainInterfaceFrame frame) {
        this.frame = frame;
        this.frame.setTitle("Gestion des alarmes");
        this.frame.getTableJPanel().removeAll();

        createStyledTable();

        JPanel tablePanel = this.frame.getTableJPanel();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        List<JButton> buttons = new ArrayList<>();

        JButton addButton = MainInterfaceFrame.createActionButton("Ajouter une alerte", MainInterfaceFrame.SUCCESS_COLOR);
        addButton.addActionListener(e -> showAddAlertDialog());
        buttons.add(addButton);

        JButton deleteButton = MainInterfaceFrame.createActionButton("Supprimer", MainInterfaceFrame.ACCENT_COLOR);
        deleteButton.addActionListener(e -> deleteSelectedAlert());
        buttons.add(deleteButton);

        JButton refreshButton = MainInterfaceFrame.createActionButton("Actualiser", MainInterfaceFrame.REFRESH_BTN_COLOR);
        refreshButton.addActionListener(e -> refreshAlertData());
        buttons.add(refreshButton);

        this.frame.registerJButtons(buttons);

        this.service = new AlertService(this.frame.getNetworkConfig());

        this.refreshAlertData();
    }

    private void createStyledTable() {
        String[] columnNames = { "ID Alarme", "Message", "Date/Heure", "Gravité", "Train" };

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


    private void refreshAlertData() {
        try {
            tableModel.setRowCount(0);

            Alerts alerts = this.service.selectAlerts();

            if (alerts != null && alerts.getAlerts() != null) {
                for (Alert alert : alerts.getAlerts()) {
                    Object[] row = {
                            alert.getId(),
                            alert.getMessage(),
                            alert.getTime().toString(),
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

    private void showAddAlertDialog() {

        MainInterfaceFrame.styleDialogUIComponents();

        AddAlertStyle dialog = new AddAlertStyle(this.frame, this.service);

        if (dialog.showDialog()) {
            dialog.addAlert();
            this.refreshAlertData();
        }

        MainInterfaceFrame.resetDialogUIComponents();
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
        String errorMessage = e.getMessage();
        String userFriendlyMessage;

        if (errorMessage != null && errorMessage.contains("Connection")) {
            userFriendlyMessage = "Erreur de connexion au serveur.\n\n" +
                    "Veuillez vérifier votre connexion réseau et réessayer.";
        } else if (errorMessage != null && errorMessage.contains("Unrecognized token")) {

            userFriendlyMessage = "L'alerte a probablement été supprimée, mais une erreur est survenue lors du traitement de la réponse.\n\n"
                    +
                    "Veuillez actualiser la liste pour vérifier.";

            try {
                refreshAlertData();
            } catch (Exception refreshError) {

            }
        } else {
            userFriendlyMessage = "Une erreur est survenue lors de la suppression de l'alerte" +
                    (alertId > 0 ? " #" + alertId : "") + ":\n\n" + errorMessage;
        }

        this.frame.showErrorDialog(e, "Erreur", userFriendlyMessage);
    }
}
