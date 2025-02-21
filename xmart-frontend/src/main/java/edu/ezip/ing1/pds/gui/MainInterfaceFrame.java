package edu.ezip.ing1.pds.gui;

import edu.ezip.ing1.pds.client.commons.ConfigLoader;
import edu.ezip.ing1.pds.client.commons.NetworkConfig;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainInterfaceFrame extends JFrame {
    private List<JButton> defaultButtons;
    private JPanel buttonsPanel;
    private JPanel mainPanel;
    private NetworkConfig networkConfig;

    public MainInterfaceFrame() {
        super("PCC");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(600, 400);
        this.networkConfig = ConfigLoader.loadConfig(NetworkConfig.class, "network.yaml");
        this.defaultButtons = new ArrayList<>();

        JButton btnTrain = new JButton("Trains");
        btnTrain.addActionListener(e -> new TrainTableView(this));
        JButton btnSchedule = new JButton("Planning");
        // btnSchedule.addActionListener(e -> new ScheduleTableView(this));
        JButton btnAlerts = new JButton("Alarmes");
        btnAlerts.addActionListener(e -> new AlertTableView(this));

        this.defaultButtons.add(btnTrain);
        this.defaultButtons.add(btnSchedule);
        this.defaultButtons.add(btnAlerts);

        this.buttonsPanel = new JPanel();
        this.add(this.buttonsPanel, BorderLayout.SOUTH);
        this.mainPanel = new JPanel();
        this.add(this.mainPanel, BorderLayout.CENTER);

        this.registerJButtons(null);
        this.setVisible(true);
        btnTrain.doClick();
    }

    public NetworkConfig getNetworkConfig() {
        return this.networkConfig;
    }

    public JPanel getMainJPanel() {
        return this.mainPanel;
    }

    public void registerJButtons(List<JButton> buttons) {
        this.buttonsPanel.removeAll();
        for (JButton btn : this.defaultButtons)
            this.buttonsPanel.add(btn);
        if (buttons != null) {
            for (JButton btn : buttons)
                this.buttonsPanel.add(btn);
        }
        this.buttonsPanel.repaint();
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title + " - PCC");
    }
}
