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
Xvfb :99 -screen 0 1400x900x24 -ac -nolisten tcp &
while [ ! -S /tmp/.X11-unix/X99 ]; do
    sleep 1
done
fluxbox -display :99 >/tmp/fluxbox.log 2>&1 &
x11vnc -display :99 -forever -shared -nopw -rfbport 5900 \
    >/tmp/x11vnc.log 2>&1 &
websockify --web=/usr/share/novnc/ 6080 localhost:5900 \
    >/tmp/novnc.log 2>&1 &

echo "SICA esta disponible en http://localhost:6080/vnc.html?autoconnect=true&resize=scale"
exec mvn -B -o javafx:run
