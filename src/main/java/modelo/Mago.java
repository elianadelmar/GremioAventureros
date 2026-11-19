package modelo;

import java.util.List;

/**
 * Mago (dado de golpe d6). Gasta espacios de conjuro:
 * - Esfera Llameante: concentracion, 2d6 durante 3 turnos.
 * - Bola de Fuego: 8d6, el monstruo hace salvacion de Destreza (mitad si la pasa).
 * - Contra inmunes al fuego usa Relampago (8d6 electrico).
 * - Sin espacios: truco Rayo de Escarcha (1d8, sube a 2d8/3d8/4d8 en niv 5/11/17).
 */
public class Mago extends Aventurero {

    private int espaciosConjuro;
    private final int espaciosMax;

    public Mago(String nombre, String genero, int nivel, int puntosVida, int espaciosConjuro) {
        super(nombre, genero, nivel, puntosVida);
        this.espaciosConjuro = espaciosConjuro;
        this.espaciosMax = espaciosConjuro;
    }

    private int dadosTruco() {
        int n = getNivelPersonaje();
        return 1 + (n >= 5 ? 1 : 0) + (n >= 11 ? 1 : 0) + (n >= 17 ? 1 : 0);
    }

    @Override
    public String simularCombate(Monstruo objetivo, List<Aventurero> grupo) {
        String nombre = getNombre();
        if (espaciosConjuro <= 0) {
            return "El Mago " + nombre + " no tiene espacios de conjuro y lanza el truco Rayo de Escarcha.\n"
                    + atacar(objetivo, "Rayo de Escarcha", dadosTruco(), 8, false, 0, Monstruo.DANIO_MAGIA);
        }
        if (!objetivo.isInmuneFuego() && !objetivo.tieneEfectoActivo() && espaciosConjuro >= 2) {
            espaciosConjuro--;
            objetivo.setEfecto(new EfectoContinuo("Esfera Llameante", 2, 6, 3, Monstruo.DANIO_FUEGO));
            return "El Mago " + nombre + " se concentra y conjura una Esfera Llameante que arderá durante 3 turnos.";
        }
        espaciosConjuro--;
        StringBuilder sb = new StringBuilder();
        String tipo;
        if (objetivo.isInmuneFuego()) {
            sb.append("El Mago ").append(nombre).append(" sabe que ").append(objetivo.getNombre())
              .append(" es inmune al fuego: gasta un espacio de conjuro para lanzar un poderoso Relámpago.");
            tipo = Monstruo.DANIO_MAGIA;
        } else {
            sb.append("El Mago ").append(nombre)
              .append(" gasta un espacio de conjuro para lanzar una poderosa Bola de Fuego.");
            tipo = Monstruo.DANIO_FUEGO;
        }
        int danio = Dado.tirar(8, 6);
        boolean salva = objetivo.tirarSalvacionDes(getCdConjuros());
        if (salva) {
            danio /= 2;
        }
        int real = objetivo.recibirDanio(danio, tipo);
        sb.append("\n  ").append(objetivo.getNombre()).append(salva ? " pasa" : " falla")
          .append(" la salvación (CD ").append(getCdConjuros()).append(") y recibe ")
          .append(real).append(" de daño. Espacios restantes: ").append(espaciosConjuro);
        return sb.toString();
    }

    @Override
    protected void reiniciarRecursos() {
        espaciosConjuro = espaciosMax;
    }

    public int getEspaciosConjuro() { return espaciosConjuro; }

    @Override public String getClase() { return "Mago"; }
    @Override public int getDadoGolpe() { return 6; }
    @Override public int getClaseArmadura() { return 12; }
    @Override public String getAtributoEspecial() { return "Espacios de conjuro: " + espaciosConjuro; }
    @Override public String getValorAtributo() { return String.valueOf(espaciosMax); }
}
