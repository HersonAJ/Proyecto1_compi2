package com.example.contacto_3xtrat3r3str3.ui;

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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class EditorCodigo extends HBox {

    private final VBox panelNumeros;
    private final TextFlow capaTexto;
    private final TextArea areaEdicion;
    private final Font fuente;
    private final ScrollPane scrollPane;

    public EditorCodigo() {
        fuente = Font.font("Consolas", 14);

        // --- Panel de números de línea ---
        panelNumeros = new VBox();
        panelNumeros.setPadding(new Insets(8, 10, 8, 8));
        panelNumeros.setAlignment(Pos.TOP_RIGHT);
        panelNumeros.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");
        panelNumeros.setMinWidth(Region.USE_PREF_SIZE);

        // --- Capa de texto visible (TextFlow) ---
        capaTexto = new TextFlow();
        capaTexto.setPadding(new Insets(8));
        capaTexto.setMouseTransparent(true);
        capaTexto.setFocusTraversable(false);
        // Color de letra visible y fondo blanco explícito
        capaTexto.setStyle("-fx-background-color: white;");

        // --- Capa editable (TextArea transparente) ---
        areaEdicion = new TextArea();
        areaEdicion.setFont(fuente);
        areaEdicion.setPadding(new Insets(8));
        areaEdicion.setWrapText(false);
        areaEdicion.setStyle(
                "-fx-text-fill: #2E3A45;" + // Color de texto real pero transparente si se prefiere sobre el TextFlow
                        "-fx-background-color: transparent;" +
                        "-fx-control-inner-background: transparent;" +
                        "-fx-highlight-fill: rgba(0,120,215,0.35);" +
                        "-fx-highlight-text-fill: black;" +
                        "-fx-background-insets: 0;"
        );

        // StackPane que superpone TextFlow y TextArea para que compartan espacio exacto
        StackPane capaEdicion = new StackPane(capaTexto, areaEdicion);
        HBox.setHgrow(capaEdicion, Priority.ALWAYS);

        // Contenedor horizontal interno (Números + Área de edición)
        HBox contenedorInterno = new HBox(panelNumeros, capaEdicion);
        HBox.setHgrow(capaEdicion, Priority.ALWAYS);

        // --- ScrollPane que envuelve todo para dar scroll global y evitar desbordes ---
        scrollPane = new ScrollPane(contenedorInterno);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: white; -fx-box-border: transparent;");

        // Hacemos que el ScrollPane ocupe todo el espacio del HBox principal
        getChildren().add(scrollPane);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);

        // Sincronizar el scroll vertical del panel de números con el texto si fuera necesario,
        // o dejar que el ScrollPane general mueva ambos al estar dentro del mismo contenido.
        panelNumeros.translateYProperty().bind(scrollPane.vvalueProperty().multiply(0)); // Opcional si scroller los mueve juntos

        // Sincronizar texto y números de línea al escribir
        areaEdicion.textProperty().addListener((obs, viejo, nuevo) -> {
            actualizarTexto(nuevo);
            actualizarNumerosLinea(nuevo);
        });

        // Foco inicial
        Platform.runLater(areaEdicion::requestFocus);

        // Estado inicial
        actualizarTexto("");
        actualizarNumerosLinea("");
    }

    private void actualizarTexto(String texto) {
        capaTexto.getChildren().clear();
        // Si el texto termina en salto de línea, TextFlow a veces ignora la última línea vacía visualmente,
        // por lo que agregamos un caracter o manejamos el texto de forma segura.
        String textoSeguro = texto.endsWith("\n") ? texto + " " : texto;
        Text t = new Text(textoSeguro);
        t.setFont(fuente);
        t.setFill(javafx.scene.paint.Color.valueOf("#2E3A45")); // Color oscuro legible
        capaTexto.getChildren().add(t);
    }

    private void actualizarNumerosLinea(String texto) {
        int lineas = texto.isEmpty() ? 1 : texto.split("\n", -1).length;
        panelNumeros.getChildren().clear();
        for (int i = 1; i <= lineas; i++) {
            Label l = new Label(String.valueOf(i));
            l.setFont(fuente);
            l.setStyle("-fx-text-fill: #888; -fx-padding: 0 4 0 0;");
            panelNumeros.getChildren().add(l);
        }
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
}