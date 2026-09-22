%estructuras

estructura Direccion:
	cadena calle
	entero numero

estructura Persona:
	cadena nombre
	entero edad
	Direccion domicilio

%funciones


definir esMayorEdad(entero edad) -> bool:
	bool resultado
	si(edad >= 18) entonces
		resultado = verdadero
	contrario
		resultado = falso
	retornar resultado

definir sumar(entero a, entero b) -> entero:
	entero resultado = a + b
	retornar resultado