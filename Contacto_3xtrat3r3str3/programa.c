#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct Cliente {
    char* nombre;
    int edad;
};

void Cliente_constructor_String_int(struct Cliente* this, char* nombreParametro, int edadParametro);
void Cliente_constructor(struct Cliente* this);
void Cliente_presentarse(struct Cliente* this);
int Cliente_obtenerEdad(struct Cliente* this);
void Cliente_probarLlamadas(struct Cliente* this);
void Cliente_probarBreakContinue(struct Cliente* this);
void Cliente_probarRecursividad_int(struct Cliente* this, int n);

void Cliente_constructor_String_int(struct Cliente* this, char* nombreParametro, int edadParametro) {
    this->nombre = nombreParametro;
    this->edad = edadParametro;
}

void Cliente_constructor(struct Cliente* this) {
    this->nombre = "Anonimo";
    this->edad = 0;
}

void Cliente_presentarse(struct Cliente* this) {
    printf("%s\n", "Hola, soy cliente");
}

int Cliente_obtenerEdad(struct Cliente* this) {
    return this->edad;
}

void Cliente_probarLlamadas(struct Cliente* this) {
    struct Cliente* otro;
    int e;
    int miEdad;
    struct Cliente* tercero;

    struct Cliente* t0;
    int t1;
    int t2;
    struct Cliente* t3;

    t0 = malloc(sizeof(struct Cliente));
    Cliente_constructor_String_int(t0, "Pedro", 30);
    otro = t0;
    Cliente_presentarse(otro);
    t1 = Cliente_obtenerEdad(otro);
    e = t1;
    printf("%s\n", "Edad obtenida");
    Cliente_presentarse(this);
    t2 = Cliente_obtenerEdad(this);
    miEdad = t2;
    printf("%s\n", "Mi edad");
    t3 = malloc(sizeof(struct Cliente));
    Cliente_constructor(t3);
    tercero = t3;
    Cliente_presentarse(tercero);
}

void Cliente_probarBreakContinue(struct Cliente* this) {
    int i;
    int contador;
    int j;
    int k;
    int opcion;

    int t0;
    int t1;
    int t2;
    int t3;
    int t4;
    int t5;
    int t6;
    int t7;
    int t8;
    int t9;

    i = 0;
L0:
    t0 = i < 10;
    if (t0 == 0) goto L2;
    t1 = i == 5;
    if (t1 == 0) goto L4;
    goto L3;
L3:
    goto L2;
    goto L4;
L4:
    printf("%s\n", "Iteracion for");
L1:
    i = i + 1;
    goto L0;
L2:
    i = 0;
L5:
    t2 = i < 5;
    if (t2 == 0) goto L7;
    t3 = i == 2;
    if (t3 == 0) goto L9;
    goto L8;
L8:
    goto L6;
    goto L9;
L9:
    printf("%s\n", "Sin saltar");
L6:
    i = i + 1;
    goto L5;
L7:
    contador = 0;
L10:
    t4 = contador < 100;
    if (t4 == 0) goto L11;
    contador = contador + 1;
    t5 = contador == 3;
    if (t5 == 0) goto L13;
    goto L12;
L12:
    goto L11;
    goto L13;
L13:
    goto L10;
L11:
    j = 0;
L14:
    t6 = j < 5;
    if (t6 == 0) goto L15;
    j = j + 1;
    t7 = j == 2;
    if (t7 == 0) goto L17;
    goto L16;
L16:
    goto L14;
    goto L17;
L17:
    printf("%s\n", "While");
    goto L14;
L15:
    k = 0;
L18:
    k = k + 1;
    t8 = k == 2;
    if (t8 == 0) goto L22;
    goto L21;
L21:
    goto L20;
    goto L22;
L22:
L19:
    t9 = k < 10;
    if (t9 != 0) goto L18;
L20:
    opcion = 2;
    switch (opcion) {
        case 1:
                printf("%s\n", "Opcion 1");
                break;
        case 2:
                printf("%s\n", "Opcion 2");
                break;
        default:
                printf("%s\n", "Default");
                break;
    }
}

void Cliente_probarRecursividad_int(struct Cliente* this, int n) {
    int t0;
    int t1;

    t0 = n <= 0;
    if (t0 == 0) goto L1;
    goto L0;
L0:
    return;
    goto L1;
L1:
    printf("%s\n", "Recursivo");
    t1 = n - 1;
    Cliente_probarRecursividad_int(this, t1);
}

