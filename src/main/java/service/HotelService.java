package service;

import exceptions.InvalidDataException;
import exceptions.ResourceNotFoundException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import model.Client;
import model.Employee;
import model.Invoice;
import model.ReportRows;
import model.Reservation;
import model.Room;
import persistence.JdbcRepository;
import persistence.OracleHotelProcedures;
import persistence.Repository;
import persistence.SqlMappers;

/** Coordina las operaciones entre la interfaz y Oracle. */
public class HotelService implements AutoCloseable {

    private final Repository<Client> clients;
    private final Repository<Employee> employees;
    private final Repository<Room> rooms;
    private final Repository<Reservation> reservations;
    private final Repository<Invoice> invoices;
    private final OracleHotelProcedures oracle;
    private final Connection connection;

    public HotelService(Connection connection,
                        Repository<Client> clients,
                        Repository<Employee> employees,
                        Repository<Room> rooms,
                        Repository<Reservation> reservations,
                        Repository<Invoice> invoices,
                        OracleHotelProcedures oracle) {
        this.connection = connection;
        this.clients = clients;
        this.employees = employees;
        this.rooms = rooms;
        this.reservations = reservations;
        this.invoices = invoices;
        this.oracle = oracle;
    }

    public static HotelService forConnection(Connection connection) {
        OracleHotelProcedures.requireInstalled(connection);
        Repository<Client> clients = new JdbcRepository<>(connection, SqlMappers.clients());
        Repository<Employee> employees = new JdbcRepository<>(connection, SqlMappers.employees());
        Repository<Room> rooms = new JdbcRepository<>(connection, SqlMappers.rooms());
        Repository<Reservation> reservations = new JdbcRepository<>(connection,
                SqlMappers.reservations(clients, employees, rooms));
        Repository<Invoice> invoices = new JdbcRepository<>(connection, SqlMappers.invoices(reservations));
        return new HotelService(connection, clients, employees, rooms, reservations, invoices,
                new OracleHotelProcedures(connection));
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    // ------------------------------------------------------------------ consultas

    public List<Client> getClients() { return clients.findAll(); }

    public List<Employee> getEmployees(Employee actor) {
        requireAdmin(actor);
        return employees.findAll();
    }

    public List<Room> getRooms() { return rooms.findAll(); }

    public List<Reservation> getReservations() { return reservations.findAll(); }

    public List<Invoice> getInvoices() { return invoices.findAll(); }

    public List<Reservation> getReservationsOf(int clientId) {
        return reservations.findAll().stream().filter(r -> r.getClient().getId() == clientId).toList();
    }

    public void requireAdmin(Employee actor) {
        oracle.requireAdmin(idOf(actor));
    }

    // ------------------------------------------------------------------ personas (PKG_PERSONAS)

    public Employee login(String username, String password) {
        int id = oracle.login(username, password);
        return employees.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el trabajador #" + id));
    }

    public Client registerClient(String name, String documentNumber, String email,
                                 String phone, String city, Client.Category category) {
        int id = oracle.registerClient(name, documentNumber, email, phone, city, nameOf(category));
        return findClient(id);
    }

    public Client registerGuest(String name, String documentNumber, String email,
                                String phone, String city) {
        int id = oracle.registerGuest(name, documentNumber, email, phone, city);
        return findClient(id);
    }

    public Employee registerEmployee(String name, String documentNumber, String email,
                                     String phone, String city, Employee.Role role,
                                     String username, String password, Employee actor) {
        int id = oracle.registerEmployee(idOf(actor), name, documentNumber, email, phone, city,
                nameOf(role), username, password);
        return employees.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el trabajador #" + id));
    }

    public Client findClientByDocument(String documentNumber) {
        return findClient(oracle.findClientIdByDocument(documentNumber));
    }

    // ------------------------------------------------------------------ habitaciones (PKG_HABITACIONES)

    public Room registerRoom(String number, Room.Type type, int capacity, double pricePerNight, Employee actor) {
        if (!Double.isFinite(pricePerNight)) {
            throw new InvalidDataException("El precio debe ser un numero mayor que cero.");
        }
        int id = oracle.registerRoom(idOf(actor), number, nameOf(type), capacity, BigDecimal.valueOf(pricePerNight));
        return findRoom(id);
    }

    public void setRoomMaintenance(int roomId, boolean underMaintenance, Employee actor) {
        if (underMaintenance) {
            oracle.startMaintenance(idOf(actor), roomId);
        } else {
            oracle.finishMaintenance(idOf(actor), roomId);
        }
    }

    // ------------------------------------------------------------------ reservas (PKG_RESERVAS)

    public Reservation createReservation(int clientId, int roomId, LocalDate checkIn,
                                         LocalDate checkOut, int guests, Employee employee) {
        Integer employeeId = employee == null ? null : employee.getId();
        int id = oracle.createReservation(clientId, employeeId, roomId, text(checkIn), text(checkOut), guests);
        return findReservation(id);
    }

    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, int guests) {
        return oracle.freeRooms(text(checkIn), text(checkOut), guests);
    }

