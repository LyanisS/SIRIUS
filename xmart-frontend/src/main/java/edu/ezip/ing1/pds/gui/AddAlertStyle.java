package edu.ezip.ing1.pds.gui;

import edu.ezip.ing1.pds.business.dto.*;
import edu.ezip.ing1.pds.services.AlertService;
import edu.ezip.ing1.pds.services.TrainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class AddAlertStyle extends JDialog {
    private final static Logger logger = LoggerFactory.getLogger("FrontEnd - AddAlertStyle");
    private MainInterfaceFrame frame;
    private JComboBox<String> statusComboBox;
    private JTextField messageField;
    private JTextField durationField;
    private JComboBox<String> trainComboBox;
    private JComboBox<String> gravityComboBox;
    private List<Train> trainList;
    private boolean confirmed = false;
    private final AlertService service;

    public AddAlertStyle(MainInterfaceFrame parent, AlertService service) {
        super(parent, "Ajouter une alerte", true);
        this.service = service;
        this.frame = parent;

        JPanel mainPanel = new JPanel(new GridLayout(4, 2, 10, 10));

        mainPanel.add(new JLabel("Message :"));
        messageField = new JTextField(10);
        mainPanel.add(messageField);

        mainPanel.add(new JLabel("Train concerné :"));
        trainComboBox = new JComboBox<>();
        trainComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        trainComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        trainComboBox.setFont(new Font("Arial", Font.PLAIN, 14));

        trainList = new ArrayList<>();

        try {
            TrainService trainService = new TrainService(this.frame.getNetworkConfig());
            Trains trains = trainService.selectTrains();
            if (trains != null && trains.getTrains() != null) {
                for (Train train : trains.getTrains()) {
                    trainList.add(train);
                    trainComboBox.addItem("Train n°" + train.getId());
                }
            }
        } catch (Exception e) {
            this.frame.showErrorDialog(e, "Erreur", "Erreur lors de la récupération des trains");
            return;
        }
        if (trainList.isEmpty()) {
            this.frame.showErrorDialog(new Exception("trainList is empty"), "Erreur", "Aucun train disponible pour ajouter une alerte.");
            return;
        }

        mainPanel.add(trainComboBox);

        mainPanel.add(new JLabel("Impact sur le planning du train (en secondes) :"));
        durationField = new JTextField(10);
        durationField.setText("0");
        mainPanel.add(durationField);

        mainPanel.add(new JLabel("Gravité :"));
        gravityComboBox = new JComboBox<>();
        for (AlertGravity gravity : AlertGravity.values()) {
            if (gravity == AlertGravity.UNKNOWN) continue;
            gravityComboBox.addItem(gravity.getType());
        }
        mainPanel.add(gravityComboBox);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Annuler");
        JButton confirmButton = new JButton("Confirmer");

        cancelButton.addActionListener(e -> dispose());
        confirmButton.addActionListener(e -> {
            if (validateInputs()) {
                confirmed = true;
                dispose();
            }
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(this.frame);
    }

    private boolean validateInputs() {
        int selectedTrainIndex = trainComboBox.getSelectedIndex();

        if (selectedTrainIndex == -1) {
            this.frame.showErrorDialog(new Exception("No train selected"), "Erreur", "Vous devez sélectionner un train dans la liste.");
            return false;
        }

        try {
            int duration = Integer.parseInt(this.durationField.getText().trim());
            if (duration < 0) {
                this.frame.showErrorDialog(new Exception("Invalid duration (< 0)"), "Erreur", "L'impact sur le planning doit être un entier positif.");
                return false;
            }
            if (duration > 1800) {
                this.frame.showErrorDialog(new Exception("Invalid duration (> 1 800)"), "Erreur", "L'impact sur le planning doit être inférieur ou égal à 30 minutes (1 800 secondes).");
                return false;
            }
        } catch (NumberFormatException e) {
            this.frame.showErrorDialog(e, "Erreur", "L'impact sur le planning doit être un entier positif.");
            return false;
        }

        return true;
    }

    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }

    public Alert getAlert() {
        if (!confirmed)
            return null;

        AlertGravity gravity = AlertGravity.UNKNOWN;
        String gravityName = (String) this.gravityComboBox.getSelectedItem();

        for (AlertGravity alertGravity : AlertGravity.values()) {
            if (alertGravity.getType().equals(gravityName)) {
                gravity = alertGravity;
                break;
            }
        }

        String message = (String) this.gravityComboBox.getSelectedItem();

        Train train = new Train();
        train.setId(trainList.get(trainComboBox.getSelectedIndex()).getId());

        int duration = Integer.parseInt(this.durationField.getText().trim());
        return new Alert(message, new Time(System.currentTimeMillis()), duration, gravity, train);
    }

    public void addAlert() {
        if (!confirmed)
            return;

        try {
            Alert alert = getAlert();
            Alerts alerts = new Alerts();
            alerts.add(alert);

            service.insertAlerts(alerts);
            JOptionPane.showMessageDialog(this,
                    "Alerte ajouté avec succès!",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            String userFriendlyMessage;

            if (errorMessage != null && errorMessage.contains("Connection")) {
                userFriendlyMessage = "Erreur de connexion au serveur.\n\n" +
                        "Veuillez vérifier votre connexion réseau et réessayer.";

                JOptionPane.showMessageDialog(this,
                        userFriendlyMessage,
                        "Erreur de connexion",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                userFriendlyMessage = "Une erreur est survenue lors de l'ajout de l'alerte :\n\n" + errorMessage;

                JOptionPane.showMessageDialog(this,
                        userFriendlyMessage,
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }

            logger.error("Error adding alert: " + e.getMessage());
        }
    }
}