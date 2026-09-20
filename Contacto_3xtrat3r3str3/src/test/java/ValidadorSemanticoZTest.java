import com.example.contacto_3xtrat3r3str3.y.semantica.error.ErrorSemantico;
import com.example.contacto_3xtrat3r3str3.zetariano.nodo.*;
import com.example.contacto_3xtrat3r3str3.zetariano.semantica.ValidadorSemanticoZ;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorSemanticoZTest {

    @Test
    void noReportaErroresConLaClasePersonaDelEnunciado() {
        // public class Persona {
        //     String nombre;
        //     int edad;
        //
        //     public Persona(String nombreParametro, int edadParametro) {
        //         nombre = nombreParametro;
        //         edad = edadParametro;
        //     }
        //
        //     public Persona() {
        //         nombre = "Sin nombre";
        //         edad = 0;
        //     }
        //
        //     public void saludar() {
        //         println("Hola! Me llamo " + nombre + " y tengo " + edad + " anios.");
        //     }
        //
        //     public int calcularAnioNacimiento(int anioActual) {
        //         return anioActual - edad;
        //     }
        // }

        NodoAtributo atrNombre = new NodoAtributo(2, 4, "String", "nombre");
        NodoAtributo atrEdad = new NodoAtributo(3, 4, "int", "edad");

        // --- constructor con parametros ---
        NodoParametro paramNombre = new NodoParametro(6, 20, "String", "nombreParametro");
        NodoParametro paramEdad = new NodoParametro(6, 40, "int", "edadParametro");

        NodoSentencia asignaNombre = new NodoSentencia.Asignacion(7, 8, "=",
                new NodoExpr.Identificador(7, 8, "nombre"),
                new NodoExpr.Identificador(7, 17, "nombreParametro"));
        NodoSentencia asignaEdad = new NodoSentencia.Asignacion(8, 8, "=",
                new NodoExpr.Identificador(8, 8, "edad"),
                new NodoExpr.Identificador(8, 15, "edadParametro"));

        NodoConstructor constructorConParametros = new NodoConstructor(6, 4, "Persona",
                List.of(paramNombre, paramEdad), List.of(asignaNombre, asignaEdad));

        // --- constructor sin parametros (sobrecarga) ---
        NodoSentencia asignaNombreDefault = new NodoSentencia.Asignacion(12, 8, "=",
                new NodoExpr.Identificador(12, 8, "nombre"),
                new NodoExpr.LiteralCadena(12, 17, "Sin nombre"));
        NodoSentencia asignaEdadDefault = new NodoSentencia.Asignacion(13, 8, "=",
                new NodoExpr.Identificador(13, 8, "edad"),
                new NodoExpr.LiteralEntero(13, 15, 0));

        NodoConstructor constructorVacio = new NodoConstructor(11, 4, "Persona",
                List.of(), List.of(asignaNombreDefault, asignaEdadDefault));

        // --- metodo saludar() ---
        NodoExpr concatenacion = new NodoExpr.Binaria(17, 16, "+",
                new NodoExpr.Binaria(17, 16, "+",
                        new NodoExpr.Binaria(17, 16, "+",
                                new NodoExpr.LiteralCadena(17, 16, "Hola! Me llamo "),
                                new NodoExpr.Identificador(17, 34, "nombre")),
                        new NodoExpr.LiteralCadena(17, 43, " y tengo ")),
                new NodoExpr.Identificador(17, 55, "edad"));

        NodoSentencia imprimirSaludo = new NodoSentencia.Imprimir(17, 8, true, concatenacion);
        NodoMetodo saludar = new NodoMetodo(16, 4, "saludar", List.of(), null, List.of(imprimirSaludo));

        // --- metodo calcularAnioNacimiento(int anioActual) -> int ---
        NodoParametro paramAnioActual = new NodoParametro(20, 40, "int", "anioActual");
        NodoExpr resta = new NodoExpr.Binaria(21, 15, "-",
                new NodoExpr.Identificador(21, 15, "anioActual"),
                new NodoExpr.Identificador(21, 28, "edad"));
        NodoSentencia retorno = new NodoSentencia.Retorno(21, 8, resta);

        NodoMetodo calcularAnio = new NodoMetodo(20, 4, "calcularAnioNacimiento",
                List.of(paramAnioActual), "int", List.of(retorno));

        NodoClase persona = new NodoClase(1, 0, "Persona",
                List.of(atrNombre, atrEdad),
                List.of(constructorConParametros, constructorVacio),
                List.of(saludar, calcularAnio));

        NodoPrograma programa = new NodoPrograma(1, 0, persona);

        ValidadorSemanticoZ validador = new ValidadorSemanticoZ();
        List<ErrorSemantico> errores = validador.analizar(programa);

        errores.forEach(System.out::println);
        assertTrue(errores.isEmpty(), "No se esperaban errores, se obtuvo: " + errores);
    }

    @Test
    void detectaConstructorDuplicadoYAtributoInexistente() {
        // public class Vacia {
        //     public Vacia() { }
        //     public Vacia() { }              <- firma identica, debe reportar error
        //
        //     public void metodo() {
        //         println(algoQueNoExiste);   <- ni variable ni atributo, debe reportar error
        //     }
        // }

        NodoConstructor c1 = new NodoConstructor(2, 4, "Vacia", List.of(), List.of());
        NodoConstructor c2 = new NodoConstructor(3, 4, "Vacia", List.of(), List.of());

        NodoSentencia imprimir = new NodoSentencia.Imprimir(6, 8, true,
                new NodoExpr.Identificador(6, 17, "algoQueNoExiste"));
        NodoMetodo metodo = new NodoMetodo(5, 4, "metodo", List.of(), null, List.of(imprimir));

        NodoClase vacia = new NodoClase(1, 0, "Vacia", List.of(), List.of(c1, c2), List.of(metodo));
        NodoPrograma programa = new NodoPrograma(1, 0, vacia);

        ValidadorSemanticoZ validador = new ValidadorSemanticoZ();
        List<ErrorSemantico> errores = validador.analizar(programa);

        errores.forEach(System.out::println);

        assertEquals(2, errores.size(), "Se esperaban exactamente 2 errores: " + errores);
        assertTrue(errores.stream().anyMatch(e -> e.categoria().equals("Declaracion duplicada") && e.linea() == 3));
        assertTrue(errores.stream().anyMatch(e -> e.categoria().equals("Identificador no declarado") && e.linea() == 6));
    }
}