package edu.ezip.ing1.pds.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import edu.ezip.ing1.pds.business.dto.Train;
import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;
import edu.ezip.ing1.pds.services.TrainService;

public class AddTrainStyle extends JDialog {
    private boolean confirmed = false;
    private final TrainService service;
    private final MainInterfaceFrame frame;

    public AddTrainStyle(MainInterfaceFrame frame) {
        super(frame, "Ajouter un train", true);
        this.service = new TrainService(ConfigLoader.loadConfig(NetworkConfig.class, "network.yaml"));
        this.frame = frame;

    
        setBackground(new Color(245, 245, 245));
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true));

    
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(255, 255, 255),
                        0, getHeight(), new Color(240, 240, 240));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));

    
        JPanel messagePanel = new JPanel(new BorderLayout(0, 15));
        messagePanel.setOpaque(false);

    
        JLabel messageLabel = new JLabel("Voulez-vous ajouter un nouveau train ?");
        messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        messageLabel.setForeground(new Color(44, 62, 80));
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        


    
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton cancelButton = MainInterfaceFrame.createButton("Annuler", new Color(231, 76, 60));
        JButton confirmButton = MainInterfaceFrame.createButton("Confirmer", new Color(46, 204, 113));

        cancelButton.addActionListener(e -> dispose());
        confirmButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);


        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);


        setMinimumSize(new Dimension(450, 250));
        setLocationRelativeTo(this.frame);
        setResizable(false);
    }

    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }

    public void addTrain() {
        if (!confirmed) return;
        
        try {
            Train train = new Train();
            this.service.insertTrain(train);


            JDialog successDialog = new JDialog(this.frame, "Succès", true);
            successDialog.setLayout(new BorderLayout());
            successDialog.getContentPane().setBackground(Color.WHITE);

            JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
            contentPanel.setBackground(Color.WHITE);
            contentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));

            JLabel messageLabel = new JLabel("Train ajouté avec succès !");
            messageLabel.setFont(new Font("Arial", Font.BOLD, 16));
            messageLabel.setForeground(new Color(46, 204, 113));
            messageLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JButton okButton = MainInterfaceFrame.createButton("OK", new Color(46, 204, 113));
            okButton.addActionListener(e -> {
                successDialog.dispose();
                new TrainTableView(this.frame);
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.add(okButton);

            contentPanel.add(messageLabel, BorderLayout.CENTER);
            contentPanel.add(buttonPanel, BorderLayout.SOUTH);

            successDialog.add(contentPanel);
            successDialog.setMinimumSize(new Dimension(300, 200));
            successDialog.setLocationRelativeTo(this.frame);
            successDialog.setResizable(false);
            successDialog.setVisible(true);

        } catch (Exception e) {
            String errorMessage = e.getMessage();
            String userFriendlyMessage;

            if (errorMessage != null && errorMessage.contains("Connection")) {
                userFriendlyMessage = "Erreur de connexion au serveur\n\n" +
                    "Veuillez vérifier votre connexion réseau et réessayer";
            } else {
                userFriendlyMessage = "Une erreur est survenue lors de l'ajout du train\n\n" +
                    "Détails techniques : " + errorMessage;
            }

            JOptionPane.showMessageDialog(this,
                userFriendlyMessage,
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}