package model;

/** Descuento por estadía larga: desde cierto número de noches se aplica un porcentaje de descuento. */
public class LongStayPricing implements PricingPolicy {

    private final int minNights;
    private final double discountPercent;

    public LongStayPricing(int minNights, double discountPercent) {
        if (minNights <= 0) {
            throw new IllegalArgumentException("El mínimo de noches debe ser mayor que cero.");
        }
        this.minNights = minNights;
        if (discountPercent <= 0) {
            throw new IllegalArgumentException("El porcentaje de descuento debe ser mayor que cero.");
        }
        this.discountPercent = discountPercent;
    }

    @Override
    public double calculateTotal(double pricePerNight, long nights) {
        double base = pricePerNight * nights;
        return nights >= minNights ? base * (1 - discountPercent / 100) : base;
    }
}
