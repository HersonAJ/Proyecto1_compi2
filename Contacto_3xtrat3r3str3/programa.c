#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct Persona {
    char* nombre;
    int edad;
};

int fuerza = 10;
char* comandante = "Estudiante X";

int calcularPoder(int fuerza);
int sumar(int a, int b);
void Persona_constructor_String_int(struct Persona* this, char* nombreParametro, int edadParametro);
char* Persona_getNombre(struct Persona* this);
int Persona_getEdad(struct Persona* this);
void Persona_saludar(struct Persona* this);
int main(void);

int calcularPoder(int fuerza) {
    int t0;
    int t1;

    t0 = (int) fuerza;
    t1 = t0 * 2;
    return t1;
}

int sumar(int a, int b) {
    int t0;

    t0 = a + b;
    return t0;
}

void Persona_constructor_String_int(struct Persona* this, char* nombreParametro, int edadParametro) {
    this->nombre = nombreParametro;
    this->edad = edadParametro;
}

char* Persona_getNombre(struct Persona* this) {
    return this->nombre;
}

int Persona_getEdad(struct Persona* this) {
    return this->edad;
}

void Persona_saludar(struct Persona* this) {
    printf("%s\n", "Hola desde Persona");
}

int main(void) {
    struct Persona* p;

    int t0;
    struct Persona* t1;
    char* t2;
    int t3;
    int t4;

    printf("%s", "Hola comandante!");
    printf("%s", "Tu fuerza es: ");
    t0 = calcularPoder(fuerza);
    printf("%d", t0);
    t1 = malloc(sizeof(struct Persona));
    Persona_constructor_String_int(t1, "Carlos", 25);
    p = t1;
    printf("%s", "Nombre: ");
    t2 = Persona_getNombre(p);
    printf("%s", t2);
    printf("%s", "Edad: ");
    t3 = Persona_getEdad(p);
    printf("%d", t3);
    Persona_saludar(p);
    printf("%s", "Suma: ");
    t4 = sumar(fuerza, 5);
    printf("%d", t4);
    return 0;
}

