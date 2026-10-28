package ui;

import exceptions.HotelException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import model.Client;
import model.Employee;
import model.Invoice;
import model.Person;
import model.PricingPolicy;
import model.Reservation;
import model.Room;
import service.HotelService;

/**
 * Presenta el menu de consola y coordina la entrada del usuario con las operaciones
 * ofrecidas por HotelService.
 */
public class HotelApp {
    private final Scanner scanner = new Scanner(System.in);
    private final HotelService hotel;
    private final PricingPolicy standardPricing;
    private final PricingPolicy longStayPricing;

    /**
     * Construye el menu y recibe el servicio del hotel y las estrategias de precios
     * disponibles.
     * @param hotel valor utilizado por la operacion.
     * @param standardPricing estrategia de tarifa normal.
     * @param longStayPricing estrategia de tarifa para estancias largas.
     */
    public HotelApp(HotelService hotel, PricingPolicy standardPricing, PricingPolicy longStayPricing) {
        this.hotel = hotel;
        this.standardPricing = standardPricing;
        this.longStayPricing = longStayPricing;
    }

    /**
     * Ejecuta el ciclo principal del menu de consola hasta que el usuario selecciona
     * salir.
     */
    public void start() {
        boolean running = true;
        System.out.println("=== HOTEL RESERVAS - DEMO ===");
        System.out.println("Los datos se guardan en archivos de texto dentro de la carpeta data.");
        System.out.println("Hay datos ficticios de ejemplo en data; puedes agregar mas desde el menu.");
        while (running) {
            printMenu();
            String option = scanner.nextLine().trim();
            try {
                switch (option) {
                    case "1" -> registerClient();
                    case "2" -> registerEmployee();
                    case "3" -> registerRoom();
                    case "4" -> showClients();
                    case "5" -> showEmployees();
                    case "6" -> showRooms();
                    case "7" -> createReservation();
                    case "8" -> showReservations();
                    case "9" -> issueInvoice();
                    case "10" -> showInvoices();
                    case "11" -> payInvoice();
                    case "12" -> cancelReservation();
                    case "13" -> showPeople();
                    case "0" -> running = false;
                    default -> System.out.println("Opcion no valida.");
                }
            } catch (HotelException | IllegalArgumentException | UncheckedIOException e) {
                System.out.println("No se pudo completar la operacion: " + e.getMessage());
            }
            System.out.println();
        }
        System.out.println("Programa finalizado.");
    }

    private void printMenu() {
        System.out.println("""

                1. Registrar cliente
                2. Registrar trabajador
                3. Registrar habitacion
                4. Listar clientes
                5. Listar trabajadores
                6. Listar habitaciones
                7. Crear reserva
                8. Listar reservas
                9. Emitir comprobante
                10. Listar comprobantes
                11. Registrar pago
                12. Cancelar reserva
                13. Listar todas las personas
                0. Salir
                """);
        System.out.print("Seleccione una opcion: ");
    }

    private void registerClient() {
        System.out.println("--- Registrar cliente ---");
        String name = readText("Nombre completo: ");
        String document = readText("Documento: ");
        String email = readText("Correo: ");
        String phone = readText("Telefono (10 digitos): ");
        String city = readText("Ciudad: ");
        System.out.println("Categoria: 1) Nuevo  2) Frecuente  3) VIP");
        Client.Category category = switch (readInt("Seleccione: ")) {
            case 1 -> Client.Category.NEW;
            case 2 -> Client.Category.FREQUENT;
            case 3 -> Client.Category.VIP;
            default -> throw new IllegalArgumentException("Categoria no valida.");
        };
        Client client = hotel.registerClient(name, document, email, phone, city, category);
        System.out.println("Cliente registrado: " + client.describe() + " | ID: " + client.getId());
    }

    private void registerEmployee() {
        System.out.println("--- Registrar trabajador ---");
        String name = readText("Nombre completo: ");
        String document = readText("Documento: ");
        String email = readText("Correo: ");
        String phone = readText("Telefono (10 digitos): ");
        String city = readText("Ciudad: ");
        System.out.println("Rol: 1) Administrador  2) Recepcionista");
        Employee.Role role = switch (readInt("Seleccione: ")) {
            case 1 -> Employee.Role.ADMIN;
            case 2 -> Employee.Role.RECEPTIONIST;
            default -> throw new IllegalArgumentException("Rol no valido.");
        };
        String username = readText("Usuario: ");
        String password = readText("Contrasena (demo academica): ");
        Employee employee = hotel.registerEmployee(name, document, email, phone, city, role, username, password);
        System.out.println("Trabajador registrado: " + employee.describe() + " | ID: " + employee.getId());
    }

    private void registerRoom() {
        System.out.println("--- Registrar habitacion ---");
        String number = readText("Numero de habitacion: ");
        System.out.println("Tipo: 1) Sencilla  2) Doble  3) Suite");
        Room.Type type = switch (readInt("Seleccione: ")) {
            case 1 -> Room.Type.SINGLE;
            case 2 -> Room.Type.DOUBLE;
            case 3 -> Room.Type.SUITE;
            default -> throw new IllegalArgumentException("Tipo de habitacion no valido.");
        };
        int capacity = readInt("Capacidad de personas: ");
        double price = readDouble("Precio por noche: ");
        Room room = hotel.registerRoom(number, type, capacity, price);
        System.out.printf("Habitacion registrada: ID %d | %s | Capacidad: %d | $%.0f por noche%n",
                room.getId(), room, room.getCapacity(), room.getPricePerNight());
    }

