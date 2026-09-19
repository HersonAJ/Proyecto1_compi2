package com.example.contacto_3xtrat3r3str3.ui;

import com.example.contacto_3xtrat3r3str3.ui.modelo.Lenguaje;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.File;

public class EditorCodigo extends HBox {

    private final CodeArea areaEdicion;

    private File archivoActual;
    private Lenguaje lenguaje = Lenguaje.DESCONOCIDO;
    private boolean modificado = false;
    private Runnable onModificado;

    public EditorCodigo() {
        this(Lenguaje.DESCONOCIDO);
    }

    public EditorCodigo(Lenguaje lenguajeInicial) {

        if (lenguajeInicial != null) {
            this.lenguaje = lenguajeInicial;
        }

        // Editor de código
        areaEdicion = new CodeArea();

        // Fuente y apariencia del editor
        areaEdicion.setStyle(
                "-fx-font-family: 'Consolas';" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-color: white;"
        );

        // Números de línea sincronizados con los párrafos
        areaEdicion.setParagraphGraphicFactory(
                LineNumberFactory.get(areaEdicion)
        );

        areaEdicion.setWrapText(false);
        HBox.setHgrow(areaEdicion, Priority.ALWAYS);

        getChildren().add(areaEdicion);
        areaEdicion.textProperty().addListener(
                (obs, viejo, nuevo) -> {
                    if (!modificado) {
                        modificado = true;

                        if (onModificado != null) {
                            onModificado.run();
                        }
                    }
                }
        );

        Platform.runLater(areaEdicion::requestFocus);
    }

    // ---------- API ----------

    public void cargarContenido(String contenido, File archivo) {

        this.archivoActual = archivo;

        if (archivo != null) {
            Lenguaje detectado =
                    Lenguaje.porExtension(archivo.getName());

            if (detectado.esConocido()) {
                this.lenguaje = detectado;
            }
        }

        areaEdicion.replaceText(
                contenido != null ? contenido : ""
        );

        // Cargar un archivo no debe dejarlo marcado como modificado
        modificado = false;
    }

    public void marcarComoGuardado() {
        this.modificado = false;
    }

    public boolean estaModificado() {
        return modificado;
    }

    public File getArchivoActual() {
        return archivoActual;
    }

    public void setArchivoActual(File archivoActual) {

        this.archivoActual = archivoActual;

        if (archivoActual != null) {
            Lenguaje detectado =
                    Lenguaje.porExtension(archivoActual.getName());

            if (detectado.esConocido()) {
                this.lenguaje = detectado;
            }
        }
    }

    public Lenguaje getLenguaje() {
        return lenguaje;
    }

    public void setLenguaje(Lenguaje lenguaje) {
        if (lenguaje != null) {
            this.lenguaje = lenguaje;
        }
    }

    public void setOnModificado(Runnable callback) {
        this.onModificado = callback;
    }

    public CodeArea getAreaEdicion() {
        return areaEdicion;
    }

    public String getTexto() {
        return areaEdicion.getText();
    }

    public void setTexto(String texto) {
        areaEdicion.replaceText(texto != null ? texto : "");
    }

    public void pedirFoco() {
        Platform.runLater(areaEdicion::requestFocus);
    }
}