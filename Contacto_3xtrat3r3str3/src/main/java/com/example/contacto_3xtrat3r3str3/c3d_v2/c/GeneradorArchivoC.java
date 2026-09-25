package com.example.contacto_3xtrat3r3str3.c3d_v2.c;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Escribe un archivo .c y lo compila con gcc.
 * Mismo enfoque que el proyecto PigLatin: guarda en la raíz del proyecto
 * y lanza gcc con ProcessBuilder.
 */
public class GeneradorArchivoC {

    private final String nombreArchivo;
    private final String nombreEjecutable;

    public GeneradorArchivoC() {
        this("programa.c", "programa");
    }

    public GeneradorArchivoC(String nombreArchivo, String nombreEjecutable) {
        this.nombreArchivo = nombreArchivo;
        this.nombreEjecutable = nombreEjecutable;
    }

    /**
     * Escribe el contenido dado en el .c y lo compila con gcc.
     * Devuelve true si la compilación fue exitosa.
     */
    public boolean generarYCompilar(String codigoC) {
        String dirProyecto = System.getProperty("user.dir");
        Path rutaArchivo = Paths.get(dirProyecto, nombreArchivo);

        try {
            Files.writeString(rutaArchivo, codigoC);
            System.out.println("[OK] Archivo .c escrito en: " + rutaArchivo.toAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(
                    "gcc", nombreArchivo, "-o", nombreEjecutable
            );
            pb.directory(Paths.get(dirProyecto).toFile());
            pb.redirectErrorStream(true);

            Process proceso = pb.start();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proceso.getInputStream()))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    System.out.println(linea);
                }
            }

            int exitCode = proceso.waitFor();
            if (exitCode == 0) {
                System.out.println("[OK] Compilación exitosa. Ejecutable: " + nombreEjecutable);
                return true;
            } else {
                System.err.println("[ERROR] gcc salió con código: " + exitCode);
                return false;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("[ERROR] Falló la escritura/compilación: " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }
}