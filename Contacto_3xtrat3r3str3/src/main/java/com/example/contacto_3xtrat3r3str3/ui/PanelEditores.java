package com.example.contacto_3xtrat3r3str3.ui;

import com.example.contacto_3xtrat3r3str3.ui.modelo.Lenguaje;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PanelEditores extends TabPane {

    private final Map<File, Tab> tabsPorArchivo = new HashMap<>();

    public PanelEditores() {
        setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
    }

    public EditorCodigo abrirArchivo(File archivo, String contenido) {
        // Si ya está abierto, lo seleccionamos
        Tab existente = tabsPorArchivo.get(archivo);
        if (existente != null) {
            getSelectionModel().select(existente);
            return (EditorCodigo) existente.getUserData();
        }

        EditorCodigo editor = new EditorCodigo();
        editor.cargarContenido(contenido, archivo);

        Tab tab = new Tab(archivo.getName(), editor);
        tab.setUserData(editor);

        // Referencia directa y limpia a la pestaña creada
        editor.setOnModificado(() -> marcarModificado(tab));

        tab.setOnCloseRequest(e -> {
            if (editor.estaModificado()) {
                // Aquí podrías preguntar "¿guardar cambios?"
                // Por ahora solo dejamos cerrar.
            }
            tabsPorArchivo.remove(archivo);
        });

        tabsPorArchivo.put(archivo, tab);
        getTabs().add(tab);
        getSelectionModel().select(tab);
        return editor;
    }

    private void marcarModificado(Tab tab) {
        if (tab == null) return;
        if (!tab.getText().startsWith("* ")) {
            tab.setText("* " + tab.getText());
        }
    }

    public Optional<EditorCodigo> editorActivo() {
        Tab t = getSelectionModel().getSelectedItem();
        if (t == null) return Optional.empty();
        return Optional.of((EditorCodigo) t.getUserData());
    }

    public void marcarGuardado(EditorCodigo editor) {
        editor.marcarComoGuardado();
        for (Tab t : getTabs()) {
            if (t.getUserData() == editor) {
                String nombre = t.getText().replaceFirst("^\\* ", "");
                t.setText(nombre);
                break;
            }
        }
    }

    public void cerrarArchivo(File archivo) {
        Tab t = tabsPorArchivo.remove(archivo);
        if (t != null) getTabs().remove(t);
    }

    public void nuevoArchivoVacio(Lenguaje lenguaje) {
        EditorCodigo nuevoEditor = new EditorCodigo();

        // Si el lenguaje por alguna razón viene nulo, evitamos el fallo usando ".y" por defecto
        String ext = (lenguaje != null) ? lenguaje.getExtension() : ".y";
        String nombreSugerido = "nuevo" + ext;

        Tab nuevaPestana = new Tab(nombreSugerido);
        nuevaPestana.setContent(nuevoEditor);
        nuevaPestana.setUserData(nuevoEditor);

        getTabs().add(nuevaPestana);
        getSelectionModel().select(nuevaPestana);
    }
    public void actualizarArchivoGuardado(EditorCodigo editor, File nuevoArchivo) {
        editor.marcarComoGuardado();
        for (Tab t : getTabs()) {
            if (t.getUserData() == editor) {
                // 1. Cambiamos el texto de la pestaña por el nombre real del archivo
                t.setText(nuevoArchivo.getName());

                // 2. Registramos el archivo en el mapa para futuras referencias (evita duplicados si se vuelve a abrir)
                tabsPorArchivo.put(nuevoArchivo, t);
                break;
            }
        }
    }
}