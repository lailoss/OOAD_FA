package payment;

public class HourlyFineScheme extends FineScheme {
    private static final double HOURLY_RATE = 20.0;
    
    public HourlyFineScheme() {
        super("Hourly Fine (RM20/hr)", FineSchemeType.HOURLY);
    }
    
    @Override
    public double calculateFine(long overstayHours) {
        return overstayHours * HOURLY_RATE;
    }
}