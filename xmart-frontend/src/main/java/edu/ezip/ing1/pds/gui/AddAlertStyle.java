package edu.ezip.ing1.pds.gui;

import edu.ezip.ing1.pds.business.dto.*;
import edu.ezip.ing1.pds.services.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;

public class AddAlertStyle extends JDialog {
    private final static Logger logger = LoggerFactory.getLogger("FrontEnd - AddAlertStyle");
    private JComboBox<String> statusComboBox;
    private JTextField messageField;
    private JTextField trainIdField;
    private JComboBox<String> gravityComboBox;
    private boolean confirmed = false;
    private final AlertService service;

    public AddAlertStyle(MainInterfaceFrame parent, AlertService service) {
        super(parent, "Ajouter une alerte", true);
        this.service = service;

        JPanel mainPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        mainPanel.add(new JLabel("Message :"));
        messageField = new JTextField(10);
        mainPanel.add(messageField);

        mainPanel.add(new JLabel("ID du train concerné :"));
        trainIdField = new JTextField(10);
        mainPanel.add(trainIdField);

        mainPanel.add(new JLabel("Gravité :"));
        gravityComboBox = new JComboBox<>();
        for (AlertGravity gravity : AlertGravity.values()) {
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
        setLocationRelativeTo(parent);
    }

    private boolean validateInputs() {
        try {
            int trainId = Integer.parseInt(this.trainIdField.getText().trim());
            if (trainId <= 0) {
                JOptionPane.showMessageDialog(this,
                        "L'ID du train doit être un nombre positif",
                        "Erreur de validation",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "L'ID du train doit être un nombre entier",
                    "Erreur de validation",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
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

        int trainId = Integer.parseInt(this.trainIdField.getText().trim());

        Train train = new Train();
        train.setId(trainId);

        return new Alert(0, message, new Timestamp(System.currentTimeMillis()), gravity, train);
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