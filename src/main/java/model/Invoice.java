package model;

import exceptions.InvalidDataException;
import java.time.LocalDate;
import validation.Validator;

/** Datos del comprobante asociado a una reserva. */
public class Invoice implements Identifiable {

    /** Metodos de pago disponibles. */
    public enum PaymentMethod {
        
        CASH,
        
        CARD,
        
        TRANSFER
    }

    /** Estados de un comprobante. */
    public enum Status {
        
        PENDING,
        
        PARTIALLY_PAID,
        
        PAID,
        
        REFUNDED,
        
        VOIDED
    }

    private final int id;
    private final Reservation reservation;
    private final LocalDate issueDate;
    private final double amount;
    private final PaymentMethod paymentMethod;
    private final Status status;
    private final double refundAmount;
    private final double paidAmount;

    public Invoice(int id, Reservation reservation, LocalDate issueDate, double amount,
                   PaymentMethod paymentMethod, Status status, double refundAmount) {
        this(id, reservation, issueDate, amount, paymentMethod, status, refundAmount,
                status == Status.PAID || status == Status.REFUNDED ? amount : 0);
    }

    public Invoice(int id, Reservation reservation, LocalDate issueDate, double amount,
                   PaymentMethod paymentMethod, Status status, double refundAmount, double paidAmount) {
        this.id = Validator.requirePositive(id, "El id del comprobante");
        this.reservation = Validator.requireNonNull(reservation, "La reserva");
        this.issueDate = Validator.requireNonNull(issueDate, "La fecha de emision");
        this.amount = Validator.requirePositive(amount, "El monto");
        this.paymentMethod = Validator.requireNonNull(paymentMethod, "El metodo de pago");
        this.status = Validator.requireNonNull(status, "El estado");
        if (!Double.isFinite(refundAmount) || refundAmount < 0 || refundAmount > amount) {
            throw new InvalidDataException("El reembolso historico no es valido.");
        }
        if (!Double.isFinite(paidAmount) || paidAmount < 0 || paidAmount > amount) {
            throw new InvalidDataException("El valor abonado no es valido.");
        }
        this.refundAmount = refundAmount;
        this.paidAmount = paidAmount;
    }

    @Override
    public int getId() { return id; }

    public Reservation getReservation() { return reservation; }

    public LocalDate getIssueDate() { return issueDate; }

    public double getAmount() { return amount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }

    public Status getStatus() { return status; }

    public double getRefundAmount() { return refundAmount; }

    public double getPaidAmount() { return paidAmount; }

    public double getBalance() {
        if (status == Status.VOIDED || status == Status.REFUNDED) return 0;
        return Math.max(0, amount - paidAmount);
    }

    @Override
    public String toString() {
        return "Comprobante #" + id + " | Total: $" + amount + " | Abonado: $" + paidAmount
                + " | Saldo: $" + getBalance() + " [" + status + "]";
    }
}
