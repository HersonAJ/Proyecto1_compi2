package com.example.contacto_3xtrat3r3str3.ui;

import com.example.contacto_3xtrat3r3str3.ui.modelo.Lenguaje;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class VentanaPrincipal {

    private final BorderPane raiz;
    private final Stage stage;
    private final ArbolTrabajo arbol;
    private final PanelEditores panelEditores;
    private final MenuPrincipal menu;
    private final PanelSalida panelSalida;

    public VentanaPrincipal(Stage stage) {
        this.stage = stage;
        raiz = new BorderPane();

        // --- Menú ---
        menu = new MenuPrincipal(stage);
        raiz.setTop(menu.getBarra());

        // --- Árbol ---
        arbol = new ArbolTrabajo();
        arbol.setPrefWidth(260);
        arbol.setOnArchivoSeleccionado(this::abrirArchivoEnEditor);
        raiz.setLeft(arbol);

        // --- Centro: pestañas + salida ---
        panelEditores = new PanelEditores();
        panelSalida = new PanelSalida();

        SplitPane splitCentral = new SplitPane(panelEditores, panelSalida);
        splitCentral.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitCentral.setDividerPositions(0.75);
        SplitPane.setResizableWithParent(panelEditores, true);
        SplitPane.setResizableWithParent(panelSalida, true);
        raiz.setCenter(splitCentral);

        // --- Barra de estado ---
        Region barraEstado = placeholder("Barra de estado");
        barraEstado.setPrefHeight(24);
        raiz.setBottom(barraEstado);

        // --- Conectar callbacks del menú ---
        menu.onNuevoArchivo     = this::accionNuevoArchivo;
        menu.onAbrirArchivo     = this::accionAbrirArchivo;
        menu.onAbrirCarpeta     = this::accionAbrirCarpeta;
        menu.onGuardar          = this::accionGuardar;
        menu.onGuardarComo      = this::accionGuardarComo;
        menu.onDescargarArchivo = this::accionDescargarArchivo;
        menu.onDescargarCarpeta = this::accionDescargarCarpeta;
        menu.onCompilar         = this::accionCompilar;
        menu.onSalir            = () -> stage.close();

        // --- Atajos de teclado a nivel de escena ---
        raiz.sceneProperty().addListener((obs, vieja, nueva) -> {
            if (nueva != null) {
                nueva.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                        this::accionGuardar);
                nueva.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN),
                        this::accionGuardarComo);
                nueva.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                        this::accionNuevoArchivo);
                nueva.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN),
                        this::accionAbrirArchivo);
                nueva.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN),
                        this::accionAbrirCarpeta);
                nueva.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.F5),
                        this::accionCompilar);
                nueva.getAccelerators().put(
                        new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN),
                        () -> stage.close());
            }
        });
    }

    public BorderPane getRaiz() { return raiz; }
    public PanelSalida getPanelSalida() { return panelSalida; }

    // ---------- Acciones ----------

    private void accionAbrirArchivo() {
        GestorArchivos.dialogoAbrirArchivo(stage)
                .ifPresent(this::abrirArchivoEnEditor);
    }

    private void accionAbrirCarpeta() {
        GestorArchivos.dialogoAbrirCarpeta(stage)
                .ifPresent(arbol::abrirCarpeta);
    }

    private void abrirArchivoEnEditor(File archivo) {
        GestorArchivos.leerArchivo(archivo).ifPresent(contenido ->
                panelEditores.abrirArchivo(archivo, contenido));
    }

    private void accionGuardar() {
        Optional<EditorCodigo> editorOpt = panelEditores.editorActivo();
        if (editorOpt.isEmpty()) {
            panelSalida.imprimirConsola("No hay pestaña activa.");
            return;
        }
        EditorCodigo editor = editorOpt.get();
        File archivo = editor.getArchivoActual();
        if (archivo == null) {
            accionGuardarComo();
            return;
        }
        if (GestorArchivos.escribirArchivo(archivo, editor.getTexto())) {
            panelEditores.marcarGuardado(editor);
            panelSalida.imprimirConsola("Guardado: " + archivo.getAbsolutePath());
        } else {
            panelSalida.imprimirError("No se pudo guardar el archivo.");
        }
        arbol.refrescar();
    }

    private void accionGuardarComo() {
        Optional<EditorCodigo> editorOpt = panelEditores.editorActivo();
        if (editorOpt.isEmpty()) {
            panelSalida.imprimirConsola("No hay pestaña activa.");
            return;
        }
        EditorCodigo editor = editorOpt.get();

        String sugerido = "nuevo.y";
        Tab tabActiva = panelEditores.getSelectionModel().getSelectedItem();
        if (tabActiva != null) {
            sugerido = tabActiva.getText().replaceFirst("^\\* ", "");
        } else if (editor.getArchivoActual() != null) {
            sugerido = editor.getArchivoActual().getName();
        }

        File carpetaProyecto = null;
        if (arbol.getRoot() != null) {
            File raizItem = arbol.getRoot().getValue();
            if (raizItem != null) {
                carpetaProyecto = raizItem.isDirectory() ? raizItem : raizItem.getParentFile();
            }
        }

        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Guardar como...");
        fileChooser.setInitialFileName(sugerido);

        if (carpetaProyecto != null && carpetaProyecto.exists()) {
            fileChooser.setInitialDirectory(carpetaProyecto);
        }

        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Archivos Y (*.y)", "*.y"),
                new javafx.stage.FileChooser.ExtensionFilter("Archivos Zetariano (*.z)", "*.z"),
                new javafx.stage.FileChooser.ExtensionFilter("Archivos Pig Latin (*.pig)", "*.pig"),
                new javafx.stage.FileChooser.ExtensionFilter("Todos los archivos", "*.*")
        );

        File destino = fileChooser.showSaveDialog(stage);
        if (destino != null) {
            if (GestorArchivos.escribirArchivo(destino, editor.getTexto())) {
                editor.setArchivoActual(destino);
                panelEditores.actualizarArchivoGuardado(editor, destino);
                panelSalida.imprimirConsola("Guardado como: " + destino.getAbsolutePath());
                arbol.refrescar();
            } else {
                panelSalida.imprimirError("No se pudo guardar el archivo.");
            }
        }
    }

    private void accionDescargarArchivo() {
        Optional<EditorCodigo> editorOpt = panelEditores.editorActivo();
        if (editorOpt.isEmpty()) return;
        EditorCodigo editor = editorOpt.get();
        String sugerido = editor.getArchivoActual() != null
                ? editor.getArchivoActual().getName()
                : "archivo.txt";
        GestorArchivos.dialogoDescargarArchivo(stage, sugerido).ifPresent(destino -> {
            File origen = editor.getArchivoActual();
            if (origen != null) {
                GestorArchivos.copiarArchivo(origen, destino);
            } else {
                GestorArchivos.escribirArchivo(destino, editor.getTexto());
            }
        });
    }

    private void accionDescargarCarpeta() {
        var itemRaiz = arbol.getRoot();
        if (itemRaiz == null) return;
        File origen = itemRaiz.getValue();
        if (origen == null || !origen.isDirectory()) return;
        GestorArchivos.dialogoDescargarCarpeta(stage).ifPresent(destino -> {
            File destinoReal = new File(destino, origen.getName());
            GestorArchivos.copiarCarpeta(origen, destinoReal);
        });
    }

    private void accionNuevoArchivo() {
        java.util.List<Lenguaje> lenguajesDisponibles =
                java.util.List.of(Lenguaje.Y, Lenguaje.ZETARIANO, Lenguaje.PIG_LATIN);
        javafx.scene.control.ChoiceDialog<Lenguaje> dialog =
                new javafx.scene.control.ChoiceDialog<>(Lenguaje.Y, lenguajesDisponibles);
        dialog.setTitle("Nuevo archivo");
        dialog.setHeaderText("Selecciona el lenguaje del nuevo archivo");
        dialog.setContentText("Lenguaje:");

        dialog.showAndWait().ifPresent(lenguajeSeleccionado -> {
            panelEditores.nuevoArchivoVacio(lenguajeSeleccionado);
        });
    }

    private void accionCompilar() {
        Optional<EditorCodigo> editorOpt = panelEditores.editorActivo();
        if (editorOpt.isEmpty()) {
            panelSalida.imprimirError("No hay pestaña activa.");
            return;
        }
        EditorCodigo editor = editorOpt.get();

        if (editor.getArchivoActual() == null) {
            panelSalida.imprimirError("El archivo no está guardado. Guárdalo (Ctrl+S) antes de analizar.");
            return;
        }

        Lenguaje lenguaje = editor.getLenguaje();
        if (!lenguaje.esConocido()) {
            panelSalida.imprimirError("No se pudo determinar el lenguaje del archivo.");
            return;
        }

        String texto = editor.getTexto();
        panelSalida.imprimirConsola("Analizando " + editor.getArchivoActual().getName()
                + " como " + lenguaje.getNombreVisible()
                + " (" + texto.length() + " caracteres)");

        // Aquí irá el pipeline ANTLR.
    }

    private static Region placeholder(String texto) {
        Label l = new Label(texto);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setMaxHeight(Double.MAX_VALUE);
        l.setStyle("-fx-alignment: center; -fx-text-fill: #888; -fx-border-color: #ccc;");
        return l;
    }
}