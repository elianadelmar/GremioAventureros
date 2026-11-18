package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Crea aventureros a partir de los datos (de la pantalla o del archivo .txt).
 */
public class FabricaAventureros {

    public static final String[] CLASES = {
        "Guerrero", "Mago", "Clérigo", "Monje", "Paladín", "Explorador", "Pícaro", "Bardo"
    };

    public static int dadoGolpe(String clase) {
        switch (clase) {
            case "Guerrero":
            case "Paladín":
            case "Explorador": return 10;
            case "Mago": return 6;
            default: return 8;
        }
    }

    /** Texto para la etiqueta del atributo especial en la pantalla de registro. */
    public static String nombreAtributo(String clase) {
        switch (clase) {
            case "Guerrero": return "¿Usa armadura pesada?";
            case "Mago": return "Espacios de conjuro";
            case "Clérigo": return "Dios venerado";
            case "Monje": return "Puntos de Ki";
            case "Paladín": return "Puntos de fuerza";
            case "Explorador": return "Terreno favorecido";
            case "Pícaro": return "¿Tiene herramientas de ladrón?";
            case "Bardo": return "Instrumento musical";
            default: return "Atributo";
        }
    }

    /** true si el atributo se responde con Sí/No (usar JComboBox en la vista). */
    public static boolean atributoEsSiNo(String clase) {
        return "Guerrero".equals(clase) || "Pícaro".equals(clase);
    }

    /** Crea una clase validando su atributo especial. */
    public static Aventurero crear(String clase, String nombre, String genero, int nivel, int pv,
                                   String atributo) throws DatosInvalidosException {
        String campo = nombreAtributo(clase);
        switch (clase) {
            case "Guerrero":
                return new Guerrero(nombre, genero, nivel, pv, ValidadorAventurero.validarSiNo(atributo, campo));
            case "Mago":
                return new Mago(nombre, genero, nivel, pv, ValidadorAventurero.validarEntero(atributo, 0,
                        ValidadorAventurero.maxAtributo(clase, nivel), campo));
            case "Clérigo":
                return new Clerigo(nombre, genero, nivel, pv, ValidadorAventurero.validarTexto(atributo, campo));
            case "Monje":
                return new Monje(nombre, genero, nivel, pv, ValidadorAventurero.validarEntero(atributo, 0,
                        ValidadorAventurero.maxAtributo(clase, nivel), campo));
            case "Paladín":
                return new Paladin(nombre, genero, nivel, pv, ValidadorAventurero.validarEntero(atributo, 0,
                        ValidadorAventurero.maxAtributo(clase, nivel), campo));
            case "Explorador":
                return new Explorador(nombre, genero, nivel, pv, ValidadorAventurero.validarTexto(atributo, campo));
            case "Pícaro":
                return new Picaro(nombre, genero, nivel, pv, ValidadorAventurero.validarSiNo(atributo, campo));
            case "Bardo":
                return new Bardo(nombre, genero, nivel, pv, ValidadorAventurero.validarTexto(atributo, campo));
            default:
                throw new DatosInvalidosException("Clase desconocida: " + clase);
        }
    }

    /** Reconstruye un aventurero desde una linea del registro_gremio.txt */
    public static Aventurero desdeLinea(String linea) throws DatosInvalidosException {
        String[] p = linea.split(";");
        try {
            if ("Multiclase".equals(p[0])) {
                Multiclase m = new Multiclase(p[1], p[2], Integer.parseInt(p[3]));
                for (String parte : p[4].split(",")) {
                    String[] c = parte.split("-");
                    m.agregarClase(crear(c[0], p[1], p[2], Integer.parseInt(c[1]), 1, c[2]));
                }
                return m;
            }
            return crear(p[0], p[1], p[2], Integer.parseInt(p[3]), Integer.parseInt(p[4]), p[5]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            throw new DatosInvalidosException("Línea dañada en el archivo: " + linea);
        }
    }

    /** Lista con los nombres de las clases (para llenar un JComboBox). */
    public static List<String> listaClases() {
        List<String> lista = new ArrayList<>();
        for (String c : CLASES) {
            lista.add(c);
        }
        return lista;
    }
}
