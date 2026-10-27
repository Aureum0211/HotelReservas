package model;

import validation.Validator;

public class LongStayPricing implements PricingPolicy {

    private final int minNights;
    private final double discountPercent;

    public LongStayPricing(int minNights, double discountPercent) {
        this.minNights = Validator.requirePositive(minNights, "El minimo de noches");
        this.discountPercent = Validator.requirePercentage(discountPercent, "El porcentaje de descuento");
    }

    @Override
    public double calculateTotal(double pricePerNight, long nights) {
        double base = pricePerNight * nights;
        return nights >= minNights ? base * (1 - discountPercent / 100) : base;
    }
}
