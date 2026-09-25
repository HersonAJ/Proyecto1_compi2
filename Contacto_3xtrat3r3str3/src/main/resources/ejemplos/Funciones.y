%estructuras
estructura Punto:
	entero x
	entero y

%funciones
definir testAnidado() -> entero:
	Punto p
	p.x = 10
	p.y = 20
	entero arr[3] = {1, 2, 3}
	arr[0] = p.x
	entero r = arr[0]
	retornar r
