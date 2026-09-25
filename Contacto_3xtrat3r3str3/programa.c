#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct TestArreglos {
    int* numeros;
};

void TestArreglos_constructor(struct TestArreglos* this);
void TestArreglos_probarArreglo(struct TestArreglos* this);

void TestArreglos_constructor(struct TestArreglos* this) {
    int* t0;

    t0 = malloc(5 * sizeof(int));
    this->numeros = t0;
}

void TestArreglos_probarArreglo(struct TestArreglos* this) {
    int* locales;
    int suma;

    int* t0;
    int t1;

    t0 = malloc(3 * sizeof(int));
    locales = t0;
    locales[0] = 10;
    locales[1] = 20;
    locales[2] = 30;
    t1 = locales[0] + locales[1];
    suma = t1;
    this->numeros[0] = suma;
    printf("%s\n", "Suma calculada");
}

