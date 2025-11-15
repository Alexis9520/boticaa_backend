# Nuevas Funcionalidades - BoticaSaid Backend

Este documento describe las nuevas funcionalidades agregadas al sistema.

## Cambios en la Base de Datos

### Tabla Stock - Campos de Auditoría

Se agregaron dos nuevos campos a la tabla `stock` para rastrear cuándo se crean y modifican los registros:

- **`fecha_creacion`** (DATETIME): Se establece automáticamente cuando se crea un nuevo registro de stock. No se puede modificar posteriormente.
- **`fecha_modificacion`** (DATETIME): Se actualiza automáticamente cada vez que se modifica un registro de stock.

Estos campos se gestionan automáticamente mediante las anotaciones de Hibernate:
- `@CreationTimestamp` para fecha_creacion
- `@UpdateTimestamp` para fecha_modificacion

**Migración**: Al ejecutar la aplicación con `spring.jpa.hibernate.ddl-auto=update`, estas columnas se agregarán automáticamente a la tabla existente.

## Nuevos Endpoints

### 1. Listar Productos por Proveedor

**Endpoint**: `GET /productos/proveedor/{proveedorId}`

**Descripción**: Obtiene la lista de todos los productos activos asociados a un proveedor específico.

**Parámetros**:
- `proveedorId` (path): ID del proveedor

**Respuesta**: Lista de objetos `ProductoResponse`

**Ejemplo**:
```http
GET /productos/proveedor/1
```

### 2. Reporte de Lotes por Rango de Fechas

**Endpoint**: `GET /api/reports/lotes?fechaInicio=YYYY-MM-DD&fechaFin=YYYY-MM-DD`

**Descripción**: Genera un reporte de todos los lotes (stocks) agregados dentro de un rango de fechas específico, mostrando el producto asociado y los detalles del lote.

**Parámetros**:
- `fechaInicio` (query, requerido): Fecha de inicio en formato ISO 8601 (ej: 2025-01-01 o 2025-01-01T00:00:00)
- `fechaFin` (query, requerido): Fecha de fin en formato ISO 8601

**Respuesta**: Lista de objetos `LoteReportDTO` con:
- Información del producto (ID, nombre, código de barras)
- Información del stock/lote (ID, código de stock, cantidad, fecha de vencimiento, precio de compra)
- Fecha de creación del lote

**Ejemplo**:
```http
GET /api/reports/lotes?fechaInicio=2025-01-01&fechaFin=2025-12-31
```

**Respuesta de ejemplo**:
```json
[
  {
    "productoId": 1,
    "nombreProducto": "Paracetamol 500mg",
    "codigoBarras": "123456789",
    "stockId": 100,
    "codigoStock": "LOTE-2025-001",
    "cantidadUnidades": 100,
    "fechaVencimiento": "2026-12-31",
    "precioCompra": 5.50,
    "fechaCreacion": "2025-11-15T10:30:00"
  }
]
```

### 3. Reporte de Proveedores con sus Productos

**Endpoint**: `GET /api/reports/proveedores`

**Descripción**: Genera un reporte completo de todos los proveedores activos con su lista de productos asociados.

**Parámetros**: Ninguno

**Respuesta**: Lista de objetos `ProveedorReportDTO` con:
- Información del proveedor (ID, RUC, razón comercial, correo, dirección)
- Total de productos del proveedor
- Lista detallada de productos (ID, nombre, código de barras, categoría, laboratorio, cantidad general)

**Ejemplo**:
```http
GET /api/reports/proveedores
```

**Respuesta de ejemplo**:
```json
[
  {
    "proveedorId": 1,
    "ruc": "20123456789",
    "razonComercial": "Farmacia ABC S.A.C.",
    "correo": "contacto@farmaciabc.com",
    "direccion": "Av. Principal 123",
    "totalProductos": 2,
    "productos": [
      {
        "productoId": 1,
        "nombre": "Paracetamol 500mg",
        "codigoBarras": "123456789",
        "categoria": "Analgésico",
        "laboratorio": "Lab ABC",
        "cantidadGeneral": 500
      },
      {
        "productoId": 2,
        "nombre": "Ibuprofeno 400mg",
        "codigoBarras": "987654321",
        "categoria": "Antiinflamatorio",
        "laboratorio": "Lab XYZ",
        "cantidadGeneral": 300
      }
    ]
  }
]
```

### 4. Exportar Productos por Rango de Fechas

**Endpoint**: `GET /api/reports/productos/export?fechaInicio=YYYY-MM-DD&fechaFin=YYYY-MM-DD`

**Descripción**: Exporta a un archivo Excel (.xlsx) todos los productos creados dentro de un rango de fechas específico.

**Parámetros**:
- `fechaInicio` (query, requerido): Fecha de inicio en formato ISO 8601
- `fechaFin` (query, requerido): Fecha de fin en formato ISO 8601

**Respuesta**: Archivo Excel con las siguientes columnas:
- ID
- Código de Barras
- Nombre
- Categoría
- Laboratorio
- Concentración
- Presentación
- Stock Total
- Precio por Unidad
- Fecha de Creación

**Ejemplo**:
```http
GET /api/reports/productos/export?fechaInicio=2025-01-01&fechaFin=2025-12-31
```

## Casos de Uso

### Caso 1: Auditoría de Inventario
Un administrador necesita revisar todos los lotes agregados durante el último mes para verificar las compras realizadas:
```http
GET /api/reports/lotes?fechaInicio=2025-10-01&fechaFin=2025-10-31
```

### Caso 2: Catálogo de Proveedor
El gerente desea ver qué productos está suministrando un proveedor específico:
```http
GET /productos/proveedor/5
```

### Caso 3: Reporte Ejecutivo
La dirección solicita un reporte completo de todos los proveedores y sus productos para evaluar la diversificación:
```http
GET /api/reports/proveedores
```

### Caso 4: Control de Nuevos Productos
El equipo de contabilidad necesita exportar todos los productos registrados en el último trimestre para actualizar el sistema contable:
```http
GET /api/reports/productos/export?fechaInicio=2025-07-01&fechaFin=2025-09-30
```

## Notas Técnicas

### Formato de Fechas
Los endpoints aceptan fechas en formato ISO 8601:
- Solo fecha: `2025-01-01`
- Fecha y hora: `2025-01-01T10:30:00`
- Con zona horaria: `2025-01-01T10:30:00-05:00`

El sistema convierte automáticamente:
- `fechaInicio` a las 00:00:00 del día especificado
- `fechaFin` a las 23:59:59 del día especificado

### Autenticación
Todos los nuevos endpoints requieren autenticación mediante Bearer Token en el header:
```
Authorization: Bearer <token>
```

### Paginación
Los endpoints de listado no incluyen paginación por defecto. Si se requiere paginación en el futuro, se puede agregar utilizando los parámetros estándar `page` y `size`.

### Rendimiento
- El reporte de proveedores carga eager todos los productos asociados
- El reporte de lotes utiliza JOIN FETCH para evitar el problema N+1
- La exportación de productos usa streaming para archivos grandes

## Seguridad

### Validaciones Implementadas
- Las fechas son validadas antes de ejecutar las consultas
- Solo se retornan productos y proveedores activos
- Los lotes se filtran por fecha de creación, no por fecha de vencimiento

### Consideraciones
- Los reportes pueden contener información sensible de precios
- Se recomienda aplicar restricciones de roles según sea necesario
- Los archivos Excel generados no tienen protección con contraseña
