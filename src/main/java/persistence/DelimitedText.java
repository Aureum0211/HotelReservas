package persistence;

import java.util.ArrayList;
import java.util.List;

/**
 * Proporciona operaciones para unir y separar campos de texto, escapando caracteres
 * especiales.
 */
public final class DelimitedText {

    private DelimitedText() { }

    /**
     * Une los campos en una sola linea y escapa los caracteres que puedan confundirse con
     * separadores.
     * @param fields campos que se uniran en una linea de texto.
     * @return linea de texto resultante con los campos escapados.
     */
    public static String join(String... fields) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) line.append(';');
            String value = fields[i] == null ? "" : fields[i];
            for (int j = 0; j < value.length(); j++) {
                char character = value.charAt(j);
                switch (character) {
                    case '\\' -> line.append("\\\\");
                    case ';' -> line.append("\\;");
                    case '\n' -> line.append("\\n");
                    case '\r' -> line.append("\\r");
                    default -> line.append(character);
                }
            }
        }
        return line.toString();
    }

    /**
     * Separa una linea en sus campos y restaura los caracteres escapados.
     * @param line linea de texto que se va a interpretar.
     * @return lista de campos recuperados de la linea.
     */
    public static List<String> split(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean escaped = false;

        for (int i = 0; i < line.length(); i++) {
            char character = line.charAt(i);
            if (escaped) {
                switch (character) {
                    case 'n' -> field.append('\n');
                    case 'r' -> field.append('\r');
                    default -> field.append(character);
                }
                escaped = false;
            } else if (character == '\\') {
                escaped = true;
            } else if (character == ';') {
                fields.add(field.toString());
                field.setLength(0);
            } else {
                field.append(character);
            }
        }
        if (escaped) field.append('\\');
        fields.add(field.toString());
        return fields;
    }

    /**
     * Comprueba que la linea contenga la cantidad esperada de campos y devuelve los campos
     * separados.
     * @param line linea de texto que se va a interpretar.
     * @param expected cantidad exacta de campos que debe contener la linea.
     * @return lista de campos separados, si la cantidad es correcta.
     */
    public static List<String> requireFieldCount(String line, int expected) {
        List<String> fields = split(line);
        if (fields.size() != expected) {
            throw new IllegalArgumentException("Se esperaban " + expected + " campos, pero se encontraron " + fields.size() + ".");
        }
        return fields;
    }
}