    public double calculateTotal(double pricePerNight, int nights, Client.Category category) {
        if (!Double.isFinite(pricePerNight)) {
            throw new InvalidDataException("El precio debe ser un numero mayor que cero.");
        }
        return oracle.calculateTotal(BigDecimal.valueOf(pricePerNight), nights, nameOf(category)).doubleValue();
    }

    public void checkIn(int reservationId) {
        oracle.checkIn(reservationId);
    }

    public void checkOut(int reservationId) {
        oracle.checkOut(reservationId);
    }

    public boolean hasPayments(int reservationId) {
        return oracle.hasPayments(reservationId);
    }

    public void cancelReservation(int reservationId, Employee actor) {
        oracle.cancelReservation(reservationId, idOf(actor));
    }

    public void cancelGuestReservation(int clientId, int reservationId) {
        oracle.cancelGuestReservation(clientId, reservationId);
    }

    public Invoice issueInvoice(int reservationId, Invoice.PaymentMethod method) {
        int id = oracle.issueInvoice(reservationId, nameOf(method));
        return findInvoice(id);
    }

    public void payInvoice(int invoiceId, double amount) {
        if (!Double.isFinite(amount)) {
            throw new InvalidDataException("El abono debe ser un numero mayor que cero.");
        }
        oracle.payInvoice(invoiceId, BigDecimal.valueOf(amount));
    }

    // ------------------------------------------------------------------ reportes (PKG_REPORTES)

    public ReportRows.BasicReport getBasicReport(Employee actor) {
        return oracle.generalStats(idOf(actor));
    }

    public List<ReportRows.ReservationsByStatus> reservationsByStatus(Employee actor, LocalDate from, LocalDate to) {
        return oracle.reservationsByStatus(idOf(actor), text(from), text(to));
    }

    public List<ReportRows.MonthlyIncome> monthlyIncome(Employee actor, LocalDate from, LocalDate to) {
        return oracle.monthlyIncome(idOf(actor), text(from), text(to));
    }

    public List<ReportRows.RoomUsage> roomUsage(Employee actor, LocalDate from, LocalDate to) {
        return oracle.roomUsage(idOf(actor), text(from), text(to));
    }

    public List<ReportRows.TopClient> topClients(Employee actor, LocalDate from, LocalDate to) {
        return oracle.topClients(idOf(actor), text(from), text(to));
    }

    public List<ReportRows.PendingBalance> pendingBalances(Employee actor, LocalDate from, LocalDate to) {
        return oracle.pendingBalances(idOf(actor), text(from), text(to));
    }

    public List<ReportRows.RoomRanking> getRoomRanking(LocalDate from, Integer limit) {
        return oracle.roomRanking(text(from), limit);
    }

    // ------------------------------------------------------------------ apoyo (solo conversion de tipos)

    private static String text(LocalDate date) {
        return date == null ? null : date.toString();
    }

    private static String nameOf(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static int idOf(Employee actor) {
        if (actor == null) {
            throw new InvalidDataException("El trabajador es obligatorio.");
        }
        return actor.getId();
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