package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Monstruo enemigo. Datos del SRD de D&D 5e (dnd5eapi.co). y ps
 * Campana: 1 Goblin -> 2 Ogro -> 3 Dragon Rojo Joven -> 4 Tarrasca (secreta: solo con 4 Leyendas).
 */
public class Monstruo {

    public static final String DANIO_ARMA = "arma";
    public static final String DANIO_FUEGO = "fuego";
    public static final String DANIO_MAGIA = "magia";
    public static final int CD_ALIENTO_DRAGON = 17;

    private final String nombre;
    private int puntosVida;
    private final int puntosVidaMax;
    private final int claseArmadura;
    private final int bonoSalvacionDes;
    private final boolean inmuneFuego;
    private final boolean resisteArmas;      // recibe la mitad del dano de armas
    private final List<AtaqueMonstruo> ataques = new ArrayList<>();
    private final int ataquesPorTurno;
    private final boolean ataquesAlAzar;
    private boolean tieneAliento = false;
    private boolean alientoDisponible = false;
    private EfectoContinuo efecto;

    public Monstruo(String nombre, int puntosVida, int claseArmadura, int bonoSalvacionDes,
                    boolean inmuneFuego, boolean resisteArmas, int ataquesPorTurno, boolean ataquesAlAzar) {
        this.nombre = nombre;
        this.puntosVida = puntosVida;
        this.puntosVidaMax = puntosVida;
        this.claseArmadura = claseArmadura;
        this.bonoSalvacionDes = bonoSalvacionDes;
        this.inmuneFuego = inmuneFuego;
        this.resisteArmas = resisteArmas;
        this.ataquesPorTurno = ataquesPorTurno;
        this.ataquesAlAzar = ataquesAlAzar;
    }

    public void agregarAtaque(AtaqueMonstruo ataque) {
        ataques.add(ataque);
    }

    // ---------------- Monstruos del SRD ----------------

    /** Goblin: CA 15, 7 PV, Des +2. Cimitarra +4, 1d6+2. */
    public static Monstruo crearGoblin() {
        Monstruo m = new Monstruo("Goblin", 7, 15, 2, false, false, 1, false);
        m.agregarAtaque(new AtaqueMonstruo("Cimitarra", 4, 1, 6, 2));
        return m;
    }

    /** Ogro: CA 11, 59 PV, Des -1. Gran garrote +6, 2d8+4. */
    public static Monstruo crearOgro() {
        Monstruo m = new Monstruo("Ogro", 59, 11, -1, false, false, 1, false);
        m.agregarAtaque(new AtaqueMonstruo("Gran garrote", 6, 2, 8, 4));
        return m;
    }

    /**
     * Dragon Rojo Joven: CA 18, 178 PV, salvacion Des +4, inmune al fuego.
     * Multiataque: mordisco (+10, 2d10+6 + 1d6 fuego) y 2 garras (+10, 2d6+6).
     * Aliento de Fuego: 16d6 a todos (CD 17 Des, mitad si salva), recarga con 5-6.
     */
    public static Monstruo crearDragonRojoJoven() {
        Monstruo m = new Monstruo("Dragón Rojo Joven", 178, 18, 4, true, false, 3, false);
        m.agregarAtaque(new AtaqueMonstruo("Mordisco", 10, 2, 10, 6, 1));
        m.agregarAtaque(new AtaqueMonstruo("Garra", 10, 2, 6, 6));
        m.agregarAtaque(new AtaqueMonstruo("Garra", 10, 2, 6, 6));
        m.tieneAliento = true;
        m.alientoDisponible = true;
        return m;
    }

    /**
     * Tarrasca (version adaptada): CA 25, 676 PV, inmune al fuego, mitad de dano de armas.
     * 2 ataques al azar por turno entre mordisco, garra, cuernos y cola (+19).
     */
    public static Monstruo crearTarrasca() {
        Monstruo m = new Monstruo("Tarrasca", 676, 25, 0, true, true, 2, true);
        m.agregarAtaque(new AtaqueMonstruo("Mordisco", 19, 4, 12, 10));
        m.agregarAtaque(new AtaqueMonstruo("Garra", 19, 4, 8, 10));
        m.agregarAtaque(new AtaqueMonstruo("Cuernos", 19, 4, 10, 10));
        m.agregarAtaque(new AtaqueMonstruo("Cola", 19, 4, 6, 10));
        return m;
    }

