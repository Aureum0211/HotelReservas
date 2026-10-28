import java.nio.file.Path;
import model.Client;
import model.Employee;
import model.Invoice;
import model.LongStayPricing;
import model.PricingPolicy;
import model.Reservation;
import model.Room;
import model.StandardPricing;
import persistence.Repository;
import persistence.ClientFileMapper;
import persistence.EmployeeFileMapper;
import persistence.InvoiceFileMapper;
import persistence.ReservationFileMapper;
import persistence.RoomFileMapper;
import persistence.TextFileRepository;
import service.HotelService;
import ui.HotelApp;

public class Main {
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
        PricingPolicy standardPricing = new StandardPricing();
        PricingPolicy longStayPricing = new LongStayPricing(7, 10);
        new HotelApp(service, standardPricing, longStayPricing).start();
    }
}
