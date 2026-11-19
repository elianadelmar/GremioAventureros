package controlador;

import com.mycompany.gremioaventureros.GremioAventureros;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import modelo.ArchivoGremio;
import modelo.Aventurero;
import modelo.Combate;
import modelo.DatosInvalidosException;
import modelo.FabricaAventureros;
import modelo.Monstruo;
import modelo.Multiclase;
import modelo.ValidadorAventurero;
import vista.VentanaCombate;
import vista.VentanaGremio;
import vista.VentanaInicio;
import vista.VentanaRegistro;

/**
 * Controlador: conecta las vistas con el modelo.
 * - Guarda a TODOS los aventureros en una unica coleccion (RNF2).
 * - Escucha los botones de las vistas (las vistas no tienen logica).
 */
public class ControladorGremio {

    public static final int ETAPA_FINAL = 3;
    public static final int ETAPA_SECRETA = 4;
    private static final String NINGUNA = "Ninguna";

    private final ArrayList<Aventurero> gremio = new ArrayList<>();
    private final ArchivoGremio archivo;
    private List<Aventurero> grupoCombate = new ArrayList<>();
    private Combate combate;
    private int etapa = 0;

    // ---------------- Vistas ----------------
    private VentanaInicio vInicio;
    private VentanaRegistro vRegistro;
    private VentanaGremio vGremio;
    private VentanaCombate vCombate;

    public ControladorGremio() {
        this(new ArchivoGremio());
    }

    public ControladorGremio(ArchivoGremio archivo) {
        this.archivo = archivo;
    }

    // =====================================================================
    //  ENLAZAMIENTO DE EVENTOS (las vistas solo prestan sus componentes)
    // =====================================================================

