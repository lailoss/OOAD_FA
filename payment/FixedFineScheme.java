package payment;

public class FixedFineScheme extends FineScheme {
    
    public FixedFineScheme() {
        super("Fixed Fine (RM50 flat)", FineSchemeType.FIXED);
    }
    
    @Override
    public double calculateFine(long overstayHours) {
        return 50.0;
    }
}