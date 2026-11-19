package com.mycompany.gremioaventureros;

import controlador.ControladorGremio;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.Aventurero;
import modelo.Multiclase;

/**
 * Clase principal. Hace los 2 reportes sobre la coleccion dinamica de aventureros.
 */
public class GremioAventureros {

    public static void main(String[] args) {
        ControladorGremio controlador = new ControladorGremio();
        controlador.cargarGremio();

        System.out.println(reporteCantidadPorClase(controlador.getGremio()));
        System.out.println(reporteMayorNivel(controlador.getGremio()));

        // Abre la ventana de inicio
        java.awt.EventQueue.invokeLater(() -> new ControladorGremio().iniciar());
    }

    /** REPORTE 1: cuantos aventureros hay de cada clase (los multiclase cuentan en cada una lol). */
    public static String reporteCantidadPorClase(List<Aventurero> lista) {
        Map<String, Integer> conteo = new LinkedHashMap<>();
        for (Aventurero a : lista) {
            if (a instanceof Multiclase) {
                for (Aventurero c : ((Multiclase) a).getClases()) {
                    conteo.put(c.getClase(), conteo.getOrDefault(c.getClase(), 0) + 1);
                }
            } else {
                conteo.put(a.getClase(), conteo.getOrDefault(a.getClase(), 0) + 1);
            }
        }
        StringBuilder sb = new StringBuilder("=== Reporte 1: Aventureros por clase ===\n");
        if (conteo.isEmpty()) {
            sb.append("El gremio está vacío.\n");
        }
        for (Map.Entry<String, Integer> e : conteo.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }
        return sb.toString();
    }

    /** REPORTE 2: el/los aventurero(s) de mayor nivel :v. */
    public static String reporteMayorNivel(List<Aventurero> lista) {
        StringBuilder sb = new StringBuilder("=== Reporte 2: Aventurero(s) de mayor nivel ===\n");
        if (lista.isEmpty()) {
            return sb.append("El gremio está vacío.\n").toString();
        }
        int mayor = 0;
        for (Aventurero a : lista) {
            mayor = Math.max(mayor, a.getNivel());
        }
        List<String> nombres = new ArrayList<>();
        for (Aventurero a : lista) {
            if (a.getNivel() == mayor) {
                nombres.add(a.getNombre() + " (" + a.getClase() + ")");
            }
        }
        sb.append("Nivel ").append(mayor).append(": ").append(String.join(", ", nombres)).append("\n");
        return sb.toString();
    }
}
