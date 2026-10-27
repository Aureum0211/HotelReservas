package model;

import exceptions.BusinessRuleException;
import exceptions.InvalidDataException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import validation.Validator;

public class Reservation implements Identifiable {

    public enum Status { ACTIVE, CANCELLED }

    private final int id;
    private final Client client;
    private final Employee employee;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final double pricePerNight;
    private final double total;
    private Status status;

    public Reservation(int id, Client client, Employee employee, Room room, LocalDate checkIn,
                       LocalDate checkOut, double pricePerNight, double total, Status status) {
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
        this.status = Validator.requireNonNull(status, "El estado");
    }

    public static Reservation create(int id, Client client, Employee employee, Room room,
                                     LocalDate checkIn, LocalDate checkOut, PricingPolicy policy) {
        Validator.requireNonNull(client, "El cliente");
        Validator.requireNonNull(room, "La habitacion");
        Validator.requireNonNull(checkIn, "La fecha de entrada");
        Validator.requireNonNull(checkOut, "La fecha de salida");
        Validator.requireNonNull(policy, "La politica de precio");
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDataException("La fecha de salida debe ser posterior a la de entrada.");
        }
        if (!client.isActive()) {
            throw new BusinessRuleException("El cliente esta inactivo.");
        }
        if (!room.isAvailable()) {
            throw new BusinessRuleException("La habitacion " + room.getNumber() + " no esta disponible.");
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double total = policy.calculateTotal(room.getPricePerNight(), nights);
        return new Reservation(id, client, employee, room, checkIn, checkOut,
                room.getPricePerNight(), total, Status.ACTIVE);
    }

    public long nights() { return ChronoUnit.DAYS.between(checkIn, checkOut); }

    public boolean isActive() { return status == Status.ACTIVE; }

    public void cancel() {
        if (!isActive()) {
            throw new BusinessRuleException("La reserva ya esta cancelada.");
        }
        this.status = Status.CANCELLED;
    }

    public boolean occupies(Room otherRoom, LocalDate in, LocalDate out) {
        return isActive() && room.getId() == otherRoom.getId() && checkIn.isBefore(out) && in.isBefore(checkOut);
    }

    @Override
    public int getId() { return id; }

    public Client getClient() { return client; }

    public Employee getEmployee() { return employee; }

    public Room getRoom() { return room; }

    public LocalDate getCheckIn() { return checkIn; }

    public LocalDate getCheckOut() { return checkOut; }

    public double getPricePerNight() { return pricePerNight; }

    public double getTotal() { return total; }

    public Status getStatus() { return status; }

    @Override
    public String toString() {
        return "Reserva #" + id + " | " + room + " | " + checkIn + " -> " + checkOut
                + " | $" + total + " [" + status + "]";
    }
}
