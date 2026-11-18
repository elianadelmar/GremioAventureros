package modelo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de las validaciones y de la persistencia en .txt
 */
public class ValidacionYArchivoTest {

    @Test
    public void nombreConNumerosEsInvalido() {
        assertThrows(DatosInvalidosException.class,
                () -> ValidadorAventurero.validarNombre("Thorin123", new ArrayList<>()));
    }

    @Test
    public void nombreRepetidoEsInvalido() {
        List<Aventurero> gremio = new ArrayList<>();
        gremio.add(new Guerrero("Thorin", "Hombre", 1, 10, true));
        assertThrows(DatosInvalidosException.class,
                () -> ValidadorAventurero.validarNombre("thorin", gremio));
    }

    @Test
    public void letrasEnCampoNumericoLanzaExcepcion() {
        assertThrows(DatosInvalidosException.class,
                () -> ValidadorAventurero.validarEntero("cinco", 1, 20, "Nivel"));
    }

    @Test
    public void nivelFueraDeRangoEsInvalido() {
        assertThrows(DatosInvalidosException.class,
                () -> ValidadorAventurero.validarEntero("21", 1, 20, "Nivel"));
    }

    @Test
    public void vidaMaximaSegunDadoDeGolpe() {
        assertEquals(50, ValidadorAventurero.calcularPuntosVidaMax(Arrays.asList("Guerrero"), Arrays.asList(5)));
        assertEquals(40, ValidadorAventurero.calcularPuntosVidaMax(Arrays.asList("Pícaro", "Bardo"), Arrays.asList(3, 2)));
    }

    @Test
    public void guardarYCargarAventureros() throws Exception {
        File temp = File.createTempFile("gremio_test", ".txt");
        temp.delete();
        ArchivoGremio archivo = new ArchivoGremio(temp.getPath());
        archivo.guardarAventurero(new Guerrero("Thorin", "Hombre", 5, 50, true));
        Multiclase m = new Multiclase("Lyra", "Mujer", 40);
        m.agregarClase(new Picaro("Lyra", "Mujer", 3, 1, true));
        m.agregarClase(new Bardo("Lyra", "Mujer", 2, 1, "Laud"));
        archivo.guardarAventurero(m);

        List<Aventurero> cargados = archivo.cargarAventureros();
        assertEquals(2, cargados.size());
        assertEquals("Thorin", cargados.get(0).getNombre());
        assertTrue(cargados.get(1) instanceof Multiclase);
        assertEquals(5, cargados.get(1).getNivel());
        temp.delete();
    }
}
