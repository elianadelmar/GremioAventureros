package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase abstracta base de todos los aventureros del gremio.
 * Reglas tomadas de D&D 5e (bono de competencia, CA, tiradas de ataque, criticos).
 */
public abstract class Aventurero implements IAccionCombate {

    public static final int NIVEL_MIN = 1;
    public static final int NIVEL_MAX = 20;
    // Modificador de caracteristica principal (+3 = puntuacion 16, arreglo estandar de 5e)
    public static final int MOD_CARACTERISTICA = 3;

    private String nombre;
    private String genero;
    private int nivel;
    private int puntosVida;
    private int puntosVidaMax;
    private int bonoInspiracion;   // caras del dado de Inspiracion Bardica (0 = no tiene)
    private int nivelPersonaje;    // nivel total del personaje (importa en multiclase)
    protected boolean ultimoAtaqueAcerto;

    public Aventurero(String nombre, String genero, int nivel, int puntosVida) {
        this.nombre = nombre;
        this.genero = genero;
        this.nivel = nivel;
        this.nivelPersonaje = nivel;
        this.puntosVida = puntosVida;
        this.puntosVidaMax = puntosVida;
    }

    // ---------------- Getters y setters ----------------
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public int getNivel() { return nivel; }
    public void setNivel(int nivel) { this.nivel = nivel; }
    public int getPuntosVida() { return puntosVida; }
    public void setPuntosVida(int puntosVida) { this.puntosVida = Math.max(0, Math.min(puntosVida, puntosVidaMax)); }
    public int getPuntosVidaMax() { return puntosVidaMax; }
    public int getBonoInspiracion() { return bonoInspiracion; }
    public int getNivelPersonaje() { return nivelPersonaje; }
    public void setNivelPersonaje(int nivelPersonaje) { this.nivelPersonaje = nivelPersonaje; }

    // ---------------- Reglas de 5e ----------------

    /** Bono de competencia: +2 (niv 1-4), +3 (5-8), +4 (9-12), +5 (13-16), +6 (17-20). */
    public int getBonoCompetencia() {
        return 2 + (nivelPersonaje - 1) / 4;
    }

    /** CD de salvacion de los conjuros: 8 + competencia + modificador. */
    public int getCdConjuros() {
        return 8 + getBonoCompetencia() + MOD_CARACTERISTICA;
    }

    /** Ataque Extra: a nivel 5 las clases marciales atacan 2 veces por accion. */
    protected int ataquesPorAccion() {
        return getNivel() >= 5 ? 2 : 1;
    }

    // SOBRECARGA 1: ataque con un dado de arma (ej. 1d8)
    public String atacar(Monstruo objetivo, String arma, int caras) {
        return atacar(objetivo, arma, 1, caras);
    }

    // SOBRECARGA 2: ataque con varios dados (ej. 2d6)
    public String atacar(Monstruo objetivo, String arma, int cantidad, int caras) {
        return atacar(objetivo, arma, cantidad, caras, false, 0, Monstruo.DANIO_ARMA);
    }

    // SOBRECARGA 3 (completa): ventaja, dados d6 extra y tipo de dano
    protected String atacar(Monstruo objetivo, String arma, int cantidad, int caras,
                            boolean ventaja, int d6Extra, String tipoDanio) {
        int d20 = ventaja ? Dado.tirarConVentaja() : Dado.tirar(20);
        int inspiracion = 0;
        if (bonoInspiracion > 0) {
            inspiracion = Dado.tirar(bonoInspiracion);
            bonoInspiracion = 0;
        }
        int bono = getBonoCompetencia() + MOD_CARACTERISTICA + inspiracion;
        int total = d20 + bono;
        StringBuilder sb = new StringBuilder();
        sb.append("  ").append(nombre).append(" ataca con ").append(arma)
          .append(": d20(").append(d20).append(")").append(ventaja ? " con ventaja" : "")
          .append(" + ").append(bono);
        if (inspiracion > 0) {
            sb.append(" [Inspiración +").append(inspiracion).append("]");
        }
        sb.append(" = ").append(total).append(" vs CA ").append(objetivo.getClaseArmadura());

        boolean critico = d20 == 20;
        if (d20 == 1) {
            ultimoAtaqueAcerto = false;
            return sb.append(" -> ¡Pifia! Falla automáticamente.").toString();
        }
        if (!critico && total < objetivo.getClaseArmadura()) {
            ultimoAtaqueAcerto = false;
            return sb.append(" -> Falla.").toString();
        }
        ultimoAtaqueAcerto = true;
        int multiplicador = critico ? 2 : 1;   // critico: se tiran el doble de dados
        int danio = Dado.tirar(cantidad * multiplicador, caras);
        if (Monstruo.DANIO_ARMA.equals(tipoDanio)) {
            danio += MOD_CARACTERISTICA;
        }
        if (d6Extra > 0) {
            danio += Dado.tirar(d6Extra * multiplicador, 6);
        }
        int real = objetivo.recibirDanio(danio, tipoDanio);
        sb.append(critico ? " -> ¡CRÍTICO! " : " -> ¡Acierta! ").append(real).append(" de daño.");
        return sb.toString();
    }

