# HUSRT-Control
Desarrolladores: 
Karla Nathalia Castro Piza
Simon Andres Herrera Bolivar 
Yuliana Andrea Sua Chacón
Danna Lucía Triana Loaiza 
Maria Paula Gonzales Patiño

Introducción:
HUSRT-Control es una aplicación de escritorio para gestionar el control de acceso hospitalario de estudiantes universitarios en prácticas clínicas. Permite registrar quién entra y sale del hospital, validar que cumplan los requisitos de su plan de prácticas y dar visibilidad en tiempo real a coordinadores, docentes y administración.

1. Inicio de sesión:<img width="1361" height="693" alt="image" src="https://github.com/user-attachments/assets/5b7dd9f6-ee93-484a-bea5-2ba04334f038" />
------------------------------------------------------------------------------------------------------------------------------------------
2. Dashboard:<img width="1363" height="693" alt="image" src="https://github.com/user-attachments/assets/bd8d80de-7d16-4315-a605-a36032306d21" />
<img width="1364" height="694" alt="image" src="https://github.com/user-attachments/assets/0a52481f-9d85-4516-8aa3-0f152ed8a254" />
------------------------------------------------------------------------------------------------------------------------------------------
3. Coordinación:
Universidades — CRUD de universidades.
<img width="1361" height="693" alt="image" src="https://github.com/user-attachments/assets/fb59fee3-bb12-43a2-9cd8-a3b08a41fdff" />
Servicios — CRUD de servicios hospitalarios.
<img width="1363" height="692" alt="image" src="https://github.com/user-attachments/assets/f017892f-a090-4195-a91a-f7d71130b9ff" />
------------------------------------------------------------------------------------------------------------------------------------------

Estudiantes — listado, registro y edición de estudiantes.
<img width="1363" height="690" alt="image" src="https://github.com/user-attachments/assets/e8e3477b-72c4-4533-9bdd-2e913f91b45c" />
<img width="1360" height="691" alt="image" src="https://github.com/user-attachments/assets/7320925e-0bb1-44a5-b1d0-6c736c517f52" />
<img width="1354" height="687" alt="image" src="https://github.com/user-attachments/assets/255796e3-57e5-4cef-85f3-fd9295aec594" />

------------------------------------------------------------------------------------------------------------------------------------------
Docentes — listado, registro y edición de docentes.
<img width="1357" height="692" alt="image" src="https://github.com/user-attachments/assets/e2e90cb5-e3dd-4a7c-bfdd-6a31327dd85a" />
Plan y asignaciones — creación de planes de práctica y asignación de estudiantes.
<img width="1365" height="697" alt="image" src="https://github.com/user-attachments/assets/8d8ff9ca-8d5d-4986-b82e-1a55ea2a3155" />
<img width="1359" height="692" alt="image" src="https://github.com/user-attachments/assets/3273eda8-b066-484c-bec7-cac678f8af00" />
------------------------------------------------------------------------------------------------------------------------------------------

4. Estudiantes:
<img width="1362" height="693" alt="image" src="https://github.com/user-attachments/assets/389dfbe1-4eed-4ed0-9b47-8db8d0ae0f80" />
<img width="1357" height="691" alt="image" src="https://github.com/user-attachments/assets/e1a17c49-9ac7-4370-a23d-bb6e97a1c7b5" />
<img width="1363" height="690" alt="image" src="https://github.com/user-attachments/assets/8bb5c16b-eb15-44b0-8d82-51f36463abee" />
------------------------------------------------------------------------------------------------------------------------------------------

5. Panel docente:
<img width="1358" height="691" alt="image" src="https://github.com/user-attachments/assets/3adcd841-4fe8-4601-b058-96397b429f7b" />
<img width="1360" height="689" alt="image" src="https://github.com/user-attachments/assets/560c9a34-8f35-465d-b6b4-0680c55fdbca" />
------------------------------------------------------------------------------------------------------------------------------------------

6. Portería:
<img width="1360" height="687" alt="image" src="https://github.com/user-attachments/assets/03164f57-ff33-4605-a5b3-367c866cbb20" />
------------------------------------------------------------------------------------------------------------------------------------------

7. Reportes:
<img width="1362" height="687" alt="image" src="https://github.com/user-attachments/assets/dcd9a3b7-ff52-49a2-9bc9-4a11e95df79a" />
<img width="1364" height="690" alt="image" src="https://github.com/user-attachments/assets/5ce89ace-ade7-4b4b-a121-5a94b8d7d81f" />
------------------------------------------------------------------------------------------------------------------------------------------

8. Administración:
<img width="1362" height="689" alt="image" src="https://github.com/user-attachments/assets/9c1a26a5-61c0-4c9a-aaf9-826eaaa5adfa" />
------------------------------------------------------------------------------------------------------------------------------------------

9. Mi perfil — estudiante:
<img width="1365" height="691" alt="image" src="https://github.com/user-attachments/assets/a254d754-0f8c-4619-955a-67ef07561bd3" />
<img width="1360" height="689" alt="image" src="https://github.com/user-attachments/assets/3217ef5f-aa20-4a36-bc8f-399f70572ab5" />
------------------------------------------------------------------------------------------------------------------------------------------


# Descripción del proyecto:
HUSRT-Control es una aplicación de escritorio para gestionar el control de acceso hospitalario de estudiantes en prácticas clínicas. Registra entradas y salidas en portería, valida reglas académicas (plan activo, docente presente, horas cumplidas) y centraliza la información para coordinadores, docentes y administradores.

# Objetivos
General: Automatizar y centralizar el control de acceso de estudiantes en prácticas hospitalarias.
Específicos:
Registrar accesos de estudiantes y docentes en portería.
Validar reglas antes de autorizar ingresos.
Gestionar universidades, servicios, planes y usuarios.
Mostrar información en tiempo real y generar reportes de horas.
Garantizar seguridad mediante roles, auditoría y control de acceso.

# Tecnologías utilizadas
Java 21 — Lógica del sistema
JavaFX 21 — Interfaz gráfica
FXML + CSS — Pantallas y diseño
MySQL 8.4 — Base de datos
Maven 3.9 — Compilación y dependencias
Git + GitHub — Control de versiones

# Características principales
Inicio de sesión seguro con roles y control de acceso.
Gestión de usuarios del sistema.
Interfaz gráfica intuitiva y organizada por módulos.
Conexión y persistencia en MySQL.
Arquitectura en capas, apta para mantenimiento y mejoras futuras.
