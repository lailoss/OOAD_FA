package payment;

import java.time.LocalDateTime;
import java.util.UUID;

public class Fine {
    private String fineId;
    private String licensePlate;
    private double amount;
    private LocalDateTime issuedDate;
    private String reason;
    private boolean isPaid;
    private LocalDateTime paidDate;
    
    public Fine(String licensePlate, double amount, String reason) {
        this.fineId = "FINE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.licensePlate = licensePlate;
        this.amount = amount;
        this.reason = reason;
        this.issuedDate = LocalDateTime.now();
        this.isPaid = false;
        this.paidDate = null;
    }
    
    public void markPaid() {
        this.isPaid = true;
        this.paidDate = LocalDateTime.now();
    }
    
    public String getFineId() {
        return fineId;
    }
    
    public void setFineId(String fineId) {
        this.fineId = fineId;
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public LocalDateTime getIssuedDate() {
        return issuedDate;
    }
    
    public String getReason() {
        return reason;
    }
    
    public boolean isPaid() {
        return isPaid;
    }
    
    public LocalDateTime getPaidDate() {
        return paidDate;
    }
    
    @Override
    public String toString() {
        return "Fine: RM" + String.format("%.2f", amount) + " - " + reason + 
               " (" + (isPaid ? "Paid" : "Unpaid") + ")";
    }
}