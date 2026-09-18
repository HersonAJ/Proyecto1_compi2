package com.example.contacto_3xtrat3r3str3.ui;

import com.example.contacto_3xtrat3r3str3.ui.modelo.Lenguaje;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
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
        Region salida = placeholder("Panel de salida (consola / errores / cuartetas)");
        salida.setMinHeight(80);

        SplitPane splitCentral = new SplitPane(panelEditores, salida);
        splitCentral.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitCentral.setDividerPositions(0.75);
        SplitPane.setResizableWithParent(panelEditores, true);
        SplitPane.setResizableWithParent(salida, true);
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
    }

    public BorderPane getRaiz() { return raiz; }

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
        if (editorOpt.isEmpty()) return;
        EditorCodigo editor = editorOpt.get();
        File archivo = editor.getArchivoActual();
        if (archivo == null) {
            accionGuardarComo();
            return;
        }
        if (GestorArchivos.escribirArchivo(archivo, editor.getTexto())) {
            panelEditores.marcarGuardado(editor);
        }
        arbol.refrescar();
    }

    private void accionGuardarComo() {
        Optional<EditorCodigo> editorOpt = panelEditores.editorActivo();
        if (editorOpt.isEmpty()) return;
        EditorCodigo editor = editorOpt.get();

        // 1. Obtener el nombre sugerido dinámicamente desde la pestaña activa (respeta .y, .z, .pig, etc.)
        String sugerido = "nuevo.y";
        Tab tabActiva = panelEditores.getSelectionModel().getSelectedItem();
        if (tabActiva != null) {
            // Quitamos el asterisco de modificado si lo tuviera (ej: "* nuevo.z" -> "nuevo.z")
            sugerido = tabActiva.getText().replaceFirst("^\\* ", "");
        } else if (editor.getArchivoActual() != null) {
            sugerido = editor.getArchivoActual().getName();
        }

        // Obtener la carpeta raíz actual desde el árbol de trabajo
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

        // Asignar la carpeta del proyecto por defecto si existe
        if (carpetaProyecto != null && carpetaProyecto.exists()) {
            fileChooser.setInitialDirectory(carpetaProyecto);
        }

        // Filtros de extensión para los lenguajes
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

                // Actualizamos la pestaña y la vinculamos al archivo físico en el mapa
                panelEditores.actualizarArchivoGuardado(editor, destino);

                // Refrescamos el árbol de trabajo para que aparezca inmediatamente
                arbol.refrescar();
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
        // Descarga la carpeta raíz del árbol, si hay una abierta
        var itemRaiz = arbol.getRoot();
        if (itemRaiz == null) return;
        File origen = itemRaiz.getValue();
        if (origen == null || !origen.isDirectory()) return;
        GestorArchivos.dialogoDescargarCarpeta(stage).ifPresent(destino -> {
            File destinoReal = new File(destino, origen.getName());
            GestorArchivos.copiarCarpeta(origen, destinoReal);
        });
    }

    private static Region placeholder(String texto) {
        Label l = new Label(texto);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setMaxHeight(Double.MAX_VALUE);
        l.setStyle("-fx-alignment: center; -fx-text-fill: #888; -fx-border-color: #ccc;");
        return l;
    }

    private void accionNuevoArchivo() {
        java.util.List<Lenguaje> lenguajesDisponibles = java.util.List.of(Lenguaje.Y, Lenguaje.ZETARIANO, Lenguaje.PIG_LATIN);
        javafx.scene.control.ChoiceDialog<Lenguaje> dialog = new javafx.scene.control.ChoiceDialog<>(Lenguaje.Y, lenguajesDisponibles);
        dialog.setTitle("Nuevo archivo");
        dialog.setHeaderText("Selecciona el lenguaje del nuevo archivo");
        dialog.setContentText("Lenguaje:");

        dialog.showAndWait().ifPresent(lenguajeSeleccionado -> {
            panelEditores.nuevoArchivoVacio(lenguajeSeleccionado);
        });
    }
}