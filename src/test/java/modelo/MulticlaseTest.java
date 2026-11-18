package modelo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de las reglas de multiclase.
 */
public class MulticlaseTest {

    @Test
    public void elNivelEsLaSumaDeLasClases() throws DatosInvalidosException {
        Multiclase m = new Multiclase("Lyra", "Mujer", 40);
        m.agregarClase(new Picaro("Lyra", "Mujer", 3, 1, true));
        m.agregarClase(new Bardo("Lyra", "Mujer", 2, 1, "Laud"));
        assertEquals(5, m.getNivel());
        assertEquals(3, m.getBonoCompetencia());
    }

    @Test
    public void noSePuedeRepetirClase() throws DatosInvalidosException {
        Multiclase m = new Multiclase("Lyra", "Mujer", 40);
        m.agregarClase(new Picaro("Lyra", "Mujer", 3, 1, true));
        assertThrows(DatosInvalidosException.class,
                () -> m.agregarClase(new Picaro("Lyra", "Mujer", 2, 1, false)));
    }

    @Test
    public void conCuatroClasesNingunaPasaDeNivelCinco() throws DatosInvalidosException {
        Multiclase m = new Multiclase("Ana", "Mujer", 60);
        m.agregarClase(new Guerrero("Ana", "Mujer", 6, 1, true));
        m.agregarClase(new Mago("Ana", "Mujer", 2, 1, 2));
        m.agregarClase(new Bardo("Ana", "Mujer", 2, 1, "Flauta"));
        assertThrows(DatosInvalidosException.class,
                () -> m.agregarClase(new Picaro("Ana", "Mujer", 2, 1, true)));
    }

    @Test
    public void laSumaNoPuedePasarDeVeinte() throws DatosInvalidosException {
        Multiclase m = new Multiclase("Ana", "Mujer", 100);
        m.agregarClase(new Guerrero("Ana", "Mujer", 15, 1, true));
        assertThrows(DatosInvalidosException.class,
                () -> m.agregarClase(new Mago("Ana", "Mujer", 6, 1, 2)));
    }

    @Test
    public void cuatroClasesANivelCincoEsLeyenda() throws DatosInvalidosException {
        Multiclase m = new Multiclase("Ana", "Mujer", 100);
        m.agregarClase(new Guerrero("Ana", "Mujer", 5, 1, true));
        m.agregarClase(new Mago("Ana", "Mujer", 5, 1, 9));
        m.agregarClase(new Bardo("Ana", "Mujer", 5, 1, "Flauta"));
        m.agregarClase(new Picaro("Ana", "Mujer", 5, 1, true));
        assertTrue(m.esLeyenda());
        assertEquals(20, m.getNivel());
        assertEquals("Leyenda", m.getClaseParaSprite());
    }

    @Test
    public void laCAEsLaMejorDeSusClases() throws DatosInvalidosException {
        Multiclase m = new Multiclase("Ana", "Mujer", 40);
        m.agregarClase(new Mago("Ana", "Mujer", 2, 1, 3));
        m.agregarClase(new Paladin("Ana", "Mujer", 2, 1, 2));
        assertEquals(18, m.getClaseArmadura());
    }
}
