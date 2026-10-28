package model;

/**
 * Define el contrato de una estrategia que calcula el precio total de una estancia.
 */
public interface PricingPolicy {

    /**
     * Calcula el precio total de una estancia segun la estrategia implementada.
     * @param pricePerNight precio establecido por cada noche.
     * @param nights cantidad de noches de la estancia.
     * @return precio total calculado para la estancia.
     */
    double calculateTotal(double pricePerNight, long nights);
}
