package service;

import exceptions.BusinessRuleException;
import exceptions.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Client;
import model.Employee;
import model.Invoice;
import model.Person;
import model.PricingPolicy;
import model.Reservation;
import model.Room;
import repository.Repository;

public class HotelService {

    private final Repository<Client> clients;
    private final Repository<Employee> employees;
    private final Repository<Room> rooms;
    private final Repository<Reservation> reservations;
    private final Repository<Invoice> invoices;

    public HotelService(Repository<Client> clients,
                        Repository<Employee> employees,
                        Repository<Room> rooms,
                        Repository<Reservation> reservations,
                        Repository<Invoice> invoices) {
        this.clients = clients;
        this.employees = employees;
        this.rooms = rooms;
        this.reservations = reservations;
        this.invoices = invoices;
    }

    public List<Client> getClients() { return clients.findAll(); }
    public List<Employee> getEmployees() { return employees.findAll(); }
    public List<Room> getRooms() { return rooms.findAll(); }
    public List<Reservation> getReservations() { return reservations.findAll(); }
    public List<Invoice> getInvoices() { return invoices.findAll(); }

    public List<Person> getPeople() {
        List<Person> people = new ArrayList<>();
        people.addAll(clients.findAll());
        people.addAll(employees.findAll());
        return List.copyOf(people);
    }

    public Reservation createReservation(int clientId, int roomId, LocalDate checkIn,
                                         LocalDate checkOut, PricingPolicy pricingPolicy) {
        Client client = findClient(clientId);
        Room room = findRoom(roomId);
        Employee employee = employees.findAll().stream().findFirst().orElse(null);

        Reservation reservation = Reservation.create(
                reservations.nextId(), client, employee, room, checkIn, checkOut, pricingPolicy);

        for (Reservation existing : reservations.findAll()) {
            if (existing.occupies(room, checkIn, checkOut)) {
                throw new BusinessRuleException("La habitacion ya esta reservada en esas fechas.");
            }
        }

        reservations.save(reservation);
        return reservation;
    }

    public Invoice issueInvoice(int reservationId, Invoice.PaymentMethod method) {
        Reservation reservation = findReservation(reservationId);
        Invoice invoice = Invoice.issue(invoices.nextId(), reservation, method);
        invoices.save(invoice);
        return invoice;
    }

    public void payInvoice(int invoiceId) {
        findInvoice(invoiceId).pay();
    }

    public void cancelReservation(int reservationId) {
        findReservation(reservationId).cancel();
    }

    private Client findClient(int id) {
        return clients.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el cliente #" + id));
    }

    private Room findRoom(int id) {
        return rooms.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la habitacion #" + id));
    }

    private Reservation findReservation(int id) {
        return reservations.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe la reserva #" + id));
    }

    private Invoice findInvoice(int id) {
        return invoices.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el comprobante #" + id));
    }
}
