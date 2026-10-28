package service;

import exceptions.BusinessRuleException;
import exceptions.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Client;
import model.Employee;
import model.Invoice;
import model.Person;
import model.PricingPolicy;
import model.Reservation;
import model.Room;
import persistence.Repository;

/**
 * Coordina las operaciones del hotel y aplica las reglas de negocio sin depender de una
 * implementacion concreta de almacenamiento.
 */
public class HotelService {

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
     * @param pricingPolicy estrategia de precios que calcula el total.
     * @return reserva creada y almacenada.
     * @throws ResourceNotFoundException si no existe el cliente o la habitacion solicitados.
     * @throws BusinessRuleException si la habitacion esta ocupada en las fechas indicadas.
     */
    public Reservation createReservation(int clientId, int roomId, LocalDate checkIn,
                                         LocalDate checkOut, PricingPolicy pricingPolicy) {
        Client client = findClient(clientId);
        Room room = findRoom(roomId);
        Employee employee = employees.findAll().stream().findFirst().orElse(null);

        Reservation reservation = Reservation.create(
                reservations.nextId(), client, employee, room, checkIn, checkOut, pricingPolicy);

        for (Reservation existing : reservations.findAll()) {
            if (existing.occupies(room, checkIn, checkOut)) {
                throw new BusinessRuleException("La habitacion ya esta reservada en esas fechas.");
            }
        }

        reservations.save(reservation);
        return reservation;
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
     * Cancela una reserva y persiste su nuevo estado.
     * @param reservationId identificador de la reserva.
     * @throws ResourceNotFoundException si no existe la reserva indicada.
     * @throws BusinessRuleException si la reserva ya estaba cancelada.
     */
    public void cancelReservation(int reservationId) {
        Reservation reservation = findReservation(reservationId);
        reservation.cancel();
        reservations.update(reservation);
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
