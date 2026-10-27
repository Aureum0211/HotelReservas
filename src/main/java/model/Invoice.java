package model;

import java.time.LocalDate;

/** Comprobante de pago de una reserva. */
public class Invoice implements Identifiable {

    public enum PaymentMethod { CASH, CARD, TRANSFER }

    public enum Status { PENDING, PAID, VOIDED }

    private final int id;
    private final Reservation reservation;
    private final LocalDate issueDate;
    private final double amount;
    private final PaymentMethod paymentMethod;
    private Status status;

    /** Constructor también sirve para reconstruir un comprobante guardado. */
    public Invoice(int id, Reservation reservation, LocalDate issueDate, double amount,
                   PaymentMethod paymentMethod, Status status) {
        if (id <= 0) {
            throw new IllegalArgumentException("El id del comprobante debe ser mayor que cero.");
        }
        this.id = id;
        if (reservation == null) {
            throw new IllegalArgumentException("La reserva es obligatorio.");
        }
        this.reservation = reservation;
        if (issueDate == null) {
            throw new IllegalArgumentException("La fecha de emisión es obligatorio.");
        }
        this.issueDate = issueDate;
        if (amount <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }
        this.amount = amount;
        if (paymentMethod == null) {
            throw new IllegalArgumentException("El método de pago es obligatorio.");
        }
        this.paymentMethod = paymentMethod;
        if (status == null) {
            throw new IllegalArgumentException("El estado es obligatorio.");
        }
        this.status = status;
    }

    /** Creator: el monto sale del total de la reserva; no se digita. Nace pendiente de pago. */
    public static Invoice issue(int id, Reservation reservation, PaymentMethod method) {
        if (reservation == null) {
            throw new IllegalArgumentException("La reserva es obligatorio.");
        }
        if (!reservation.isActive()) {
            throw new IllegalStateException("No se puede facturar una reserva cancelada.");
        }
        return new Invoice(id, reservation, LocalDate.now(), reservation.getTotal(), method, Status.PENDING);
    }

    public void pay() {
        if (status != Status.PENDING) {
            throw new IllegalStateException("Solo se puede pagar un comprobante pendiente.");
        }
        this.status = Status.PAID;
    }

    public void annul() {
        if (status == Status.VOIDED) {
            throw new IllegalStateException("El comprobante ya está anulado.");
        }
        this.status = Status.VOIDED;
    }

    @Override
    public int getId() { return id; }

    public Reservation getReservation() { return reservation; }

    public LocalDate getIssueDate() { return issueDate; }

    public double getAmount() { return amount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }

    public Status getStatus() { return status; }

    @Override
    public String toString() { return "Comprobante #" + id + " | $" + amount + " [" + status + "]"; }
}
