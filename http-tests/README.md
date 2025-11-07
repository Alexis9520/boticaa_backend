# HTTP Test Files for Botica Said API

Este directorio contiene archivos `.http` para realizar pruebas manuales de la API REST de Botica Said. Estos archivos pueden ser utilizados con extensiones de VS Code o IntelliJ IDEA para ejecutar requests HTTP directamente desde el editor.

## 🛠️ Herramientas Compatibles

### Visual Studio Code
Instalar la extensión: **REST Client** (por Huachao Mao)
- Extensión ID: `humao.rest-client`
- [Link en VS Code Marketplace](https://marketplace.visualstudio.com/items?itemName=humao.rest-client)

### IntelliJ IDEA
- Soporte nativo para archivos `.http`
- No requiere extensiones adicionales

### Otras herramientas
- **JetBrains HTTP Client** (línea de comandos)
- Cualquier herramienta que soporte el formato de archivos HTTP

## 📁 Archivos Disponibles

1. **auth.http** - Autenticación y registro de usuarios
   - Login
   - Registro de nuevos usuarios
   - Diferentes roles (VENDEDOR, ADMINISTRADOR)

2. **producto.http** - Operaciones CRUD de productos
   - Crear productos con stock inicial
   - Consultar por ID o código de barras
   - Actualizar información
   - Agregar stock
   - Eliminar productos

3. **venta.http** - Operaciones de ventas
   - Registrar ventas
   - Diferentes métodos de pago (efectivo, digital, mixto)
   - Consultar ventas por fecha, cliente, vendedor
   - Estadísticas de ventas

4. **caja.http** - Gestión de caja
   - Apertura y cierre de caja
   - Movimientos de efectivo
   - Consultar saldos
   - Historial de cajas

5. **stock.http** - Gestión de inventario
   - Consultar stock por producto
   - Stock bajo y productos vencidos
   - Agregar stock
   - Reportes de inventario

6. **dashboard.http** - Dashboard y analíticas
   - Resúmenes de ventas
   - Productos más vendidos
   - Productos críticos
   - Métricas de rendimiento

7. **boleta.http** - Gestión de boletas/recibos
   - Consultar boletas
   - Imprimir boletas
   - Cancelar boletas
   - Exportar reportes

## 🚀 Cómo Usar

### 1. Configurar Variables

Antes de ejecutar las peticiones, actualiza las siguientes variables en cada archivo:

```http
@baseUrl = http://localhost:8080
@authToken = Bearer YOUR_TOKEN_HERE
```

### 2. Obtener Token de Autenticación

1. Abre el archivo `auth.http`
2. Ejecuta el request "Register New User" o "Login with User Credentials"
3. Copia el token JWT del response
4. Actualiza la variable `@authToken` en los demás archivos

### 3. Ejecutar Requests

**En VS Code con REST Client:**
1. Abre cualquier archivo `.http`
2. Verás un botón "Send Request" sobre cada petición
3. Haz clic para ejecutar la petición
4. El resultado aparecerá en un panel lateral

**En IntelliJ IDEA:**
1. Abre cualquier archivo `.http`
2. Verás un ícono de "play" (▶) al lado de cada petición
3. Haz clic para ejecutar
4. El resultado aparecerá en la parte inferior

### 4. Secuencia Recomendada para Pruebas Iniciales

1. **Registrar usuario** (`auth.http` - request #1)
2. **Login** (`auth.http` - request #2)
3. **Copiar token** del response
4. **Actualizar `@authToken`** en todos los archivos
5. **Crear productos** (`producto.http` - request #1)
6. **Abrir caja** (`caja.http` - request #1)
7. **Registrar ventas** (`venta.http` - request #1)
8. **Ver dashboard** (`dashboard.http` - request #1)

## 📝 Notas Importantes

- **Servidor Local**: Por defecto, los archivos apuntan a `http://localhost:8080`. Cambia `@baseUrl` si tu servidor está en otro puerto o host.

- **Autenticación**: La mayoría de los endpoints requieren autenticación. Asegúrate de tener un token válido.

- **Orden de Operaciones**: Algunos endpoints requieren que existan datos previos (ej: no puedes registrar una venta sin productos).

- **Validación de Datos**: Los datos en los ejemplos son ficticios. Ajústalos según tus necesidades.

- **Códigos de Barras**: Usa códigos de barras válidos de productos existentes en tu base de datos.

## 🔍 Ejemplos de Variables

Puedes personalizar estas variables según tus datos:

```http
# IDs y Códigos
@productoId = 1
@codigoBarras = 7501234567890
@ventaId = 1
@cajaId = 1
@boletaNumero = BOL-001

# Usuario
@dniUsuario = 87654321
@authToken = Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Configuración
@baseUrl = http://localhost:8080
@contentType = application/json
```

## 📊 Códigos de Respuesta HTTP

- `200 OK` - Operación exitosa
- `201 Created` - Recurso creado exitosamente
- `400 Bad Request` - Datos inválidos
- `401 Unauthorized` - Token inválido o ausente
- `403 Forbidden` - No tienes permisos
- `404 Not Found` - Recurso no encontrado
- `500 Internal Server Error` - Error del servidor

## 🤝 Contribuir

Si encuentras algún error o quieres agregar más tests:
1. Crea una nueva petición en el archivo correspondiente
2. Usa la misma estructura y formato
3. Documenta claramente qué hace cada petición

## 📞 Soporte

Para más información sobre la API, consulta:
- Documentación Swagger: `http://localhost:8080/compilado`
- README del proyecto principal
