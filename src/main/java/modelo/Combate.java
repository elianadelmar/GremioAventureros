package modelo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maneja un combate de 5e: iniciativa (1d20), turnos en orden y rondas
 * hasta que cae el monstruo o cae todo el grupo.
 */
public class Combate {

    public static final int MAX_RONDAS = 20;
    public static final int MAX_RONDAS_TARRASCA = 60;

    private final List<Aventurero> grupo;
    private final Monstruo monstruo;
    private final List<Aventurero> ordenHeroes = new ArrayList<>();
    private int posicionMonstruo;   // cuantos heroes actuan antes que el monstruo
    private int ronda = 0;
    private final List<String> bitacora = new ArrayList<>();

    public Combate(List<Aventurero> grupo, Monstruo monstruo) {
        if (grupo == null || grupo.isEmpty() || grupo.size() > 4) {
            throw new IllegalArgumentException("El grupo debe tener entre 1 y 4 aventureros.");
        }
        this.grupo = grupo;
        this.monstruo = monstruo;
    }

    /** Tira iniciativa (1d20) para todos y ordena los turnos de mayor a menor. */
    public List<String> iniciar() {
        List<String> lineas = new ArrayList<>();
        lineas.add("¡Comienza el combate contra " + monstruo.getNombre() + "!");
        Map<Aventurero, Integer> iniciativas = new HashMap<>();
        StringBuilder sb = new StringBuilder("Iniciativa (1d20): ");
        for (Aventurero a : grupo) {
            int tirada = Dado.tirar(20);
            iniciativas.put(a, tirada);
            sb.append(a.getNombre()).append(" ").append(tirada).append(", ");
        }
        int iniMonstruo = monstruo.tirarIniciativa();
        sb.append(monstruo.getNombre()).append(" ").append(iniMonstruo);
        lineas.add(sb.toString());

        ordenHeroes.clear();
        ordenHeroes.addAll(grupo);
        ordenHeroes.sort((a, b) -> iniciativas.get(b) - iniciativas.get(a));
        posicionMonstruo = 0;
        for (Aventurero a : ordenHeroes) {
            if (iniciativas.get(a) > iniMonstruo) {
                posicionMonstruo++;
            }
        }
        String primero = posicionMonstruo == 0 ? monstruo.getNombre() : ordenHeroes.get(0).getNombre();
        lineas.add("¡" + primero + " inicia el combate!");
        bitacora.addAll(lineas);
        return lineas;
    }

    /** Ejecuta una ronda completa y devuelve lo que paso. */
    public List<String> ejecutarRonda() {
        List<String> lineas = new ArrayList<>();
        if (terminado()) {
            return lineas;
        }
        if (ordenHeroes.isEmpty()) {
            lineas.addAll(iniciar());
        }
        ronda++;
        lineas.add("----- Ronda " + ronda + " -----");
        for (int i = 0; i <= ordenHeroes.size(); i++) {
            if (estaResuelto()) {
                break;
            }
            if (i == posicionMonstruo) {
                lineas.addAll(monstruo.realizarTurno(grupo));
                if (estaResuelto()) {
                    break;
                }
            }
            if (i < ordenHeroes.size()) {
                Aventurero heroe = ordenHeroes.get(i);
                if (!heroe.estaCaido()) {
                    // POLIMORFISMO: cada clase resuelve su propio turno
                    lineas.add(heroe.simularCombate(monstruo, grupo));
                }
            }
        }
        if (terminado()) {
            lineas.add(getResultado());
        }
        bitacora.addAll(lineas);
        return lineas;
    }

    /** Simula el combate completo de una vez. */
    public List<String> ejecutarCompleto() {
        List<String> todo = new ArrayList<>(iniciar());
        while (!terminado()) {
            todo.addAll(ejecutarRonda());
        }
        return todo;
    }

    private boolean estaResuelto() {
        return monstruo.estaDerrotado() || Aventurero.vivos(grupo).isEmpty();
    }

    public int getMaxRondas() {
        return "Tarrasca".equals(monstruo.getNombre()) ? MAX_RONDAS_TARRASCA : MAX_RONDAS;
    }

    public boolean terminado() {
        return estaResuelto() || ronda >= getMaxRondas();
    }

    public boolean huboVictoria() {
        return monstruo.estaDerrotado();
    }

    public String getResultado() {
        if (huboVictoria()) {
            return "¡VICTORIA! " + monstruo.getNombre() + " ha sido derrotado.";
        }
        if (Aventurero.vivos(grupo).isEmpty()) {
            return "DERROTA... todo el grupo ha caído ante " + monstruo.getNombre() + ".";
        }
        return "DERROTA... el grupo se retira agotado después de " + ronda + " rondas.";
    }

    public List<Aventurero> getGrupo() { return grupo; }
    public Monstruo getMonstruo() { return monstruo; }
    public int getRonda() { return ronda; }
    public List<String> getBitacora() { return bitacora; }
}
