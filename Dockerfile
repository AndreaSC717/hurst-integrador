# ── Stage 1: Build ─────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache Maven dependencies before copying source (faster rebuilds)
COPY pom.xml .
RUN mvn -B dependency:go-offline -q 2>/dev/null || true

COPY src ./src
# The javafx-linux Maven profile activates automatically on Linux
RUN mvn -B package -DskipTests -q

# ── Stage 2: Runtime ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk

# Install virtual display + VNC + noVNC + lightweight WM + Mesa SW renderer
RUN apt-get update && apt-get install -y --no-install-recommends \
        xvfb \
        x11vnc \
        x11-utils \
        novnc \
        websockify \
        fluxbox \
        libgl1 \
        libglu1-mesa \
        libgtk-3-0 \
        libgdk-pixbuf-2.0-0 \
        libpangocairo-1.0-0 \
        libatk1.0-0 \
        libdbus-1-3 \
        libasound2t64 \
        libxtst6 \
        libxrender1 \
        libxi6 \
        fonts-dejavu-core \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /build/target/husrt-control-1.0-SNAPSHOT.jar app.jar
COPY --from=builder /build/target/lib                             ./lib

RUN mkdir -p data/fotos-estudiantes

COPY docker-entrypoint.sh /entrypoint.sh
# Normalise line endings (CRLF → LF) in case the file was edited on Windows
RUN sed -i 's/\r$//' /entrypoint.sh && chmod +x /entrypoint.sh

# 5900 = VNC client  |  6080 = noVNC browser interface
EXPOSE 5900 6080

ENTRYPOINT ["/entrypoint.sh"]
