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
        /** Reserva confirmada; el huesped todavia no ha llegado. */
        ACTIVE,
        /** El huesped ya hizo el check-in y esta en la habitacion. */
        CHECKED_IN,
        /** El huesped ya hizo el check-out y la habitacion quedo libre. */
        CHECKED_OUT,
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
    private final int guests;
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
     * @param guests cantidad de personas que se hospedan.
     * @param status estado inicial del registro.
     */
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

    /**
     * Crea una reserva activa, verifica las reglas iniciales y calcula el total con la
     * politica recibida.
     * @param id identificador numerico del registro.
     * @param client cliente de la reserva.
     * @param employee trabajador asociado, si existe.
     * @param room habitacion de la reserva.
     * @param checkIn fecha de entrada.
     * @param checkOut fecha de salida.
     * @param guests cantidad de personas que se hospedaran.
     * @param policy estrategia de precios que calcula el total.
     * @return reserva activa creada.
     * @throws InvalidDataException si las fechas no forman una estancia valida o la entrada es anterior a hoy.
     * @throws BusinessRuleException si el cliente esta inactivo, la habitacion no esta disponible
     *         o no tiene capacidad para todas las personas.
     */
    public static Reservation create(int id, Client client, Employee employee, Room room,
                                     LocalDate checkIn, LocalDate checkOut, int guests,
                                     PricingPolicy policy) {
        Validator.requireNonNull(client, "El cliente");
        Validator.requireNonNull(room, "La habitacion");
        Validator.requireNonNull(checkIn, "La fecha de entrada");
        Validator.requireNonNull(checkOut, "La fecha de salida");
        Validator.requireNonNull(policy, "La politica de precio");
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDataException("La fecha de salida debe ser posterior a la de entrada.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new InvalidDataException("La fecha de entrada no puede ser anterior a hoy.");
        }
        if (!client.isActive()) {
            throw new BusinessRuleException("El cliente esta inactivo.");
        }
        if (!room.isAvailable()) {
            throw new BusinessRuleException("La habitacion " + room.getNumber() + " no esta disponible.");
        }
        Validator.requirePositive(guests, "La cantidad de personas");
        if (guests > room.getCapacity()) {
            throw new BusinessRuleException("La habitacion " + room.getNumber()
                    + " admite maximo " + room.getCapacity() + " personas.");
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double total = policy.calculateTotal(room.getPricePerNight(), nights);
        return new Reservation(id, client, employee, room, checkIn, checkOut,
                room.getPricePerNight(), total, guests, Status.ACTIVE);
    }

    /**
     * Calcula la cantidad de noches entre las fechas de entrada y salida.
     * @return cantidad de noches de la reserva.
     */
    public long nights() { return ChronoUnit.DAYS.between(checkIn, checkOut); }

    /**
     * Indica si la reserva esta vigente, es decir, si bloquea la habitacion. Lo esta
     * mientras el huesped no haya llegado (ACTIVE) o este hospedado (CHECKED_IN).
     * @return true si la reserva bloquea la habitacion; de lo contrario, false.
     */
    public boolean isActive() { return status == Status.ACTIVE || status == Status.CHECKED_IN; }

    /**
     * Indica si la reserva fue cancelada.
     * @return true si la reserva esta cancelada.
     */
    public boolean isCancelled() { return status == Status.CANCELLED; }

    /**
     * Cancela la reserva mientras el huesped todavia no haya hecho el check-in.
     * @throws BusinessRuleException si la reserva ya esta cancelada o el huesped ya llego.
     */
    public void cancel() {
        if (isCancelled()) {
            throw new BusinessRuleException("La reserva ya esta cancelada.");
        }
        if (status != Status.ACTIVE) {
            throw new BusinessRuleException("No se puede cancelar una reserva que ya tuvo check-in.");
        }
        this.status = Status.CANCELLED;
    }

    /**
     * Registra la llegada del huesped. Solo se puede hacer entre la fecha de entrada y la
     * fecha de salida.
     * @param today fecha en la que se hace el check-in.
     * @throws BusinessRuleException si la reserva no esta confirmada o la fecha no corresponde.
     */
    public void registerCheckIn(LocalDate today) {
        if (status != Status.ACTIVE) {
            throw new BusinessRuleException("Solo se puede hacer check-in de una reserva confirmada.");
        }
        if (today.isBefore(checkIn)) {
            throw new BusinessRuleException("Todavia no es la fecha de entrada (" + checkIn + ").");
        }
        if (!today.isBefore(checkOut)) {
            throw new BusinessRuleException("La fecha de salida de esta reserva ya paso.");
        }
        this.status = Status.CHECKED_IN;
    }

    /**
     * Registra la salida del huesped y libera la habitacion.
     * @throws BusinessRuleException si el huesped no ha hecho el check-in.
     */
    public void registerCheckOut() {
        if (status != Status.CHECKED_IN) {
            throw new BusinessRuleException("Solo se puede hacer check-out de una reserva con check-in.");
        }
        this.status = Status.CHECKED_OUT;
    }

    /**
     * Indica si esta reserva vigente ocupa la misma habitacion durante el periodo indicado.
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
     * Devuelve la cantidad de personas que se hospedan.
     * @return cantidad de huespedes de la reserva.
     */
    public int getGuests() { return guests; }

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
        return "Reserva #" + id + " | " + room + " | " + guests + " personas | " + checkIn + " -> " + checkOut
                + " | $" + total + " [" + status + "]";
    }
}
