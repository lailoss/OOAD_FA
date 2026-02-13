package payment;

public class CardPayment extends Payment {
    private String cardNumber;
    private String cardHolder;
    private String expiryDate;
    private String cvv;
    private String cardLastFour;
    
    public CardPayment(double amount, String cardNumber, String cardHolder, String expiryDate, String cvv) {
        super(amount);
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.cardLastFour = maskCardNumber();
    }
    
    public boolean validateCard() {
        if (cardNumber == null || cardNumber.length() != 16) {
            return false;
        }
        if (expiryDate == null || expiryDate.length() != 4) {
            return false;
        }
        if (cvv == null || cvv.length() != 3) {
            return false;
        }
        return true;
    }
    
    private String maskCardNumber() {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return "****" + cardNumber.substring(cardNumber.length() - 4);
        }
        return "****";
    }
    
    @Override
    public boolean validate() {
        return validateCard();
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
    
    public String getCardLastFour() {
        return cardLastFour;
    }
}