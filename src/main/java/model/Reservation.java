package model;

import exceptions.BusinessRuleException;
import exceptions.InvalidDataException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
        if (id <= 0) {
            throw new InvalidDataException("El id de la reserva debe ser mayor que cero.");
        }
        this.id = id;
        if (client == null) {
            throw new InvalidDataException("El cliente es obligatorio.");
        }
        this.client = client;
        this.employee = employee;
        if (room == null) {
            throw new InvalidDataException("La habitacion es obligatorio.");
        }
        this.room = room;
        if (checkIn == null) {
            throw new InvalidDataException("La fecha de entrada es obligatorio.");
        }
        this.checkIn = checkIn;
        if (checkOut == null) {
            throw new InvalidDataException("La fecha de salida es obligatorio.");
        }
        this.checkOut = checkOut;
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDataException("La fecha de salida debe ser posterior a la de entrada.");
        }
        if (pricePerNight <= 0) {
            throw new InvalidDataException("El precio debe ser mayor que cero.");
        }
        this.pricePerNight = pricePerNight;
        if (total <= 0) {
            throw new InvalidDataException("El total debe ser mayor que cero.");
        }
        this.total = total;
        if (status == null) {
            throw new InvalidDataException("El estado es obligatorio.");
        }
        this.status = status;
    }

    public static Reservation create(int id, Client client, Employee employee, Room room,
                                     LocalDate checkIn, LocalDate checkOut, PricingPolicy policy) {
        if (client == null) {
            throw new InvalidDataException("El cliente es obligatorio.");
        }
        if (room == null) {
            throw new InvalidDataException("La habitacion es obligatorio.");
        }
        if (checkIn == null) {
            throw new InvalidDataException("La fecha de entrada es obligatorio.");
        }
        if (checkOut == null) {
            throw new InvalidDataException("La fecha de salida es obligatorio.");
        }
        if (policy == null) {
            throw new InvalidDataException("La politica de precio es obligatorio.");
        }
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
