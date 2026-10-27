package model;

import exceptions.BusinessRuleException;
import exceptions.InvalidDataException;
import java.time.LocalDate;

public class Invoice implements Identifiable {

    public enum PaymentMethod { CASH, CARD, TRANSFER }

    public enum Status { PENDING, PAID, VOIDED }

    private final int id;
    private final Reservation reservation;
    private final LocalDate issueDate;
    private final double amount;
    private final PaymentMethod paymentMethod;
    private Status status;

    public Invoice(int id, Reservation reservation, LocalDate issueDate, double amount,
                   PaymentMethod paymentMethod, Status status) {
        if (id <= 0) {
            throw new InvalidDataException("El id del comprobante debe ser mayor que cero.");
        }
        this.id = id;
        if (reservation == null) {
            throw new InvalidDataException("La reserva es obligatorio.");
        }
        this.reservation = reservation;
        if (issueDate == null) {
            throw new InvalidDataException("La fecha de emision es obligatorio.");
        }
        this.issueDate = issueDate;
        if (amount <= 0) {
            throw new InvalidDataException("El monto debe ser mayor que cero.");
        }
        this.amount = amount;
        if (paymentMethod == null) {
            throw new InvalidDataException("El metodo de pago es obligatorio.");
        }
        this.paymentMethod = paymentMethod;
        if (status == null) {
            throw new InvalidDataException("El estado es obligatorio.");
        }
        this.status = status;
    }

    public static Invoice issue(int id, Reservation reservation, PaymentMethod method) {
        if (reservation == null) {
            throw new InvalidDataException("La reserva es obligatorio.");
        }
        if (!reservation.isActive()) {
            throw new BusinessRuleException("No se puede facturar una reserva cancelada.");
        }
        return new Invoice(id, reservation, LocalDate.now(), reservation.getTotal(), method, Status.PENDING);
    }

    public void pay() {
        if (status != Status.PENDING) {
            throw new BusinessRuleException("Solo se puede pagar un comprobante pendiente.");
        }
        this.status = Status.PAID;
    }

    public void annul() {
        if (status == Status.VOIDED) {
            throw new BusinessRuleException("El comprobante ya esta anulado.");
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
