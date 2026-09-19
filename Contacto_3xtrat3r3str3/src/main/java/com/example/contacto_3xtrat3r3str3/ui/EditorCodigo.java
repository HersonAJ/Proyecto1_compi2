package com.example.contacto_3xtrat3r3str3.ui;

import com.example.contacto_3xtrat3r3str3.ui.modelo.Lenguaje;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.File;

public class EditorCodigo extends HBox {

    private final VBox panelNumeros;
    private final TextFlow capaTexto;
    private final TextArea areaEdicion;
    private final Font fuente;
    private final ScrollPane scrollPane;
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

        fuente = Font.font("Consolas", 14);

        panelNumeros = new VBox();
        panelNumeros.setPadding(new Insets(8, 10, 8, 8));
        panelNumeros.setAlignment(Pos.TOP_RIGHT);
        panelNumeros.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");
        panelNumeros.setMinWidth(Region.USE_PREF_SIZE);

        capaTexto = new TextFlow();
        capaTexto.setPadding(new Insets(8));
        capaTexto.setMouseTransparent(true);
        capaTexto.setFocusTraversable(false);
        capaTexto.setStyle("-fx-background-color: transparent;");

        areaEdicion = new TextArea();
        areaEdicion.setFont(fuente);
        areaEdicion.setWrapText(false);
        areaEdicion.setStyle(
                "-fx-text-fill: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-control-inner-background: transparent;" +
                        "-fx-highlight-fill: rgba(0,120,215,0.35);" +
                        "-fx-highlight-text-fill: transparent;" +
                        "-fx-background-insets: 0;" +
                        "-fx-padding: 8;" +
                        "-fx-border-color: transparent;"
        );

        StackPane capaEdicion = new StackPane(areaEdicion, capaTexto);
        HBox.setHgrow(capaEdicion, Priority.ALWAYS);

        HBox contenedorInterno = new HBox(panelNumeros, capaEdicion);
        HBox.setHgrow(capaEdicion, Priority.ALWAYS);

        scrollPane = new ScrollPane(contenedorInterno);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-box-border: transparent;");

        getChildren().add(scrollPane);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);

        areaEdicion.textProperty().addListener((obs, viejo, nuevo) -> {
            actualizarTexto(nuevo);
            actualizarNumerosLinea(nuevo);
            if (!modificado) {
                modificado = true;
                if (onModificado != null) onModificado.run();
            }
        });

        Platform.runLater(areaEdicion::requestFocus);
        actualizarTexto("");
        actualizarNumerosLinea("");
    }

    private void actualizarTexto(String texto) {
        capaTexto.getChildren().clear();
        String textoSeguro = texto.endsWith("\n") ? texto + " " : texto;
        Text t = new Text(textoSeguro);
        t.setFont(fuente);
        t.setFill(Color.valueOf("#2E3A45"));
        capaTexto.getChildren().add(t);
    }

    private void actualizarNumerosLinea(String texto) {
        int lineas = texto.isEmpty() ? 1 : texto.split("\n", -1).length;
        panelNumeros.getChildren().clear();
        for (int i = 1; i <= lineas; i++) {
            Label l = new Label(String.valueOf(i));
            l.setFont(fuente);
            l.setStyle("-fx-text-fill: #888; -fx-padding: 0;");
            l.setMinHeight(19.01); // Altura exacta estimada modificar para ajustar las lineas y los numeros
            l.setPrefHeight(19.01);
            l.setAlignment(Pos.TOP_RIGHT);
            panelNumeros.getChildren().add(l);
        }
    }

    // ---------- API ----------

    public void cargarContenido(String contenido, File archivo) {
        this.archivoActual = archivo;
        if (archivo != null) {
            Lenguaje detectado = Lenguaje.porExtension(archivo.getName());
            if (detectado.esConocido()) {
                this.lenguaje = detectado;
            }
        }
        areaEdicion.setText(contenido);
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
            Lenguaje detectado = Lenguaje.porExtension(archivoActual.getName());
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

    public TextArea getAreaEdicion() {
        return areaEdicion;
    }

    public String getTexto() {
        return areaEdicion.getText();
    }

    public void setTexto(String texto) {
        areaEdicion.setText(texto);
    }

    public void pedirFoco() {
        Platform.runLater(areaEdicion::requestFocus);
    }
}