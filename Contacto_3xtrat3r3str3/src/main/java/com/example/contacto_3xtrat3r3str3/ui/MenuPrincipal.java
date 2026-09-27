package com.example.contacto_3xtrat3r3str3.ui;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;

public class MenuPrincipal {

    private final MenuBar barra;

    // Callbacks que la VentanaPrincipal conectará
    public Runnable onNuevoArchivo;
    public Runnable onAbrirArchivo;
    public Runnable onAbrirCarpeta;
    public Runnable onGuardar;
    public Runnable onGuardarComo;
    public Runnable onDescargarArchivo;
    public Runnable onDescargarCarpeta;
    public Runnable onSalir;
    public Runnable onAnalizar;
    public Runnable onCompilar;

    public MenuPrincipal(Stage stage) {
        barra = new MenuBar();

        // -------- Archivo --------
        Menu archivo = new Menu("Archivo");
        archivo.getItems().addAll(
                item("Nuevo archivo",         () -> ejecutar(onNuevoArchivo)),
                item("Abrir archivo...",      () -> ejecutar(onAbrirArchivo)),
                item("Abrir carpeta...",      () -> ejecutar(onAbrirCarpeta)),
                new SeparatorMenuItem(),
                item("Guardar",               () -> ejecutar(onGuardar)),
                item("Guardar como...",       () -> ejecutar(onGuardarComo)),
                new SeparatorMenuItem(),
                item("Descargar archivo...",  () -> ejecutar(onDescargarArchivo)),
                item("Descargar carpeta...",  () -> ejecutar(onDescargarCarpeta)),
                new SeparatorMenuItem(),
                item("Salir",                 () -> {
                    if (onSalir != null) onSalir.run();
                    else stage.close();
                })
        );

        // -------- Editar --------
        Menu editar = new Menu("Editar");
        editar.getItems().addAll(
                item("Deshacer", null),
                item("Rehacer",  null),
                new SeparatorMenuItem(),
                item("Cortar",   null),
                item("Copiar",   null),
                item("Pegar",    null)
        );

        // -------- Ejecutar --------
        Menu ejecutar = new Menu("Ejecutar");
        ejecutar.getItems().addAll(
                item("Analizar",  () -> ejecutar(onAnalizar)),
                item("Compilar",  () -> ejecutar(onCompilar))
        );

        // -------- Ayuda --------
        Menu ayuda = new Menu("Ayuda");
        ayuda.getItems().addAll(
                item("Manual de usuario",     null),
                item("Documentación técnica", null),
                new SeparatorMenuItem(),
                item("Acerca de...",          null)
        );

        barra.getMenus().addAll(archivo, editar, ejecutar, ayuda);
    }

    public MenuBar getBarra() { return barra; }

    private void ejecutar(Runnable r) { if (r != null) r.run(); }

    private static MenuItem item(String texto, Runnable accion) {
        MenuItem mi = new MenuItem(texto);
        if (accion != null) mi.setOnAction(e -> accion.run());
        return mi;
    }
}