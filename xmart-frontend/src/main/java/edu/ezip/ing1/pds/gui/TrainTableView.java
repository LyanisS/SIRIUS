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
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.business.dto.TrainStatus;
import edu.ezip.ing1.pds.business.dto.Trains;
import edu.ezip.ing1.pds.services.TrainService;

public class TrainTableView {
    private MainInterfaceFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private final TrainService service;

    public TrainTableView(MainInterfaceFrame frame) {
        this.frame = frame;
        this.frame.setTitle("Gestion des Trains", "Système de Contrôle");
        this.frame.getTableJPanel().removeAll();

        createStyledTable();

        JPanel tablePanel = this.frame.getTableJPanel();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);


        List<JButton> buttons = new ArrayList<>();

        JButton addButton = MainInterfaceFrame.createActionButton("Ajouter un train", MainInterfaceFrame.SUCCESS_COLOR);
        addButton.addActionListener(e -> showAddTrainDialog());
        buttons.add(addButton);

        JButton deleteButton = MainInterfaceFrame.createActionButton("Supprimer", MainInterfaceFrame.ACCENT_COLOR);
        deleteButton.addActionListener(e -> deleteSelectedTrain());
        buttons.add(deleteButton);

        JButton updateStatusButton = MainInterfaceFrame.createActionButton("Mettre à jour le statut", MainInterfaceFrame.PRIMARY_COLOR);
        updateStatusButton.addActionListener(e -> updateSelectedTrainStatus());
        buttons.add(updateStatusButton);

        JButton refreshButton = MainInterfaceFrame.createActionButton("Actualiser", MainInterfaceFrame.REFRESH_BTN_COLOR);
        refreshButton.addActionListener(e -> refreshTrainData());
        buttons.add(refreshButton);

        this.frame.registerJButtons(buttons);