    /** Crea las pantallas, conecta sus botones y muestra el inicio. */
    public void iniciar() {
        cargarGremio();
        vInicio = new VentanaInicio();
        vRegistro = new VentanaRegistro();
        vGremio = new VentanaGremio();
        vCombate = new VentanaCombate();

        // Inicio
        vInicio.getBtnRegistrar().addActionListener(e -> abrirRegistro());
        vInicio.getBtnVerGremio().addActionListener(e -> abrirGremio());
        vInicio.getBtnCampania().addActionListener(e -> abrirGremio());

        // Registro
        JComboBox<?>[] combos = vRegistro.getCmbClases();
        for (int i = 0; i < combos.length; i++) {
            final int fila = i;
            combos[i].addActionListener(e -> actualizarFilaClase(fila));
        }
        vRegistro.getBtnGuardar().addActionListener(e -> guardarRegistro());
        vRegistro.getBtnVolver().addActionListener(e -> mostrar(vInicio));

        // Gremio
        vGremio.getBtnCombatir().addActionListener(e -> empezarCombate());
        vGremio.getBtnVolver().addActionListener(e -> mostrar(vInicio));

        // Combate
        vCombate.getBtnSiguienteRonda().addActionListener(e -> jugarRonda());
        vCombate.getBtnContinuar().addActionListener(e -> continuarCampania());
        vCombate.getBtnVolver().addActionListener(e -> mostrar(vInicio));

        // Cerrar una ventana secundaria con la X devuelve al inicio
        for (JFrame v : new JFrame[]{vRegistro, vGremio, vCombate}) {
            v.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            v.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    mostrar(vInicio);
                }
            });
        }
        vGremio.getTxtReportes().setEditable(false);

        mostrar(vInicio);
    }

    /** Muestra una pantalla y esconde las demas. */
    private void mostrar(JFrame ventana) {
        for (JFrame v : new JFrame[]{vInicio, vRegistro, vGremio, vCombate}) {
            if (v != ventana) {
                v.setVisible(false);
            }
        }
        ventana.setLocationRelativeTo(null);
        ventana.setVisible(true);
    }

    // =====================================================================
    //  PANTALLA DE REGISTRO (RF1)
    // =====================================================================

    private void abrirRegistro() {
        limpiarRegistro();
        mostrar(vRegistro);
    }

    private void limpiarRegistro() {
        vRegistro.getTxtNombre().setText("");
        vRegistro.getTxtPuntosVida().setText("");
        vRegistro.getCmbGenero().setSelectedIndex(0);
        JComboBox<?>[] combos = vRegistro.getCmbClases();
        for (int i = 0; i < combos.length; i++) {
            combos[i].setSelectedIndex(i == 0 ? 1 : 0);   // fila 1 arranca en Guerrero
            vRegistro.getTxtNiveles()[i].setText("");
            vRegistro.getTxtAtributos()[i].setText("");
            actualizarFilaClase(i);
        }
    }

    /** Cambia la etiqueta del atributo segun la clase elegida y activa/desactiva la fila. */
    private void actualizarFilaClase(int fila) {
        String clase = String.valueOf(vRegistro.getCmbClases()[fila].getSelectedItem());
        boolean activa = !NINGUNA.equals(clase);
        JTextField nivel = vRegistro.getTxtNiveles()[fila];
        JTextField atributo = vRegistro.getTxtAtributos()[fila];
        nivel.setEnabled(activa);
        atributo.setEnabled(activa);
        if (!activa) {
            nivel.setText("");
            atributo.setText("");
            vRegistro.getLblAtributos()[fila].setText("-");
            return;
        }
        String texto = FabricaAventureros.nombreAtributo(clase);
        if (FabricaAventureros.atributoEsSiNo(clase)) {
            texto += " (Sí/No)";
        }
        vRegistro.getLblAtributos()[fila].setText(texto);
    }

    private void guardarRegistro() {
        List<String> clases = new ArrayList<>();
        List<String> niveles = new ArrayList<>();
        List<String> atributos = new ArrayList<>();
        for (int i = 0; i < vRegistro.getCmbClases().length; i++) {
            String clase = String.valueOf(vRegistro.getCmbClases()[i].getSelectedItem());
            if (!NINGUNA.equals(clase)) {
                clases.add(clase);
                niveles.add(vRegistro.getTxtNiveles()[i].getText());
                atributos.add(vRegistro.getTxtAtributos()[i].getText());
            }
        }
        try {
            Aventurero nuevo = registrarAventurero(
                    vRegistro.getTxtNombre().getText(),
                    String.valueOf(vRegistro.getCmbGenero().getSelectedItem()),
                    vRegistro.getTxtPuntosVida().getText(),
                    clases, niveles, atributos);
            String extra = nuevo.esLeyenda() ? "\n\n¡¡Es una LEYENDA DEL GREMIO!!" : "";
            JOptionPane.showMessageDialog(vRegistro, "¡" + nuevo.getNombre() + " se unió al gremio!\n"
                    + nuevo.getClase() + " - Nivel " + nuevo.getNivel() + extra,
                    "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            limpiarRegistro();
        } catch (DatosInvalidosException ex) {
            JOptionPane.showMessageDialog(vRegistro, ex.getMessage(), "Datos inválidos", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================
    //  PANTALLA DEL GREMIO (RF2)
    // =====================================================================

    private void abrirGremio() {
        DefaultTableModel modelo = new DefaultTableModel(
                new Object[]{"Nombre", "Género", "Clase(s)", "Nivel", "PV", "CA", "Atributo especial"}, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        for (Aventurero a : gremio) {
            modelo.addRow(new Object[]{a.getNombre(), a.getGenero(), a.getClase(), a.getNivel(),
                a.getPuntosVidaMax(), a.getClaseArmadura(), a.getAtributoEspecial()});
        }
        vGremio.getTblGremio().setModel(modelo);
        vGremio.getTxtReportes().setText(GremioAventureros.reporteCantidadPorClase(gremio)
                + "\n" + GremioAventureros.reporteMayorNivel(gremio));
        mostrar(vGremio);
    }

    private void empezarCombate() {
        vGremio.getBtnCombatir().setSelected(false);
        int[] filas = vGremio.getTblGremio().getSelectedRows();
        List<Aventurero> elegidos = new ArrayList<>();
        for (int f : filas) {
            elegidos.add(gremio.get(f));
        }
        int etapaInicial = vGremio.getCmbMonstruo().getSelectedIndex() + 1;
        try {
            List<String> lineas = iniciarCampania(elegidos, etapaInicial);
            prepararPantallaCombate();
            vCombate.getTxtBitacora().setText("");
            escribir(lineas);
            mostrar(vCombate);
        } catch (DatosInvalidosException ex) {
            JOptionPane.showMessageDialog(vGremio, ex.getMessage()
                    + "\n(Usa Ctrl + clic para seleccionar varios en la tabla)",
                    "No se puede combatir", JOptionPane.WARNING_MESSAGE);
        }
    }

    // =====================================================================
    //  PANTALLA DE COMBATE (RF3)
    // =====================================================================

    private void prepararPantallaCombate() {
        for (int i = 0; i < 4; i++) {
            boolean usado = i < grupoCombate.size();
            vCombate.getLblHeroes()[i].setVisible(usado);
            vCombate.getBarHeroes()[i].setVisible(usado);
            if (usado) {
                Aventurero a = grupoCombate.get(i);
                vCombate.getLblHeroes()[i].setText(a.getNombre());
                vCombate.getLblHeroes()[i].setIcon(cargarSprite(a.getClaseParaSprite() + "_" + a.getGenero(), a.getClaseParaSprite(), "heroe"));
                vCombate.getBarHeroes()[i].setMaximum(a.getPuntosVidaMax());
                vCombate.getBarHeroes()[i].setStringPainted(true);
            }
        }
        Monstruo m = combate.getMonstruo();
        vCombate.getLblMonstruo().setText(m.getNombre());
        vCombate.getLblMonstruo().setIcon(cargarSprite(m.getNombre()));
        vCombate.getBarMonstruo().setMaximum(m.getPuntosVidaMax());
        vCombate.getBarMonstruo().setStringPainted(true);
        vCombate.getLblEtapa().setText(etapa == ETAPA_SECRETA ? "¡¡JEFE SECRETO!!" : "Etapa " + etapa + " de " + ETAPA_FINAL);
        vCombate.getLblResultado().setText("");
        vCombate.getBtnSiguienteRonda().setEnabled(true);
        vCombate.getBtnContinuar().setEnabled(false);
        actualizarBarras();
    }

    private void actualizarBarras() {
        for (int i = 0; i < grupoCombate.size(); i++) {
            Aventurero a = grupoCombate.get(i);
            vCombate.getBarHeroes()[i].setValue(a.getPuntosVida());
            vCombate.getBarHeroes()[i].setString(a.estaCaido() ? "Caído" : a.getPuntosVida() + " / " + a.getPuntosVidaMax());
        }
        Monstruo m = combate.getMonstruo();
        vCombate.getBarMonstruo().setValue(m.getPuntosVida());
        vCombate.getBarMonstruo().setString(m.getPuntosVida() + " / " + m.getPuntosVidaMax());
    }

    private void jugarRonda() {
        escribir(siguienteRonda());
        actualizarBarras();
        if (combate.terminado()) {
            vCombate.getBtnSiguienteRonda().setEnabled(false);
            if (hayOtraEtapa()) {
                vCombate.getLblResultado().setText("¡VICTORIA! Sigue el próximo enemigo...");
                vCombate.getBtnContinuar().setEnabled(true);
            } else if (campaniaGanada()) {
                vCombate.getLblResultado().setText("¡LEYENDAS DEL GREMIO! Campaña completada");
            } else {
                vCombate.getLblResultado().setText("DERROTA... Vuelve al inicio para intentarlo de nuevo");
            }
        }
    }

    private void continuarCampania() {
        escribir(avanzarEtapa());
        prepararPantallaCombate();
    }

    private void escribir(List<String> lineas) {
        for (String l : lineas) {
            vCombate.getTxtBitacora().append(l + "\n");
        }
        vCombate.getTxtBitacora().setCaretPosition(vCombate.getTxtBitacora().getDocument().getLength());
    }

    /**
     * Busca el primer sprite que exista en /imagenes/sprites/ (sin tildes, en minuscula).
     * Ej: "mago_mujer" -> si no existe prueba "mago" -> si no existe prueba "heroe". Si nada existe, sin imagen.
     */
    private ImageIcon cargarSprite(String... nombres) {
        for (String nombre : nombres) {
            String archivoSprite = Normalizer.normalize(nombre, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "").toLowerCase().replace(" ", "_");
            URL url = getClass().getResource("/imagenes/sprites/" + archivoSprite + ".png");
            if (url != null) {
                return new ImageIcon(url);
            }
        }
        return null;
    }

    // =====================================================================
    //  LOGICA (usa el modelo)
    // =====================================================================

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

    // SOBRECARGA: por defecto la campana empieza en el Goblin
    public List<String> iniciarCampania(List<Aventurero> elegidos) throws DatosInvalidosException {
        return iniciarCampania(elegidos, 1);
    }

    /** Empieza la campana con los aventureros elegidos desde la etapa indicada (1 Goblin, 2 Ogro, 3 Dragon). */
    public List<String> iniciarCampania(List<Aventurero> elegidos, int etapaInicial) throws DatosInvalidosException {
        if (elegidos == null || elegidos.isEmpty() || elegidos.size() > 4) {
            throw new DatosInvalidosException("Debe elegir entre 1 y 4 aventureros para el combate.");
        }
        if (etapaInicial < 1 || etapaInicial > ETAPA_FINAL) {
            throw new DatosInvalidosException("Debe elegir un monstruo válido.");
        }
        grupoCombate = new ArrayList<>(elegidos);
        for (Aventurero a : grupoCombate) {
            a.descansoLargo();
        }
        etapa = etapaInicial;
        combate = new Combate(grupoCombate, Monstruo.crearPorEtapa(etapa));
        return combate.iniciar();
    }

    /** Ejecuta la siguiente ronda. Al terminar el combate lo guarda en el archivo (RF4). */
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
        lineas.add("");
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
