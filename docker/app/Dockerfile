FROM debian:bookworm-slim

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates \
        fluxbox \
        fonts-dejavu-core \
        libgtk-3-0 \
        libxi6 \
        libxrender1 \
        libxtst6 \
        maven \
        novnc \
        openjdk-17-jdk \
        postgresql-client \
        websockify \
        x11vnc \
        xvfb \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY sica/pom.xml ./pom.xml
RUN mvn -B dependency:go-offline

COPY sica/src ./src
RUN mvn -B -DskipTests package

COPY docker/app/iniciar-app.sh /usr/local/bin/iniciar-sica
RUN chmod +x /usr/local/bin/iniciar-sica

EXPOSE 6080

ENTRYPOINT ["/usr/local/bin/iniciar-sica"]
