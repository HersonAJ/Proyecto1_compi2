#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct Punto {
    int x;
    int y;
};

char* testLeer(void);

char* testLeer(void) {
    char* nombre;

    char t0[256];

    scanf("%s", t0);
    nombre = t0;
    return nombre;
}

