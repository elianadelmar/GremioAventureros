package modelo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistencia en archivo de texto plano: registro_gremio.txt
 * Formato:
 *   AVENTURERO|fecha|Guerrero;Thorin;Hombre;5;40;true
 *   COMBATE|fecha|Goblin|VICTORIA
 *       ...lineas del combate...
 */
public class ArchivoGremio {

    public static final String RUTA_POR_DEFECTO = "registro_gremio.txt";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String ruta;

    public ArchivoGremio() {
        this(RUTA_POR_DEFECTO);
    }

    public ArchivoGremio(String ruta) {
        this.ruta = ruta;
    }

    private String ahora() {
        return LocalDateTime.now().format(FORMATO);
    }

    /** Anexa lineas al final del archivo (sin borrar lo anterior). */
    private boolean anexar(List<String> lineas) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ruta, true))) {
            for (String l : lineas) {
                bw.write(l);
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("No se pudo escribir en " + ruta + ": " + e.getMessage());
            return false;
        }
    }

    public boolean guardarAventurero(Aventurero a) {
        List<String> l = new ArrayList<>();
        l.add("AVENTURERO|" + ahora() + "|" + a.toLineaArchivo());
        return anexar(l);
    }

    public boolean guardarCombate(String monstruo, boolean victoria, List<String> bitacora) {
        List<String> l = new ArrayList<>();
        l.add("COMBATE|" + ahora() + "|" + monstruo + "|" + (victoria ? "VICTORIA" : "DERROTA"));
        for (String linea : bitacora) {
            for (String sub : linea.split("\n")) {
                l.add("    " + sub);
            }
        }
        return anexar(l);
    }

    /** Lee los aventureros guardados. Si una linea esta danada la salta. */
    public List<Aventurero> cargarAventureros() {
        List<Aventurero> lista = new ArrayList<>();
        File archivo = new File(ruta);
        if (!archivo.exists()) {
            return lista;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.startsWith("AVENTURERO|")) {
                    String[] partes = linea.split("\\|", 3);
                    try {
                        Aventurero a = FabricaAventureros.desdeLinea(partes[2]);
                        boolean repetido = false;
                        for (Aventurero x : lista) {
                            if (x.getNombre().equalsIgnoreCase(a.getNombre())) {
                                repetido = true;
                            }
                        }
                        if (!repetido) {
                            lista.add(a);
                        }
                    } catch (DatosInvalidosException | ArrayIndexOutOfBoundsException e) {
                        System.err.println("Se omitió una línea dañada: " + linea);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer " + ruta + ": " + e.getMessage());
        }
        return lista;
    }

    public String getRuta() {
        return ruta;
    }
}
