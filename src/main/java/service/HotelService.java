package service;

import exceptions.BusinessRuleException;
import exceptions.InvalidDataException;
import exceptions.ResourceNotFoundException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.Client;
import model.Employee;
import model.DiscountPricing;
import model.Invoice;
import model.LongStayPricing;
import model.Person;
import model.PricingPolicy;
import model.Reservation;
import model.Room;
import persistence.Repository;
import validation.Validator;

/**
 * Coordina las operaciones del hotel y aplica las reglas de negocio sin depender de una
 * implementacion concreta de almacenamiento.
 */
public class HotelService {

    /**
     * Habitacion junto con la cantidad de reservas no canceladas que ha tenido.
     * @param room habitacion consultada.
     * @param reservations cantidad de reservas no canceladas de esa habitacion.
     */
    public record RoomRanking(Room room, long reservations) { }

    private static final int LONG_STAY_MIN_NIGHTS = 7;
    private static final double LONG_STAY_DISCOUNT = 10;
    private static final double FREQUENT_DISCOUNT = 5;
    private static final double VIP_DISCOUNT = 15;
    private static final int FULL_REFUND_MIN_DAYS = 2;
    private static final double PARTIAL_REFUND_PERCENT = 50;

    private final Repository<Client> clients;
    private final Repository<Employee> employees;
    private final Repository<Room> rooms;
    private final Repository<Reservation> reservations;
    private final Repository<Invoice> invoices;

    /**
     * Construye el servicio recibiendo los repositorios de clientes, trabajadores,
     * habitaciones, reservas y comprobantes.
     * @param clients repositorio de clientes.
     * @param employees repositorio de trabajadores.
     * @param rooms repositorio de habitaciones.
     * @param reservations repositorio de reservas.
     * @param invoices repositorio de comprobantes.
     */
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

    /**
     * Devuelve todos los clientes registrados.
     * @return lista de clientes registrados.
     */
    public List<Client> getClients() { return clients.findAll(); }

    /**
     * Devuelve todos los trabajadores registrados.
     * @return lista de trabajadores registrados.
     */
    public List<Employee> getEmployees() { return employees.findAll(); }

    /**
     * Devuelve todas las habitaciones registradas.
     * @return lista de habitaciones registradas.
     */
    public List<Room> getRooms() { return rooms.findAll(); }

    /**
     * Devuelve todas las reservas registradas.
     * @return lista de reservas registradas.
     */
    public List<Reservation> getReservations() { return reservations.findAll(); }

    /**
     * Devuelve todos los comprobantes registrados.
     * @return lista de comprobantes registrados.
     */
    public List<Invoice> getInvoices() { return invoices.findAll(); }

    /**
     * Valida las credenciales de un trabajador.
     * @param username usuario escrito.
     * @param password contrasena escrita.
     * @return el trabajador que inicio sesion.
     * @throws BusinessRuleException si el usuario o la contrasena no coinciden, o si el
     *         trabajador esta inactivo.
     */
    public Employee login(String username, String password) {
        for (Employee employee : employees.findAll()) {
            boolean sameUser = employee.getUsername().equalsIgnoreCase(username.trim());
            if (sameUser && employee.getPassword().equals(password) && employee.isActive()) {
                return employee;
            }
        }
        throw new BusinessRuleException("Usuario o contrasena incorrectos.");
    }

    /**
     * Valida la unicidad de los datos, asigna un identificador y registra un nuevo
     * cliente.
     * @param name nombre de la persona.
     * @param documentNumber numero de documento.
     * @param email correo electronico.
     * @param phone numero de telefono.
     * @param city ciudad asociada a la persona.
     * @param category categoria asignada al cliente.
     * @return cliente registrado.
     * @throws BusinessRuleException si ya existe una persona con el mismo documento, correo o telefono.
     */
    public Client registerClient(String name, String documentNumber, String email,
                                 String phone, String city, Client.Category category) {
        ensurePersonIsUnique(documentNumber, email, phone);
        Client client = new Client(clients.nextId(), name, documentNumber, email, phone, city, category);
        clients.save(client);
        return client;
    }

    /**
     * Registra a un huesped que se inscribe por su cuenta. Siempre entra con la categoria
     * NEW, para que nadie se asigne por su cuenta un descuento.
     * @param name nombre completo.
     * @param documentNumber numero de documento.
     * @param email correo electronico.
     * @param phone telefono de 10 digitos.
     * @param city ciudad.
     * @return el cliente registrado.
     * @throws BusinessRuleException si el documento, correo o telefono ya existen.
     */
    public Client registerGuest(String name, String documentNumber, String email,
                                String phone, String city) {
        return registerClient(name, documentNumber, email, phone, city, Client.Category.NEW);
    }

    /**
     * Valida la unicidad de los datos y del usuario, asigna un identificador y registra un
     * trabajador.
     * @param name nombre de la persona.
     * @param documentNumber numero de documento.
     * @param email correo electronico.
     * @param phone numero de telefono.
     * @param city ciudad asociada a la persona.
     * @param role rol asignado al trabajador.
     * @param username nombre de usuario.
     * @param password contrasena del trabajador.
     * @return trabajador registrado.
     * @throws BusinessRuleException si los datos personales o el usuario ya existen.
     */
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

