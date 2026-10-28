package persistence;

import java.time.LocalDate;
import java.util.List;
import model.Client;
import model.Employee;
import model.Reservation;
import model.Room;

public class ReservationFileMapper implements EntityFileMapper<Reservation> {
    private final Repository<Client> clients;
    private final Repository<Employee> employees;
    private final Repository<Room> rooms;

    public ReservationFileMapper(Repository<Client> clients, Repository<Employee> employees,
                                 Repository<Room> rooms) {
        this.clients = clients;
        this.employees = employees;
        this.rooms = rooms;
    }

    @Override
    public String toLine(Reservation reservation) {
        return DelimitedText.join(Integer.toString(reservation.getId()),
                Integer.toString(reservation.getClient().getId()),
                reservation.getEmployee() == null ? "0" : Integer.toString(reservation.getEmployee().getId()),
                Integer.toString(reservation.getRoom().getId()), reservation.getCheckIn().toString(),
                reservation.getCheckOut().toString(), Double.toString(reservation.getPricePerNight()),
                Double.toString(reservation.getTotal()), reservation.getStatus().name());
    }

    @Override
    public Reservation fromLine(String line) {
        List<String> f = DelimitedText.requireFieldCount(line, 9);
        int clientId = Integer.parseInt(f.get(1));
        int employeeId = Integer.parseInt(f.get(2));
        int roomId = Integer.parseInt(f.get(3));
        Client client = clients.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el cliente vinculado #" + clientId));
        Employee employee = employeeId == 0 ? null : employees.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el trabajador vinculado #" + employeeId));
        Room room = rooms.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la habitacion vinculada #" + roomId));
        return new Reservation(Integer.parseInt(f.get(0)), client, employee, room,
                LocalDate.parse(f.get(4)), LocalDate.parse(f.get(5)), Double.parseDouble(f.get(6)),
                Double.parseDouble(f.get(7)), Reservation.Status.valueOf(f.get(8)));
    }
}
