package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Bardo (dado de golpe d8).
 * - Palabra Curativa: si un aliado tiene menos de la mitad de vida, lo cura 1d4 + 3 (3 usos).
 * - Inspiracion Bardica: da un dado extra al siguiente ataque de un aliado
 *   (d6, d8 en niv 5, d10 en niv 10, d12 en niv 15). 3 usos (modificador de Carisma).
 */
public class Bardo extends Aventurero {

    private String instrumentoMusical;
    private int inspiraciones = 3;
    private int curaciones = 3;

    public Bardo(String nombre, String genero, int nivel, int puntosVida, String instrumentoMusical) {
        super(nombre, genero, nivel, puntosVida);
        this.instrumentoMusical = instrumentoMusical;
    }

    public int dadoInspiracion() {
        if (getNivel() >= 15) return 12;
        if (getNivel() >= 10) return 10;
        if (getNivel() >= 5) return 8;
        return 6;
    }

    @Override
    public String simularCombate(Monstruo objetivo, List<Aventurero> grupo) {
        Aventurero herido = Aventurero.aliadoMasHerido(grupo);
        if (herido != null && curaciones > 0) {
            curaciones--;
            int cura = herido.curar(Dado.tirar(4) + MOD_CARACTERISTICA);
            return "El Bardo " + getNombre() + " canta una Palabra Curativa con su " + instrumentoMusical
                    + ": " + herido.getNombre() + " recupera " + cura + " PV.";
        }
        StringBuilder sb = new StringBuilder();
        List<Aventurero> otros = new ArrayList<>();
        for (Aventurero a : Aventurero.vivos(grupo)) {
            if (a != this && a.getBonoInspiracion() == 0 && !a.getNombre().equals(getNombre())) {
                otros.add(a);
            }
        }
        if (inspiraciones > 0 && !otros.isEmpty()) {
            inspiraciones--;
            Aventurero aliado = otros.get(Dado.tirar(otros.size()) - 1);
            aliado.recibirInspiracion(dadoInspiracion());
            sb.append("El Bardo ").append(getNombre())
              .append(" toca su instrumento para otorgar Inspiración Bárdica a un aliado.")
              .append("\n  ").append(aliado.getNombre()).append(" recibe un d").append(dadoInspiracion())
              .append(" de Inspiración para su próximo ataque.\n");
        } else {
            sb.append("El Bardo ").append(getNombre()).append(" ataca al ritmo de su ").append(instrumentoMusical).append(".\n");
        }
        sb.append(atacar(objetivo, "estoque", 8));
        return sb.toString();
    }

    @Override
    protected void reiniciarRecursos() {
        inspiraciones = 3;
        curaciones = 3;
    }

    public String getInstrumentoMusical() { return instrumentoMusical; }
    public int getInspiraciones() { return inspiraciones; }

    @Override public String getClase() { return "Bardo"; }
    @Override public int getDadoGolpe() { return 8; }
    @Override public int getClaseArmadura() { return 13; }
    @Override public String getAtributoEspecial() { return "Instrumento: " + instrumentoMusical; }
    @Override public String getValorAtributo() { return instrumentoMusical; }
}
