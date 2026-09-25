package com.example.contacto_3xtrat3r3str3.c3d_v2;

/**
 * Acceso a un campo de estructura.
 *   base.campo     ->  base.campo
 *   base->campo    ->  base->campo   (cuando la base es un puntero)
 */
public class AccesoAtributo extends AccesoMemoria {

    private final AccesoMemoria base;
    private final String campo;
    private final boolean porPuntero;
    private final String tipoCampo; // tipo del campo

    public AccesoAtributo(AccesoMemoria base, String campo, boolean porPuntero, String tipoCampo) {
        this.base = base;
        this.campo = campo;
        this.porPuntero = porPuntero;
        this.tipoCampo = tipoCampo;
    }

    public AccesoMemoria getBase() { return base; }
    public String getCampo()       { return campo; }
    public boolean isPorPuntero()  { return porPuntero; }

    @Override
    public String getTipo() { return tipoCampo; }

    @Override
    public void aCodigoC(StringBuilder sb) {
        base.aCodigoC(sb);
        sb.append(porPuntero ? "->" : ".");
        sb.append(campo);
    }
}