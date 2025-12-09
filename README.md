# Fatality - Juego de Combate por Turnos

## Descripción

Fatality es un juego multijugador de combate por turnos donde cada jugador controla hasta 4 luchadores, cada uno con armas asignadas que causan daño según el tipo del objetivo.

---

## Comandos Disponibles

### Comandos de Configuración Inicial

Oigan este comando no lo usen porque puse que el jugador se inicializa automaticamente

#### `NAME <nombre>`

Establece el nombre del jugador.

```
NAME Juan
```

#### `CREATEFIGHTER <nombre_luchador> <tipo>`

Crea un nuevo luchador con el tipo especificado. Máximo 4 luchadores por jugador.

**Luchadores permitidos:**

- black_manta, hellboy, omni_man, peacemaker, red_hood, shazam, sub_zero, terminator, the_flash, the_joker

**Tipos disponibles:**

- FUEGO, AIRE, AGUA, TIERRA, VOLADOR, VENENO, HIELO, METAL, PSIQUICO, HADA

```
CREATEFIGHTER sub_zero HIELO
CREATEFIGHTER the_flash VOLADOR
```

#### `ASSIGNWEAPON <nombre_luchador> <nombre_arma>`

Asigna un arma a un luchador. Cada luchador puede tener múltiples armas.

```
ASSIGNWEAPON sub_zero espada_helada
ASSIGNWEAPON the_flash rayo
```

#### `READY`

Indica que el jugador está listo para iniciar la partida. La partida comienza cuando todos los jugadores están listos.

```
READY
```

---

### Comandos de Combate

#### `ATTACK <jugador_objetivo> <mi_luchador> <arma>`

Ataca a otro jugador usando un luchador y arma específicos.

- Solo puede usarse durante tu turno
- El luchador debe estar vivo (vida > 0)
- El arma no debe haber sido usada previamente

```
ATTACK Pedro sub_zero espada_helada
```

#### `SKIP`

Salta tu turno sin realizar ninguna acción.

```
SKIP
```

#### `GIVEUP`

Te rindes y sales de la partida. Serás marcado como inactivo.

```
GIVEUP
```

---

### Comandos de Comunicación

#### `MESSAGE <mensaje>`

Envía un mensaje público a todos los jugadores.

```
MESSAGE Hola a todos!
```

#### `PRIVATE_MESSAGE <jugador> <mensaje>`

Envía un mensaje privado a un jugador específico.

```
PRIVATE_MESSAGE Pedro Prepárate para perder!
```

---

## Sistema de Combate

### Daño por Tipo

El daño que causa un arma depende del tipo del luchador objetivo. Cada arma tiene un arreglo de daño para cada uno de los 10 tipos:

- Índice 0: FUEGO
- Índice 1: AIRE
- Índice 2: AGUA
- Índice 3: TIERRA
- Índice 4: VOLADOR
- Índice 5: VENENO
- Índice 6: HIELO
- Índice 7: METAL
- Índice 8: PSIQUICO
- Índice 9: HADA

### Golpe Exitoso

Un ataque es exitoso si el daño calculado es >= 60. Si el daño es menor a 60, el ataque falla.

### Victoria

- Un jugador está **vivo** si al menos uno de sus luchadores tiene vida > 0
- La partida termina cuando solo queda **un jugador vivo**
- El último jugador vivo es declarado **ganador**

---

## Estadísticas

El juego mantiene un registro de estadísticas por jugador:

- **WINS**: Victorias totales
- **LOSSES**: Derrotas totales
- **ATTACKS**: Ataques realizados
- **SUCCESS**: Ataques exitosos (daño >= 60)
- **FAILED**: Ataques fallidos (daño < 60)
- **GIVEUP**: Veces que se rindió
