package payment;

public class CashPayment extends Payment {
    private double cashTendered;
    private double change;
    
    public CashPayment(double amount, double cashTendered) {
        super(amount);
        this.cashTendered = cashTendered;
        this.change = calculateChange();
    }
    
    public double calculateChange() {
        return cashTendered - amount;
    }
    
    @Override
    public boolean validate() {
        return cashTendered >= amount;
    }
    
    @Override
    public boolean processPayment() {
        if (validate()) {
            this.status = "COMPLETED";
            return true;
        } else {
            this.status = "FAILED";
            return false;
        }
    }
    
    public double getCashTendered() {
        return cashTendered;
    }
    
    public double getChange() {
        return change;
    }
}