package com.example.contacto_3xtrat3r3str3.ui;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;

public class MenuPrincipal {

    private final MenuBar barra;

    public MenuPrincipal(Stage stage) {
        barra = new MenuBar();

        // --- Archivo ---
        Menu archivo = new Menu("Archivo");
        archivo.getItems().addAll(
                item("Nuevo archivo"),
                item("Abrir archivo..."),
                item("Abrir carpeta..."),
                new SeparatorMenuItem(),
                item("Guardar"),
                item("Guardar como..."),
                new SeparatorMenuItem(),
                item("Descargar archivo..."),
                item("Descargar carpeta..."),
                new SeparatorMenuItem(),
                item("Salir", e -> stage.close())
        );

        // --- Editar ---
        Menu editar = new Menu("Editar");
        editar.getItems().addAll(
                item("Deshacer"),
                item("Rehacer"),
                new SeparatorMenuItem(),
                item("Cortar"),
                item("Copiar"),
                item("Pegar")
        );

        // --- Ejecutar ---
        Menu ejecutar = new Menu("Ejecutar");
        ejecutar.getItems().addAll(
                item("Compilar"),
                item("Generar cuartetas"),
                item("Generar C3D"),
                item("Traducir a C")
        );

        // --- Ayuda ---
        Menu ayuda = new Menu("Ayuda");
        ayuda.getItems().addAll(
                item("Manual de usuario"),
                item("Documentación técnica"),
                item("Acerca de...")
        );

        barra.getMenus().addAll(archivo, editar, ejecutar, ayuda);
    }

    public MenuBar getBarra() {
        return barra;
    }

    private static MenuItem item(String texto) {
        return new MenuItem(texto);
    }

    private static MenuItem item(String texto, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        MenuItem mi = new MenuItem(texto);
        mi.setOnAction(handler);
        return mi;
    }
}