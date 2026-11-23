package model;

import exceptions.InvalidDataException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import validation.Validator;

/** Datos de una reserva del hotel. */
public class Reservation implements Identifiable {

    /** Estados de una reserva. */
    public enum Status {
        
        ACTIVE,
        
        CHECKED_IN,
        
        CHECKED_OUT,
        
        CANCELLED
    }

    private final int id;
    private final Client client;
    private final Employee employee;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final double pricePerNight;
    private final double total;
    private final int guests;
    private final Status status;

    public Reservation(int id, Client client, Employee employee, Room room, LocalDate checkIn,
                       LocalDate checkOut, double pricePerNight, double total, int guests,
                       Status status) {
        this.id = Validator.requirePositive(id, "El id de la reserva");
        this.client = Validator.requireNonNull(client, "El cliente");
        this.employee = employee;
        this.room = Validator.requireNonNull(room, "La habitacion");
        this.checkIn = Validator.requireNonNull(checkIn, "La fecha de entrada");
        this.checkOut = Validator.requireNonNull(checkOut, "La fecha de salida");
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDataException("La fecha de salida debe ser posterior a la de entrada.");
        }
        this.pricePerNight = Validator.requirePositive(pricePerNight, "El precio");
        this.total = Validator.requirePositive(total, "El total");
        this.guests = Validator.requirePositive(guests, "La cantidad de personas");
        this.status = Validator.requireNonNull(status, "El estado");
    }

    public long nights() { return ChronoUnit.DAYS.between(checkIn, checkOut); }

    public boolean isActive() { return status == Status.ACTIVE || status == Status.CHECKED_IN; }

    public boolean isCancelled() { return status == Status.CANCELLED; }

    @Override
    public int getId() { return id; }

    public Client getClient() { return client; }

    public Employee getEmployee() { return employee; }

    public Room getRoom() { return room; }

    public LocalDate getCheckIn() { return checkIn; }

    public LocalDate getCheckOut() { return checkOut; }

    public double getPricePerNight() { return pricePerNight; }

    public double getTotal() { return total; }

    public int getGuests() { return guests; }

    public Status getStatus() { return status; }

    @Override
    public String toString() {
        return "Reserva #" + id + " | " + room + " | " + guests + " personas | " + checkIn + " -> " + checkOut
                + " | $" + total + " [" + status + "]";
    }
}
