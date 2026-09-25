package com.example.contacto_3xtrat3r3str3.c3d_v2;

import com.example.contacto_3xtrat3r3str3.c3d_v2.Cuarteta;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

//Buffer de cuádruplas + contador de temporales/etiquetas + pila de ciclos
public class GestorCodigoIntermedio {

    private final List<Cuarteta> cuartetas;
    private final ContadorIds contador;
    private final Deque<ContextoCiclo> pilaCiclos;

    public GestorCodigoIntermedio() {
        this.cuartetas = new ArrayList<>();
        this.contador = new ContadorIds();
        this.pilaCiclos = new ArrayDeque<>();
    }

    // ===== Cuádruplas =====
    public void emitir(Cuarteta c) {
        cuartetas.add(c);
    }

    public List<Cuarteta> getCuartetas() {
        return cuartetas;
    }

    // ===== Contador =====
    public ContadorIds getContador() {
        return contador;
    }

    // ===== Pila de ciclos =====
    public void entrarCiclo(ContextoCiclo c) {
        pilaCiclos.push(c);
    }

    public void salirCiclo() {
        if (!pilaCiclos.isEmpty()) {
            pilaCiclos.pop();
        }
    }

    public ContextoCiclo cicloActual() {
        return pilaCiclos.peek();
    }
}