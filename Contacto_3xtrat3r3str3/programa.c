#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int testMatriz(void);

int testMatriz(void) {
    int m[3][2] = {{1, 2}, {3, 4}, {5, 6}};

    return m[0][0];
}

