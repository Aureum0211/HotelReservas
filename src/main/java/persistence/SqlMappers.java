package persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import model.Client;
import model.Employee;
import model.Invoice;
import model.Reservation;
import model.Room;

/** Mapea los resultados de Oracle a los modelos Java. */
public final class SqlMappers {
    private SqlMappers() { }

    private static String missing(String what, int id) {
        return "No existe " + what + " vinculado #" + id;
    }

    public static RowMapper<Client> clients() {
        return new RowMapper<>() {
            @Override public String table() { return "clientes"; }
            @Override public Client read(ResultSet rs) throws SQLException {
                Client client = new Client(rs.getInt("id"), rs.getString("nombre"), rs.getString("documento"),
                        rs.getString("correo"), rs.getString("telefono"), rs.getString("ciudad"),
                        Client.Category.valueOf(rs.getString("categoria")));
                if (rs.getInt("activo") == 0) client.deactivate();
                return client;
            }
        };
    }

    public static RowMapper<Employee> employees() {
        return new RowMapper<>() {
            @Override public String table() { return "trabajadores"; }
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

    public static RowMapper<Room> rooms() {
        return new RowMapper<>() {
            @Override public String table() { return "habitaciones"; }
            @Override public Room read(ResultSet rs) throws SQLException {
                return new Room(rs.getInt("id"), rs.getString("numero"), Room.Type.valueOf(rs.getString("tipo")),
                        rs.getInt("capacidad"), rs.getDouble("precio_noche"),
                        rs.getInt("disponible") == 1, rs.getInt("en_mantenimiento") == 1);
            }
        };
    }

    public static RowMapper<Reservation> reservations(Repository<Client> clients,
                                                       Repository<Employee> employees,
                                                       Repository<Room> rooms) {
        return new RowMapper<>() {
            @Override public String table() { return "reservas"; }
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

    public static RowMapper<Invoice> invoices(Repository<Reservation> reservations) {
        return new RowMapper<>() {
            @Override public String table() { return "comprobantes"; }
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
