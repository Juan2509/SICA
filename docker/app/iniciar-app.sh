#!/bin/sh
set -eu

echo "Esperando a que PostgreSQL acepte la conexion de SICA..."
until PGPASSWORD="$SICA_DB_PASSWORD" psql \
    --host=postgres \
    --username="$SICA_DB_USER" \
    --dbname=sica_db \
    --command="SELECT 1" >/dev/null 2>&1
do
    sleep 2
done

echo "Iniciando el escritorio virtual de SICA..."

# Al reiniciar el mismo contenedor pueden quedar el bloqueo y el socket del
# servidor grafico anterior. Se eliminan porque ya no existe aquel proceso.
rm -f /tmp/.X99-lock /tmp/.X11-unix/X99
mkdir -p /tmp/.X11-unix

Xvfb :99 -screen 0 1400x900x24 -ac -nolisten tcp \
    >/tmp/xvfb.log 2>&1 &
xvfb_pid=$!

while [ ! -S /tmp/.X11-unix/X99 ]; do
    if ! kill -0 "$xvfb_pid" 2>/dev/null; then
        echo "No fue posible iniciar el escritorio virtual:"
        cat /tmp/xvfb.log
        exit 1
    fi
    sleep 1
done
fluxbox -display :99 >/tmp/fluxbox.log 2>&1 &
x11vnc -display :99 -forever -shared -nopw -rfbport 5900 \
    >/tmp/x11vnc.log 2>&1 &
websockify --web=/usr/share/novnc/ 6080 localhost:5900 \
    >/tmp/novnc.log 2>&1 &

echo "SICA esta disponible en http://localhost:6080/vnc.html?autoconnect=true&resize=scale"
exec mvn -B -o javafx:run
