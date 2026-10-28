package persistence;

import java.time.LocalDate;
import java.util.List;
import model.Invoice;
import model.Reservation;

/**
 * Convierte comprobantes entre objetos Java y registros de texto, resolviendo la reserva
 * vinculada.
 */
public class InvoiceFileMapper implements EntityFileMapper<Invoice> {
    private final Repository<Reservation> reservations;

    /**
     * Crea el convertidor de comprobantes y recibe el repositorio necesario para resolver
     * las reservas asociadas.
     * @param reservations repositorio de reservas.
     */
    public InvoiceFileMapper(Repository<Reservation> reservations) {
        this.reservations = reservations;
    }

    /**
     * Convierte el comprobante en una linea delimitada.
     * @param invoice valor utilizado por la operacion.
     * @return representacion textual de la entidad.
     */
    @Override
    public String toLine(Invoice invoice) {
        return DelimitedText.join(Integer.toString(invoice.getId()),
                Integer.toString(invoice.getReservation().getId()), invoice.getIssueDate().toString(),
                Double.toString(invoice.getAmount()), invoice.getPaymentMethod().name(), invoice.getStatus().name());
    }

    /**
     * Reconstruye un comprobante y busca la reserva asociada por identificador.
     * @param line linea de texto que se va a interpretar.
     * @return entidad reconstruida a partir de la linea.
     */
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
