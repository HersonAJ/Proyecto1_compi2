lexer grammar PigLexer;

// Comentarios y espacios en blanco
LINEA_COMENTARIO   : '//' ~[\r\n]* -> channel(HIDDEN) ;
BLOQUE_COMENTARIO  : '##' .*? '##' -> channel(HIDDEN) ;
WS                 : [ \t\r\n\u200B]+ -> channel(HIDDEN) ;

// Importaciones
IMPORT : 'import' ;

// Secciones del programa
VARIABILES   : 'VARIABILES' ;
MAIOR        : 'MAIOR' ;
FIN_PROGRAMA : 'FINIS' ;

// Palabras reservadas
ESTO         : 'esto' ;
SERIES       : 'series' ;
NOVUS        : 'novus' ;
SI           : 'si' ;
ALITER       : 'aliter' ;
DUM          : 'dum' ;
FACERE       : 'facere' ;
PER          : 'per' ;
PERGE        : 'perge' ;
INTERRUMPE   : 'interrumpe' ;
FIN_BLOQUE   : 'finis';

// Tipos primitivos
NUMERUS   : 'numerus' ;
TEXTUM    : 'textum' ;
DECIMALIS : 'decimalis' ;
LITTERA   : 'littera' ;
BOOL      : 'bool' ;
VERUM     : 'verum' ;
FALSUS    : 'falsus' ;

// Operadores de dos caracteres
LEER      : '<<' ;
ESCRIBIR  : '>>' ;
INC       : '++' ;
DEC       : '--' ;
IGUAL     : '==' ;
DISTINTO  : '!=' ;
MENIG     : '<=' ;
MAYIG     : '>=' ;
AND       : '&&' ;
OR        : '||' ;

// Operadores y simbolos de un caracter
ASIGNAR    : '=' ;
MAS        : '+' ;
MENOS      : '-' ;
MULT       : '*' ;
DIV        : '/' ;
MENOR      : '<' ;
MAYOR      : '>' ;
DOSPUNTOS  : ':' ;
PUNTOCOMA  : ';' ;
COMA       : ',' ;
PUNTO      : '.' ;
LLAVE_A    : '{' ;
LLAVE_C    : '}' ;
CORCH_A    : '[' ;
CORCH_C    : ']' ;
PAR_A      : '(' ;
PAR_C      : ')' ;

// Literales
FLOAT   : [0-9]+ '.' [0-9]+ ;
INT     : [0-9]+ ;
STRING  : '"' (~["\r\n])* '"' ;
CHAR    : '\'' . '\'' ;

// se reconoce como palabra reservada.
ID : [a-zA-Z_][a-zA-Z0-9_]* ;