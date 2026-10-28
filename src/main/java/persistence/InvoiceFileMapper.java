package persistence;

import java.time.LocalDate;
import java.util.List;
import model.Invoice;
import model.Reservation;

public class InvoiceFileMapper implements EntityFileMapper<Invoice> {
    private final Repository<Reservation> reservations;

    public InvoiceFileMapper(Repository<Reservation> reservations) {
        this.reservations = reservations;
    }

    @Override
    public String toLine(Invoice invoice) {
        return DelimitedText.join(Integer.toString(invoice.getId()),
                Integer.toString(invoice.getReservation().getId()), invoice.getIssueDate().toString(),
                Double.toString(invoice.getAmount()), invoice.getPaymentMethod().name(), invoice.getStatus().name());
    }

    @Override
    public Invoice fromLine(String line) {
        List<String> f = DelimitedText.requireFieldCount(line, 6);
        int reservationId = Integer.parseInt(f.get(1));
        Reservation reservation = reservations.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la reserva vinculada #" + reservationId));
        return new Invoice(Integer.parseInt(f.get(0)), reservation, LocalDate.parse(f.get(2)),
                Double.parseDouble(f.get(3)), Invoice.PaymentMethod.valueOf(f.get(4)),
                Invoice.Status.valueOf(f.get(5)));
    }
}
