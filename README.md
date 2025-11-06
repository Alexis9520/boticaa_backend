# Botica_API

Botica_API es una API RESTful desarrollada en Java que proporciona las funcionalidades esenciales para la gestión de una botica (farmacia). Incluye control de usuarios, autenticación segura con JWT, manejo de caja, gestión de productos y stock bajo lógica FIFO, todo trabajando sobre una base de datos local. El despliegue se realiza fácilmente a través de un archivo `.jar`.

## Descarga rápida

[Descargar Botica_API.jar](https://github.com/LoP-1/Botica_API/releases/tag/api-rest)

## Funcionalidades principales

- **Gestión de usuarios:**  
  - Registro de nuevos usuarios.
  - Autenticación (login) segura utilizando JWT.
  - Rutas protegidas mediante tokens JWT para garantizar la seguridad de la API.

- **Caja:**  
  - Control de operaciones de caja (apertura, cierre, movimientos, etc.).

- **Productos y stock:**  
  - Alta, baja y modificación de productos.
  - Gestión de stock con lógica FIFO para salidas.
  - Consulta y control de existencias.

- **Base de datos local:**  
  - Persistencia de la información en una base de datos local.

## Tecnologías y Calidad de Código

### Stack Tecnológico
- **Java 17** - Lenguaje principal
- **Spring Boot 3.5.0** - Framework principal
- **Spring Security** - Autenticación y autorización
- **Spring Data JPA** - Persistencia de datos
- **MySQL** - Base de datos en producción
- **H2** - Base de datos para testing
- **Maven** - Gestión de dependencias
- **JaCoCo** - Cobertura de código
- **SonarQube** - Análisis estático de código

### CI/CD y Análisis de Calidad
Este proyecto incluye integración continua con GitHub Actions que automatiza:

- ✅ **Compilación** del proyecto con Maven
- ✅ **Ejecución de pruebas unitarias** con perfiles de test (H2)
- ✅ **Análisis de código con SonarQube** para calidad y seguridad
- ✅ **Generación de reportes de cobertura** con JaCoCo
- ✅ **Quality Gates** para asegurar estándares mínimos

## Desarrollo y Testing

### Configuración del Entorno de Desarrollo

#### Prerrequisitos
- Java 17+
- Maven 3.6+
- MySQL (para desarrollo local)
- SonarQube (opcional, para análisis local)

#### Configuración de la Base de Datos
1. **Desarrollo local** (MySQL):
   ```bash
   # Crear base de datos
   CREATE DATABASE botica_v2;
   
   # Configurar en src/main/resources/application.properties
   spring.datasource.url=jdbc:mysql://localhost:3306/botica_v2
   spring.datasource.username=root
   spring.datasource.password=tu_password
   ```

2. **Testing** (H2 automático):
   Las pruebas utilizan automáticamente H2 en memoria con el perfil `test`.

### Comandos de Desarrollo

#### Compilar el proyecto
```bash
./mvnw clean compile
```

#### Ejecutar pruebas
```bash
# Todas las pruebas
./mvnw test

# Con reporte de cobertura
./mvnw clean test jacoco:report
```

#### Ejecutar la aplicación
```bash
# Desarrollo
./mvnw spring-boot:run

# Producción
java -jar target/BoticaSaid-0.0.1-SNAPSHOT.jar
```

## SonarQube - Análisis de Calidad de Código

### Configuración de SonarQube

#### 1. Configuración en GitHub (recomendado)
Para habilitar el análisis automático de SonarQube en CI/CD:

1. **Configurar Secrets** en tu repositorio de GitHub:
   - `SONAR_TOKEN`: Token de autenticación de SonarQube

2. **Configurar Variables** en tu repositorio de GitHub:
   - `SONAR_HOST_URL`: URL de tu instancia de SonarQube (ej: `https://sonarqube.tu-dominio.com`)

#### 2. SonarQube Local
Para ejecutar análisis de SonarQube localmente:

```bash
# Opción 1: Con servidor SonarQube local
./mvnw clean test jacoco:report sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=tu_token_local

# Opción 2: Con SonarQube Cloud
./mvnw clean test jacoco:report sonar:sonar \
  -Dsonar.host.url=https://sonarcloud.io \
  -Dsonar.organization=tu_organizacion \
  -Dsonar.token=tu_token
```

#### 3. Configuración del Servidor SonarQube

Si necesitas configurar tu propia instancia de SonarQube:

```bash
# Con Docker
docker run -d --name sonarqube \
  -p 9000:9000 \
  sonarqube:lts-community

# Acceder a http://localhost:9000
# Usuario/Password inicial: admin/admin
```

### Métricas Analizadas

SonarQube analiza automáticamente:

- **🔍 Calidad del Código**: Complejidad, mantenibilidad, legibilidad
- **🛡️ Vulnerabilidades de Seguridad**: Problemas de seguridad conocidos
- **🧪 Cobertura de Código**: Porcentaje de código cubierto por tests
- **📋 Duplicación de Código**: Código duplicado y redundante
- **⚠️ Code Smells**: Problemas de diseño y estilo
- **📊 Technical Debt**: Estimación del tiempo para resolver issues

### Workflow de CI/CD

El workflow `.github/workflows/ci-sonarqube.yml` se ejecuta automáticamente:

- **En Push** a `main` o `develop`
- **En Pull Requests** a `main` o `develop`

#### Pasos del Workflow:
1. ☕ **Setup Java 17** y cache de dependencias
2. 🔧 **Compilación** del proyecto
3. 🧪 **Ejecución de tests** con H2
4. 📊 **Generación de reportes** de cobertura
5. 🔍 **Análisis SonarQube** (si está configurado)
6. 💬 **Comentario en PR** con resultados
7. ✅ **Quality Gate** verification

## Despliegue

1. **Compilación:**  
   Asegúrate de tener Java instalado. Compila el proyecto y genera el archivo `.jar`.

2. **Ejecución:**  
   Ejecuta el archivo `.jar` con el siguiente comando:
   ```bash
   java -jar BoticaSaid-0.0.1-SNAPSHOT.jar
   ```

3. **Configuración:**  
   Modifica los parámetros de conexión a la base de datos en el archivo de configuración según tu entorno local.

## Autenticación y Seguridad

- El acceso a la mayoría de los endpoints requiere autenticación con JWT.
- Obtén un token realizando login y úsalo en la cabecera `Authorization: Bearer <token>` para acceder a rutas protegidas.

## Estructura de la API (resumida)

- `/usuarios`  
  - POST `/registro`: Crear usuario  
  - POST `/login`: Autenticación y obtención de JWT

- `/productos`  
  - CRUD de productos  
  - Gestión de stock

- `/caja`  
  - Manejo de movimientos de caja

> **Nota:** Consulta la documentación Swagger en `/compilado` o el código fuente para más detalles sobre las rutas y parámetros disponibles.

## Contribución y Calidad

Este proyecto mantiene altos estándares de calidad mediante:

- **Tests automatizados** con cobertura medida
- **Análisis estático** de código con SonarQube
- **CI/CD** automatizado con GitHub Actions
- **Quality Gates** que bloquean merge de código con problemas

Para contribuir:
1. Fork el repositorio
2. Crea una feature branch
3. Escribe tests para tu código
4. Asegúrate de que pase el Quality Gate
5. Crea un Pull Request

## Acerca del proyecto

Este proyecto busca ofrecer una solución sencilla y segura para la gestión de una botica tradicional, enfocándose en la facilidad de uso, seguridad y control de stock eficiente.

---
