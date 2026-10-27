package model;

import exceptions.BusinessRuleException;
import java.time.LocalDate;
import validation.Validator;

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
        this.id = Validator.requirePositive(id, "El id del comprobante");
        this.reservation = Validator.requireNonNull(reservation, "La reserva");
        this.issueDate = Validator.requireNonNull(issueDate, "La fecha de emision");
        this.amount = Validator.requirePositive(amount, "El monto");
        this.paymentMethod = Validator.requireNonNull(paymentMethod, "El metodo de pago");
        this.status = Validator.requireNonNull(status, "El estado");
    }

    public static Invoice issue(int id, Reservation reservation, PaymentMethod method) {
        Validator.requireNonNull(reservation, "La reserva");
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
