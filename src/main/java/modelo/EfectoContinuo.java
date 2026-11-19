package modelo;

/**
 * Hechizo de concentracion que hace dano durante varios turnos
 * (por ejemplo, la Esfera Llameante del Mago).
 */
public class EfectoContinuo {

    private final String nombre;
    private final int cantidadDados;
    private final int carasDado;
    private final String tipoDanio;
    private int turnosRestantes;

    public EfectoContinuo(String nombre, int cantidadDados, int carasDado, int turnos, String tipoDanio) {
        this.nombre = nombre;
        this.cantidadDados = cantidadDados;
        this.carasDado = carasDado;
        this.turnosRestantes = turnos;
        this.tipoDanio = tipoDanio;
    }

    /** Aplica el dano del turno al monstruo y descuenta un turno. */
    public String aplicar(Monstruo objetivo) {
        int danio = Dado.tirar(cantidadDados, carasDado);
        int real = objetivo.recibirDanio(danio, tipoDanio);
        turnosRestantes--;
        return "  La " + nombre + " golpea a " + objetivo.getNombre() + ": " + real
                + " de daño (le quedan " + turnosRestantes + " turnos).";
    }

    public boolean estaActivo() {
        return turnosRestantes > 0;
    }

    public String getNombre() {
        return nombre;
    }

    public int getTurnosRestantes() {
        return turnosRestantes;
    }
}
