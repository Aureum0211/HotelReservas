package persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import model.Client;
import model.Employee;
import model.Invoice;
import model.Reservation;
import model.Room;

/**
 * Fabrica los convertidores entre las entidades del hotel y las filas de sus tablas.
 */
public final class SqlMappers {

    private SqlMappers() { }

    private static int bool(boolean value) { return value ? 1 : 0; }

    private static String missing(String what, int id) { return "No existe " + what + " vinculado #" + id; }

    /**
     * Convertidor de clientes.
     * @return convertidor para la tabla clientes.
     */
    public static RowMapper<Client> clients() {
        return new RowMapper<>() {
            @Override public String table() { return "clientes"; }
            @Override public List<String> columns() {
                return List.of("nombre", "documento", "correo", "telefono", "ciudad", "categoria", "activo");
            }
            @Override public Object[] values(Client c) {
                return new Object[] {c.getName(), c.getDocumentNumber(), c.getEmail(), c.getPhone(),
                        c.getCity(), c.getCategory().name(), bool(c.isActive())};
            }
            @Override public Client read(ResultSet rs) throws SQLException {
                Client client = new Client(rs.getInt("id"), rs.getString("nombre"), rs.getString("documento"),
                        rs.getString("correo"), rs.getString("telefono"), rs.getString("ciudad"),
                        Client.Category.valueOf(rs.getString("categoria")));
                if (rs.getInt("activo") == 0) client.deactivate();
                return client;
            }
        };
    }

    /**
     * Convertidor de trabajadores.
     * @return convertidor para la tabla trabajadores.
     */
    public static RowMapper<Employee> employees() {
        return new RowMapper<>() {
            @Override public String table() { return "trabajadores"; }
            @Override public List<String> columns() {
                return List.of("nombre", "documento", "correo", "telefono", "ciudad", "activo", "rol",
                        "usuario", "contrasena");
            }
            @Override public Object[] values(Employee e) {
                return new Object[] {e.getName(), e.getDocumentNumber(), e.getEmail(), e.getPhone(),
                        e.getCity(), bool(e.isActive()), e.getRole().name(), e.getUsername(), e.getPassword()};
            }
            @Override public Employee read(ResultSet rs) throws SQLException {
                Employee employee = new Employee(rs.getInt("id"), rs.getString("nombre"), rs.getString("documento"),
                        rs.getString("correo"), rs.getString("telefono"), rs.getString("ciudad"),
                        Employee.Role.valueOf(rs.getString("rol")), rs.getString("usuario"),
                        rs.getString("contrasena"));
                if (rs.getInt("activo") == 0) employee.deactivate();
                return employee;
            }
        };
    }

    /**
     * Convertidor de habitaciones.
     * @return convertidor para la tabla habitaciones.
     */
    public static RowMapper<Room> rooms() {
        return new RowMapper<>() {
            @Override public String table() { return "habitaciones"; }
            @Override public List<String> columns() {
                return List.of("numero", "tipo", "capacidad", "precio_noche", "disponible", "en_mantenimiento");
            }
            @Override public Object[] values(Room r) {
                return new Object[] {r.getNumber(), r.getType().name(), r.getCapacity(), r.getPricePerNight(),
                        bool(r.isAvailable()), bool(r.isUnderMaintenance())};
            }
            @Override public Room read(ResultSet rs) throws SQLException {
                return new Room(rs.getInt("id"), rs.getString("numero"), Room.Type.valueOf(rs.getString("tipo")),
                        rs.getInt("capacidad"), rs.getDouble("precio_noche"),
                        rs.getInt("disponible") == 1, rs.getInt("en_mantenimiento") == 1);
            }
        };
    }

    /**
     * Convertidor de reservas; resuelve cliente, trabajador y habitacion por su id.
     * @param clients repositorio de clientes.
     * @param employees repositorio de trabajadores.
     * @param rooms repositorio de habitaciones.
     * @return convertidor para la tabla reservas.
     */
    public static RowMapper<Reservation> reservations(Repository<Client> clients, Repository<Employee> employees,
                                                      Repository<Room> rooms) {
        return new RowMapper<>() {
            @Override public String table() { return "reservas"; }
            @Override public List<String> columns() {
                return List.of("cliente_id", "trabajador_id", "habitacion_id", "entrada", "salida",
                        "precio_noche", "total", "huespedes", "estado");
            }
            @Override public Object[] values(Reservation r) {
                return new Object[] {r.getClient().getId(),
                        r.getEmployee() == null ? null : r.getEmployee().getId(),
                        r.getRoom().getId(), r.getCheckIn().toString(), r.getCheckOut().toString(),
                        r.getPricePerNight(), r.getTotal(), r.getGuests(), r.getStatus().name()};
            }
            @Override public Reservation read(ResultSet rs) throws SQLException {
                int clientId = rs.getInt("cliente_id");
                int employeeId = rs.getInt("trabajador_id");
                boolean noEmployee = rs.wasNull();
                int roomId = rs.getInt("habitacion_id");
                int id = rs.getInt("id");
                LocalDate checkIn = LocalDate.parse(rs.getString("entrada"));
                LocalDate checkOut = LocalDate.parse(rs.getString("salida"));
                double pricePerNight = rs.getDouble("precio_noche");
                double total = rs.getDouble("total");
                int guests = rs.getInt("huespedes");
                Reservation.Status status = Reservation.Status.valueOf(rs.getString("estado"));
                Client client = clients.findById(clientId)
                        .orElseThrow(() -> new IllegalStateException(missing("el cliente", clientId)));
                Employee employee = noEmployee ? null : employees.findById(employeeId)
                        .orElseThrow(() -> new IllegalStateException(missing("el trabajador", employeeId)));
                Room room = rooms.findById(roomId)
                        .orElseThrow(() -> new IllegalStateException(missing("la habitacion", roomId)));
                return new Reservation(id, client, employee, room, checkIn, checkOut, pricePerNight, total,
                        guests, status);
            }
        };
    }

    /**
     * Convertidor de comprobantes; resuelve la reserva vinculada por su id.
     * @param reservations repositorio de reservas.
     * @return convertidor para la tabla comprobantes.
     */
    public static RowMapper<Invoice> invoices(Repository<Reservation> reservations) {
        return new RowMapper<>() {
            @Override public String table() { return "comprobantes"; }
            @Override public List<String> columns() {
                return List.of("reserva_id", "fecha_emision", "monto", "metodo_pago", "estado", "reembolso", "pagado");
            }
            @Override public Object[] values(Invoice i) {
                return new Object[] {i.getReservation().getId(), i.getIssueDate().toString(), i.getAmount(),
                        i.getPaymentMethod().name(), i.getStatus().name(), i.getRefundAmount(), i.getPaidAmount()};
            }
            @Override public Invoice read(ResultSet rs) throws SQLException {
                int reservationId = rs.getInt("reserva_id");
                int id = rs.getInt("id");
                LocalDate issueDate = LocalDate.parse(rs.getString("fecha_emision"));
                double amount = rs.getDouble("monto");
                Invoice.PaymentMethod method = Invoice.PaymentMethod.valueOf(rs.getString("metodo_pago"));
                Invoice.Status status = Invoice.Status.valueOf(rs.getString("estado"));
                double refund = rs.getDouble("reembolso");
                double paid = rs.getDouble("pagado");
                Reservation reservation = reservations.findById(reservationId)
                        .orElseThrow(() -> new IllegalStateException(missing("la reserva", reservationId)));
                return new Invoice(id, reservation, issueDate, amount, method, status, refund, paid);
            }
        };
    }
}
