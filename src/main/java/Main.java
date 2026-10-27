import model.Client;
import model.Employee;
import model.Invoice;
import model.LongStayPricing;
import model.PricingPolicy;
import model.Reservation;
import model.Room;
import model.StandardPricing;
import repository.InMemoryRepository;
import repository.Repository;
import service.HotelService;
import ui.HotelApp;

public class Main {
    public static void main(String[] args) {
        Repository<Client> clients = new InMemoryRepository<>();
        Repository<Employee> employees = new InMemoryRepository<>();
        Repository<Room> rooms = new InMemoryRepository<>();
        Repository<Reservation> reservations = new InMemoryRepository<>();
        Repository<Invoice> invoices = new InMemoryRepository<>();

        clients.save(new Client(1, "Ana Lopez", "1010123456", "ana@example.com",
                "3109876543", "Valledupar", Client.Category.VIP));
        clients.save(new Client(2, "Luis Martinez", "1020987654", "luis@example.com",
                "3204567890", "Valledupar", Client.Category.NEW));
        employees.save(new Employee(1, "Carlos Gomez", "1065123456", "carlos@hotel.com",
                "3001234567", "Valledupar", Employee.Role.RECEPTIONIST, "cgomez", "demo123"));
        rooms.save(new Room(1, "101", Room.Type.SINGLE, 1, 80000, true));
        rooms.save(new Room(2, "102", Room.Type.DOUBLE, 2, 120000, true));
        rooms.save(new Room(3, "201", Room.Type.SUITE, 2, 250000, true));

        HotelService service = new HotelService(clients, employees, rooms, reservations, invoices);
        PricingPolicy standardPricing = new StandardPricing();
        PricingPolicy longStayPricing = new LongStayPricing(7, 10);
        new HotelApp(service, standardPricing, longStayPricing).start();
    }
}
