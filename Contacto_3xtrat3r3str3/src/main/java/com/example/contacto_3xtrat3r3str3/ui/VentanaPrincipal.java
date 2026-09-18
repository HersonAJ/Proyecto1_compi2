package com.example.contacto_3xtrat3r3str3.ui;

import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class VentanaPrincipal {

    private final BorderPane raiz;

    public VentanaPrincipal(Stage stage) {
        raiz = new BorderPane();

        // --- Top: menú ---
        MenuPrincipal menu = new MenuPrincipal(stage);
        raiz.setTop(menu.getBarra());

        // --- Left: árbol de trabajo (placeholder por ahora) ---
        Region arbol = placeholder("Árbol de trabajo");
        arbol.setPrefWidth(260);
        raiz.setLeft(arbol);

// --- Center: editor + panel de salida ---
        EditorCodigo editor = new EditorCodigo();
        Region salida = placeholder("Panel de salida (consola / errores / cuartetas)");
        salida.setMinHeight(80); // Evita que colapse por completo

        SplitPane splitCentral = new SplitPane(editor, salida);
        splitCentral.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitCentral.setDividerPositions(0.75);

// Asegurar que el editor se expanda correctamente dentro del SplitPane
        SplitPane.setResizableWithParent(editor, true);
        SplitPane.setResizableWithParent(salida, true);

        raiz.setCenter(splitCentral);

        // --- Bottom: barra de estado ---
        Region barraEstado = placeholder("Barra de estado");
        barraEstado.setPrefHeight(24);
        raiz.setBottom(barraEstado);
    }

    public BorderPane getRaiz() {
        return raiz;
    }

    private static Region placeholder(String texto) {
        Label l = new Label(texto);
        l.setMaxWidth(Double.MAX_VALUE);
        l.setMaxHeight(Double.MAX_VALUE);
        l.setStyle("-fx-alignment: center; -fx-text-fill: #888; -fx-border-color: #ccc;");
        return l;
    }
}