    /**
     * Comprueba que el numero no este repetido y registra una habitacion disponible.
     * @param number numero visible de la habitacion.
     * @param type tipo de habitacion.
     * @param capacity capacidad maxima de personas.
     * @param pricePerNight precio establecido por cada noche.
     * @return habitacion registrada.
     * @throws BusinessRuleException si ya existe una habitacion con ese numero.
     */
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

    /**
     * Combina los clientes y trabajadores en una lista comun de personas.
     * @return lista no modificable que contiene clientes y trabajadores.
     */
    public List<Person> getPeople() {
        List<Person> people = new ArrayList<>();
        people.addAll(clients.findAll());
        people.addAll(employees.findAll());
        return List.copyOf(people);
    }

    /**
     * Busca el cliente y la habitacion, valida las fechas y los cruces de reserva, y
     * guarda la nueva reserva.
     * @param clientId valor utilizado por la operacion.
     * @param roomId valor utilizado por la operacion.
     * @param checkIn fecha de entrada.
     * @param checkOut fecha de salida.
     * @param guests cantidad de personas que se hospedaran.
     * @param employee trabajador que registra la reserva.
     * @return reserva creada y almacenada.
     * @throws ResourceNotFoundException si no existe el cliente o la habitacion solicitados.
     * @throws BusinessRuleException si la habitacion esta ocupada en las fechas indicadas.
     */
    public Reservation createReservation(int clientId, int roomId, LocalDate checkIn,
                                         LocalDate checkOut, int guests, Employee employee) {
        Validator.requireNonNull(employee, "El trabajador");
        Client client = findClient(clientId);
        Room room = findRoom(roomId);

        Reservation reservation = Reservation.create(
                reservations.nextId(), client, employee, room, checkIn, checkOut, guests, pricingFor(client));

        if (isBooked(room, checkIn, checkOut)) {
            throw new BusinessRuleException("La habitacion ya esta reservada en esas fechas.");
        }

        reservations.save(reservation);
        return reservation;
    }

