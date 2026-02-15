package gui;

import facade.ParkingFacade;
import model.Floor;
import model.ParkingLot;
import model.ParkingSpot;
import model.SpotStatus;
import payment.FineSchemeType;
import vehicle.Vehicle;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;
import java.util.Map;

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
    private JLabel occupiedLabel;  
    private JLabel totalSpotsLabel; 
    
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
        
        // Top Stats Panel
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "System Statistics",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        // Revenue Card
        JPanel revenueCard = createStatCard("💰 Total Revenue", "RM 0.00", new Color(230, 255, 230), new Color(0, 100, 0));
        revenueLabel = (JLabel) revenueCard.getComponent(1);
        statsPanel.add(revenueCard);
        
        // Occupancy Card
        JPanel occupancyCard = createStatCard("📊 Occupancy Rate", "0%", new Color(230, 230, 255), new Color(0, 0, 150));
        occupancyLabel = (JLabel) occupancyCard.getComponent(1);
        statsPanel.add(occupancyCard);
        
        // Total Spots Card
        JPanel spotsCard = createStatCard("Total Spots", "150", new Color(255, 255, 230), Color.BLACK);
        totalSpotsLabel = (JLabel) spotsCard.getComponent(1);
        statsPanel.add(spotsCard);
        
        // Occupied Spots Card - STORE REFERENCE TO THIS LABEL
        JPanel occupiedCard = createStatCard("🚗 Occupied", "0", new Color(255, 230, 230), new Color(150, 0, 0));
        occupiedLabel = (JLabel) occupiedCard.getComponent(1); // Store reference
        statsPanel.add(occupiedCard);
        
        // Fine Scheme Panel
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
        
        applySchemeButton = new JButton("Apply Scheme");
        applySchemeButton.setBackground(new Color(255, 165, 0));
        applySchemeButton.setForeground(Color.BLACK);
        applySchemeButton.setFont(new Font("Arial", Font.BOLD, 12));
        configPanel.add(applySchemeButton);
        
        // Spots Table Panel
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
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        refreshButton = new JButton("🔄 Refresh Data");
        refreshButton.setPreferredSize(new Dimension(150, 35));
        refreshButton.setBackground(new Color(255, 165, 0));
        refreshButton.setForeground(Color.BLACK);
        refreshButton.setFont(new Font("Arial", Font.BOLD, 12));
        buttonPanel.add(refreshButton);
        
        spotsViewPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(statsPanel, BorderLayout.NORTH);
        topPanel.add(configPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(spotsViewPanel, BorderLayout.CENTER);
        
        // Action Listeners
        applySchemeButton.addActionListener(e -> {
            FineSchemeType selected = (FineSchemeType) fineSchemeCombo.getSelectedItem();
            facade.setFineScheme(selected);
            schemeLabel.setText(facade.getCurrentFineScheme().getName());
            JOptionPane.showMessageDialog(this, "✅ Fine scheme updated!");
        });
        
        refreshButton.addActionListener(e -> {
            refreshStats();
            refreshSpotsTable();
        });
    }
    
        // Add this method to show more detailed stats
    private String getOccupancyByFloor() {
        StringBuilder sb = new StringBuilder();
        for (int f = 1; f <= 5; f++) {
            Floor floor = parkingLot.getFloor(f);
            if (floor != null) {
                int occupied = floor.getOccupiedCount();
                int total = floor.getTotalSpots();
                sb.append(String.format("Floor %d: %d/%d (%.1f%%)\n", 
                    f, occupied, total, occupied * 100.0 / total));
            }
        }
        return sb.toString();
    }
    private JPanel createStatCard(String title, String value, Color bgColor, Color textColor) {
        JPanel card = new JPanel();
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        card.setBackground(bgColor);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(titleLabel);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 18));
        valueLabel.setForeground(textColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(valueLabel);
        
        return card;
    }
    
    public void refreshStats() {
        // Get REAL data from database
        List<Vehicle> vehicles = facade.getCurrentVehiclesFromDB();
        double revenue = facade.getTotalRevenueFromDB();
        
        // Count occupied vehicles from database
        int occupiedCount = vehicles.size();
        int totalSpots = 150; // Your total spots
        
        // Update labels
        revenueLabel.setText(String.format("RM %.2f", revenue));
        occupiedLabel.setText(String.valueOf(occupiedCount)); // Update occupied count
        
        double occupancyRate = totalSpots > 0 ? (occupiedCount * 100.0 / totalSpots) : 0;
        occupancyLabel.setText(String.format("%.1f%%", occupancyRate));
        
        schemeLabel.setText(facade.getCurrentFineScheme().getName());
        
        // Debug output
        System.out.println("📊 Admin Panel Refreshed: " + occupiedCount + " occupied vehicles");
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