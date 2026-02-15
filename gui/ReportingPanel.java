package gui;

import facade.ParkingFacade;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

public class ReportingPanel extends JPanel {
    private ParkingFacade facade;
    private JComboBox<String> reportTypeCombo;
    private JButton generateButton;
    private JTextArea reportArea;
    
    public ReportingPanel(ParkingFacade facade) {
        this.facade = facade;
        initialize();
    }
    
    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Generate Report",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        topPanel.add(new JLabel("Report Type:"));
        
        String[] reportTypes = {
            "Current Vehicles",
            "Revenue Report",
            "Occupancy Report",
            "Fine Report"
        };
        reportTypeCombo = new JComboBox<>(reportTypes);
        reportTypeCombo.setPreferredSize(new Dimension(200, 30));
        topPanel.add(reportTypeCombo);
        
        generateButton = new JButton("📊 Generate Report");
        generateButton.setPreferredSize(new Dimension(150, 30));
        generateButton.setBackground(new Color(70, 130, 200));
        generateButton.setForeground(Color.WHITE);
        topPanel.add(generateButton);
        
        JPanel reportPanel = new JPanel(new BorderLayout());
        reportPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Report Output",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setBackground(new Color(245, 245, 220));
        JScrollPane scrollPane = new JScrollPane(reportArea);
        scrollPane.setPreferredSize(new Dimension(800, 500));
        reportPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(reportPanel, BorderLayout.CENTER);
        
        generateButton.addActionListener(e -> generateReport());
    }
    
    private void generateReport() {
        String selected = (String) reportTypeCombo.getSelectedItem();
        String report = "";
        
        switch (selected) {
            case "Current Vehicles":
                report = facade.getCurrentVehiclesReport();
                break;
            case "Revenue Report":
                report = facade.getRevenueReport();
                break;
            case "Occupancy Report":
                report = facade.getOccupancyReport();
                break;
            case "Fine Report":
                report = facade.getFineReport();
                break;
        }
        
        reportArea.setText(report);
    }
}