package payment;

public abstract class FineScheme {
    protected String name;
    protected FineSchemeType type;
    
    public FineScheme(String name, FineSchemeType type) {
        this.name = name;
        this.type = type;
    }
    
    public abstract double calculateFine(long overstayHours);
    
    public String getName() {
        return name;
    }
    
    public FineSchemeType getType() {
        return type;
    }
}