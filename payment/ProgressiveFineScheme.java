package payment;

public class ProgressiveFineScheme extends FineScheme {
    
    public ProgressiveFineScheme() {
        super("Progressive Fine", FineSchemeType.PROGRESSIVE);
    }
    
    @Override
    public double calculateFine(long overstayHours) {
        if (overstayHours <= 24) {
            return 50.0;
        } else if (overstayHours <= 48) {
            return 150.0;
        } else if (overstayHours <= 72) {
            return 300.0;
        } else {
            return 500.0;
        }
    }
}