#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int testEstructuraLocal(void);

struct Punto_testEstructuraLocal {
    int x;
    int y;
};

int testEstructuraLocal(void) {
    struct Punto_testEstructuraLocal p;

    int t0;

    p.x = 10;
    p.y = 20;
    t0 = p.x + p.y;
    return t0;
}

