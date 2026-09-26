#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int edad = 20;
char* nombre = "Comandante";

int main(void);

int main(void) {
    int t0;
    int t1;

    printf("%s", "Hola!");
    printf("%s", nombre);
    t0 = (int) edad;
    t1 = t0 + 1;
    edad = t1;
    printf("%s", "Edad actualizada");
    return 0;
}

