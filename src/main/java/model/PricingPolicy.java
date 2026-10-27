package model;

/**
 * Para agregar una nueva tarifa se crea otra clase
 * que implemente esta interfaz, sin modificar Reservation.
 */
public interface PricingPolicy {

    double calculateTotal(double pricePerNight, long nights);
}
