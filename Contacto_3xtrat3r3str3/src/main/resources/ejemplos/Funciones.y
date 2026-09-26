%funciones
definir testEstructuraLocal() -> entero:
	estructura Punto:
		entero x
		entero y

	Punto p
	p.x = 10
	p.y = 20
	retornar p.x + p.y