    public static Monstruo crearPorEtapa(int etapa) {
        switch (etapa) {
            case 1: return crearGoblin();
            case 2: return crearOgro();
            case 3: return crearDragonRojoJoven();
            case 4: return crearTarrasca();
            default: throw new IllegalArgumentException("Etapa inválida: " + etapa);
        }
    }

    // ---------------- Combate ----------------

    /** Turno del monstruo: efecto continuo, aliento o ataques. */
    public List<String> realizarTurno(List<Aventurero> grupo) {
        List<String> lineas = new ArrayList<>();
        if (tieneEfectoActivo()) {
            lineas.add(efecto.aplicar(this));
            if (estaDerrotado()) {
                return lineas;
            }
        }
        if (Aventurero.vivos(grupo).isEmpty()) {
            return lineas;
        }
        lineas.add("Turno de " + nombre + ":");
        if (tieneAliento && !alientoDisponible) {
            int d6 = Dado.tirar(6);
            if (d6 >= 5) {
                alientoDisponible = true;
                lineas.add("  (Sacó " + d6 + " en el d6: ¡su Aliento de Fuego se recargó!)");
            }
        }
        if (tieneAliento && alientoDisponible) {
            alientoDisponible = false;
            lineas.add("  ¡" + nombre + " exhala un cono de fuego sobre todo el grupo!");
            for (Aventurero a : Aventurero.vivos(grupo)) {
                int danio = Dado.tirar(16, 6);
                boolean salva = a.tirarSalvacion(CD_ALIENTO_DRAGON);
                if (salva) {
                    danio /= 2;
                }
                a.recibirDanio(danio);
                lineas.add("  " + a.getNombre() + (salva ? " salva a medias" : " falla la salvación")
                        + " y recibe " + danio + " de daño." + (a.estaCaido() ? " ¡Ha caído!" : ""));
            }
            return lineas;
        }
        for (int i = 0; i < ataquesPorTurno; i++) {
            List<Aventurero> vivos = Aventurero.vivos(grupo);
            if (vivos.isEmpty()) {
                break;
            }
            Aventurero objetivo = vivos.get(Dado.tirar(vivos.size()) - 1);
            AtaqueMonstruo ataque = ataquesAlAzar
                    ? ataques.get(Dado.tirar(ataques.size()) - 1)
                    : ataques.get(i % ataques.size());
            lineas.add(ataque.ejecutar(nombre, objetivo));
        }
        return lineas;
    }

    /** Recibe dano segun su tipo. Devuelve el dano que realmente recibio. */
    public int recibirDanio(int danio, String tipo) {
        int real = Math.max(0, danio);
        if (DANIO_FUEGO.equals(tipo) && inmuneFuego) {
            real = 0;
        } else if (DANIO_ARMA.equals(tipo) && resisteArmas) {
            real = real / 2;
        }
        puntosVida = Math.max(0, puntosVida - real);
        return real;
    }

    public boolean tirarSalvacionDes(int cd) {
        return Dado.tirar(20) + bonoSalvacionDes >= cd;
    }

    public int tirarIniciativa() {
        return Dado.tirar(20) + bonoSalvacionDes;
    }

    public boolean estaDerrotado() {
        return puntosVida <= 0;
    }

    public boolean tieneEfectoActivo() {
        return efecto != null && efecto.estaActivo();
    }

    public void setEfecto(EfectoContinuo efecto) {
        this.efecto = efecto;
    }

    public EfectoContinuo getEfecto() { return efecto; }
    public String getNombre() { return nombre; }
    public int getPuntosVida() { return puntosVida; }
    public int getPuntosVidaMax() { return puntosVidaMax; }
    public int getClaseArmadura() { return claseArmadura; }
    public boolean isInmuneFuego() { return inmuneFuego; }
    public boolean isResisteArmas() { return resisteArmas; }
    public List<AtaqueMonstruo> getAtaques() { return ataques; }

    @Override
    public String toString() {
        return nombre + " | PV " + puntosVida + "/" + puntosVidaMax + " | CA " + claseArmadura;
    }
}
