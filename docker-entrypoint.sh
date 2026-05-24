#!/bin/bash
set -e

# ── Clean stale Xvfb lock (left over on container restart) ─────────────────────
rm -f /tmp/.X1-lock /tmp/.X11-unix/X1 2>/dev/null || true

export DISPLAY=:1
export XAUTHORITY=/dev/null   # skip Xauth entirely

# ── Virtual framebuffer ─────────────────────────────────────────────────────────
Xvfb :1 -screen 0 1280x800x24 -nolisten tcp &

# Wait until Xvfb is actually accepting connections
for i in $(seq 1 30); do
    if xdpyinfo -display :1 >/dev/null 2>&1; then
        echo "[entrypoint] Xvfb ready."
        break
    fi
    sleep 0.5
done

# ── Lightweight window manager ──────────────────────────────────────────────────
fluxbox 2>/dev/null &
sleep 0.5

# ── VNC server (no XKB, no Xauth) ──────────────────────────────────────────────
x11vnc -display :1 -nopw -listen 0.0.0.0 -forever -shared \
       -rfbport 5900 -noxrecord -noxdamage &
sleep 1

# ── noVNC web interface ─────────────────────────────────────────────────────────
websockify --web /usr/share/novnc 6080 localhost:5900 &

echo ""
echo "╔══════════════════════════════════════════════════╗"
echo "║  HUSRT-Control — acceso al escritorio            ║"
echo "║                                                  ║"
echo "║  Navegador : http://localhost:6080/vnc.html      ║"
echo "║  Cliente VNC: localhost:5900                     ║"
echo "╚══════════════════════════════════════════════════╝"
echo ""

# ── JavaFX application ──────────────────────────────────────────────────────────
exec java \
    -Dprism.order=sw \
    -Djavafx.verbose=false \
    -Djava.awt.headless=false \
    --module-path lib \
    --add-modules javafx.base,javafx.graphics,javafx.controls,javafx.fxml \
    -jar app.jar
