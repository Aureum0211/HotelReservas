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

    public Client registerClient(String name, String documentNumber, String email,
                                 String phone, String city, Client.Category category) {
        ensurePersonIsUnique(documentNumber, email, phone);
        Client client = new Client(clients.nextId(), name, documentNumber, email, phone, city, category);
        clients.save(client);
        return client;
    }

    public Employee registerEmployee(String name, String documentNumber, String email,
                                     String phone, String city, Employee.Role role,
                                     String username, String password) {
        ensurePersonIsUnique(documentNumber, email, phone);
        boolean usernameExists = employees.findAll().stream()
                .anyMatch(employee -> employee.getUsername().equalsIgnoreCase(username.trim()));
        if (usernameExists) {
            throw new BusinessRuleException("Ya existe un trabajador con ese usuario.");
        }
        Employee employee = new Employee(employees.nextId(), name, documentNumber, email,
                phone, city, role, username, password);
        employees.save(employee);
        return employee;
    }

    public Room registerRoom(String number, Room.Type type, int capacity, double pricePerNight) {
        boolean numberExists = rooms.findAll().stream()
                .anyMatch(room -> room.getNumber().equalsIgnoreCase(number.trim()));
        if (numberExists) {
            throw new BusinessRuleException("Ya existe una habitacion con ese numero.");
        }
        Room room = new Room(rooms.nextId(), number, type, capacity, pricePerNight, true);
        rooms.save(room);
        return room;
    }

    private void ensurePersonIsUnique(String documentNumber, String email, String phone) {
        for (Person person : getPeople()) {
            if (person.getDocumentNumber().equalsIgnoreCase(documentNumber.trim())) {
                throw new BusinessRuleException("Ya existe una persona con ese documento.");
            }
            if (person.getEmail().equalsIgnoreCase(email.trim())) {
                throw new BusinessRuleException("Ya existe una persona con ese correo.");
            }
            if (person.getPhone().equals(phone.trim())) {
                throw new BusinessRuleException("Ya existe una persona con ese telefono.");
            }
        }
    }

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
