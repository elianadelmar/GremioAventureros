package modelo;

import java.util.List;

/**
 * Monje (dado de golpe d8). Artes Marciales: el dado de sus golpes sube con el nivel
 * (d4, d6 en niv 5, d8 en niv 11, d10 en niv 17). Rafaga de Golpes: gasta 1 punto de Ki
 * para dar 2 golpes extra.
 */
public class Monje extends Aventurero {

    private int puntosKi;
    private final int kiMax;

    public Monje(String nombre, String genero, int nivel, int puntosVida, int puntosKi) {
        super(nombre, genero, nivel, puntosVida);
        this.puntosKi = puntosKi;
        this.kiMax = puntosKi;
    }

    private int dadoArtesMarciales() {
        if (getNivel() >= 17) return 10;
        if (getNivel() >= 11) return 8;
        if (getNivel() >= 5) return 6;
        return 4;
    }

    @Override
    public String simularCombate(Monstruo objetivo, List<Aventurero> grupo) {
        StringBuilder sb = new StringBuilder();
        int golpesExtra;
        if (puntosKi > 0) {
            puntosKi--;
            golpesExtra = 2;
            sb.append("El Monje ").append(getNombre())
              .append(" gasta puntos de Ki para ejecutar una Ráfaga de Golpes y un Ataque Aturdidor.")
              .append(" (Ki restante: ").append(puntosKi).append(")");
        } else {
            golpesExtra = 1;
            sb.append("El Monje ").append(getNombre()).append(" pelea con sus Artes Marciales.");
        }
        int total = ataquesPorAccion() + golpesExtra;
        for (int i = 0; i < total && !objetivo.estaDerrotado(); i++) {
            sb.append("\n").append(atacar(objetivo, "golpe sin armas", dadoArtesMarciales()));
        }
        return sb.toString();
    }

    @Override
    protected void reiniciarRecursos() {
        puntosKi = kiMax;
    }

    public int getPuntosKi() { return puntosKi; }

    @Override public String getClase() { return "Monje"; }
    @Override public int getDadoGolpe() { return 8; }
    @Override public int getClaseArmadura() { return 15; }
    @Override public String getAtributoEspecial() { return "Puntos de Ki: " + puntosKi; }
    @Override public String getValorAtributo() { return String.valueOf(kiMax); }
}
