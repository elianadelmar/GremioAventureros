package modelo;

import java.util.List;

/**
 * Obliga a todo aventurero a implementar su mecanica de combate.
 */
public interface IAccionCombate {

    /**
     * Ejecuta el turno del aventurero contra el monstruo.
     *
     * @param objetivo el monstruo enemigo
     * @param grupo    todos los aventureros del combate (aliados)
     * @return el texto de lo que paso en el turno
     */
    String simularCombate(Monstruo objetivo, List<Aventurero> grupo);
}
