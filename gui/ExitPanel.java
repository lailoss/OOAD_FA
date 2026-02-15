package gui;

import facade.ParkingFacade;
import payment.Payment;
import payment.PaymentMethod;
import payment.Receipt;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

public class ExitPanel extends JPanel {
    private ParkingFacade facade;
    private JTextField plateField;
    private JButton searchButton;
    private JTextArea receiptArea;
    private JPanel paymentPanel;
    private JRadioButton cashButton;
    private JRadioButton cardButton;
    private JButton payButton;
    private JLabel amountLabel;
    private JTextField cashField;
    
    private Receipt currentReceipt;
    private double currentAmount;
    
    public ExitPanel(ParkingFacade facade) {
        this.facade = facade;
        initialize();
    }
    
    private void initialize() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Exit Vehicle",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        searchPanel.add(new JLabel("License Plate:"));
        plateField = new JTextField(15);
        searchPanel.add(plateField);
        
        searchButton = new JButton("🔍 Search");
        searchButton.setPreferredSize(new Dimension(100, 30));
        searchButton.setBackground(new Color(70, 130, 200));
        searchButton.setForeground(Color.WHITE);
        searchPanel.add(searchButton);
        
        JPanel receiptPanel = new JPanel(new BorderLayout());
        receiptPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Exit Receipt",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        
        receiptArea = new JTextArea();
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setBackground(new Color(245, 245, 220));
        JScrollPane scrollPane = new JScrollPane(receiptArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));
        receiptPanel.add(scrollPane, BorderLayout.CENTER);
        
        paymentPanel = new JPanel();
        paymentPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(),
            "Payment",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14)
        ));
        paymentPanel.setLayout(new GridBagLayout());
        paymentPanel.setVisible(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        paymentPanel.add(new JLabel("Total Amount Due:"), gbc);
        
        gbc.gridx = 1;
        amountLabel = new JLabel("RM 0.00");
        amountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        amountLabel.setForeground(Color.RED);
        paymentPanel.add(amountLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        paymentPanel.add(new JLabel("Payment Method:"), gbc);
        
        gbc.gridx = 1;
        JPanel methodPanel = new JPanel();
        cashButton = new JRadioButton("Cash");
        cardButton = new JRadioButton("Card");
        ButtonGroup group = new ButtonGroup();
        group.add(cashButton);
        group.add(cardButton);
        cashButton.setSelected(true);
        methodPanel.add(cashButton);
        methodPanel.add(cardButton);
        paymentPanel.add(methodPanel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel cashLabel = new JLabel("Cash Tendered:");
        paymentPanel.add(cashLabel, gbc);
        
        gbc.gridx = 1;
        cashField = new JTextField(10);
        paymentPanel.add(cashField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        payButton = new JButton("💳 Process Payment");
        payButton.setPreferredSize(new Dimension(200, 40));
        payButton.setBackground(new Color(34, 139, 34));
        payButton.setForeground(Color.WHITE);
        payButton.setFont(new Font("Arial", Font.BOLD, 14));
        paymentPanel.add(payButton, gbc);
        
        cashButton.addActionListener(e -> cashField.setEnabled(true));
        cardButton.addActionListener(e -> cashField.setEnabled(false));
        
        add(searchPanel, BorderLayout.NORTH);
        add(receiptPanel, BorderLayout.CENTER);
        add(paymentPanel, BorderLayout.SOUTH);
        
        searchButton.addActionListener(e -> searchVehicle());
        payButton.addActionListener(e -> processPayment());
    }
    
    private void searchVehicle() {
        String plate = plateField.getText().trim().toUpperCase();
        if (plate.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter license plate number.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            currentReceipt = facade.exitVehicle(plate);
            currentAmount = currentReceipt.getTotalAmount();
            
            displayReceipt(currentReceipt);
            
            paymentPanel.setVisible(true);
            amountLabel.setText(String.format("RM %.2f", currentAmount));
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error: " + ex.getMessage(), 
                "Vehicle Not Found", 
                JOptionPane.ERROR_MESSAGE);
            paymentPanel.setVisible(false);
        }
    }
    
    private void processPayment() {
        if (currentReceipt == null) return;
        
        try {
            Payment payment = null;
            
            if (cashButton.isSelected()) {
                double cashTendered = Double.parseDouble(cashField.getText().trim());
                payment = facade.processPayment(currentAmount, PaymentMethod.CASH, cashTendered);
            } else {
                payment = facade.processPayment(currentAmount, PaymentMethod.CARD, null);
            }
            
            if (payment != null && payment.processPayment()) {
                currentReceipt.setPayment(payment);
                
                facade.payFines(plateField.getText().trim().toUpperCase(), 
                              currentReceipt.getFines());
                
                JOptionPane.showMessageDialog(this, 
                    "✅ Payment successful!\nReceipt ID: " + currentReceipt.getReceiptId(), 
                    "Payment Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                plateField.setText("");
                cashField.setText("");
                paymentPanel.setVisible(false);
                receiptArea.setText("");
                currentReceipt = null;
                
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Payment failed. Please try again.", 
                    "Payment Failed", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid cash amount.", 
                "Invalid Input", 
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Payment error: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void displayReceipt(Receipt receipt) {
        receiptArea.setText(receipt.printReceipt());
    }
}