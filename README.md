# HUSRT-Control

Sistema de control de acceso hospitalario para prácticas universitarias.  
Tecnologías: **Java 21**, **JavaFX 21**, **MySQL 8.4**, **Maven 3.9**.

---

## ¿Qué necesito tener instalado antes de empezar?

Antes de poder correr la aplicación, asegúrate de tener instalados estos tres programas:

| Programa | Para qué sirve | Cómo verificar que está instalado |
|----------|---------------|----------------------------------|
| **JDK 21 o superior** | Ejecutar el código Java | Abre CMD y escribe `java -version` |
| **Maven 3.9 o superior** | Compilar y gestionar dependencias | Abre CMD y escribe `mvn -version` |
| **MySQL 8.x** | Base de datos de la aplicación | Busca en Servicios de Windows → `MySQL84` debe estar `En ejecución` |

> **¿Cómo abro CMD?**  
> Presiona las teclas **Windows + R**, escribe `cmd` y presiona Enter.

---

## PASO 1 — Crear la base de datos (solo se hace una vez)

Esto le dice a MySQL que cree todas las tablas y los datos de prueba que necesita la app.

**Abre CMD** y ejecuta estos dos comandos, uno por uno:

```cmd
cd "C:\Users\ADMIN\OneDrive\Desktop\integrador"
```

```cmd
"C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p123456 --default-character-set=utf8mb4 < setup_db.sql
```

> **¿Qué hace este comando?**  
> Le envía el archivo `setup_db.sql` a MySQL.  
> MySQL crea la base de datos `husrt`, el usuario `husrt`, las 13 tablas y los datos demo.

> **¿Sale un error que dice "Duplicate entry"?**  
> No te preocupes. Significa que la base de datos ya fue creada antes y todo está bien. Ignóralo y sigue al Paso 2.

> **¿Sale un error que dice "Access denied"?**  
> La contraseña de tu MySQL root no es `123456`. Cámbiala en el comando por la tuya.

---

## PASO 2 — Abrir la aplicación

Una vez que la base de datos está lista, **abre CMD**, navega a la carpeta del proyecto y ejecuta la app:

```cmd
cd "C:\Users\ADMIN\OneDrive\Desktop\integrador"
```

```cmd
mvn org.openjfx:javafx-maven-plugin:0.0.8:run
```

> **¿Qué pasa mientras carga?**  
> Verás varios mensajes de WARNING en la consola. Eso es **completamente normal**, no son errores.  
> Espera unos segundos y aparecerá la ventana de inicio de sesión de la aplicación.

> **¿Sale el error "No plugin found for prefix 'javafx'"?**  
> Usa exactamente el comando de arriba (el largo), no `mvn javafx:run`.

---

## PASO 3 — Iniciar sesión

Una vez que aparece la ventana, usa cualquiera de estos usuarios. La contraseña es siempre **`password`**:

| Usuario | Contraseña | Rol | ¿Qué puede hacer? |
|---------|-----------|-----|-------------------|
| `admin` | `password` | Administrador | Todo: usuarios, auditoría, configuración |
| `coordinador` | `password` | Coordinador | Gestionar estudiantes, docentes, planes, servicios |
| `porteria` | `password` | Portería | Registrar entradas y salidas |
| `consulta` | `password` | Consulta | Solo ver información, sin modificar |
| `1090123456` | `password` | Estudiante | Ver su propio perfil e historial |

---

## PASO 4 — Probar el flujo completo (demo)

Para ver todo el sistema funcionando de punta a punta, sigue estos pasos en orden:

### 4.1 — Registrar ingreso del docente
1. Inicia sesión como **`porteria`**
2. En el menú lateral haz clic en **Portería**
3. En la sección **Docente**, escribe la cédula `80000001` y el Plan ID `1`
4. Haz clic en **Registrar Ingreso**

### 4.2 — Registrar ingreso del estudiante
1. En la misma pantalla de Portería, sección **Estudiante**
2. Escribe la cédula `1090123456`
3. Haz clic en **Registrar Ingreso**
4. Debería aparecer el mensaje "Ingreso autorizado. Bienvenido/a."

### 4.3 — Ver el dashboard en tiempo real
1. En el menú lateral haz clic en **Dashboard**
2. Verás los contadores actualizados con el estudiante que acaba de entrar
3. El dashboard se refresca automáticamente cada 15 segundos

---

## Datos de conexión a la base de datos

Por si necesitas conectarte directamente con MySQL Workbench u otra herramienta:

| Campo | Valor |
|-------|-------|
| Host | `127.0.0.1` |
| Puerto | `3306` |
| Base de datos | `husrt` |
| Usuario | `husrt` |
| Contraseña | `husrt_secret` |

La configuración está en `src/main/resources/application.properties`.

---

## Estructura del proyecto

| Carpeta / Archivo | Contenido |
|-------------------|-----------|
| `src/main/java/com/husrt/` | Todo el código Java (modelos, lógica, pantallas) |
| `src/main/resources/com/husrt/ui/` | Pantallas FXML y estilos CSS |
| `src/main/resources/application.properties` | Configuración de conexión a BD |
| `docker/mysql/inicializacion-bd/` | Scripts SQL del esquema y datos demo (referencia) |
| `setup_db.sql` | Script todo-en-uno para crear la base de datos |
| `run.ps1` | Atajo alternativo para ejecutar la app |
| `target/` | Archivos compilados generados por Maven (no tocar) |

---

## Seguridad

- Tras **5 intentos fallidos** de login, la cuenta se bloquea **15 minutos**.
- Para desbloquearla: inicia sesión como `admin` → **Administración** → busca el usuario → **Restablecer contraseña**.
- Para cambiar tu propia contraseña: menú **Sesión → Cambiar contraseña** (disponible para todos los roles).
- La pestaña **Auditoría** (roles admin y portería) registra todas las acciones del sistema.

---

## Solución de problemas frecuentes

| Error | Causa probable | Solución |
|-------|---------------|----------|
| `Connection refused` al abrir la app | MySQL no está corriendo | Abre Servicios de Windows y arranca `MySQL84` |
| `Access denied for user 'husrt'` | La BD no fue creada | Ejecuta el Paso 1 de este README |
| `Duplicate entry` al ejecutar setup_db.sql | La BD ya existe | Normal, ignóralo. Todo está bien |
| La app no abre y sale `BUILD FAILURE` | Error de compilación | Ejecuta `mvn compile` en CMD para ver el error |
| Ventana en blanco o no aparece | JavaFX no cargó | Verifica que JDK sea versión 21 o superior con `java -version` |

---

## Empaquetado (para distribuir)

Si quieres generar el `.jar` ejecutable, abre CMD y ejecuta:

```cmd
cd "C:\Users\ADMIN\OneDrive\Desktop\integrador"
mvn -B package -DskipTests
```

Esto genera `target/husrt-control-1.0-SNAPSHOT.jar` y una carpeta `target/lib/` con todas las dependencias.