    /**
     * Busca las habitaciones que se pueden reservar en un periodo para cierta cantidad de
     * personas. Una habitacion sirve si esta habilitada, tiene capacidad suficiente y no
     * tiene otra reserva activa en esas fechas.
     * @param checkIn fecha de entrada.
     * @param checkOut fecha de salida.
     * @param guests cantidad de personas que se van a hospedar.
     * @return habitaciones disponibles, ordenadas de la mas barata a la mas cara.
     * @throws InvalidDataException si las fechas o la cantidad de personas no son validas.
     */
    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut, int guests) {
        Validator.requireNonNull(checkIn, "La fecha de entrada");
        Validator.requireNonNull(checkOut, "La fecha de salida");
        Validator.requirePositive(guests, "La cantidad de personas");
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidDataException("La fecha de salida debe ser posterior a la de entrada.");
        }
        List<Room> available = new ArrayList<>();
        for (Room room : rooms.findAll()) {
            if (room.isAvailable() && room.getCapacity() >= guests && !isBooked(room, checkIn, checkOut)) {
                available.add(room);
            }
        }
        available.sort(Comparator.comparingDouble(Room::getPricePerNight));
        return available;
    }

    /**
     * Elige la politica de precios de un cliente. Todos reciben el descuento por estadia
     * larga y, ademas, los clientes frecuentes y VIP reciben un descuento por categoria.
     * Ambos descuentos se acumulan.
     * @param client cliente que reserva.
     * @return politica de precios que le corresponde.
     */
    private PricingPolicy pricingFor(Client client) {
        PricingPolicy longStay = new LongStayPricing(LONG_STAY_MIN_NIGHTS, LONG_STAY_DISCOUNT);
        return switch (client.getCategory()) {
            case NEW -> longStay;
            case FREQUENT -> new DiscountPricing(longStay, FREQUENT_DISCOUNT);
            case VIP -> new DiscountPricing(longStay, VIP_DISCOUNT);
        };
    }

    /**
     * Devuelve las habitaciones mas reservadas. Cuenta las reservas no canceladas y, si dos
     * habitaciones empatan, va primero la de menor numero. Las que nunca se han reservado
     * no aparecen.
     * @param limit cantidad maxima de habitaciones que se devuelven.
     * @return ranking de habitaciones, de la mas reservada a la menos reservada.
     */
    public List<RoomRanking> topRooms(int limit) {
        List<RoomRanking> ranking = new ArrayList<>();
        for (Room room : rooms.findAll()) {
            long count = reservations.findAll().stream()
                    .filter(r -> r.getRoom().getId() == room.getId() && !r.isCancelled())
                    .count();
            if (count > 0) {
                ranking.add(new RoomRanking(room, count));
            }
        }
        ranking.sort(Comparator.comparingLong(RoomRanking::reservations).reversed()
                .thenComparing(item -> item.room().getNumber()));
        return List.copyOf(ranking.subList(0, Math.min(limit, ranking.size())));
    }

    /**
     * Busca la reserva vigente que ocupa una habitacion en un dia concreto.
     * @param room habitacion que se consulta.
     * @param day dia que se consulta.
     * @return la reserva vigente de ese dia, o null si la habitacion esta libre.
     */
    public Reservation reservationOn(Room room, LocalDate day) {
        return reservations.findAll().stream()
                .filter(r -> r.occupies(room, day, day.plusDays(1)))
                .findFirst()
                .orElse(null);
    }

    /**
     * Indica si la habitacion ya tiene una reserva activa que se cruza con el periodo.
     * @param room habitacion que se consulta.
     * @param checkIn fecha de entrada.
     * @param checkOut fecha de salida.
     * @return true si la habitacion esta ocupada en algun dia del periodo.
     */
    private boolean isBooked(Room room, LocalDate checkIn, LocalDate checkOut) {
        return reservations.findAll().stream().anyMatch(r -> r.occupies(room, checkIn, checkOut));
    }

    /**
     * Emite y guarda un comprobante para una reserva existente.
     * @param reservationId identificador de la reserva.
     * @param method metodo de pago seleccionado.
     * @return comprobante emitido y almacenado.
     * @throws ResourceNotFoundException si no existe la reserva indicada.
     * @throws BusinessRuleException si la reserva no puede facturarse.
     */
    public Invoice issueInvoice(int reservationId, Invoice.PaymentMethod method) {
        Reservation reservation = findReservation(reservationId);
        boolean alreadyInvoiced = invoices.findAll().stream()
                .anyMatch(i -> i.getReservation().getId() == reservationId
                        && i.getStatus() != Invoice.Status.VOIDED);
        if (alreadyInvoiced) {
            throw new BusinessRuleException("La reserva #" + reservationId + " ya tiene un comprobante.");
        }
        Invoice invoice = Invoice.issue(invoices.nextId(), reservation, method);
        invoices.save(invoice);
        return invoice;
    }

    /**
     * Registra el pago de un comprobante y persiste su nuevo estado.
     * @param invoiceId valor utilizado por la operacion.
     * @throws ResourceNotFoundException si no existe el comprobante indicado.
     * @throws BusinessRuleException si el comprobante no puede pagarse.
     */
    public void payInvoice(int invoiceId) {
        Invoice invoice = findInvoice(invoiceId);
        invoice.pay();
        invoices.update(invoice);
    }

    /**
     * Registra la llegada del huesped (check-in) usando la fecha de hoy.
     * @param reservationId identificador de la reserva.
     * @throws BusinessRuleException si la reserva no admite check-in en esta fecha.
     */
    public void checkIn(int reservationId) {
        Reservation reservation = findReservation(reservationId);
        reservation.registerCheckIn(LocalDate.now());
        reservations.update(reservation);
    }

    /**
     * Registra la salida del huesped (check-out) y deja la habitacion libre.
     * @param reservationId identificador de la reserva.
     * @throws BusinessRuleException si la reserva no tiene check-in.
     */
    public void checkOut(int reservationId) {
        Reservation reservation = findReservation(reservationId);
        reservation.registerCheckOut();
        reservations.update(reservation);
    }

    /**
     * Indica si la reserva tiene un comprobante pagado.
     * @param reservationId identificador de la reserva.
     * @return true si existe al menos un comprobante pagado para esa reserva.
     */
    public boolean hasPaidInvoice(int reservationId) {
        return invoices.findAll().stream().anyMatch(i ->
                i.getReservation().getId() == reservationId && i.getStatus() == Invoice.Status.PAID);
    }

    /**
     * Cancela una reserva, anula su comprobante pendiente y reembolsa el comprobante pagado.
     * Politica de reembolso: si faltan 2 dias o mas para la entrada se devuelve el 100 %;
     * si falta menos, se devuelve el 50 %.
     * @param reservationId identificador de la reserva.
     * @return valor reembolsado al cliente; cero si no habia nada pagado.
     * @throws ResourceNotFoundException si no existe la reserva indicada.
     * @throws BusinessRuleException si la reserva ya estaba cancelada o ya tuvo check-in.
     */
    public double cancelReservation(int reservationId) {
        Reservation reservation = findReservation(reservationId);
        reservation.cancel();
        reservations.update(reservation);

        long daysBeforeCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), reservation.getCheckIn());
        double percent = daysBeforeCheckIn >= FULL_REFUND_MIN_DAYS ? 100 : PARTIAL_REFUND_PERCENT;

        double totalRefund = 0;
        for (Invoice invoice : invoices.findAll()) {
            if (invoice.getReservation().getId() != reservationId) {
                continue;
            }
            if (invoice.getStatus() == Invoice.Status.PENDING) {
                invoice.annul();
                invoices.update(invoice);
            } else if (invoice.getStatus() == Invoice.Status.PAID) {
                double refund = invoice.getAmount() * percent / 100;
                invoice.refund(refund);
                invoices.update(invoice);
                totalRefund += refund;
            }
        }
        return totalRefund;
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
