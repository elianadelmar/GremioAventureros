package modelo;

/**
 * Un ataque de un monstruo, con los datos del SRD de D&D 5e (dnd5eapi.co).
 * Ej: Cimitarra del Goblin = +4 para impactar, 1d6+2 de dano.
 */
public class AtaqueMonstruo {

    private final String nombre;
    private final int bonoAtaque;
    private final int cantidadDados;
    private final int carasDado;
    private final int bonoDanio;
    private final int dadosFuegoExtra;   // d6 de fuego extra (mordisco del dragon)

    public AtaqueMonstruo(String nombre, int bonoAtaque, int cantidadDados, int carasDado, int bonoDanio) {
        this(nombre, bonoAtaque, cantidadDados, carasDado, bonoDanio, 0);
    }

    public AtaqueMonstruo(String nombre, int bonoAtaque, int cantidadDados, int carasDado,
                          int bonoDanio, int dadosFuegoExtra) {
        this.nombre = nombre;
        this.bonoAtaque = bonoAtaque;
        this.cantidadDados = cantidadDados;
        this.carasDado = carasDado;
        this.bonoDanio = bonoDanio;
        this.dadosFuegoExtra = dadosFuegoExtra;
    }

    /** Tira el ataque contra un aventurero y devuelve el texto del resultado. */
    public String ejecutar(String atacante, Aventurero objetivo) {
        int d20 = Dado.tirar(20);
        int total = d20 + bonoAtaque;
        String texto = "  " + atacante + " usa " + nombre + " contra " + objetivo.getNombre()
                + ": d20(" + d20 + ") + " + bonoAtaque + " = " + total + " vs CA " + objetivo.getClaseArmadura();
        boolean critico = d20 == 20;
        if (d20 == 1 || (!critico && total < objetivo.getClaseArmadura())) {
            return texto + " -> Falla.";
        }
        int mult = critico ? 2 : 1;
        int danio = Dado.tirar(cantidadDados * mult, carasDado) + bonoDanio;
        if (dadosFuegoExtra > 0) {
            danio += Dado.tirar(dadosFuegoExtra * mult, 6);
        }
        objetivo.recibirDanio(danio);
        texto += (critico ? " -> ¡CRÍTICO! " : " -> ¡Impacta! ") + danio + " de daño.";
        if (objetivo.estaCaido()) {
            texto += "\n  " + objetivo.getNombre() + " ha caído.";
        }
        return texto;
    }

    public String getNombre() { return nombre; }
    public int getBonoAtaque() { return bonoAtaque; }
}
