package model;

public class Room implements Identifiable {

    public enum Type { SINGLE, DOUBLE, SUITE }

    private final int id;
    private final String number;
    private final Type type;
    private final int capacity;
    private double pricePerNight;
    private boolean available;

    public Room(int id, String number, Type type, int capacity, double pricePerNight, boolean available) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id de la habitación debe ser mayor que cero.");
        }
        this.id = id;
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("El número de habitación es obligatorio.");
        }
        this.number = number;
        if (type == null) {
            throw new IllegalArgumentException("El tipo de habitación es obligatorio.");
        }
        this.type = type;
        if (capacity <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor que cero.");
        }
        this.capacity = capacity;
        if (pricePerNight <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        this.pricePerNight = pricePerNight;
        this.available = available;
    }

    public void markAvailable() { this.available = true; }

    public void markUnavailable() { this.available = false; }

    @Override
    public int getId() { return id; }

    public String getNumber() { return number; }

    public Type getType() { return type; }

    public int getCapacity() { return capacity; }

    public double getPricePerNight() { return pricePerNight; }

    public void setPricePerNight(double pricePerNight) {
        if (pricePerNight <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        this.pricePerNight = pricePerNight;
    }

    public boolean isAvailable() { return available; }

    @Override
    public String toString() { return "Habitación " + number + " (" + type + ")"; }
}
