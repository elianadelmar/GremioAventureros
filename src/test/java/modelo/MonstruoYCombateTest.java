package modelo;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de monstruos, dados y combate.
 */
public class MonstruoYCombateTest {

    @BeforeEach
    public void setUp() {
        Dado.setSemilla(7);
    }

    @Test
    public void losDadosQuedanEnSuRango() {
        for (int i = 0; i < 500; i++) {
            int d20 = Dado.tirar(20);
            assertTrue(d20 >= 1 && d20 <= 20);
            int bola = Dado.tirar(8, 6);
            assertTrue(bola >= 8 && bola <= 48);
        }
    }

    @Test
    public void datosDelGoblinSegunElSRD() {
        Monstruo g = Monstruo.crearGoblin();
        assertEquals(15, g.getClaseArmadura());
        assertEquals(7, g.getPuntosVidaMax());
    }

    @Test
    public void elDragonEsInmuneAlFuego() {
        Monstruo d = Monstruo.crearDragonRojoJoven();
        assertEquals(0, d.recibirDanio(30, Monstruo.DANIO_FUEGO));
        assertEquals(178, d.getPuntosVida());
    }

    @Test
    public void laTarrascaRecibeMitadDeDanioDeArmas() {
        Monstruo t = Monstruo.crearTarrasca();
        assertEquals(10, t.recibirDanio(20, Monstruo.DANIO_ARMA));
        assertEquals(20, t.recibirDanio(20, Monstruo.DANIO_MAGIA));
    }

    @Test
    public void elEfectoContinuoDuraTresTurnos() {
        Monstruo ogro = Monstruo.crearOgro();
        EfectoContinuo esfera = new EfectoContinuo("Esfera Llameante", 2, 6, 3, Monstruo.DANIO_FUEGO);
        esfera.aplicar(ogro);
        esfera.aplicar(ogro);
        assertTrue(esfera.estaActivo());
        esfera.aplicar(ogro);
        assertFalse(esfera.estaActivo());
        assertTrue(ogro.getPuntosVida() < 59);
    }

    @Test
    public void unGrupoFuerteDerrotaAlGoblin() {
        List<Aventurero> grupo = new ArrayList<>();
        grupo.add(new Guerrero("Thorin", "Hombre", 10, 100, true));
        grupo.add(new Paladin("Arthas", "Hombre", 10, 100, 5));
        Combate combate = new Combate(grupo, Monstruo.crearGoblin());
        combate.ejecutarCompleto();
        assertTrue(combate.terminado());
        assertTrue(combate.huboVictoria());
    }

    @Test
    public void elCombateSiempreTermina() {
        List<Aventurero> grupo = new ArrayList<>();
        grupo.add(new Bardo("Lyra", "Mujer", 1, 8, "Laud"));
        Combate combate = new Combate(grupo, Monstruo.crearDragonRojoJoven());
        combate.ejecutarCompleto();
        assertTrue(combate.terminado());
        assertFalse(combate.huboVictoria());
    }

    @Test
    public void noSePuedeCombatirSinAventureros() {
        assertThrows(IllegalArgumentException.class,
                () -> new Combate(new ArrayList<>(), Monstruo.crearGoblin()));
    }
}
