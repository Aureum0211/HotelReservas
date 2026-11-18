package ui;

import exceptions.BusinessRuleException;
import exceptions.HotelException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Scanner;
import model.Client;
import model.Employee;
import model.Invoice;
import model.Person;
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
    private Employee currentUser;

    /**
     * Construye el menu y recibe el servicio del hotel y las estrategias de precios
     * disponibles.
     * @param hotel valor utilizado por la operacion.
     * @param standardPricing estrategia de tarifa normal.
     * @param longStayPricing estrategia de tarifa para estancias largas.
     */
    public HotelApp(HotelService hotel) {
        this.hotel = hotel;
    }

    /**
     * Pide fechas y cantidad de personas y muestra las habitaciones que se pueden reservar.
     */
    private void searchAvailableRooms() {
        System.out.println("--- Buscar habitaciones disponibles ---");
        LocalDate checkIn = readDate("Fecha de entrada (AAAA-MM-DD): ");
        LocalDate checkOut = readDate("Fecha de salida (AAAA-MM-DD): ");
        int guests = readInt("Cantidad de personas: ");
        printAvailableRooms(checkIn, checkOut, guests);
    }

    /**
     * Muestra las habitaciones disponibles para el periodo y la cantidad de personas.
     * @param checkIn fecha de entrada.
     * @param checkOut fecha de salida.
     * @param guests cantidad de personas.
     * @return true si habia al menos una habitacion disponible.
     */
    private boolean printAvailableRooms(LocalDate checkIn, LocalDate checkOut, int guests) {
        List<Room> available = hotel.findAvailableRooms(checkIn, checkOut, guests);
        if (available.isEmpty()) {
            System.out.println("No hay habitaciones disponibles para esas fechas y esa cantidad de personas.");
            return false;
        }
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        System.out.println("Habitaciones disponibles (" + nights + " noches, " + guests + " personas):");
        for (Room room : available) {
            System.out.printf("ID %d | %s | Capacidad: %d | $%.0f por noche | $%.0f en total%n",
                    room.getId(), room, room.getCapacity(), room.getPricePerNight(),
                    room.getPricePerNight() * nights);
        }
        return true;
    }

    /**
     * Registra la llegada de un huesped a una reserva confirmada.
     */
    private void checkIn() {
        if (hotel.getReservations().isEmpty()) throw new IllegalArgumentException("No hay reservas.");
        showReservations();
        int id = readInt("ID de la reserva que hace check-in: ");
        hotel.checkIn(id);
        System.out.println("Check-in registrado. La habitacion queda ocupada.");
    }

    /**
     * Registra la salida de un huesped y avisa si la reserva no tiene un pago registrado.
     */
    private void checkOut() {
        if (hotel.getReservations().isEmpty()) throw new IllegalArgumentException("No hay reservas.");
        showReservations();
        int id = readInt("ID de la reserva que hace check-out: ");
        hotel.checkOut(id);
        System.out.println("Check-out registrado. La habitacion queda libre.");
        if (!hotel.hasPayments(id)) {
            System.out.println("Aviso: esta reserva no tiene abonos registrados.");
        }
    }

    /** Muestra un resumen basico de ocupacion e ingresos. */
    private void showBasicReport() {
        requireRole(Employee.Role.ADMIN);
        HotelService.BasicReport report = hotel.getBasicReport(currentUser);
        System.out.println("=== REPORTE BASICO DEL HOTEL ===");
        System.out.println("Habitaciones registradas: " + report.totalRooms());
        System.out.println("Ocupadas hoy: " + report.occupiedRooms());
        System.out.println("Reservadas para hoy: " + report.reservedRooms());
        System.out.println("Libres hoy: " + report.freeRooms());
        System.out.println("En mantenimiento: " + report.maintenanceRooms());
        System.out.println("No disponibles: " + report.unavailableRooms());
        System.out.printf("Ingresos netos registrados: $%.2f%n", report.income());
        System.out.printf("Saldo pendiente: $%.2f%n", report.pendingBalance());
    }

    /**
     * Pide usuario y contrasena hasta que sean correctos. Si se escribe "volver" como
     * usuario, regresa a la pantalla de inicio.
     * @return true si el trabajador inicio sesion; false si decidio volver.
     */
    private boolean login() {
        System.out.println("--- Iniciar sesion (escriba \"volver\" como usuario para regresar) ---");
        while (true) {
            String username = readText("Usuario: ");
            if (username.equalsIgnoreCase("volver")) {
                return false;
            }
            String password = readText("Contrasena: ");
            try {
                currentUser = hotel.login(username, password);
                System.out.println("Bienvenido/a, " + currentUser.getName() + ".");
                return true;
            } catch (HotelException e) {
                System.out.println(e.getMessage() + " Intente de nuevo.");
            }
        }
    }

    /**
     * Comprueba que quien tiene la sesion iniciada tenga al menos el grado indicado.
     * @param minimum grado minimo que exige la operacion.
     * @throws BusinessRuleException si el grado del trabajador es inferior.
     */
    private void requireRole(Employee.Role minimum) {
        hotel.requireRole(currentUser, minimum);
    }

    /**
     * Pide un grado de trabajador.
     * @return grado seleccionado.
     */
    private Employee.Role readRole() {
        System.out.println("Rol: 1) Recepcionista  2) Administrador");
        return switch (readInt("Seleccione: ")) {
            case 1 -> Employee.Role.RECEPTIONIST;
            case 2 -> Employee.Role.ADMIN;
            default -> throw new IllegalArgumentException("Grado no valido.");
        };
    }

    /**
     * Muestra la pantalla de inicio, donde cada persona elige si entra como huesped o como
     * trabajador, hasta que se selecciona salir.
     */
    public void start() {
        System.out.println("=== HOTEL RESERVA ===");
        System.out.println("Los datos se guardan en la base de datos.");
        boolean running = true;
        while (running) {
            System.out.println("""

                    === BIENVENIDO ===
                    1. Soy huesped
                    2. Soy trabajador
                    0. Salir
                    """);
            System.out.print("Seleccione una opcion: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> guestMenu();
                case "2" -> {
                    if (login()) {
                        workerMenu();
                        currentUser = null;
                    }
                }
                case "0" -> running = false;
                default -> System.out.println("Opcion no valida.");
            }
        }
        System.out.println("Programa finalizado.");
    }

    /**
     * Menu de la zona de huespedes. No pide inicio de sesion: el huesped se identifica
     * con su documento para reservar, ver sus reservas y cancelarlas.
     */
    private void guestMenu() {
        boolean inside = true;
        while (inside) {
            System.out.println("""

                    === ZONA DE HUESPEDES ===
                    1. Registrarme
                    2. Buscar habitaciones disponibles
                    3. Reservar una habitacion
                    4. Ver mis reservas
                    5. Cancelar una reserva
                    0. Volver
                    """);
            System.out.print("Seleccione una opcion: ");
            String option = scanner.nextLine().trim();
            try {
                switch (option) {
                    case "1" -> registerGuest();
                    case "2" -> searchAvailableRooms();
                    case "3" -> guestReserve();
                    case "4" -> guestShowReservations();
                    case "5" -> guestCancelReservation();
                    case "0" -> inside = false;
                    default -> System.out.println("Opcion no valida.");
                }
            } catch (HotelException | IllegalArgumentException | UncheckedIOException e) {
                System.out.println("No se pudo completar la operacion: " + e.getMessage());
            }
        }
    }

    /**
     * Registro de un huesped por su cuenta. Siempre queda con la categoria Nuevo.
     */
    private void registerGuest() {
        System.out.println("--- Registro de huesped ---");
        String name = readText("Nombre completo: ");
        String document = readText("Documento: ");
        String email = readText("Correo: ");
        String phone = readText("Telefono (10 digitos): ");
        String city = readText("Ciudad: ");
        Client client = hotel.registerGuest(name, document, email, phone, city);
        System.out.println("Registro exitoso. Tu codigo de cliente es " + client.getId() + ".");
        System.out.println("Ya puedes reservar desde esta zona usando tu documento.");
    }

    private void guestReserve() {
        Client client = hotel.findClientByDocument(readText("Tu documento: "));
        bookRoom(client.getId(), null);
        System.out.println("Para pagar, acercate a recepcion.");
    }

    private void guestShowReservations() {
        Client client = hotel.findClientByDocument(readText("Tu documento: "));
        showGuestReservations(client);
    }

    private void guestCancelReservation() {
        Client client = hotel.findClientByDocument(readText("Tu documento: "));
        if (!showGuestReservations(client)) return;
        hotel.cancelGuestReservation(client.getId(), readInt("ID de la reserva que desea cancelar: "));
        System.out.println("Reserva cancelada.");
    }

    /** Muestra las reservas del huesped. Devuelve false si no tiene ninguna. */
    private boolean showGuestReservations(Client client) {
        List<Reservation> mine = hotel.getReservationsOf(client.getId());
        if (mine.isEmpty()) {
            System.out.println("No tienes reservas.");
            return false;
        }
        mine.forEach(System.out::println);
        return true;
    }

    private void workerMenu() {
        boolean running = true;
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
                    case "14" -> searchAvailableRooms();
                    case "15" -> checkIn();
                    case "16" -> checkOut();
                    case "17" -> showBasicReport();
                    case "18" -> manageRoomMaintenance();
                    case "0" -> running = false;
                    default -> System.out.println("Opcion no valida.");
                }
            } catch (HotelException | IllegalArgumentException | UncheckedIOException e) {
                System.out.println("No se pudo completar la operacion: " + e.getMessage());
            }
            System.out.println();
        }
        System.out.println("Sesion cerrada.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("Sesion: " + currentUser.getName() + " - " + currentUser.getRole().getLabel());
        System.out.println("""

                1. Registrar cliente
                2. Registrar trabajador (Administrador)
                3. Registrar habitacion (Administrador)
                4. Listar clientes
                5. Listar trabajadores (Administrador)
                6. Listar habitaciones
                7. Crear reserva
                8. Listar reservas
                9. Emitir comprobante
                10. Listar comprobantes
                11. Registrar pago
                12. Cancelar reserva (si esta pagada: Administrador)
                13. Listar todas las personas (Administrador)
                14. Buscar habitaciones disponibles
                15. Registrar check-in
                16. Registrar check-out
                17. Reporte basico de ocupacion e ingresos (Administrador)
                18. Mantenimiento de habitaciones (Administrador)
                0. Cerrar sesion
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
        requireRole(Employee.Role.ADMIN);
        System.out.println("--- Registrar trabajador ---");
        String name = readText("Nombre completo: ");
        String document = readText("Documento: ");
        String email = readText("Correo: ");
        String phone = readText("Telefono (10 digitos): ");
        String city = readText("Ciudad: ");
        Employee.Role role = readRole();
        String username = readText("Usuario: ");
        String password = readText("Contrasena (demo academica): ");
        Employee employee = hotel.registerEmployee(name, document, email, phone, city, role, username, password, currentUser);
        System.out.println("Trabajador registrado: " + employee.describe() + " | ID: " + employee.getId());
    }

    private void registerRoom() {
        requireRole(Employee.Role.ADMIN);
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
        Room room = hotel.registerRoom(number, type, capacity, price, currentUser);
        System.out.printf("Habitacion registrada: ID %d | %s | Capacidad: %d | $%.0f por noche%n",
                room.getId(), room, room.getCapacity(), room.getPricePerNight());
    }

    private void manageRoomMaintenance() {
        requireRole(Employee.Role.ADMIN);
        if (hotel.getRooms().isEmpty()) {
            throw new IllegalArgumentException("No hay habitaciones registradas.");
        }
        showRooms();
        int roomId = readInt("ID de la habitacion: ");
        System.out.println("1. Iniciar mantenimiento\n2. Finalizar mantenimiento");
        boolean maintenance = switch (readInt("Seleccione: ")) {
            case 1 -> true;
            case 2 -> false;
            default -> throw new IllegalArgumentException("Opcion no valida.");
        };
        hotel.setRoomMaintenance(roomId, maintenance, currentUser);
        System.out.println(maintenance ? "Mantenimiento iniciado." : "Mantenimiento finalizado.");
    }

    private void showClients() {
        if (hotel.getClients().isEmpty()) {
            System.out.println("Todavia no hay clientes registrados.");
            return;
        }
        hotel.getClients().forEach(client -> System.out.println("ID " + client.getId() + " | " + client.describe()));
    }

    private void showEmployees() {
        requireRole(Employee.Role.ADMIN);
        if (hotel.getEmployees(currentUser).isEmpty()) {
            System.out.println("Todavia no hay trabajadores registrados.");
            return;
        }
        hotel.getEmployees(currentUser).forEach(employee -> System.out.println("ID " + employee.getId() + " | " + employee.describe()));
    }

    private void showPeople() {
        requireRole(Employee.Role.ADMIN);
        if (hotel.getPeople(currentUser).isEmpty()) {
            System.out.println("Todavia no hay personas registradas.");
            return;
        }
        for (Person person : hotel.getPeople(currentUser)) {
            System.out.println("ID " + person.getId() + " | " + person.describe());
        }
    }

    private void showRooms() {
        if (hotel.getRooms().isEmpty()) {
            System.out.println("Todavia no hay habitaciones registradas.");
            return;
        }
        for (Room room : hotel.getRooms()) {
            String status = room.isUnderMaintenance() ? "En mantenimiento"
                    : room.isAvailable() ? "Habilitada" : "No disponible";
            System.out.printf("ID %d | %s | Capacidad: %d | $%.0f por noche | %s%n",
                    room.getId(), room, room.getCapacity(), room.getPricePerNight(), status);
        }
    }

    private void createReservation() {
        if (hotel.getClients().isEmpty()) throw new IllegalArgumentException("Primero registra al menos un cliente.");
        if (hotel.getRooms().isEmpty()) throw new IllegalArgumentException("Primero registra al menos una habitacion.");
        showClients();
        bookRoom(readInt("ID del cliente: "), currentUser);
    }

    /** Pide fechas, muestra las habitaciones libres y crea la reserva. employee es null si reserva el huesped. */
    private void bookRoom(int clientId, Employee employee) {
        LocalDate checkIn = readDate("Fecha de entrada (AAAA-MM-DD): ");
        LocalDate checkOut = readDate("Fecha de salida (AAAA-MM-DD): ");
        int guests = readInt("Cantidad de personas: ");
        if (!printAvailableRooms(checkIn, checkOut, guests)) {
            return;
        }
        int roomId = readInt("ID de la habitacion: ");
        Reservation reservation = hotel.createReservation(clientId, roomId, checkIn, checkOut, guests, employee);
        System.out.println("Reserva creada correctamente:");
        System.out.println(reservation);
        double base = reservation.getPricePerNight() * reservation.nights();
        System.out.println("Noches: " + reservation.nights());
        System.out.printf("Precio sin descuento: $%.0f%n", base);
        System.out.printf("Descuento aplicado:   $%.0f%n", base - reservation.getTotal());
        System.out.printf("Total a pagar:        $%.0f%n", reservation.getTotal());
    }

    private void showReservations() {
        if (hotel.getReservations().isEmpty()) {
            System.out.println("Todavia no hay reservas.");
            return;
        }
        for (Reservation reservation : hotel.getReservations()) {
            String attendedBy = reservation.getEmployee() == null
                    ? " | Reservada por el huesped" : " | Atendio: " + reservation.getEmployee().getName();
            System.out.println(reservation + attendedBy);
        }
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
        int id = readInt("ID del comprobante que desea abonar: ");
        double amount = readDouble("Monto del abono: ");
        hotel.payInvoice(id, amount);
        System.out.println("Abono registrado y guardado correctamente.");
    }

    private void cancelReservation() {
        if (hotel.getReservations().isEmpty()) throw new IllegalArgumentException("No hay reservas para cancelar.");
        showReservations();
        int id = readInt("ID de la reserva que desea cancelar: ");
        boolean paid = hotel.hasPayments(id);
        hotel.cancelReservation(id, currentUser);
        System.out.println("Reserva cancelada y guardada.");
        if (paid) {
            System.out.println("El comprobante pagado permanece registrado. No se realiza un reembolso automatico.");
        }
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
