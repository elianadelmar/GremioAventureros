package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Aventurero con 2 a 4 clases (multiclase de D&D 5e).
 * - El nivel del personaje es la suma de los niveles de sus clases (maximo 20).
 * - Con 4 clases, ninguna puede pasar de nivel 5.
 * - En combate usa una clase por turno, rotando.
 */
public class Multiclase extends Aventurero {

    public static final int MAX_CLASES = 4;
    public static final int NIVEL_MAX_CON_4_CLASES = 5;

    private final List<Aventurero> clases = new ArrayList<>();
    private int turnoClase = 0;

    public Multiclase(String nombre, String genero, int puntosVida) {
        super(nombre, genero, 0, puntosVida);
    }

    /** Agrega una clase validando las reglas de multiclase. */
    public void agregarClase(Aventurero clase) throws DatosInvalidosException {
        if (clase instanceof Multiclase) {
            throw new DatosInvalidosException("Una clase no puede ser otro multiclase.");
        }
        if (clases.size() >= MAX_CLASES) {
            throw new DatosInvalidosException("Un aventurero puede tener máximo " + MAX_CLASES + " clases.");
        }
        for (Aventurero c : clases) {
            if (c.getClase().equals(clase.getClase())) {
                throw new DatosInvalidosException("No se puede repetir la clase " + clase.getClase() + ".");
            }
        }
        int total = getNivel() + clase.getNivel();
        if (total > NIVEL_MAX) {
            throw new DatosInvalidosException("La suma de niveles no puede pasar de " + NIVEL_MAX + ".");
        }
        if (clases.size() + 1 == MAX_CLASES) {
            for (Aventurero c : clases) {
                if (c.getNivel() > NIVEL_MAX_CON_4_CLASES) {
                    throw new DatosInvalidosException("Con 4 clases ninguna puede pasar de nivel " + NIVEL_MAX_CON_4_CLASES + ".");
                }
            }
            if (clase.getNivel() > NIVEL_MAX_CON_4_CLASES) {
                throw new DatosInvalidosException("Con 4 clases ninguna puede pasar de nivel " + NIVEL_MAX_CON_4_CLASES + ".");
            }
        }
        clases.add(clase);
        setNivel(total);
        setNivelPersonaje(total);
        for (Aventurero c : clases) {
            c.setNivelPersonaje(total);   // el bono de competencia usa el nivel total
        }
    }

    public List<Aventurero> getClases() {
        return clases;
    }

    /** La clase de mayor nivel (la primera si hay empate). Define el sprite. */
    public Aventurero getClasePrincipal() {
        Aventurero principal = clases.get(0);
        for (Aventurero c : clases) {
            if (c.getNivel() > principal.getNivel()) {
                principal = c;
            }
        }
        return principal;
    }

    @Override
    public String simularCombate(Monstruo objetivo, List<Aventurero> grupo) {
        Aventurero clase = clases.get(turnoClase % clases.size());
        turnoClase++;
        if (getBonoInspiracion() > 0) {        // la inspiracion pasa a la clase que actua
            clase.recibirInspiracion(getBonoInspiracion());
            recibirInspiracion(0);
        }
        return "[" + getNombre() + " actúa como " + clase.getClase() + "]\n"
                + clase.simularCombate(objetivo, grupo);
    }

    @Override
    protected void reiniciarRecursos() {
        turnoClase = 0;
        for (Aventurero c : clases) {
            c.descansoLargo();
        }
    }

    @Override
    public boolean esLeyenda() {
        if (clases.size() != MAX_CLASES) {
            return false;
        }
        for (Aventurero c : clases) {
            if (c.getNivel() != NIVEL_MAX_CON_4_CLASES) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getClase() {
        StringBuilder sb = new StringBuilder();
        for (Aventurero c : clases) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(c.getClase()).append(" ").append(c.getNivel());
        }
        return sb.toString();
    }

    @Override
    public String getClaseParaSprite() {
        return esLeyenda() ? "Leyenda" : getClasePrincipal().getClase();
    }

    @Override
    public int getDadoGolpe() {
        return getClasePrincipal().getDadoGolpe();
    }

    /** La mejor CA entre sus clases. */
    @Override
    public int getClaseArmadura() {
        int mejor = 0;
        for (Aventurero c : clases) {
            mejor = Math.max(mejor, c.getClaseArmadura());
        }
        return mejor;
    }

    @Override
    public String getAtributoEspecial() {
        StringBuilder sb = new StringBuilder();
        for (Aventurero c : clases) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(c.getAtributoEspecial());
        }
        return sb.toString();
    }

    @Override
    public String getValorAtributo() {
        StringBuilder sb = new StringBuilder();
        for (Aventurero c : clases) {
            if (sb.length() > 0) sb.append(",");
            sb.append(c.getClase()).append("-").append(c.getNivel()).append("-").append(c.getValorAtributo());
        }
        return sb.toString();
    }

    /** Multiclase;nombre;genero;pvMax;Clase-nivel-atributo,Clase-nivel-atributo */
    @Override
    public String toLineaArchivo() {
        return "Multiclase;" + getNombre() + ";" + getGenero() + ";" + getPuntosVidaMax() + ";" + getValorAtributo();
    }
}
