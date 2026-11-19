package modelo;

/**
 * excepcion propia que se lanza cuando el usuario ingresa datos invalidos.
 */
public class DatosInvalidosException extends Exception {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
