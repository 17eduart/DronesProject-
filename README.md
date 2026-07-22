# DronesProject

API REST de gestion de drones con autenticacion JWT y roles
(`ADMINISTRADOR` / `CLIENTE`). Proyecto final de POO — EPN.

Spring Boot 4.1 · Java 21 · MySQL 8

---

## Configuracion

### Base de datos

`src/main/resources/application.properties` apunta por defecto a:

```
jdbc:mysql://localhost:3306/drones_proyecto
```

Con `spring.jpa.hibernate.ddl-auto=update`, Hibernate crea y actualiza las
tablas solo; basta con que la base `drones_proyecto` exista.

### El secreto de firma de los JWT

`jwt.secret` es la clave con la que se firman los tokens. **Quien la tenga
puede fabricar un token valido para cualquier usuario, incluido un
ADMINISTRADOR**, asi que no puede vivir en el repositorio.

En `application.properties` (el que si se sube) esta declarado asi:

```properties
jwt.secret=${JWT_SECRET:<valor-de-desarrollo-publico>}
```

Es decir: usa la variable de entorno `JWT_SECRET` si existe y, si no, cae a un
valor de desarrollo que esta committeado a proposito para que cualquiera pueda
clonar el repo y arrancar sin configurar nada. Ese valor **no sirve para
produccion**: es publico por definicion.

#### Opcion recomendada para desarrollo local: el perfil `local`

1. Crea `src/main/resources/application-local.properties`
   (ya esta en `.gitignore`, nunca se sube):

   ```properties
   jwt.secret=<tu-secreto-en-Base64>
   ```

2. Genera un secreto valido. Tiene que ser **Base64** de al menos 32 bytes
   (256 bits, minimo de HS256); `JwtService` lo decodifica con
   `Base64.getDecoder()`, asi que un texto libre tipo `cambiame` hace fallar
   el arranque:

   ```powershell
   $b = New-Object byte[] 48
   [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b)
   [Convert]::ToBase64String($b)
   ```

3. Arranca con el perfil activo:

   ```bash
   mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
   ```

   Si ejecutas el jar empaquetado:

   ```bash
   java -jar target/DronesProject-1.0-SNAPSHOT.jar --spring.profiles.active=local
   ```

   En IntelliJ: *Run/Debug Configurations* → campo **Active profiles** → `local`.

`application-local.properties` solo necesita las claves que quieras
sobrescribir; el resto se sigue leyendo de `application.properties`.

#### Alternativa: variable de entorno

```powershell
$env:JWT_SECRET = "<tu-secreto-en-Base64>"
mvnw.cmd spring-boot:run
```

#### Cual usar

Para esta demo local, **el perfil `local`**. La razon es que se configura una
sola vez y sobrevive: queda en un archivo que sigue ahi despues de cerrar la
terminal, de reiniciar la maquina y de cambiar de IDE. Una variable de entorno
en Windows es de una sola sesion de consola (`$env:...` muere al cerrarla) o
te obliga a `setx`, que la deja puesta globalmente para todos los procesos del
usuario — mas invasivo de lo que hace falta para levantar un proyecto de
clase. Ademas el archivo de perfil escala: si manana quieres un puerto o una
contraseña de base distintos en tu maquina, los agregas ahi sin inventar una
variable nueva por cada cosa.

La variable de entorno es la opcion correcta en un servidor, un contenedor o
un CI, donde no controlas el sistema de archivos y los secretos los inyecta el
orquestador. Ahi el perfil no aplica.

#### Precedencia (verificada, no supuesta)

Si coexisten varias fuentes, gana la de mas arriba:

| Prioridad | Fuente |
|---|---|
| 1 | Variable de entorno `JWT_SECRET` |
| 2 | `application-local.properties` (con el perfil `local` activo) |
| 3 | Valor por defecto de `application.properties` |

Conviene tenerlo presente: si un dia el perfil `local` "no toma efecto",
lo primero que hay que mirar es si quedo un `JWT_SECRET` colgado en el
entorno, porque ese gana.

### Cambiar la version de Java

Se controla con **una sola propiedad** en `pom.xml`:

```xml
<properties>
    <java.version>21</java.version>
</properties>
```

No sirve poner `<source>`/`<target>` en `maven-compiler-plugin`: el parent de
Spring Boot define `<release>${java.version}</release>`, y `release` gana
sobre ambos. Para comprobar la version efectiva:

```bash
mvnw.cmd help:evaluate -Dexpression=maven.compiler.release -q -DforceStdout
```

---

## Compilar y probar

```bash
mvnw.cmd compile
```

```bash
mvnw.cmd test
```

Los tests de servicio (`@SpringBootTest`) necesitan MySQL levantado; los de
modelo y los de capa web (`@WebMvcTest`) no.
