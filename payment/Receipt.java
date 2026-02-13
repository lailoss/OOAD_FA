package payment;

import vehicle.Ticket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Receipt {
    private String receiptId;
    private Ticket ticket;
    private LocalDateTime exitTime;
    private long durationHours;
    private double parkingFee;
    private List<Fine> fines;
    private double totalFines;
    private double totalAmount;
    private Payment payment;
    
    public Receipt(Ticket ticket) {
        this.receiptId = "RCP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.ticket = ticket;
        this.fines = new ArrayList<>();
        this.totalFines = 0.0;
        this.exitTime = LocalDateTime.now();
    }
    
    public void addFine(Fine fine) {
        fines.add(fine);
        totalFines += fine.getAmount();
    }
    
    public double calculateTotal() {
        this.totalAmount = parkingFee + totalFines;
        return totalAmount;
    }
    
    public String getReceiptId() {
        return receiptId;
    }
    
    public Ticket getTicket() {
        return ticket;
    }
    
    public LocalDateTime getExitTime() {
        return exitTime;
    }
    
    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }
    
    public long getDurationHours() {
        return durationHours;
    }
    
    public void setDurationHours(long durationHours) {
        this.durationHours = durationHours;
    }
    
    public double getParkingFee() {
        return parkingFee;
    }
    
    public void setParkingFee(double parkingFee) {
        this.parkingFee = parkingFee;
    }
    
    public List<Fine> getFines() {
        return fines;
    }
    
    public double getTotalFines() {
        return totalFines;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public Payment getPayment() {
        return payment;
    }
    
    public void setPayment(Payment payment) {
        this.payment = payment;
    }
    
    public String printReceipt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("                    PARKING RECEIPT\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append("Receipt ID: ").append(receiptId).append("\n");
        sb.append("Ticket ID: ").append(ticket.getTicketId()).append("\n");
        sb.append("-".repeat(60)).append("\n");
        sb.append("License Plate: ").append(ticket.getVehicle().getLicensePlate()).append("\n");
        sb.append("Vehicle Type: ").append(ticket.getVehicle().getVehicleType()).append("\n");
        sb.append("Spot: ").append(ticket.getSpot().getSpotId()).append(" (")
          .append(ticket.getSpot().getType()).append(")\n");
        sb.append("-".repeat(60)).append("\n");
        sb.append("Entry Time: ").append(ticket.getEntryTime().format(formatter)).append("\n");
        sb.append("Exit Time: ").append(exitTime.format(formatter)).append("\n");
        sb.append("Duration: ").append(durationHours).append(" hours\n");
        sb.append("-".repeat(60)).append("\n");
        sb.append("Parking Fee: RM").append(String.format("%.2f", parkingFee)).append("\n");
        
        if (!fines.isEmpty()) {
            sb.append("\nFINES APPLIED:\n");
            for (Fine fine : fines) {
                sb.append("  • ").append(fine.getReason()).append(": RM")
                  .append(String.format("%.2f", fine.getAmount())).append("\n");
            }
            sb.append("Total Fines: RM").append(String.format("%.2f", totalFines)).append("\n");
        }
        
        sb.append("-".repeat(60)).append("\n");
        sb.append("TOTAL AMOUNT: RM").append(String.format("%.2f", totalAmount)).append("\n");
        sb.append("-".repeat(60)).append("\n");
        
        if (payment != null) {
            if (payment instanceof CashPayment) {
                CashPayment cash = (CashPayment) payment;
                sb.append("Payment Method: CASH\n");
                sb.append("Cash Tendered: RM").append(String.format("%.2f", cash.getCashTendered())).append("\n");
                sb.append("Change: RM").append(String.format("%.2f", cash.getChange())).append("\n");
            } else if (payment instanceof CardPayment) {
                CardPayment card = (CardPayment) payment;
                sb.append("Payment Method: CARD\n");
                sb.append("Card: ").append(card.getCardLastFour()).append("\n");
            }
            sb.append("Payment Status: ").append(payment.getStatus()).append("\n");
        }
        
        sb.append("=".repeat(60)).append("\n");
        sb.append("                    THANK YOU!\n");
        sb.append("           Please come again!\n");
        sb.append("=".repeat(60)).append("\n");
        
        return sb.toString();
    }
}