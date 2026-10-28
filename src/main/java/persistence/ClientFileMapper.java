package persistence;

import java.util.List;
import model.Client;

/**
 * Convierte clientes entre objetos Java y registros de texto delimitados por punto y coma.
 */
public class ClientFileMapper implements EntityFileMapper<Client> {

    /**
     * Convierte los datos del cliente en una linea delimitada para almacenarla en un
     * archivo.
     * @param client cliente de la reserva.
     * @return representacion textual de la entidad.
     */
    @Override
    public String toLine(Client client) {
        return DelimitedText.join(
                Integer.toString(client.getId()), client.getName(), client.getDocumentNumber(),
                client.getEmail(), client.getPhone(), client.getCity(), client.getCategory().name(),
                Boolean.toString(client.isActive()));
    }

    /**
     * Reconstruye un cliente a partir de una linea del archivo.
     * @param line linea de texto que se va a interpretar.
     * @return entidad reconstruida a partir de la linea.
     */
    @Override
    public Client fromLine(String line) {
        List<String> f = DelimitedText.requireFieldCount(line, 8);
        Client client = new Client(Integer.parseInt(f.get(0)), f.get(1), f.get(2), f.get(3),
                f.get(4), f.get(5), Client.Category.valueOf(f.get(6)));
        if (!Boolean.parseBoolean(f.get(7))) client.deactivate();
        return client;
    }
}
