package model;

/** Tarifa normal precio por noche × número de noches. */
public class StandardPricing implements PricingPolicy {

    @Override
    public double calculateTotal(double pricePerNight, long nights) {
        return pricePerNight * nights;
    }
}
