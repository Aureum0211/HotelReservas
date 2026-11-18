import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.sql.Connection;
import model.Client;
import model.Employee;
import model.Invoice;
import model.Reservation;
import model.Room;
import persistence.Database;
import persistence.JdbcRepository;
import persistence.Repository;
import persistence.SqlMappers;
import service.HotelService;
import ui.HotelApp;

/**
 * Punto de entrada de la aplicacion. Conecta la base de datos Oracle, el servicio del
 * hotel y el menu de consola.
 */
public class Main {

    /**
     * Inicia la aplicacion, abre la conexion, crea los repositorios y el servicio e
     * inicia el menu de consola.
     * @param args argumentos recibidos desde la linea de comandos.
     */
    public static void main(String[] args) {
        try {
            Connection connection = Database.open(Path.of("db.properties"));

            Repository<Client> clients = new JdbcRepository<>(connection, SqlMappers.clients());
            Repository<Employee> employees = new JdbcRepository<>(connection, SqlMappers.employees());
            Repository<Room> rooms = new JdbcRepository<>(connection, SqlMappers.rooms());
            Repository<Reservation> reservations = new JdbcRepository<>(connection,
                    SqlMappers.reservations(clients, employees, rooms));
            Repository<Invoice> invoices = new JdbcRepository<>(connection, SqlMappers.invoices(reservations));

            HotelService service = new HotelService(clients, employees, rooms, reservations, invoices);
            new HotelApp(service).start();
        } catch (UncheckedIOException | IllegalStateException error) {
            System.out.println("No se pudo iniciar el programa: " + error.getMessage());
        }
    }
}