        this.service = new TrainService(this.frame.getNetworkConfig());
        this.refreshTrainData();
    }

    private void createStyledTable() {
        String[] columnNames = {
                "ID Train",
                "Statut",
                "Position",
                "Station"
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

                if (column == 1 && !isRowSelected(row)) {
                    String status = (String) getValueAt(row, column);
                    if ("EN_PANNE".equals(status)) {
                        comp.setForeground(MainInterfaceFrame.ACCENT_COLOR);
                    } else if ("EN_MARCHE".equals(status)) {
                        comp.setForeground(MainInterfaceFrame.SUCCESS_COLOR);
                    } else {
                        comp.setForeground(MainInterfaceFrame.TEXT_COLOR);
                    }
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

    private void refreshTrainData() {
        try {
            tableModel.setRowCount(0);

            Trains trains = this.service.selectTrains();

            if (trains != null && trains.getTrains() != null) {
                for (Train train : trains.getTrains()) {
                    String stationName = train.getTrackElement().getStation() != null
                            ? train.getTrackElement().getStation().getName()
                            : "";

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
            this.frame.showErrorDialog(e, "Erreur de chargement",
                    "Erreur dans le chargement des données du train");
        }
    }

    private void deleteSelectedTrain() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            this.frame.showWarningDialog("Aucun train sélectionné",
                    "Veuillez sélectionner un train à supprimer.");
            return;
        }

        try {
            int trainId = (int) tableModel.getValueAt(selectedRow, 0);
            String stationName = (String) tableModel.getValueAt(selectedRow, 3);

            if (!"POSE".equals(stationName) && !"MAMO".equals(stationName)) {
                this.frame.showWarningDialog("Suppression impossible",
                        "Impossible de supprimer ce train.\n\n" +
                                "Seuls les trains situés dans les stations POSE ou MAMO peuvent être supprimés.");
                return;
            }

            if (this.frame.showConfirmDialog("Confirmer la suppression",
                    "Êtes-vous sûr de vouloir supprimer le train #" + trainId + " ?")) {
                try {

                    this.service.deleteTrain(trainId);

                    refreshTrainData();

                    this.frame.showSuccessDialog("Suppression réussie",
                            "Le train #" + trainId + " a été supprimé avec succès.");
                } catch (Exception e) {
                    handleDeleteError(e, trainId);
                }
            }
        } catch (Exception e) {
            handleDeleteError(e, -1);
        }
    }

    private void handleDeleteError(Exception e, int trainId) {
        String errorMessage = e.getMessage();
        String userFriendlyMessage;

        if (errorMessage != null && errorMessage.contains("Connection")) {
            userFriendlyMessage = "Erreur de connexion au serveur.\n\n" +
                    "Veuillez vérifier votre connexion réseau et réessayer.";
        } else if (errorMessage != null && errorMessage.contains("Unrecognized token")) {

            userFriendlyMessage = "Le train a probablement été supprimé, mais une erreur est survenue lors du traitement de la réponse.\n\n"
                    +
                    "Veuillez actualiser la liste pour vérifier.";

            try {
                refreshTrainData();
            } catch (Exception refreshError) {

            }
        } else {
            userFriendlyMessage = "Une erreur est survenue lors de la suppression du train" +
                    (trainId > 0 ? " #" + trainId : "") + ":\n\n" + errorMessage;
        }

        this.frame.showErrorDialog(e, "Erreur", userFriendlyMessage);
    }

    private void showAddTrainDialog() {

        MainInterfaceFrame.styleDialogUIComponents();

        AddTrainStyle dialog = new AddTrainStyle(this.frame, this.service);

        if (dialog.showDialog()) {
            dialog.addTrain();
            refreshTrainData();
        }

        MainInterfaceFrame.resetDialogUIComponents();
    }

    private void updateSelectedTrainStatus() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            this.frame.showWarningDialog("Aucun train sélectionné",
                    "Veuillez sélectionner un train pour mettre à jour son statut.");
            return;
        }

        try {
            int trainId = (int) tableModel.getValueAt(selectedRow, 0);
            String currentStatus = (String) tableModel.getValueAt(selectedRow, 1);

            JPanel panel = new JPanel();
            panel.setBackground(MainInterfaceFrame.BACKGROUND_COLOR);
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel titleLabel = new JLabel("Mettre à jour le statut du train #" + trainId);
            titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
            titleLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(titleLabel);

            panel.add(Box.createRigidArea(new Dimension(0, 15)));

            JLabel statusLabel = new JLabel("Nouveau statut:");
            statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            statusLabel.setForeground(MainInterfaceFrame.TEXT_COLOR);
            statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(statusLabel);

            panel.add(Box.createRigidArea(new Dimension(0, 5)));

            JComboBox<String> statusComboBox = new JComboBox<>();
            statusComboBox.setBackground(Color.WHITE);
            statusComboBox.setForeground(MainInterfaceFrame.TEXT_COLOR);
            statusComboBox.setFont(new Font("Arial", Font.PLAIN, 12));
            statusComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
            statusComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);

            for (TrainStatus status : TrainStatus.values()) {
                if (status == TrainStatus.UNKNOWN) continue;
                statusComboBox.addItem(status.getName());
            }

            statusComboBox.setSelectedItem(currentStatus);
            panel.add(statusComboBox);

            int result = JOptionPane.showConfirmDialog(this.frame, panel,
                    "Mise à jour du statut",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String newStatusName = (String) statusComboBox.getSelectedItem();

                TrainStatus newStatus = null;
                for (TrainStatus status : TrainStatus.values()) {
                    if (status.getName().equals(newStatusName)) {
                        newStatus = status;
                        break;
                    }
                }

                if (newStatus != null) {

                    this.service.updateTrainStatus(trainId, newStatus.getId());

                    refreshTrainData();

                    this.frame.showSuccessDialog("Mise à jour réussie",
                            "Le statut du train #" + trainId + " a été mis à jour avec succès.");
                }
            }
        } catch (Exception e) {
            this.frame.showErrorDialog(e, "Erreur",
                    "Une erreur est survenue lors de la mise à jour du statut du train");
        }
    }
}
