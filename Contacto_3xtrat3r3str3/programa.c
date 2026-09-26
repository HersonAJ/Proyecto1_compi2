#include <stdio.h>
#include <stdlib.h>
#include <string.h>

char* concat(char* a, char* b) {
    char* r = malloc(strlen(a) + strlen(b) + 1);
    strcpy(r, a);
    strcat(r, b);
    return r;
}

struct Direccion {
    char* calle;
    int numero;
};

struct Persona {
    char* nombre;
    int edad;
    struct Direccion domicilio;
};

struct PersonaZ {
    char* nombre;
    int edad;
    double altura;
};

int fuerza = 10;
int edad = 25;
char* nombre = "Comandante";
int activo = 1;

int calcularPoder(int fuerza);
int esMayorEdad(int edad);
int sumar(int a, int b);
void PersonaZ_constructor_String_int_double(struct PersonaZ* this, char* nombreParametro, int edadParametro, double alturaParametro);
char* PersonaZ_getNombre(struct PersonaZ* this);
int PersonaZ_getEdad(struct PersonaZ* this);
double PersonaZ_getAltura(struct PersonaZ* this);
void PersonaZ_saludar(struct PersonaZ* this);
int PersonaZ_calcularAnioNacimiento_int(struct PersonaZ* this, int anioActual);
int PersonaZ_esMayorEdad(struct PersonaZ* this);
int main(void);

int calcularPoder(int fuerza) {
    int resultado;

    int t0;
    int t1;

    t0 = (int) fuerza;
    t1 = t0 * 2;
    resultado = t1;
    return resultado;
}

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

void PersonaZ_constructor_String_int_double(struct PersonaZ* this, char* nombreParametro, int edadParametro, double alturaParametro) {
    this->nombre = nombreParametro;
    this->edad = edadParametro;
    this->altura = alturaParametro;
}

char* PersonaZ_getNombre(struct PersonaZ* this) {
    return this->nombre;
}

int PersonaZ_getEdad(struct PersonaZ* this) {
    return this->edad;
}

double PersonaZ_getAltura(struct PersonaZ* this) {
    return this->altura;
}

void PersonaZ_saludar(struct PersonaZ* this) {
    char* t0;

    t0 = concat("Hola, mi nombre es ", this->nombre);
    printf("%s\n", t0);
}

int PersonaZ_calcularAnioNacimiento_int(struct PersonaZ* this, int anioActual) {
    int t0;

    t0 = anioActual - this->edad;
    return t0;
}

int PersonaZ_esMayorEdad(struct PersonaZ* this) {
    int t0;

    t0 = this->edad >= 18;
    return t0;
}

int main(void) {
    int poder;
    int suma;
    int esMayor;
    struct PersonaZ* persona;
    int anioNac;
    int esMayorObj;

    int t0;
    int t1;
    int t2;
    int t3;
    int t4;
    struct PersonaZ* t5;
    char* t6;
    int t7;
    int t8;
    int t9;

    printf("%s", "Hola comandante!");
    printf("%s", "Tu fuerza es:");
    printf("%d", fuerza);
    t0 = (int) edad;
    t1 = t0 >= 18;
    if (t1 == 0) goto L1;
    printf("%s", "Eres mayor de edad");
    activo = 1;
    goto L0;
L1:
L0:
    t2 = calcularPoder(fuerza);
    poder = t2;
    printf("%s", "Tu poder es:");
    printf("%d", poder);
    t3 = sumar(fuerza, edad);
    suma = t3;
    printf("%s", "La suma es:");
    printf("%d", suma);
    t4 = esMayorEdad(edad);
    esMayor = t4;
    printf("%s", "Es mayor de edad?");
    printf("%d", esMayor);
    t5 = malloc(sizeof(struct PersonaZ));
    PersonaZ_constructor_String_int_double(t5, "Carlos", 25, 1.75);
    persona = t5;
    printf("%s", "Nombre de la persona:");
    t6 = PersonaZ_getNombre(persona);
    printf("%s", t6);
    printf("%s", "Edad de la persona:");
    t7 = PersonaZ_getEdad(persona);
    printf("%d", t7);
    PersonaZ_saludar(persona);
    t8 = PersonaZ_calcularAnioNacimiento_int(persona, 2026);
    anioNac = t8;
    printf("%s", "Anio de nacimiento:");
    printf("%d", anioNac);
    t9 = PersonaZ_esMayorEdad(persona);
    esMayorObj = t9;
    printf("%s", "La persona es mayor de edad?");
    printf("%d", esMayorObj);
    return 0;
}

