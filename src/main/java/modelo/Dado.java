package modelo;

import java.util.Random;

/**
 * Simula las tiradas de dados de D&D 5e (d4, d6, d8, d10, d12, d20).
 */
public class Dado {

    private static Random random = new Random();

    // SOBRECARGA 1: tira un solo dado. Ej: tirar(20) = 1d20
    public static int tirar(int caras) {
        return random.nextInt(caras) + 1;
    }

    // SOBRECARGA 2: tira varios dados y los suma. Ej: tirar(8, 6) = 8d6
    public static int tirar(int cantidad, int caras) {
        int total = 0;
        for (int i = 0; i < cantidad; i++) {
            total += tirar(caras);
        }
        return total;
    }

    // Ventaja en 5e: se tiran 2d20 y se queda el mayor
    public static int tirarConVentaja() {
        return Math.max(tirar(20), tirar(20));
    }

    // Permite repetir los mismos resultados (sirve para las pruebas JUnit)
    public static void setSemilla(long semilla) {
        random = new Random(semilla);
    }
}
