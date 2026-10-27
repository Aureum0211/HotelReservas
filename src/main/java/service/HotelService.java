package service;

import exceptions.BusinessRuleException;
import exceptions.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Client;
import model.Employee;
import model.Invoice;
import model.Reservation;
import model.Room;
import model.StandardPricing;

public class HotelService {
    private final List<Client> clients = new ArrayList<>();
    private final List<Employee> employees = new ArrayList<>();
    private final List<Room> rooms = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private final List<Invoice> invoices = new ArrayList<>();

    public HotelService() {
        clients.add(new Client(1, "Ana Lopez", "1010123456", "ana@example.com",
                "3109876543", "Valledupar", Client.Category.VIP));
        clients.add(new Client(2, "Luis Martinez", "1020987654", "luis@example.com",
                "3204567890", "Valledupar", Client.Category.NEW));
        employees.add(new Employee(1, "Carlos Gomez", "1065123456", "carlos@hotel.com",
                "3001234567", "Valledupar", Employee.Role.RECEPTIONIST, "cgomez", "demo123"));
        rooms.add(new Room(1, "101", Room.Type.SINGLE, 1, 80000, true));
        rooms.add(new Room(2, "102", Room.Type.DOUBLE, 2, 120000, true));
        rooms.add(new Room(3, "201", Room.Type.SUITE, 2, 250000, true));
    }

    public List<Client> getClients() { return List.copyOf(clients); }
    public List<Employee> getEmployees() { return List.copyOf(employees); }
    public List<Room> getRooms() { return List.copyOf(rooms); }
    public List<Reservation> getReservations() { return List.copyOf(reservations); }
    public List<Invoice> getInvoices() { return List.copyOf(invoices); }

    public Reservation createReservation(int clientId, int roomId, LocalDate checkIn,
                                         LocalDate checkOut, boolean longStayDiscount) {
        Client client = findClient(clientId);
        Room room = findRoom(roomId);

        for (Reservation existing : reservations) {
            if (existing.occupies(room, checkIn, checkOut)) {
                throw new BusinessRuleException("La habitacion ya esta reservada en esas fechas.");
            }
        }

        int id = reservations.stream().mapToInt(Reservation::getId).max().orElse(0) + 1;
        model.PricingPolicy pricing = longStayDiscount
                ? new model.LongStayPricing(7, 10)
                : new StandardPricing();
        Reservation reservation = Reservation.create(id, client, employees.get(0), room,
                checkIn, checkOut, pricing);
        reservations.add(reservation);
        return reservation;
    }

    public Invoice issueInvoice(int reservationId, Invoice.PaymentMethod method) {
        Reservation reservation = findReservation(reservationId);
        int id = invoices.stream().mapToInt(Invoice::getId).max().orElse(0) + 1;
        Invoice invoice = Invoice.issue(id, reservation, method);
        invoices.add(invoice);
        return invoice;
    }

    public void payInvoice(int invoiceId) {
        findInvoice(invoiceId).pay();
    }

    public void cancelReservation(int reservationId) {
        findReservation(reservationId).cancel();
    }

    private Client findClient(int id) {
        return clients.stream().filter(c -> c.getId() == id).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No existe el cliente #" + id));
    }

    private Room findRoom(int id) {
        return rooms.stream().filter(r -> r.getId() == id).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No existe la habitacion #" + id));
    }

    private Reservation findReservation(int id) {
        return reservations.stream().filter(r -> r.getId() == id).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No existe la reserva #" + id));
    }

    private Invoice findInvoice(int id) {
        return invoices.stream().filter(i -> i.getId() == id).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No existe el comprobante #" + id));
    }
}
