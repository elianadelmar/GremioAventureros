package controlador;

import java.util.ArrayList;
import java.util.List;
import modelo.ArchivoGremio;
import modelo.Aventurero;
import modelo.Combate;
import modelo.DatosInvalidosException;
import modelo.FabricaAventureros;
import modelo.Monstruo;
import modelo.Multiclase;
import modelo.ValidadorAventurero;

/**
 * Controlador: conecta las vistas con el modelo.
 * Guarda a TODOS los aventureros en una unica coleccion (RNF2).
 *
 * (En la noche le agregamos las ventanas y los eventos de los botones.)
 */
public class ControladorGremio {

    public static final int ETAPA_FINAL = 3;
    public static final int ETAPA_SECRETA = 4;

    private final ArrayList<Aventurero> gremio = new ArrayList<>();
    private final ArchivoGremio archivo;
    private List<Aventurero> grupoCombate = new ArrayList<>();
    private Combate combate;
    private int etapa = 0;

    public ControladorGremio() {
        this(new ArchivoGremio());
    }

    public ControladorGremio(ArchivoGremio archivo) {
        this.archivo = archivo;
    }

    /** Carga los aventureros guardados en registro_gremio.txt */
    public void cargarGremio() {
        gremio.clear();
        gremio.addAll(archivo.cargarAventureros());
    }

    /**
     * Registra un aventurero con 1 a 4 clases (RF1).
     *
     * @param clases      nombres de las clases elegidas
     * @param niveles     nivel de cada clase (texto de la pantalla)
     * @param atributos   atributo especial de cada clase (texto de la pantalla)
     */
    public Aventurero registrarAventurero(String nombre, String genero, String puntosVidaTexto,
                                          List<String> clases, List<String> niveles,
                                          List<String> atributos) throws DatosInvalidosException {
        String nombreLimpio = ValidadorAventurero.validarNombre(nombre, gremio);
        ValidadorAventurero.validarGenero(genero);

        List<Integer> nivelesNum = new ArrayList<>();
        for (int i = 0; i < niveles.size(); i++) {
            nivelesNum.add(ValidadorAventurero.validarEntero(niveles.get(i), Aventurero.NIVEL_MIN,
                    Aventurero.NIVEL_MAX, "Nivel de " + clases.get(i)));
        }
        ValidadorAventurero.validarClases(clases, nivelesNum);

        int pvMax = ValidadorAventurero.calcularPuntosVidaMax(clases, nivelesNum);
        int pv = ValidadorAventurero.validarEntero(puntosVidaTexto, 1, pvMax, "Puntos de vida");

        Aventurero nuevo;
        if (clases.size() == 1) {
            nuevo = FabricaAventureros.crear(clases.get(0), nombreLimpio, genero, nivelesNum.get(0), pv, atributos.get(0));
        } else {
            Multiclase m = new Multiclase(nombreLimpio, genero, pv);
            for (int i = 0; i < clases.size(); i++) {
                m.agregarClase(FabricaAventureros.crear(clases.get(i), nombreLimpio, genero,
                        nivelesNum.get(i), pv, atributos.get(i)));
            }
            nuevo = m;
        }
        gremio.add(nuevo);
        archivo.guardarAventurero(nuevo);   // RF4
        return nuevo;
    }

    /** Empieza la campana con los aventureros elegidos: etapa 1 = Goblin. */
    public List<String> iniciarCampania(List<Aventurero> elegidos) throws DatosInvalidosException {
        if (elegidos == null || elegidos.isEmpty() || elegidos.size() > 4) {
            throw new DatosInvalidosException("Debe elegir entre 1 y 4 aventureros para el combate.");
        }
        grupoCombate = new ArrayList<>(elegidos);
        for (Aventurero a : grupoCombate) {
            a.descansoLargo();
        }
        etapa = 1;
        combate = new Combate(grupoCombate, Monstruo.crearPorEtapa(etapa));
        return combate.iniciar();
    }

    /** Ejecuta la siguiente ronda. Al terminar el combate lo guarda en el archivo. */
    public List<String> siguienteRonda() {
        List<String> lineas = combate.ejecutarRonda();
        if (combate.terminado()) {
            archivo.guardarCombate(combate.getMonstruo().getNombre(), combate.huboVictoria(), combate.getBitacora());
        }
        return lineas;
    }

    /** true si ganaron y hay otro monstruo por pelear. */
    public boolean hayOtraEtapa() {
        if (combate == null || !combate.terminado() || !combate.huboVictoria()) {
            return false;
        }
        if (etapa < ETAPA_FINAL) {
            return true;
        }
        return etapa == ETAPA_FINAL && tarrascaDesbloqueada();
    }

    /** La Tarrasca solo aparece si son 4 jugadores y los 4 son Leyendas. */
    public boolean tarrascaDesbloqueada() {
        if (grupoCombate.size() != 4) {
            return false;
        }
        for (Aventurero a : grupoCombate) {
            if (!a.esLeyenda()) {
                return false;
            }
        }
        return true;
    }

    /** Descanso corto y siguiente monstruo. */
    public List<String> avanzarEtapa() {
        List<String> lineas = new ArrayList<>();
        etapa++;
        lineas.add("Descanso corto: el grupo recupera la mitad de su vida.");
        for (Aventurero a : grupoCombate) {
            a.descansoCorto();
        }
        if (etapa == ETAPA_SECRETA) {
            lineas.add("¡La tierra tiembla... LA TARRASCA HA DESPERTADO!");
        }
        combate = new Combate(grupoCombate, Monstruo.crearPorEtapa(etapa));
        lineas.addAll(combate.iniciar());
        return lineas;
    }

    public boolean campaniaGanada() {
        return combate != null && combate.terminado() && combate.huboVictoria() && !hayOtraEtapa();
    }

    public ArrayList<Aventurero> getGremio() { return gremio; }
    public List<Aventurero> getGrupoCombate() { return grupoCombate; }
    public Combate getCombate() { return combate; }
    public int getEtapa() { return etapa; }
}
