package model;

import validation.Validator;

/**
 * Calcula el total de una estancia y aplica un descuento cuando se alcanza el minimo de
 * noches configurado.
 */
public class LongStayPricing implements PricingPolicy {

    private final int minNights;
    private final double discountPercent;

    /**
     * Crea tarifas con un minimo de noches y un porcentaje de descuento.
     * @param minNights cantidad minima de noches para aplicar el descuento.
     * @param discountPercent porcentaje de descuento que se aplicara.
     */
    public LongStayPricing(int minNights, double discountPercent) {
        this.minNights = Validator.requirePositive(minNights, "El minimo de noches");
        this.discountPercent = Validator.requirePercentage(discountPercent, "El porcentaje de descuento");
    }

    /**
     * Calcula el total de la estancia y aplica el descuento cuando se alcanza el minimo
     * configurado.
     * @param pricePerNight precio establecido por cada noche.
     * @param nights cantidad de noches de la estancia.
     * @return precio total calculado para la estancia.
     */
    @Override
    public double calculateTotal(double pricePerNight, long nights) {
        double base = pricePerNight * nights;
        return nights >= minNights ? base * (1 - discountPercent / 100) : base;
    }
}
