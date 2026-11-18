package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Clerigo (dado de golpe d8). Canalizar Divinidad (Preservar la Vida en 5e):
 * reparte 5 x nivel puntos de curacion entre los aliados heridos, sin pasar de la mitad de su vida.
 */
public class Clerigo extends Aventurero {

    private String diosVenerado;
    private boolean canalizarDisponible = true;

    public Clerigo(String nombre, String genero, int nivel, int puntosVida, String diosVenerado) {
        super(nombre, genero, nivel, puntosVida);
        this.diosVenerado = diosVenerado;
    }

    @Override
    public String simularCombate(Monstruo objetivo, List<Aventurero> grupo) {
        if (canalizarDisponible && Aventurero.aliadoMasHerido(grupo) != null) {
            canalizarDisponible = false;
            StringBuilder sb = new StringBuilder("El Clérigo " + getNombre()
                    + " usa Canalizar Divinidad para curar a sus aliados y expulsar muertos vivientes.");
            int reserva = 5 * getNivel();
            List<Aventurero> heridos = new ArrayList<>(Aventurero.vivos(grupo));
            heridos.sort((a, b) -> Double.compare((double) a.getPuntosVida() / a.getPuntosVidaMax(),
                                                  (double) b.getPuntosVida() / b.getPuntosVidaMax()));
            for (Aventurero a : heridos) {
                int tope = a.getPuntosVidaMax() / 2 - a.getPuntosVida();
                int cura = Math.min(reserva, tope);
                if (cura > 0) {
                    a.curar(cura);
                    reserva -= cura;
                    sb.append("\n  ").append(a.getNombre()).append(" recupera ").append(cura)
                      .append(" PV gracias a ").append(diosVenerado).append(".");
                }
            }
            return sb.toString();
        }
        return "El Clérigo " + getNombre() + " golpea con su maza bendecida por " + diosVenerado + ".\n"
                + atacar(objetivo, "maza", 6);
    }

    @Override
    protected void reiniciarRecursos() {
        canalizarDisponible = true;
    }

    public String getDiosVenerado() { return diosVenerado; }

    @Override public String getClase() { return "Clérigo"; }
    @Override public int getDadoGolpe() { return 8; }
    @Override public int getClaseArmadura() { return 16; }
    @Override public String getAtributoEspecial() { return "Dios venerado: " + diosVenerado; }
    @Override public String getValorAtributo() { return diosVenerado; }
}
