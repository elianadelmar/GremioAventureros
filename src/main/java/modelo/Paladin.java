package modelo;

import java.util.List;

/**
 * Paladin (dado de golpe d10). Castigo Divino: cuando acierta, gasta un punto de fuerza
 * para sumar 2d8 de dano radiante (dano magico).
 */
public class Paladin extends Aventurero {

    private int puntosFuerza;
    private final int fuerzaMax;

    public Paladin(String nombre, String genero, int nivel, int puntosVida, int puntosFuerza) {
        super(nombre, genero, nivel, puntosVida);
        this.puntosFuerza = puntosFuerza;
        this.fuerzaMax = puntosFuerza;
    }

    @Override
    public String simularCombate(Monstruo objetivo, List<Aventurero> grupo) {
        StringBuilder sb = new StringBuilder("El Paladín " + getNombre() + " carga con su espada larga.");
        boolean castigoUsado = false;
        for (int i = 0; i < ataquesPorAccion() && !objetivo.estaDerrotado(); i++) {
            sb.append("\n").append(atacar(objetivo, "espada larga", 8));
            if (ultimoAtaqueAcerto && !castigoUsado && puntosFuerza > 0 && !objetivo.estaDerrotado()) {
                castigoUsado = true;
                puntosFuerza--;
                int real = objetivo.recibirDanio(Dado.tirar(2, 8), Monstruo.DANIO_MAGIA);
                sb.append("\nEl Paladín ").append(getNombre())
                  .append(" consume magia para imbuir su arma con un Castigo Divino de daño radiante.")
                  .append("\n  +").append(real).append(" de daño radiante.");
            }
        }
        return sb.toString();
    }

    @Override
    protected void reiniciarRecursos() {
        puntosFuerza = fuerzaMax;
    }

    public int getPuntosFuerza() { return puntosFuerza; }

    @Override public String getClase() { return "Paladín"; }
    @Override public int getDadoGolpe() { return 10; }
    @Override public int getClaseArmadura() { return 18; }
    @Override public String getAtributoEspecial() { return "Puntos de fuerza: " + puntosFuerza; }
    @Override public String getValorAtributo() { return String.valueOf(fuerzaMax); }
}
