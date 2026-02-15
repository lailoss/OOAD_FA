package gui;

import facade.ParkingFacade;
import model.ParkingLot;
import model.ParkingSpot;
import model.SpotStatus;
import payment.FineSchemeType;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class AdminPanel extends JPanel {
    private ParkingFacade facade;
    private ParkingLot parkingLot;
    private JComboBox<FineSchemeType> fineSchemeCombo;
    private JButton applySchemeButton;
    private JLabel revenueLabel;
    private JLabel occupancyLabel;
    private JLabel schemeLabel;
    private JTable spotsTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;
    
    public AdminPanel(ParkingFacade facade) {
        this.facade = facade;
        this.parkingLot = facade.getParkingLot();
        initialize();
        refreshStats();
        refreshSpotsTable();
    }
    
    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "System Statistics",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        JPanel revenueCard = new JPanel();
        revenueCard.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        revenueCard.setBackground(new Color(230, 255, 230));
        revenueCard.setLayout(new BoxLayout(revenueCard, BoxLayout.Y_AXIS));
        revenueCard.add(new JLabel("💰 Total Revenue"));
        revenueLabel = new JLabel("RM 0.00");
        revenueLabel.setFont(new Font("Arial", Font.BOLD, 18));
        revenueLabel.setForeground(new Color(0, 100, 0));
        revenueCard.add(revenueLabel);
        statsPanel.add(revenueCard);
        
        JPanel occupancyCard = new JPanel();
        occupancyCard.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        occupancyCard.setBackground(new Color(230, 230, 255));
        occupancyCard.setLayout(new BoxLayout(occupancyCard, BoxLayout.Y_AXIS));
        occupancyCard.add(new JLabel("📊 Occupancy Rate"));
        occupancyLabel = new JLabel("0%");
        occupancyLabel.setFont(new Font("Arial", Font.BOLD, 18));
        occupancyLabel.setForeground(new Color(0, 0, 150));
        occupancyCard.add(occupancyLabel);
        statsPanel.add(occupancyCard);
        
        JPanel spotsCard = new JPanel();
        spotsCard.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        spotsCard.setBackground(new Color(255, 255, 230));
        spotsCard.setLayout(new BoxLayout(spotsCard, BoxLayout.Y_AXIS));
        spotsCard.add(new JLabel("🅿️ Total Spots"));
        JLabel totalSpotsLabel = new JLabel(String.valueOf(parkingLot.getTotalSpots()));
        totalSpotsLabel.setFont(new Font("Arial", Font.BOLD, 18));
        spotsCard.add(totalSpotsLabel);
        statsPanel.add(spotsCard);
        
        JPanel occupiedCard = new JPanel();
        occupiedCard.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        occupiedCard.setBackground(new Color(255, 230, 230));
        occupiedCard.setLayout(new BoxLayout(occupiedCard, BoxLayout.Y_AXIS));
        occupiedCard.add(new JLabel("🚗 Occupied"));
        JLabel occupiedLabel = new JLabel(String.valueOf(parkingLot.getOccupiedSpots()));
        occupiedLabel.setFont(new Font("Arial", Font.BOLD, 18));
        occupiedLabel.setForeground(new Color(150, 0, 0));
        occupiedCard.add(occupiedLabel);
        statsPanel.add(occupiedCard);
        
        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        configPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Fine Scheme Configuration",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        configPanel.add(new JLabel("Current Scheme:"));
        schemeLabel = new JLabel(facade.getCurrentFineScheme().getName());
        schemeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        schemeLabel.setForeground(Color.BLUE);
        configPanel.add(schemeLabel);
        
        configPanel.add(new JLabel("Change to:"));
        fineSchemeCombo = new JComboBox<>(FineSchemeType.values());
        fineSchemeCombo.setPreferredSize(new Dimension(150, 25));
        configPanel.add(fineSchemeCombo);
        
        applySchemeButton = new JButton("Apply");
        applySchemeButton.setBackground(new Color(255, 165, 0));
        applySchemeButton.setForeground(Color.WHITE);
        configPanel.add(applySchemeButton);
        
        JPanel spotsViewPanel = new JPanel(new BorderLayout());
        spotsViewPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Parking Spots Status",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        String[] columns = {"Spot ID", "Floor", "Row", "Type", "Rate", "Status", "Current Vehicle"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        spotsTable = new JTable(tableModel);
        spotsTable.setFont(new Font("Monospaced", Font.PLAIN, 11));
        spotsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        spotsTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(spotsTable);
        scrollPane.setPreferredSize(new Dimension(800, 300));
        spotsViewPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        refreshButton = new JButton("🔄 Refresh Data");
        refreshButton.setPreferredSize(new Dimension(150, 30));
        refreshButton.setBackground(new Color(70, 130, 200));
        refreshButton.setForeground(Color.WHITE);
        buttonPanel.add(refreshButton);
        spotsViewPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(statsPanel, BorderLayout.NORTH);
        topPanel.add(configPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(spotsViewPanel, BorderLayout.CENTER);
        
        applySchemeButton.addActionListener(e -> {
            FineSchemeType selected = (FineSchemeType) fineSchemeCombo.getSelectedItem();
            facade.setFineScheme(selected);
            schemeLabel.setText(facade.getCurrentFineScheme().getName());
            
            JOptionPane.showMessageDialog(AdminPanel.this,
                "✅ Fine scheme changed to: " + selected,
                "Scheme Updated",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        refreshButton.addActionListener(e -> {
            refreshStats();
            refreshSpotsTable();
        });
    }
    
    public void refreshStats() {
        revenueLabel.setText(String.format("RM %.2f", parkingLot.getTotalRevenue()));
        occupancyLabel.setText(String.format("%.1f%%", parkingLot.getOccupancyRate()));
        schemeLabel.setText(facade.getCurrentFineScheme().getName());
    }
    
    private void refreshSpotsTable() {
        tableModel.setRowCount(0);
        
        for (ParkingSpot spot : parkingLot.getAllSpots()) {
            String vehiclePlate = "";
            if (spot.getCurrentVehicle() != null) {
                vehiclePlate = spot.getCurrentVehicle().getLicensePlate();
            }
            
            Object[] row = {
                spot.getSpotId(),
                spot.getFloorNum(),
                spot.getRowNum(),
                spot.getType(),
                String.format("RM %.2f", spot.getHourlyRate()),
                spot.getStatus(),
                vehiclePlate
            };
            
            tableModel.addRow(row);
        }
    }
}