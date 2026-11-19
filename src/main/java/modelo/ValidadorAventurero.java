package modelo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validaciones "picky" de los datos que ingresa el usuario.
 * Todas lanzan DatosInvalidosException con un mensaje claro para mostrar en pantalla.
 */
public class ValidadorAventurero {

    public static final int MAX_LARGO_NOMBRE = 20;
    private static final String SOLO_LETRAS = "[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]+";

    // Espacios de conjuro totales de un mago por nivel (tabla del Mago en 5e)
    private static final int[] ESPACIOS_MAGO = {2, 3, 6, 7, 9, 10, 11, 12, 14, 15, 16, 16, 17, 17, 18, 18, 19, 20, 21, 22};

    public static String validarNombre(String nombre, List<Aventurero> gremio) throws DatosInvalidosException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new DatosInvalidosException("El nombre no puede estar vacío.");
        }
        String limpio = nombre.trim().replaceAll("\\s+", " ");
        if (!limpio.matches(SOLO_LETRAS)) {
            throw new DatosInvalidosException("El nombre solo puede tener letras y espacios.");
        }
        if (limpio.length() < 2 || limpio.length() > MAX_LARGO_NOMBRE) {
            throw new DatosInvalidosException("El nombre debe tener entre 2 y " + MAX_LARGO_NOMBRE + " letras.");
        }
        for (Aventurero a : gremio) {
            if (a.getNombre().equalsIgnoreCase(limpio)) {
                throw new DatosInvalidosException("Ya existe un aventurero llamado " + limpio + " en el gremio.");
            }
        }
        return limpio;
    }

    public static String validarGenero(String genero) throws DatosInvalidosException {
        if (!"Hombre".equals(genero) && !"Mujer".equals(genero)) {
            throw new DatosInvalidosException("Debe elegir Hombre o Mujer.");
        }
        return genero;
    }

    /** Convierte texto a entero validando el rango. Atrapa NumberFormatException. */
    public static int validarEntero(String texto, int min, int max, String campo) throws DatosInvalidosException {
        if (texto == null || texto.trim().isEmpty()) {
            throw new DatosInvalidosException("El campo " + campo + " no puede estar vacío.");
        }
        int valor;
        try {
            valor = Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new DatosInvalidosException("El campo " + campo + " debe ser un número entero (sin letras ni decimales).");
        }
        if (valor < min || valor > max) {
            throw new DatosInvalidosException("El campo " + campo + " debe estar entre " + min + " y " + max + ".");
        }
        return valor;
    }

    public static String validarTexto(String texto, String campo) throws DatosInvalidosException {
        if (texto == null || texto.trim().isEmpty()) {
            throw new DatosInvalidosException("El campo " + campo + " no puede estar vacío.");
        }
        String limpio = texto.trim().replaceAll("\\s+", " ");
        if (!limpio.matches(SOLO_LETRAS) || limpio.length() < 2 || limpio.length() > 25) {
            throw new DatosInvalidosException("El campo " + campo + " debe tener solo letras (2 a 25).");
        }
        return limpio;
    }

    public static boolean validarSiNo(String texto, String campo) throws DatosInvalidosException {
        if ("Sí".equalsIgnoreCase(texto) || "Si".equalsIgnoreCase(texto) || "true".equalsIgnoreCase(texto)) {
            return true;
        }
        if ("No".equalsIgnoreCase(texto) || "false".equalsIgnoreCase(texto)) {
            return false;
        }
        throw new DatosInvalidosException("El campo " + campo + " debe ser Sí o No.");
    }

    /** Reglas de multiclase: 1 a 4 clases, sin repetir, suma <= 20 y con 4 clases cada una <= 5. */
    public static void validarClases(List<String> clases, List<Integer> niveles) throws DatosInvalidosException {
        if (clases.isEmpty() || clases.size() > Multiclase.MAX_CLASES) {
            throw new DatosInvalidosException("Debe elegir entre 1 y " + Multiclase.MAX_CLASES + " clases.");
        }
        Set<String> sinRepetir = new HashSet<>(clases);
        if (sinRepetir.size() != clases.size()) {
            throw new DatosInvalidosException("No se puede repetir una clase en el mismo aventurero.");
        }
        int suma = 0;
        for (int n : niveles) {
            suma += n;
            if (clases.size() == Multiclase.MAX_CLASES && n > Multiclase.NIVEL_MAX_CON_4_CLASES) {
                throw new DatosInvalidosException("Con 4 clases ninguna puede pasar de nivel "
                        + Multiclase.NIVEL_MAX_CON_4_CLASES + ".");
            }
        }
        if (suma > Aventurero.NIVEL_MAX) {
            throw new DatosInvalidosException("La suma de los niveles es " + suma + " y no puede pasar de "
                    + Aventurero.NIVEL_MAX + ".");
        }
    }

    /** Vida maxima permitida: suma de (dado de golpe x nivel) de cada clase. */
    public static int calcularPuntosVidaMax(List<String> clases, List<Integer> niveles) {
        int total = 0;
        for (int i = 0; i < clases.size(); i++) {
            total += FabricaAventureros.dadoGolpe(clases.get(i)) * niveles.get(i);
        }
        return total;
    }

    public static int maxAtributo(String clase, int nivel) {
        switch (clase) {
            case "Mago": return ESPACIOS_MAGO[nivel - 1];
            case "Monje":
            case "Paladín": return nivel;
            default: return 0;
        }
    }
}
