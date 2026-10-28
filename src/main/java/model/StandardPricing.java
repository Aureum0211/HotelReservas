package model;

/**
 * Implementa la tarifa normal, calculada multiplicando el precio por noche por la cantidad
 * de noches.
 */
public class StandardPricing implements PricingPolicy {

    /**
     * Calcula el total multiplicando la tarifa por noche por la cantidad de noches.
     * @param pricePerNight precio establecido por cada noche.
     * @param nights cantidad de noches de la estancia.
     * @return precio total calculado para la estancia.
     */
    @Override
    public double calculateTotal(double pricePerNight, long nights) {
        return pricePerNight * nights;
    }
}
