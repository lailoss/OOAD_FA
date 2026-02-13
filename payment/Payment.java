package payment;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Payment {
    protected String paymentId;
    protected double amount;
    protected LocalDateTime paymentTime;
    protected String status;
    
    public Payment(double amount) {
        this.paymentId = UUID.randomUUID().toString();
        this.amount = amount;
        this.paymentTime = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    public abstract boolean processPayment();
    public abstract boolean validate();
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    protected void setStatus(String status) {
        this.status = status;
    }
}