    /** Salvacion generica de Destreza (+2) contra una CD. */
    public boolean tirarSalvacion(int cd) {
        return Dado.tirar(20) + 2 >= cd;
    }

    public void recibirDanio(int danio) {
        puntosVida = Math.max(0, puntosVida - Math.max(0, danio));
    }

    /** Cura sin pasar del maximo. Devuelve cuanto curo realmente. */
    public int curar(int cantidad) {
        int antes = puntosVida;
        puntosVida = Math.min(puntosVidaMax, puntosVida + Math.max(0, cantidad));
        return puntosVida - antes;
    }

    public void recibirInspiracion(int carasDado) {
        this.bonoInspiracion = carasDado;
    }

    public boolean estaCaido() {
        return puntosVida <= 0;
    }

    /** Descanso corto entre combates de la campana: recupera la mitad de la vida maxima. */
    public void descansoCorto() {
        curar(puntosVidaMax / 2);
    }

    /** Antes de una campana nueva: vida completa y recursos recuperados (descanso largo). */
    public void descansoLargo() {
        puntosVida = puntosVidaMax;
        bonoInspiracion = 0;
        reiniciarRecursos();
    }

    /** Cada clase recupera aqui sus recursos (espacios, ki, Oleada de Accion...). */
    protected void reiniciarRecursos() {
    }

    /** Clase que se usa para escoger el sprite. */
    public String getClaseParaSprite() {
        return getClase();
    }

    /** Solo un Multiclase con 4 clases a nivel 5 es Leyenda. */
    public boolean esLeyenda() {
        return false;
    }

    // ---------------- Metodos de ayuda ----------------

    /** Devuelve los aventureros que siguen en pie. */
    public static List<Aventurero> vivos(List<Aventurero> grupo) {
        List<Aventurero> vivos = new ArrayList<>();
        for (Aventurero a : grupo) {
            if (!a.estaCaido()) {
                vivos.add(a);
            }
        }
        return vivos;
    }

    /** El aliado en pie con menos de la mitad de vida y menor porcentaje, o null. */
    public static Aventurero aliadoMasHerido(List<Aventurero> grupo) {
        Aventurero masHerido = null;
        double menor = 0.5;
        for (Aventurero a : vivos(grupo)) {
            double porcentaje = (double) a.getPuntosVida() / a.getPuntosVidaMax();
            if (porcentaje < menor) {
                menor = porcentaje;
                masHerido = a;
            }
        }
        return masHerido;
    }

    // ---------------- Metodos abstractos ----------------
    public abstract String getClase();
    public abstract int getDadoGolpe();
    public abstract int getClaseArmadura();
    public abstract String getAtributoEspecial();
    public abstract String getValorAtributo();

    /** Linea para guardar en registro_gremio.txt */
    public String toLineaArchivo() {
        return getClase() + ";" + nombre + ";" + genero + ";" + nivel + ";" + puntosVidaMax + ";" + getValorAtributo();
    }

    @Override
    public String toString() {
        return getClase() + " | " + nombre + " (" + genero + ") | Nivel " + getNivel()
                + " | PV " + puntosVida + "/" + puntosVidaMax + " | CA " + getClaseArmadura()
                + " | " + getAtributoEspecial();
    }
}
