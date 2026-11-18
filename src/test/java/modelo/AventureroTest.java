package modelo;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la logica de los aventureros y sus clases.
 */
public class AventureroTest {

    @BeforeEach
    public void setUp() {
        Dado.setSemilla(42);   // resultados repetibles
    }

    @Test
    public void bonoCompetenciaSegunNivel() {
        assertEquals(2, new Guerrero("Thorin", "Hombre", 1, 10, true).getBonoCompetencia());
        assertEquals(3, new Guerrero("Thorin", "Hombre", 5, 50, true).getBonoCompetencia());
        assertEquals(6, new Guerrero("Thorin", "Hombre", 20, 200, true).getBonoCompetencia());
    }

    @Test
    public void elDanioNoDejaLaVidaNegativa() {
        Mago mago = new Mago("Merlin", "Hombre", 1, 6, 2);
        mago.recibirDanio(100);
        assertEquals(0, mago.getPuntosVida());
        assertTrue(mago.estaCaido());
    }

    @Test
    public void curarNoPasaDelMaximo() {
        Bardo bardo = new Bardo("Lyra", "Mujer", 3, 24, "Laud");
        bardo.recibirDanio(10);
        int curado = bardo.curar(50);
        assertEquals(10, curado);
        assertEquals(24, bardo.getPuntosVida());
    }

    @Test
    public void claseArmaduraDelGuerreroDependeDeLaArmadura() {
        assertEquals(16, new Guerrero("A", "Hombre", 1, 10, true).getClaseArmadura());
        assertEquals(13, new Guerrero("B", "Hombre", 1, 10, false).getClaseArmadura());
    }

    @Test
    public void elGuerreroUsaOleadaDeAccionUnaSolaVez() {
        Guerrero g = new Guerrero("Thorin", "Hombre", 5, 50, true);
        List<Aventurero> grupo = new ArrayList<>();
        grupo.add(g);
        String turno1 = g.simularCombate(Monstruo.crearDragonRojoJoven(), grupo);
        assertTrue(turno1.contains("Oleada de Acción"));
        assertFalse(g.isOleadaDisponible());
        String turno2 = g.simularCombate(Monstruo.crearDragonRojoJoven(), grupo);
        assertFalse(turno2.contains("Oleada de Acción"));
    }

    @Test
    public void elMagoGastaEspaciosDeConjuro() {
        Mago mago = new Mago("Merlin", "Hombre", 5, 30, 1);
        List<Aventurero> grupo = new ArrayList<>();
        grupo.add(mago);
        String turno = mago.simularCombate(Monstruo.crearOgro(), grupo);
        assertTrue(turno.contains("Bola de Fuego"));
        assertEquals(0, mago.getEspaciosConjuro());
    }

    @Test
    public void elMagoSinEspaciosUsaTruco() {
        Mago mago = new Mago("Merlin", "Hombre", 5, 30, 0);
        List<Aventurero> grupo = new ArrayList<>();
        grupo.add(mago);
        String turno = mago.simularCombate(Monstruo.crearOgro(), grupo);
        assertTrue(turno.contains("Rayo de Escarcha"));
    }

    @Test
    public void elMagoUsaRelampagoContraInmunesAlFuego() {
        Mago mago = new Mago("Merlin", "Hombre", 5, 30, 1);
        List<Aventurero> grupo = new ArrayList<>();
        grupo.add(mago);
        String turno = mago.simularCombate(Monstruo.crearDragonRojoJoven(), grupo);
        assertTrue(turno.contains("Relámpago"));
    }

    @Test
    public void elBardoCuraAUnAliadoMuyHerido() {
        Bardo bardo = new Bardo("Lyra", "Mujer", 3, 24, "Laud");
        Guerrero g = new Guerrero("Thorin", "Hombre", 3, 30, true);
        g.recibirDanio(25);
        List<Aventurero> grupo = new ArrayList<>();
        grupo.add(bardo);
        grupo.add(g);
        bardo.simularCombate(Monstruo.crearOgro(), grupo);
        assertTrue(g.getPuntosVida() > 5);
    }

    @Test
    public void descansoLargoRecuperaVidaYRecursos() {
        Mago mago = new Mago("Merlin", "Hombre", 5, 30, 1);
        List<Aventurero> grupo = new ArrayList<>();
        grupo.add(mago);
        mago.simularCombate(Monstruo.crearOgro(), grupo);
        mago.recibirDanio(20);
        mago.descansoLargo();
        assertEquals(30, mago.getPuntosVida());
        assertEquals(1, mago.getEspaciosConjuro());
    }
}
