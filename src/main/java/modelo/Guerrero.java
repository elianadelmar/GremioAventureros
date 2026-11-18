package modelo;

import java.util.List;

/**
 * Guerrero (dado de golpe d10). Oleada de Accion: una vez por combate repite sus ataques.
 * Ataque Extra de 5e: 2 ataques (niv 5), 3 (niv 11), 4 (niv 20).
 */
public class Guerrero extends Aventurero {

    private boolean armaduraPesada;
    private boolean oleadaDisponible = true;

    public Guerrero(String nombre, String genero, int nivel, int puntosVida, boolean armaduraPesada) {
        super(nombre, genero, nivel, puntosVida);
        this.armaduraPesada = armaduraPesada;
    }

    @Override
    protected int ataquesPorAccion() {
        if (getNivel() >= 20) return 4;
        if (getNivel() >= 11) return 3;
        if (getNivel() >= 5) return 2;
        return 1;
    }

    @Override
    public String simularCombate(Monstruo objetivo, List<Aventurero> grupo) {
        StringBuilder sb = new StringBuilder();
        int ataques = ataquesPorAccion();
        if (oleadaDisponible) {
            oleadaDisponible = false;
            ataques *= 2;
            sb.append("El Guerrero ").append(getNombre())
              .append(" usa Oleada de Acción para realizar múltiples ataques físicos continuos.");
        } else {
            sb.append("El Guerrero ").append(getNombre()).append(" ataca con su espada larga.");
        }
        for (int i = 0; i < ataques && !objetivo.estaDerrotado(); i++) {
            sb.append("\n").append(atacar(objetivo, "espada larga", 8));
        }
        return sb.toString();
    }

    @Override
    protected void reiniciarRecursos() {
        oleadaDisponible = true;
    }

    public boolean isArmaduraPesada() { return armaduraPesada; }
    public boolean isOleadaDisponible() { return oleadaDisponible; }

    @Override public String getClase() { return "Guerrero"; }
    @Override public int getDadoGolpe() { return 10; }
    @Override public int getClaseArmadura() { return armaduraPesada ? 16 : 13; }
    @Override public String getAtributoEspecial() { return "Armadura pesada: " + (armaduraPesada ? "Sí" : "No"); }
    @Override public String getValorAtributo() { return String.valueOf(armaduraPesada); }
}
