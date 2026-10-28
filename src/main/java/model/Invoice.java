package model;

import exceptions.BusinessRuleException;
import java.time.LocalDate;
import validation.Validator;

/**
 * Representa el comprobante de pago asociado a una reserva y controla su estado de pago.
 */
public class Invoice implements Identifiable {

    /**
     * Metodos de pago admitidos para un comprobante.
     */
    public enum PaymentMethod {
        /** Pago realizado en efectivo. */
        CASH,
        /** Pago realizado con tarjeta. */
        CARD,
        /** Pago realizado mediante transferencia. */
        TRANSFER
    }

    /**
     * Estados posibles de un comprobante.
     */
    public enum Status {
        /** Comprobante emitido cuyo pago esta pendiente. */
        PENDING,
        /** Comprobante cuyo pago ya fue registrado. */
        PAID,
        /** Comprobante que fue anulado. */
        VOIDED
    }

    private final int id;
    private final Reservation reservation;
    private final LocalDate issueDate;
    private final double amount;
    private final PaymentMethod paymentMethod;
    private Status status;

    /**
     * Construye un comprobante con los datos de emision y su estado inicial o recuperado
     * del archivo.
     * @param id identificador numerico del registro.
     * @param reservation reserva relacionada con la operacion.
     * @param issueDate fecha de emision del comprobante.
     * @param amount monto del comprobante.
     * @param paymentMethod metodo de pago.
     * @param status estado inicial del registro.
     */
    public Invoice(int id, Reservation reservation, LocalDate issueDate, double amount,
                   PaymentMethod paymentMethod, Status status) {
        this.id = Validator.requirePositive(id, "El id del comprobante");
        this.reservation = Validator.requireNonNull(reservation, "La reserva");
        this.issueDate = Validator.requireNonNull(issueDate, "La fecha de emision");
        this.amount = Validator.requirePositive(amount, "El monto");
        this.paymentMethod = Validator.requireNonNull(paymentMethod, "El metodo de pago");
        this.status = Validator.requireNonNull(status, "El estado");
    }

    /**
     * Emite un comprobante pendiente a partir del total de una reserva activa.
     * @param id identificador numerico del registro.
     * @param reservation reserva relacionada con la operacion.
     * @param method metodo de pago seleccionado.
     * @return comprobante emitido en estado pendiente.
     * @throws BusinessRuleException si la reserva esta cancelada.
     */
    public static Invoice issue(int id, Reservation reservation, PaymentMethod method) {
        Validator.requireNonNull(reservation, "La reserva");
        if (!reservation.isActive()) {
            throw new BusinessRuleException("No se puede facturar una reserva cancelada.");
        }
        return new Invoice(id, reservation, LocalDate.now(), reservation.getTotal(), method, Status.PENDING);
    }

    /**
     * Marca el comprobante como pagado si se encuentra pendiente.
     * @throws BusinessRuleException si el comprobante no esta pendiente.
     */
    public void pay() {
        if (status != Status.PENDING) {
            throw new BusinessRuleException("Solo se puede pagar un comprobante pendiente.");
        }
        this.status = Status.PAID;
    }

    /**
     * Anula el comprobante si todavia no esta anulado.
     * @throws BusinessRuleException si el comprobante ya esta anulado.
     */
    public void annul() {
        if (status == Status.VOIDED) {
            throw new BusinessRuleException("El comprobante ya esta anulado.");
        }
        this.status = Status.VOIDED;
    }

    /**
     * Devuelve el identificador del comprobante.
     * @return identificador numerico de la entidad.
     */
    @Override
    public int getId() { return id; }

    /**
     * Devuelve la reserva asociada al comprobante.
     * @return reserva asociada al comprobante.
     */
    public Reservation getReservation() { return reservation; }

    /**
     * Devuelve la fecha de emision del comprobante.
     * @return fecha de emision.
     */
    public LocalDate getIssueDate() { return issueDate; }

    /**
     * Devuelve el monto registrado en el comprobante.
     * @return monto registrado.
     */
    public double getAmount() { return amount; }

    /**
     * Devuelve el metodo de pago seleccionado.
     * @return metodo de pago del comprobante.
     */
    public PaymentMethod getPaymentMethod() { return paymentMethod; }

    /**
     * Devuelve el estado actual del comprobante.
     * @return estado actual del comprobante.
     */
    public Status getStatus() { return status; }

    /**
     * Devuelve una representacion breve del comprobante.
     * @return representacion textual breve del objeto.
     */
    @Override
    public String toString() { return "Comprobante #" + id + " | $" + amount + " [" + status + "]"; }
}
