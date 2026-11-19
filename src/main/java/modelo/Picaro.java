package modelo;

import java.util.List;

/**
 * Picaro (dado de golpe d8). Ataque Furtivo: 1d6 extra por cada 2 niveles, si tiene ventaja
 * o un aliado distrayendo al monstruo (como en 5e). Con herramientas de ladron ataca con ventaja.
 */
public class Picaro extends Aventurero {

    private boolean herramientasLadron;

    public Picaro(String nombre, String genero, int nivel, int puntosVida, boolean herramientasLadron) {
        super(nombre, genero, nivel, puntosVida);
        this.herramientasLadron = herramientasLadron;
    }

    public int dadosAtaqueFurtivo() {
        return (getNivel() + 1) / 2;
    }

    @Override
    public String simularCombate(Monstruo objetivo, List<Aventurero> grupo) {
        boolean hayAliado = Aventurero.vivos(grupo).size() > 1;
        boolean furtivo = herramientasLadron || hayAliado;
        String inicio = furtivo
                ? "El Pícaro " + getNombre() + " aprovecha la ventaja táctica para asestar un Ataque Furtivo masivo y se reposiciona."
                : "El Pícaro " + getNombre() + " está solo y ataca sin poder usar su Ataque Furtivo.";
        return inicio + "\n" + atacar(objetivo, "estoque", 1, 8, herramientasLadron,
                furtivo ? dadosAtaqueFurtivo() : 0, Monstruo.DANIO_ARMA);
    }

    public boolean isHerramientasLadron() { return herramientasLadron; }

    @Override public String getClase() { return "Pícaro"; }
    @Override public int getDadoGolpe() { return 8; }
    @Override public int getClaseArmadura() { return 14; }
    @Override public String getAtributoEspecial() { return "Herramientas de ladrón: " + (herramientasLadron ? "Sí" : "No"); }
    @Override public String getValorAtributo() { return String.valueOf(herramientasLadron); }
}
