parser grammar YParser;

options { tokenVocab = YLexer; }

//programa principal
programa    : seccionEstructuras? seccionFunciones EOF
            ;

//seccion de estructuras opcional
seccionEstructuras  : SEC_ESTRUCTURAS NEWLINE definicionEstructura+
                    ;

definicionEstructura    : ESTRUCTURA ID DOS_PUNTOS NEWLINE INDENT atributoEstructura+ DEDENT
                        ;

atributoEstructura  : tipo ID (COR_IZQ ENTERO_LIT COR_DER)? NEWLINE
                    | ID ID NEWLINE
                    ;

//seccion de funciones obligatoria
seccionFunciones    : SEC_FUNCIONES NEWLINE definicionFuncion+
                    ;

definicionFuncion   : DEFINIR ID PAR_IZQ parametros? PAR_DER (FLECHA tipo)? DOS_PUNTOS NEWLINE cuerpoFuncion DEDENT
                    ;

parametros  :   parametro (COMA parametro)*
            ;

parametro   : tipo ID
            | COR_IZQ COR_DER tipo ID
            | LLAVE_IZQ LLAVE_DER ID ID
            ;

cuerpoFuncion   : instruccion+
                ;

//instrucciones
instruccion : declaracion
            | asignacion
            | incrementoDecremento
            | condicional
            | elegir
            | cicloPara
            | cicloMientras
            | cicloHacerMientras
            | retorno
            | imprimir
            | leer
            | romper
            | continuar
            ;

//declaraciones
declaracion : tipo ID (IGUAL expresion)? NEWLINE
            | tipo ID COR_IZQ ENTERO_LIT COR_DER (IGUAL LLAVE_IZQ listaExpresiones LLAVE_DER)? NEWLINE
            | tipo ID COR_IZQ ENTERO_LIT COR_DER COR_IZQ ENTERO_LIT COR_DER NEWLINE
            | ID ID (IGUAL LLAVE_IZQ listaExpresiones LLAVE_DER)? NEWLINE
            ;

listaExpresiones    : expresion (COMA expresion)*
                    ;

//asignaciones
asignacion  : accesoVariable IGUAL expresion NEWLINE
            ;

accesoVariable  : ID (PUNTO ID | COR_IZQ expresion COR_DER)*
                ;

//incremento/decremento
incrementoDecremento    : ID (INCREMENTO | DECREMENTO) NEWLINE
                        ;

//condicional
condicional : SI PAR_IZQ expresion PAR_DER ENTONCES NEWLINE INDENT bloque DEDENT
                (SINO PAR_IZQ expresion PAR_DER ENTONCES NEWLINE INDENT bloque DEDENT)?
                (CONTRARIO NEWLINE INDENT bloque DEDENT)?
                ;

bloque  : instruccion+
        ;

//elegir
elegir  : ELEGIR PAR_IZQ expresion PAR_DER DOS_PUNTOS NEWLINE INDENT caso+ (siempre)? DEDENT
        ;

caso    : CASO literal DOS_PUNTOS NEWLINE INDENT bloque DEDENT
        ;

siempre : SIEMPRE DOS_PUNTOS NEWLINE INDENT bloque DEDENT
        ;

//ciclo PARA
cicloPara   : PARA PAR_IZQ inicializacionPara PUNTO_COMA condicionPara PUNTO_COMA actualizacionPara PAR_DER DOS_PUNTOS NEWLINE INDENT bloque DEDENT
            ;

inicializacionPara  : tipo ID IGUAL expresion
                    ;

condicionPara   : expresion
                ;

actualizacionPara   : ID (INCREMENTO | DECREMENTO)
                    ;

//ciclo MIENTRAS
cicloMientras   : MIENTRAS PAR_IZQ expresion PAR_DER HACER NEWLINE INDENT bloque DEDENT
                ;

//ciclo HACER - MIENTRAS
cicloHacerMientras  : HACER DOS_PUNTOS NEWLINE INDENT bloque DEDENT MIENTRAS PAR_IZQ expresion PAR_DER NEWLINE
                    ;

//retorno
retorno : RETORNAR expresion NEWLINE
        ;

//funciones especiales
imprimir    : IMPRIMIR PAR_IZQ expresion PAR_DER NEWLINE
            ;

leer    : LEER PAR_IZQ PAR_DER NEWLINE
        ;

//control de ciclos
romper  : ROMPER NEWLINE
        ;

continuar   : CONTINUAR NEWLINE
            ;

//Expresiones con precedencia
expresion   : PAR_IZQ expresion PAR_DER                         # ExprParentesis
            | ID PAR_IZQ argumentos? PAR_DER                    # ExprLlamadaFuncion
            | accesoVariable                                    # ExprAcceso
            | literal                                           # ExprLiteral
            | (MENOS | NOT) expresion                           # ExprUnaria
            | expresion (POR | DIV) expresion                   # ExprMultiplicacion
            | expresion (MAS | MENOS) expresion                 # ExprSumaResta
            | expresion (MENOR | MAYOR | MENOR_IGUAL | MAYOR_IGUAL) expresion           # ExprRelacional
            | expresion (IGUAL_IGUAL | DIFERENTE) expresion     # ExprIgualdad
            | expresion AND expresion                           # ExprAnd
            | expresion OR expresion                            # ExprOr
            ;

argumentos  : expresion (COMA expresion)*
            ;

//Literales
literal : ENTERO_LIT
        | FLOTANTE_LIT
        | CADENA_LIT
        | CARACTER_LIT
        | VERDADERO
        | FALSO
        ;

//Tipos
tipo    : ENTERO
        | FLOTANTE
        | CARACTER
        | CADENA
        | BOOL
        ;