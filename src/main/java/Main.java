import java.nio.file.Path;
import model.Client;
import model.Employee;
import model.Invoice;
import model.Reservation;
import model.Room;
import persistence.Repository;
import persistence.ClientFileMapper;
import persistence.EmployeeFileMapper;
import persistence.InvoiceFileMapper;
import persistence.ReservationFileMapper;
import persistence.RoomFileMapper;
import persistence.TextFileRepository;
import service.HotelService;
import ui.HotelApp;

/**
 * Punto de entrada de la aplicacion. Conecta los repositorios de archivos, el servicio del
 * hotel y el menu de consola.
 */
public class Main {

    /**
     * Inicia la aplicacion, configura los repositorios de archivos, crea el servicio e
     * inicia el menu de consola.
     * @param args argumentos recibidos desde la linea de comandos.
     */
    public static void main(String[] args) {
        Path dataDirectory = Path.of("data");

        Repository<Client> clients = new TextFileRepository<>(dataDirectory.resolve("clientes.txt"), new ClientFileMapper());
        Repository<Employee> employees = new TextFileRepository<>(dataDirectory.resolve("trabajadores.txt"), new EmployeeFileMapper());
        Repository<Room> rooms = new TextFileRepository<>(dataDirectory.resolve("habitaciones.txt"), new RoomFileMapper());
        Repository<Reservation> reservations = new TextFileRepository<>(dataDirectory.resolve("reservas.txt"),
                new ReservationFileMapper(clients, employees, rooms));
        Repository<Invoice> invoices = new TextFileRepository<>(dataDirectory.resolve("comprobantes.txt"),
                new InvoiceFileMapper(reservations));

        HotelService service = new HotelService(clients, employees, rooms, reservations, invoices);
        new HotelApp(service).start();
    }
}
