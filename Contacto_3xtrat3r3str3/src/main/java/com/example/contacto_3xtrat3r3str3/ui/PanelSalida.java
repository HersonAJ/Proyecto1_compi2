package com.example.contacto_3xtrat3r3str3.ui;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;

public class PanelSalida extends TabPane {

    private final TextArea txtConsola;
    private final TextArea txtErrores;
    private final TextArea txtCuartetas;

    public PanelSalida() {
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        // 1. Pestaña de Consola / Logs de ejecución
        txtConsola = crearAreaTerminal();
        Tab tabConsola = new Tab("Consola", txtConsola);

        // 2. Pestaña de Errores (Léxicos, sintácticos, semánticos)
        txtErrores = crearAreaTerminal();
        Tab tabErrores = new Tab("Errores", txtErrores);

        // 3. Pestaña para Cuartetas / Código de Tres Direcciones
        txtCuartetas = crearAreaTerminal();
        Tab tabCuartetas = new Tab("Cuartetas / C3D", txtCuartetas);

        getTabs().addAll(tabConsola, tabErrores, tabCuartetas);
    }

    private TextArea crearAreaTerminal() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setFont(Font.font("Consolas", 13));
        // Estilo oscuro tipo terminal (estilo VS Code / IDEs modernos)
        area.setStyle(
                "-fx-control-inner-background: #1e1e1e;" +
                        "-fx-text-fill: #d4d4d4;" +
                        "-fx-highlight-fill: #264f78;" +
                        "-fx-highlight-text-fill: #ffffff;" +
                        "-fx-border-color: transparent;"
        );
        return area;
    }

    public void limpiarTodo() {
        txtConsola.clear();
        txtErrores.clear();
        txtCuartetas.clear();
    }

    public void imprimirConsola(String mensaje) {
        txtConsola.appendText(mensaje + "\n");
    }

    public void imprimirError(String error) {
        txtErrores.appendText(error + "\n");
        // Opcional: salta automáticamente a la pestaña de errores si ocurre uno
        getSelectionModel().select(1);
    }

    public void mostrarCuartetas(String cuartetas) {
        txtCuartetas.setText(cuartetas);
    }
}