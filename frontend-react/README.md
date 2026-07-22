# frontend-react

Segunda versión del frontend del sistema de cotización de drones, en React.
Consume **la misma API** Spring Boot que el cliente JavaFX (`src/main/java/.../vista`);
ninguno de los dos sustituye al otro, conviven contra el mismo backend.

Proyecto independiente del de Maven: se compila con npm y no participa del
`mvnw package` del backend.

Vite 8 · React 19 · React Router 7 · Tailwind CSS 4

---

## Cómo levantarlo

Hacen falta **dos terminales**: el backend y el frontend son procesos separados.

**1. Backend** (desde la raíz del repositorio):

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Queda en `http://localhost:8080`. Necesita MySQL levantado.

**2. Frontend** (desde esta carpeta):

```bash
npm install
```

```bash
npm run dev
```

Queda en `http://localhost:5173`.

> El puerto **5173 es obligatorio**: es el único origen que `SecurityConfig`
> permite por CORS. Por eso `vite.config.js` usa `strictPort: true` — si el
> puerto está ocupado, Vite falla con un mensaje claro en vez de moverse al
> 5174 y dejarte depurando errores de CORS que en realidad son de puerto.
>
> Si necesitas otro puerto, hay que cambiarlo en los dos lados: en
> `vite.config.js` y en `corsConfigurationSource()` de `SecurityConfig.java`.

Para apuntar a un backend en otra dirección, sin tocar código:

```bash
echo "VITE_API_URL=http://otro-host:8080" > .env.local
```

## Compilar para producción

```bash
npm run build
```

Deja el sitio estático en `dist/`.

---

## Estructura

```
src/
  api/
    apiClient.js     fetch nativo; adjunta el Bearer salvo en login/registro
    tiposDron.js     traducción UI <-> backend (ver nota abajo)
  auth/
    AuthContext.jsx  sesión en memoria (token, username, rol)
    RutaProtegida.jsx  redirige a /login; opcionalmente exige ADMINISTRADOR
  components/
    ui.jsx           primitivos derivados de los tokens del diseño
    AppShell.jsx     cabecera 64px + barra lateral 220px
  pages/             Login, Registro, SeleccionTipo, Cotizacion, Factura, AdminCatalogo
  styles/index.css   sistema de diseño (@theme de Tailwind v4) + estilos de impresión
```

## Dos cosas que conviene saber antes de tocar el código

**El tipo "pesado" no existe en el backend.** La interfaz habla de
"Carga pesada", pero `DronServicio` solo reconoce `LIVIANO`, `CARGA` y
`EMERGENCIA`; mandar `PESADO` devuelve 400. Esa traducción vive **solo** en
`src/api/tiposDron.js` y ninguna pantalla escribe el tipo a mano.

**La sesión no se persiste.** El token vive en memoria (React Context), no en
`localStorage`: ahí sería legible por cualquier script de la página y es válido
24 horas. La consecuencia aceptada es que **al recargar la página se cierra la
sesión**. Es el mismo criterio del `SessionManager` del cliente JavaFX.
