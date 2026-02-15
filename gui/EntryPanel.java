package gui;

import facade.ParkingFacade;
import model.ParkingSpot;
import vehicle.Ticket;
import vehicle.VehicleType;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class EntryPanel extends JPanel {
    private ParkingFacade facade;
    private JTextField plateField;
    private JComboBox<VehicleType> typeCombo;
    private JCheckBox cardCheckBox;
    private JList<ParkingSpot> spotList;
    private DefaultListModel<ParkingSpot> spotModel;
    private JButton refreshButton;
    private JButton parkButton;
    private JTextArea ticketArea;
    
    public EntryPanel(ParkingFacade facade) {
        this.facade = facade;
        initialize();
    }
    
    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), 
            "Vehicle Information", 
            TitledBorder.LEFT, 
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("License Plate:"), gbc);
        
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        plateField = new JTextField(15);
        inputPanel.add(plateField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Vehicle Type:"), gbc);
        
        gbc.gridx = 1;
        typeCombo = new JComboBox<>(VehicleType.values());
        typeCombo.setPreferredSize(new Dimension(150, 25));
        inputPanel.add(typeCombo, gbc);
        
        gbc.gridx = 2;
        cardCheckBox = new JCheckBox("Handicapped Card");
        inputPanel.add(cardCheckBox, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        refreshButton = new JButton("🔍 Show Available Spots");
        refreshButton.setPreferredSize(new Dimension(200, 30));
        refreshButton.setBackground(new Color(70, 130, 200));
        refreshButton.setForeground(Color.WHITE);
        inputPanel.add(refreshButton, gbc);
        
        JPanel spotPanel = new JPanel(new BorderLayout());
        spotPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Available Parking Spots",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        spotModel = new DefaultListModel<>();
        spotList = new JList<>(spotModel);
        spotList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        spotList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane spotScrollPane = new JScrollPane(spotList);
        spotScrollPane.setPreferredSize(new Dimension(400, 200));
        spotPanel.add(spotScrollPane, BorderLayout.CENTER);
        
        JPanel actionPanel = new JPanel();
        parkButton = new JButton("🅿️ Park Vehicle");
        parkButton.setPreferredSize(new Dimension(200, 40));
        parkButton.setBackground(new Color(34, 139, 34));
        parkButton.setForeground(Color.WHITE);
        parkButton.setFont(new Font("Arial", Font.BOLD, 14));
        parkButton.setEnabled(false);
        actionPanel.add(parkButton);
        
        JPanel ticketPanel = new JPanel(new BorderLayout());
        ticketPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Parking Ticket",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        ticketArea = new JTextArea();
        ticketArea.setEditable(false);
        ticketArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ticketArea.setBackground(new Color(245, 245, 220));
        JScrollPane ticketScrollPane = new JScrollPane(ticketArea);
        ticketScrollPane.setPreferredSize(new Dimension(400, 300));
        ticketPanel.add(ticketScrollPane, BorderLayout.CENTER);
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(inputPanel, BorderLayout.NORTH);
        leftPanel.add(spotPanel, BorderLayout.CENTER);
        leftPanel.add(actionPanel, BorderLayout.SOUTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, ticketPanel);
        splitPane.setDividerLocation(450);
        
        add(splitPane, BorderLayout.CENTER);
        
        refreshButton.addActionListener(e -> refreshAvailableSpots());
        typeCombo.addActionListener(e -> refreshAvailableSpots());
        
        spotList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                parkButton.setEnabled(spotList.getSelectedValue() != null);
            }
        });
        
        parkButton.addActionListener(e -> parkVehicle());
    }
    
    private void refreshAvailableSpots() {
        VehicleType selectedType = (VehicleType) typeCombo.getSelectedItem();
        List<ParkingSpot> spots = facade.getAvailableSpots(selectedType);
        
        spotModel.clear();
        for (ParkingSpot spot : spots) {
            spotModel.addElement(spot);
        }
        
        if (spots.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "No available spots for " + selectedType + " vehicles.", 
                "No Spots", 
                JOptionPane.WARNING_MESSAGE);
        }
        
        parkButton.setEnabled(false);
    }
    
    private void parkVehicle() {
        String plate = plateField.getText().trim().toUpperCase();
        if (plate.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter license plate number.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        VehicleType type = (VehicleType) typeCombo.getSelectedItem();
        boolean hasCard = cardCheckBox.isSelected();
        ParkingSpot selectedSpot = spotList.getSelectedValue();
        
        try {
            Ticket ticket = facade.parkVehicle(plate, type, hasCard, selectedSpot.getSpotId());
            displayTicket(ticket);
            clearForm();
            
            JOptionPane.showMessageDialog(this, 
                "✅ Vehicle parked successfully!\nTicket ID: " + ticket.getTicketId(), 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error parking vehicle: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void displayTicket(Ticket ticket) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(50)).append("\n");
        sb.append("           PARKING TICKET\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append("Ticket ID: ").append(ticket.getTicketId()).append("\n");
        sb.append("-".repeat(50)).append("\n");
        sb.append("License Plate: ").append(ticket.getVehicle().getLicensePlate()).append("\n");
        sb.append("Vehicle Type: ").append(ticket.getVehicle().getVehicleType()).append("\n");
        sb.append("Spot: ").append(ticket.getSpot().getSpotId()).append("\n");
        sb.append("Floor: ").append(ticket.getSpot().getFloorNum()).append("\n");
        sb.append("Row: ").append(ticket.getSpot().getRowNum()).append("\n");
        sb.append("Rate: RM").append(String.format("%.2f", ticket.getSpot().getHourlyRate())).append("/hour\n");
        sb.append("-".repeat(50)).append("\n");
        sb.append("Entry Time: ").append(ticket.getEntryTime()).append("\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append("     PLEASE KEEP THIS TICKET\n");
        sb.append("=".repeat(50)).append("\n");
        
        ticketArea.setText(sb.toString());
    }
    
    private void clearForm() {
        plateField.setText("");
        cardCheckBox.setSelected(false);
        spotModel.clear();
        parkButton.setEnabled(false);
    }
}