package modelo;

import java.util.List;

/**
 * Explorador (dado de golpe d10). Marca del Cazador: el primer turno marca al monstruo
 * y desde ahi cada golpe con el arco hace 1d6 extra.
 */
public class Explorador extends Aventurero {

    private String terrenoFavorecido;
    private boolean marcaActiva = false;

    public Explorador(String nombre, String genero, int nivel, int puntosVida, String terrenoFavorecido) {
        super(nombre, genero, nivel, puntosVida);
        this.terrenoFavorecido = terrenoFavorecido;
    }

    @Override
    public String simularCombate(Monstruo objetivo, List<Aventurero> grupo) {
        StringBuilder sb = new StringBuilder();
        if (!marcaActiva) {
            marcaActiva = true;
            sb.append("El Explorador ").append(getNombre())
              .append(" aplica Marca del Cazador sobre el objetivo para maximizar su daño sostenido.");
        } else {
            sb.append("El Explorador ").append(getNombre()).append(" dispara su arco largo a la presa marcada.");
        }
        for (int i = 0; i < ataquesPorAccion() && !objetivo.estaDerrotado(); i++) {
            sb.append("\n").append(atacar(objetivo, "arco largo", 1, 8, false, 1, Monstruo.DANIO_ARMA));
        }
        return sb.toString();
    }

    @Override
    protected void reiniciarRecursos() {
        marcaActiva = false;
    }

    public String getTerrenoFavorecido() { return terrenoFavorecido; }

    @Override public String getClase() { return "Explorador"; }
    @Override public int getDadoGolpe() { return 10; }
    @Override public int getClaseArmadura() { return 14; }
    @Override public String getAtributoEspecial() { return "Terreno favorecido: " + terrenoFavorecido; }
    @Override public String getValorAtributo() { return terrenoFavorecido; }
}