    private void showClients() {
        if (hotel.getClients().isEmpty()) {
            System.out.println("Todavia no hay clientes registrados.");
            return;
        }
        hotel.getClients().forEach(client -> System.out.println("ID " + client.getId() + " | " + client.describe()));
    }

    private void showEmployees() {
        if (hotel.getEmployees().isEmpty()) {
            System.out.println("Todavia no hay trabajadores registrados.");
            return;
        }
        hotel.getEmployees().forEach(employee -> System.out.println("ID " + employee.getId() + " | " + employee.describe()));
    }

    private void showPeople() {
        if (hotel.getPeople().isEmpty()) {
            System.out.println("Todavia no hay personas registradas.");
            return;
        }
        for (Person person : hotel.getPeople()) {
            System.out.println("ID " + person.getId() + " | " + person.describe());
        }
    }

    private void showRooms() {
        if (hotel.getRooms().isEmpty()) {
            System.out.println("Todavia no hay habitaciones registradas.");
            return;
        }
        for (Room room : hotel.getRooms()) {
            System.out.printf("ID %d | %s | Capacidad: %d | $%.0f por noche | %s%n",
                    room.getId(), room, room.getCapacity(), room.getPricePerNight(),
                    room.isAvailable() ? "Habilitada" : "No disponible");
        }
    }

    private void createReservation() {
        if (hotel.getClients().isEmpty()) throw new IllegalArgumentException("Primero registra al menos un cliente.");
        if (hotel.getRooms().isEmpty()) throw new IllegalArgumentException("Primero registra al menos una habitacion.");
        showClients();
        int clientId = readInt("ID del cliente: ");
        showRooms();
        int roomId = readInt("ID de la habitacion: ");
        LocalDate checkIn = readDate("Fecha de entrada (AAAA-MM-DD): ");
        LocalDate checkOut = readDate("Fecha de salida (AAAA-MM-DD): ");
        System.out.print("¿Aplicar 10% de descuento desde 7 noches? (s/n): ");
        boolean discount = scanner.nextLine().trim().equalsIgnoreCase("s");
        PricingPolicy selectedPricing = discount ? longStayPricing : standardPricing;
        Reservation reservation = hotel.createReservation(clientId, roomId, checkIn, checkOut, selectedPricing);
        System.out.println("Reserva creada correctamente:");
        System.out.println(reservation);
        System.out.println("Noches: " + reservation.nights());
    }

    private void showReservations() {
        if (hotel.getReservations().isEmpty()) {
            System.out.println("Todavia no hay reservas.");
            return;
        }
        hotel.getReservations().forEach(System.out::println);
    }

    private void issueInvoice() {
        if (hotel.getReservations().isEmpty()) throw new IllegalArgumentException("Primero registra una reserva.");
        showReservations();
        int reservationId = readInt("ID de la reserva que desea facturar: ");
        System.out.println("Metodo de pago: 1) Efectivo  2) Tarjeta  3) Transferencia");
        Invoice.PaymentMethod paymentMethod = switch (readInt("Seleccione: ")) {
            case 1 -> Invoice.PaymentMethod.CASH;
            case 2 -> Invoice.PaymentMethod.CARD;
            case 3 -> Invoice.PaymentMethod.TRANSFER;
            default -> throw new IllegalArgumentException("Metodo de pago no valido.");
        };
        Invoice invoice = hotel.issueInvoice(reservationId, paymentMethod);
        System.out.println("Comprobante emitido: " + invoice);
    }

    private void showInvoices() {
        if (hotel.getInvoices().isEmpty()) {
            System.out.println("Todavia no hay comprobantes.");
            return;
        }
        hotel.getInvoices().forEach(System.out::println);
    }

    private void payInvoice() {
        if (hotel.getInvoices().isEmpty()) throw new IllegalArgumentException("Primero emite un comprobante.");
        showInvoices();
        int id = readInt("ID del comprobante que desea pagar: ");
        hotel.payInvoice(id);
        System.out.println("Pago registrado y guardado correctamente.");
    }

    private void cancelReservation() {
        if (hotel.getReservations().isEmpty()) throw new IllegalArgumentException("No hay reservas para cancelar.");
        showReservations();
        int id = readInt("ID de la reserva que desea cancelar: ");
        hotel.cancelReservation(id);
        System.out.println("Reserva cancelada y guardada.");
    }

    private String readText(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Debe escribir un numero entero.");
        }
    }

    private double readDouble(String prompt) {
        System.out.print(prompt);
        try {
            return Double.parseDouble(scanner.nextLine().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Debe escribir un numero valido.");
        }
    }

    private LocalDate readDate(String prompt) {
        System.out.print(prompt);
        try {
            return LocalDate.parse(scanner.nextLine().trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Use el formato AAAA-MM-DD.");
        }
    }
}
