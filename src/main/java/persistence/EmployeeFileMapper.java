package persistence;

import java.util.List;
import model.Employee;

/**
 * Convierte trabajadores entre objetos Java y registros de texto delimitados por punto y
 * coma.
 */
public class EmployeeFileMapper implements EntityFileMapper<Employee> {

    /**
     * Convierte los datos del trabajador en una linea delimitada para almacenarla en un
     * archivo.
     * @param employee trabajador asociado, si existe.
     * @return representacion textual de la entidad.
     */
    @Override
    public String toLine(Employee employee) {
        return DelimitedText.join(
                Integer.toString(employee.getId()), employee.getName(), employee.getDocumentNumber(),
                employee.getEmail(), employee.getPhone(), employee.getCity(),
                Boolean.toString(employee.isActive()), employee.getRole().name(),
                employee.getUsername(), employee.getPassword());
    }

    /**
     * Reconstruye un trabajador a partir de una linea del archivo.
     * @param line linea de texto que se va a interpretar.
     * @return entidad reconstruida a partir de la linea.
     */
    @Override
    public Employee fromLine(String line) {
        List<String> f = DelimitedText.requireFieldCount(line, 10);
        Employee employee = new Employee(Integer.parseInt(f.get(0)), f.get(1), f.get(2), f.get(3),
                f.get(4), f.get(5), Employee.Role.valueOf(f.get(7)), f.get(8), f.get(9));
        if (!Boolean.parseBoolean(f.get(6))) employee.deactivate();
        return employee;
    }
}
