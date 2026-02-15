package gui;

import facade.ParkingFacade;
import javax.swing.*;
import java.awt.*;

public class MainGUI extends JFrame {
    private ParkingFacade facade;
    private JTabbedPane tabbedPane;
    private EntryPanel entryPanel;
    private ExitPanel exitPanel;
    private AdminPanel adminPanel;
    private ReportingPanel reportingPanel;
    
    public MainGUI() {
        this.facade = ParkingFacade.getInstance();
        initialize();
    }
    
    private void initialize() {
        setTitle("Parking Lot Management System - CCP6224");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        tabbedPane = new JTabbedPane();
        
        entryPanel = new EntryPanel(facade);
        exitPanel = new ExitPanel(facade);
        adminPanel = new AdminPanel(facade);
        reportingPanel = new ReportingPanel(facade);
        
        tabbedPane.addTab("🚗 ENTRY", entryPanel);
        tabbedPane.addTab("🚙 EXIT", exitPanel);
        tabbedPane.addTab("ADMIN", adminPanel);
        tabbedPane.addTab("📊 REPORTS", reportingPanel);
        
        add(tabbedPane);
        
        setUIFont(new javax.swing.plaf.FontUIResource("Arial", Font.PLAIN, 12));
    }
    
    private void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }
}