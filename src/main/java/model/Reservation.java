package model;

import exceptions.BusinessRuleException;
import exceptions.InvalidDataException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import validation.Validator;

/**
 * Representa una reserva con cliente, trabajador, habitacion, fechas, tarifa, total y
 * estado.
 */
public class Reservation implements Identifiable {

    /**
     * Estados posibles de una reserva.
     */
    public enum Status {
        /** Reserva vigente que ocupa la habitacion en las fechas indicadas. */
        ACTIVE,
        /** Reserva cancelada que ya no bloquea la habitacion. */
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
    private Status status;

    /**
     * Construye una reserva con los datos completos, validando fechas, tarifa, total y
     * estado.
     * @param id identificador numerico del registro.
     * @param client cliente de la reserva.
     * @param employee trabajador asociado, si existe.
     * @param room habitacion de la reserva.
     * @param checkIn fecha de entrada.
     * @param checkOut fecha de salida.
     * @param pricePerNight precio establecido por cada noche.
     * @param total valor utilizado por la operacion.
     * @param status estado inicial del registro.
     */
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

    /**
     * Crea una reserva activa, verifica las reglas iniciales y calcula el total con la
     * politica recibida.
     * @param id identificador numerico del registro.
     * @param client cliente de la reserva.
     * @param employee trabajador asociado, si existe.
     * @param room habitacion de la reserva.
     * @param checkIn fecha de entrada.
     * @param checkOut fecha de salida.
     * @param policy estrategia de precios que calcula el total.
     * @return reserva activa creada.
     * @throws InvalidDataException si las fechas no forman una estancia valida.
     * @throws BusinessRuleException si el cliente esta inactivo o la habitacion no esta disponible.
     */
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

    /**
     * Calcula la cantidad de noches entre las fechas de entrada y salida.
     * @return cantidad de noches de la reserva.
     */
    public long nights() { return ChronoUnit.DAYS.between(checkIn, checkOut); }

    /**
     * Indica si la reserva se encuentra activa.
     * @return true si la reserva o persona esta activa; de lo contrario, false.
     */
    public boolean isActive() { return status == Status.ACTIVE; }

    /**
     * Cancela la reserva si todavia esta activa.
     * @throws BusinessRuleException si la reserva ya esta cancelada.
     */
    public void cancel() {
        if (!isActive()) {
            throw new BusinessRuleException("La reserva ya esta cancelada.");
        }
        this.status = Status.CANCELLED;
    }

    /**
     * Indica si esta reserva activa ocupa la misma habitacion durante el periodo indicado.
     * @param otherRoom habitacion que se desea comparar con la reserva.
     * @param in fecha inicial del periodo que se consulta.
     * @param out fecha final del periodo que se consulta.
     * @return true si la reserva activa bloquea esa habitacion durante el periodo indicado.
     */
    public boolean occupies(Room otherRoom, LocalDate in, LocalDate out) {
        return isActive() && room.getId() == otherRoom.getId() && checkIn.isBefore(out) && in.isBefore(checkOut);
    }

    /**
     * Devuelve el identificador de la reserva.
     * @return identificador numerico de la entidad.
     */
    @Override
    public int getId() { return id; }

    /**
     * Devuelve el cliente asociado a la reserva.
     * @return cliente asociado a la reserva.
     */
    public Client getClient() { return client; }

    /**
     * Devuelve el trabajador asociado a la reserva, si existe.
     * @return trabajador asociado a la reserva, o null si no fue asignado.
     */
    public Employee getEmployee() { return employee; }

    /**
     * Devuelve la habitacion reservada.
     * @return habitacion asociada a la reserva.
     */
    public Room getRoom() { return room; }

    /**
     * Devuelve la fecha de entrada.
     * @return fecha de entrada.
     */
    public LocalDate getCheckIn() { return checkIn; }

    /**
     * Devuelve la fecha de salida.
     * @return fecha de salida.
     */
    public LocalDate getCheckOut() { return checkOut; }

    /**
     * Devuelve el precio por noche aplicado a la reserva.
     * @return tarifa por noche aplicada.
     */
    public double getPricePerNight() { return pricePerNight; }

    /**
     * Devuelve el total calculado para la estancia.
     * @return importe total de la reserva.
     */
    public double getTotal() { return total; }

    /**
     * Devuelve el estado actual de la reserva.
     * @return estado actual del comprobante.
     */
    public Status getStatus() { return status; }

    /**
     * Devuelve una representacion breve de la reserva.
     * @return representacion textual breve del objeto.
     */
    @Override
    public String toString() {
        return "Reserva #" + id + " | " + room + " | " + checkIn + " -> " + checkOut
                + " | $" + total + " [" + status + "]";
    }
}
