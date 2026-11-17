package model;

import validation.Validator;

/**
 * Politica de precios que aplica un descuento adicional sobre el resultado de otra
 * politica. Sirve para sumar el descuento por categoria de cliente al descuento por
 * estadia larga.
 */
public class DiscountPricing implements PricingPolicy {

    private final PricingPolicy base;
    private final double discountPercent;

    /**
     * Crea una politica que descuenta un porcentaje al total de la politica base.
     * @param base politica cuyo total se reduce.
     * @param discountPercent porcentaje de descuento, mayor que 0 y menor que 100.
     */
    public DiscountPricing(PricingPolicy base, double discountPercent) {
        this.base = Validator.requireNonNull(base, "La politica base");
        this.discountPercent = Validator.requirePercentage(discountPercent, "El porcentaje de descuento");
    }

    /**
     * Calcula el total con la politica base y le resta el descuento adicional.
     * @param pricePerNight tarifa por noche.
     * @param nights cantidad de noches.
     * @return total a pagar despues de aplicar ambos descuentos.
     */
    @Override
    public double calculateTotal(double pricePerNight, long nights) {
        return base.calculateTotal(pricePerNight, nights) * (1 - discountPercent / 100);
    }
}
