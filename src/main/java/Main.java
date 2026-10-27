import java.time.LocalDate;
import model.*;

/** Demostración rápida del modelo. */
public class Main {

    public static void main(String[] args) {
        Client edward = new Client(1, "Edward Carrillo", "1065000000", "edward@mail.com",
                "3001234567", "Valledupar", Client.Category.VIP);
        Employee ana = new Employee(2, "Ana Pérez", "1065111111", "ana@hotel.com",
                "3109876543", "Valledupar", Employee.Role.RECEPTIONIST, "ana", "1234");
        Room room101 = new Room(1, "101", Room.Type.SUITE, 3, 350000, true);

        System.out.println(edward.describe());   // polimorfismo: describe() de cada subclase
        System.out.println(ana.describe());

        LocalDate in = LocalDate.of(2026, 11, 1);
        LocalDate out = LocalDate.of(2026, 11, 8);
        Reservation standard = Reservation.create(1, edward, ana, room101, in, out, new StandardPricing());
        Reservation longStay = Reservation.create(2, edward, ana, room101, in, out, new LongStayPricing(7, 10));
        System.out.println(standard);
        System.out.println(longStay + "  <- 10% por 7+ noches");

        Invoice invoice = Invoice.issue(1, standard, Invoice.PaymentMethod.CARD);
        invoice.pay();
        System.out.println(invoice);

        probar("Pagar un comprobante ya pagado", invoice::pay);
        probar("Fechas invertidas", () -> Reservation.create(3, edward, ana, room101, out, in, new StandardPricing()));
        standard.cancel();
        probar("Cancelar dos veces", standard::cancel);
        probar("Facturar reserva cancelada", () -> Invoice.issue(2, standard, Invoice.PaymentMethod.CASH));
        room101.markUnavailable();
        probar("Reservar habitación no disponible",
                () -> Reservation.create(3, edward, ana, room101, in, out, new StandardPricing()));
    }

    private static void probar(String caso, Runnable accion) {
        try {
            accion.run();
            System.out.println(caso + ": (sin error)");
        } catch (RuntimeException e) {
            System.out.println(caso + " -> " + e.getMessage());
        }
    }
}
