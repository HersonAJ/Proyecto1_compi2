#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int esMayorEdad(int edad);
int sumar(int a, int b);

int esMayorEdad(int edad) {
    int resultado;

    int t0;
    int t1;

    t0 = (int) edad;
    t1 = t0 >= 18;
    if (t1 != 0) goto L0;
    goto L1;
L0:
    resultado = 1;
    goto L1;
    resultado = 0;
L1:
    return resultado;
}

int sumar(int a, int b) {
    int resultado;

    int t0;

    t0 = a + b;
    resultado = t0;
    return resultado;
}

