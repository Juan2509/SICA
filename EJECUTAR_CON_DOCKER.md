# Ejecutar SICA completamente con Docker

Esta modalidad incluye PostgreSQL y la aplicación JavaFX. El equipo anfitrión
solo necesita Docker con Docker Compose versión 2. No necesita instalar Java,
Maven ni PostgreSQL.

La interfaz JavaFX se ejecuta en un escritorio virtual dentro del contenedor y
se visualiza desde el navegador. Esto evita depender del sistema gráfico de
Windows, macOS o Linux.

## Primera ejecución

1. Descarga o clona el repositorio completo.
2. Abre una terminal en la raíz, donde está `compose.yaml`.
3. Construye e inicia los dos contenedores:

~~~bash
docker compose up -d --build
~~~

Eso es suficiente para una instalación local. Compose utiliza credenciales
técnicas predeterminadas y crea automáticamente la estructura y los datos.

Si se desean contraseñas técnicas personalizadas, antes del primer inicio se
puede crear un archivo `.env` a partir de `.env.example`.

En macOS o Linux:

~~~bash
POSTGRES_PASSWORD="pg_$(openssl rand -hex 16)"
SICA_APP_PASSWORD="app_$(openssl rand -hex 16)"
printf 'POSTGRES_PASSWORD=%s\nSICA_APP_PASSWORD=%s\n' "$POSTGRES_PASSWORD" "$SICA_APP_PASSWORD" > .env
~~~

En PowerShell:

~~~powershell
$postgresPassword = "pg_" + [Guid]::NewGuid().ToString("N")
$appPassword = "app_" + [Guid]::NewGuid().ToString("N")
@(
    "POSTGRES_PASSWORD=$postgresPassword"
    "SICA_APP_PASSWORD=$appPassword"
) | Set-Content -LiteralPath ".env" -Encoding ASCII
~~~

4. Consulta los mensajes de inicio:

~~~bash
docker compose logs -f app
~~~

Cuando aparezca la siguiente línea se puede detener la consulta con
`Ctrl + C`:

~~~text
SICA esta disponible en http://localhost:6080/vnc.html?autoconnect=true&resize=scale
~~~

5. Abre en el navegador:

~~~text
http://localhost:6080/vnc.html?autoconnect=true&resize=scale
~~~

6. noVNC se conectará automáticamente y mostrará la ventana de SICA.

## Credenciales de SICA

| Rol | Usuario | Contraseña |
|---|---|---|
| Administrador | `admin` | `admin123` |
| Guarda de Seguridad | `guarda` | `guarda123` |
| Funcionario | `funcionario` | `funcionario123` |

Estas credenciales pertenecen a SICA. Los valores de `.env`, cuando se
personalizan, son únicamente para la conexión técnica con PostgreSQL.

## Qué se crea automáticamente

Durante la primera ejecución:

1. Docker crea PostgreSQL 17.
2. `schema.sql` crea las tablas, relaciones, restricciones y triggers.
3. `data.sql` carga roles, permisos, usuarios, empresas, personas y visitas.
4. `03-security.sh` crea el usuario técnico limitado `sica_app`.
5. El contenedor `app` compila e inicia SICA.
6. noVNC publica la interfaz localmente en el puerto `6080`.

Los datos permanecen en el volumen `sica_postgres_data`.

## Inicios posteriores

~~~bash
docker compose up -d
~~~

Después abre
`http://localhost:6080/vnc.html?autoconnect=true&resize=scale`.

## Detener SICA conservando los datos

~~~bash
docker compose down
~~~

## Reconstruir todo desde cero

El siguiente comando elimina permanentemente los datos guardados:

~~~bash
docker compose down -v
docker compose up -d --build
~~~

## Consultar el estado

~~~bash
docker compose ps
docker compose logs postgres
docker compose logs app
~~~

Los servicios esperados son:

- `sica-postgres`: base de datos.
- `sica-app`: interfaz y lógica Java.

## Desarrollo desde VS Code

La tarea `Preparar PostgreSQL SICA` continúa iniciando únicamente el servicio
`postgres`. Después VS Code ejecuta JavaFX directamente en el equipo. Esto
evita abrir dos copias de la aplicación durante el desarrollo.

Para probar la modalidad completamente contenida se debe utilizar
`docker compose up -d --build` y abrir noVNC en el navegador.
