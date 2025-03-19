package edu.ezip.ing1.pds.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.Container;
import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.border.EmptyBorder;

import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.business.dto.TrackElement;
import edu.ezip.ing1.pds.business.dto.TrainStatus;
import edu.ezip.ing1.pds.business.dto.Trains;
import edu.ezip.ing1.pds.business.dto.Station;
import edu.ezip.ing1.pds.services.TrainService;

public class AddTrainStyle extends JDialog {
    private JComboBox<String> statusComboBox;
    private JTextField trackElementIdField;
    private JComboBox<String> stationComboBox;
    private boolean confirmed = false;
    private final TrainService service;

    private final Map<String, Integer> stationMap = new HashMap<>();

    private final Map<String, Integer> statusMap = new HashMap<>();

    public AddTrainStyle(MainInterfaceFrame parent, TrainService service) {
        super(parent, "Ajouter un train", true);
        this.service = service;

        stationMap.put("POSE", 1);
        stationMap.put("JASM", 2);
        stationMap.put("TROC", 3);
        stationMap.put("BONO", 4);
        stationMap.put("STSD", 5);
        stationMap.put("NATN", 6);
        stationMap.put("MAMO", 7);

        statusMap.put("EN_Circulation", 1);
        statusMap.put("EN_MAINTENANCE", 2);
        statusMap.put("GARE", 3);

        JPanel mainPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        mainPanel.add(new JLabel("Statut:"));
        statusComboBox = new JComboBox<>();
        statusComboBox.addItem("EN_Circulation");
        statusComboBox.addItem("GARE");
        statusComboBox.addItem("EN_MAINTENANCE");
        mainPanel.add(statusComboBox);

        mainPanel.add(new JLabel("ID de l'élément de voie:"));
        trackElementIdField = new JTextField(10);
        mainPanel.add(trackElementIdField);

        mainPanel.add(new JLabel("Station:"));
        stationComboBox = new JComboBox<>();
        for (String stationName : stationMap.keySet()) {
            stationComboBox.addItem(stationName);
        }
        mainPanel.add(stationComboBox);

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
            int trackElementId = Integer.parseInt(trackElementIdField.getText().trim());
            if (trackElementId <= 0) {
                JOptionPane.showMessageDialog(this,
                        "L'ID de l'élément de voie doit être un nombre positif",
                        "Erreur de validation",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }

            try {
                if (service.isTrackElementInUse(trackElementId)) {
                    JOptionPane.showMessageDialog(this,
                            "L'élément de voie (ID: " + trackElementId + ") est déjà utilisé par un autre train\n\n" +
                                    "Veuillez choisir un autre élément de voie.",
                            "Élément de voie déjà utilisé",
                            JOptionPane.WARNING_MESSAGE);
                    return false;
                }
            } catch (Exception e) {

                System.err.println("Error : " + e.getMessage());
            }

            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "L'ID de l'élément de voie doit être un nombre entier",
                    "Erreur de validation",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }

    public Train getTrain() {
        if (!confirmed)
            return null;

        TrainStatus status;
        String statusName = (String) statusComboBox.getSelectedItem();
        int statusId = statusMap.get(statusName);
        status = TrainStatus.getById(statusId);

        String stationName = (String) stationComboBox.getSelectedItem();
        int stationId = stationMap.get(stationName);
        Station station = new Station(stationId, stationName);

        int trackElementId = Integer.parseInt(trackElementIdField.getText().trim());
        TrackElement trackElement = new TrackElement(trackElementId);
        trackElement.setStation(station);

        return new Train(0, status, trackElement, station);
    }

    public void addTrain() {
        if (!confirmed)
            return;

        try {
            Train train = getTrain();
            Trains trains = new Trains();
            trains.add(train);

            service.insertTrains(trains);
            JOptionPane.showMessageDialog(this,
                    "Train ajouté avec succès!",
                    "Succès",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            String userFriendlyMessage;

            if (errorMessage != null && (errorMessage.contains("Duplicate entry") ||
                    errorMessage.contains("Un train est déjà associé à cet élément de voie"))) {
                userFriendlyMessage = "Erreur: Un train est déjà associé à cet élément de voie (ID: " +
                        trackElementIdField.getText().trim() + ").\n\n" +

                        "Veuillez choisir un autre élément de voie.";

                JOptionPane.showMessageDialog(this,
                        userFriendlyMessage,
                        "Élément de voie déjà utilisé",
                        JOptionPane.WARNING_MESSAGE);
            } else if (errorMessage != null && errorMessage.contains("ID error")) {
                userFriendlyMessage = "Erreur: L'élément de voie spécifié (ID: " +
                        trackElementIdField.getText().trim() + ") n'existe pas.\n\n" +
                        "Veuillez entrer un ID d'élément de voie valide";

                JOptionPane.showMessageDialog(this,
                        userFriendlyMessage,
                        "Élément de voie invalide",
                        JOptionPane.ERROR_MESSAGE);
            } else if (errorMessage != null && errorMessage.contains("Connection")) {
                userFriendlyMessage = "Erreur de connexion au serveur.\n\n" +
                        "Veuillez vérifier votre connexion réseau et réessayer.";

                JOptionPane.showMessageDialog(this,
                        userFriendlyMessage,
                        "Erreur de connexion",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                userFriendlyMessage = "Une erreur  lors de l'ajout du train:\n\n" + errorMessage;

                JOptionPane.showMessageDialog(this,
                        userFriendlyMessage,
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }

            System.err.println("Error adding train: " + e.getMessage());
            e.printStackTrace();
        }
    }
}