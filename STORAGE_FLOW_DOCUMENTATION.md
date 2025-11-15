# Documentación del Flujo de Almacenamiento - BoticaSaid

## Resumen General

El sistema de almacenamiento de BoticaSaid maneja la gestión de productos, stock, y proveedores con una arquitectura relacional robusta que permite el control detallado de inventario por lotes.

## Entidades Principales

### 1. Proveedor (Supplier)
Tabla que almacena información de los proveedores que suministran productos a la botica.

**Campos:**
- `id` (Long): Identificador único
- `nombre` (String): Nombre del proveedor (obligatorio)
- `contacto` (String): Nombre de la persona de contacto (opcional)
- `telefono` (String): Teléfono de contacto (opcional)
- `email` (String): Email de contacto (opcional)
- `direccion` (String): Dirección física del proveedor (opcional)
- `fecha_creacion` (Date): Timestamp de creación (auto)
- `fecha_actualizacion` (Date): Timestamp de última actualización (auto)
- `activo` (Boolean): Estado del proveedor (true por defecto)

**Relaciones:**
- OneToMany con Producto: Un proveedor puede suministrar múltiples productos

### 2. Producto (Product)
Tabla principal de productos que almacena información general del medicamento o producto.

**Campos:**
- `id` (Long): Identificador único
- `codigo_barras` (String): Código de barras único (opcional)
- `nombre` (String): Nombre del producto (obligatorio)
- `concentracion` (String): Concentración del medicamento (opcional)
- `cantidad_general` (Integer): Cantidad total en stock (calculado automáticamente)
- `cantidad_minima` (Integer): Umbral mínimo de stock (opcional)
- `precio_venta_und` (BigDecimal): Precio de venta por unidad (opcional)
- `descuento` (BigDecimal): Descuento aplicable (opcional)
- `laboratorio` (String): Laboratorio fabricante (opcional)
- `categoria` (String): Categoría del producto (opcional)
- `principio_activo` (String): Principio activo del medicamento (opcional)
- `tipo_medicamento` (String): Tipo (GENÉRICO/MARCA) (opcional)
- `presentacion` (String): Presentación del producto (opcional)
- `cantidad_unidades_blister` (Integer): Unidades por blister (opcional)
- `precio_venta_blister` (BigDecimal): Precio por blister (opcional)
- `proveedor_id` (Long): FK a Proveedor (nullable)
- `activo` (Boolean): Estado del producto
- `fecha_creacion`, `fecha_actualizacion`: Timestamps automáticos

**Relaciones:**
- ManyToOne con Proveedor: Un producto puede tener un proveedor asociado (nullable)
- OneToMany con Stock: Un producto puede tener múltiples lotes de stock

### 3. Stock (Inventory Batch)
Tabla que almacena los lotes individuales de stock con su información específica.

**Campos:**
- `id` (Int): Identificador único del lote
- `codigo_stock` (String): Código del lote (opcional)
- `cantidad_unidades` (Int): Cantidad de unidades en este lote
- `fecha_vencimiento` (LocalDate): Fecha de vencimiento del lote
- `precio_compra` (BigDecimal): Precio de compra de este lote
- `producto_id` (Long): FK a Producto

**Relaciones:**
- ManyToOne con Producto: Cada lote pertenece a un producto

## Flujo de Almacenamiento

### 1. Creación de Proveedor
```
POST /proveedores
Body: {
  "nombre": "Distribuidora Farmacéutica ABC",
  "contacto": "Juan Pérez",
  "telefono": "123456789",
  "email": "contacto@distribuidora.com",
  "direccion": "Av. Principal 123"
}
```

### 2. Creación de Producto con Stock Inicial
```
POST /productos/nuevo
Body: {
  "nombre": "Paracetamol 500mg",
  "codigoBarras": "1234567890",
  "laboratorio": "Lab ABC",
  "categoria": "Analgésicos",
  "precioVentaUnd": 5.50,
  "proveedorId": 1,
  "stocks": [
    {
      "codigoStock": "LOTE001",
      "cantidadUnidades": 100,
      "fechaVencimiento": "2025-12-31",
      "precioCompra": 3.00
    }
  ]
}
```

**Proceso:**
1. El sistema valida que el nombre del producto sea obligatorio
2. Si se proporciona un `proveedorId`, verifica que exista y esté activo
3. Si hay stocks en el request, los crea y calcula automáticamente `cantidadGeneral`
4. Si no hay stocks, `cantidadGeneral` se establece en 0 o el valor proporcionado
5. El producto se crea con la relación al proveedor establecida

### 3. Agregar Lotes de Stock (SIN modificar datos del producto)

**Endpoint Nuevo:** `POST /productos/agregar-lote`

```json
{
  "productoId": 1,  // O usar "codigoBarras": "1234567890"
  "lotes": [
    {
      "codigoStock": "LOTE002",
      "cantidadUnidades": 150,
      "fechaVencimiento": "2026-06-30",
      "precioCompra": 2.80
    },
    {
      "codigoStock": "LOTE003",
      "cantidadUnidades": 200,
      "fechaVencimiento": "2026-09-15",
      "precioCompra": 2.90
    }
  ]
}
```

**Características importantes:**
- ✅ NO modifica nombre, precio de venta, laboratorio, u otros datos del producto
- ✅ Solo agrega nuevos lotes de stock
- ✅ Actualiza automáticamente `cantidadGeneral` sumando las nuevas unidades
- ✅ Permite agregar uno o múltiples lotes en una sola operación
- ✅ Puede buscar el producto por ID o por código de barras

**Diferencia con `/productos/agregar-stock`:**
- `/agregar-stock`: Agrega UN solo lote
- `/agregar-lote`: Agrega MÚLTIPLES lotes en una operación

### 4. Actualizar Producto (incluyendo proveedor)
```
PUT /productos/{id}
Body: {
  "nombre": "Paracetamol 500mg - Actualizado",
  "proveedorId": 2,  // Cambiar o remover proveedor
  "precioVentaUnd": 6.00,
  ...
}
```

## Endpoints de la API

### Proveedores
- `POST /proveedores` - Crear nuevo proveedor
- `GET /proveedores` - Listar todos los proveedores activos
- `GET /proveedores?nombre={nombre}` - Buscar proveedores por nombre
- `GET /proveedores/{id}` - Obtener proveedor por ID
- `PUT /proveedores/{id}` - Actualizar proveedor
- `DELETE /proveedores/{id}` - Eliminar proveedor (borrado lógico)

### Productos
- `POST /productos/nuevo` - Crear producto con stock opcional
- `GET /productos` - Listar productos con paginación y filtros
- `GET /productos/{id}` - Obtener producto por ID
- `GET /productos/codigo-barras/{codigo}` - Obtener producto por código de barras
- `PUT /productos/{id}` - Actualizar producto
- `POST /productos/agregar-stock` - Agregar UN lote de stock
- `POST /productos/agregar-lote` - Agregar MÚLTIPLES lotes de stock (NUEVO)
- `DELETE /productos/{id}` - Eliminar producto (borrado lógico)

### Stock
- `GET /api/stock` - Listar stock con paginación y filtros
- `GET /api/stock/products` - Resumen de stock por producto
- `PUT /api/stock/{id}` - Actualizar lote de stock

## Reglas de Negocio

### Cantidad General
- Se calcula automáticamente como la suma de todos los lotes activos
- Al agregar lotes: `cantidadGeneral += suma(nuevos lotes)`
- Al vender: se descuenta del lote más próximo a vencer (FIFO)

### Proveedor
- La relación es **opcional** (nullable)
- Un producto puede existir sin proveedor asignado
- Solo se pueden asignar proveedores activos
- Si se intenta asignar un proveedor inactivo o inexistente, se establece como null

### Stock (Lotes)
- Cada lote tiene su propia fecha de vencimiento y precio de compra
- Permite el control FIFO (First In, First Out)
- El sistema puede rastrear qué lotes se vendieron

### Borrado Lógico
- Proveedores: Al eliminar, se marca `activo = false`
- Productos: Al eliminar, se marca `activo = false`
- Los registros inactivos no aparecen en consultas normales

## Mejores Prácticas

1. **Al recibir productos nuevos:**
   - Si es un producto nuevo, usar `POST /productos/nuevo` con stocks incluidos
   - Si es un producto existente, usar `POST /productos/agregar-lote` para no tocar datos del producto

2. **Gestión de proveedores:**
   - Crear proveedores antes de asignarlos a productos
   - Mantener información de contacto actualizada

3. **Control de inventario:**
   - Revisar regularmente productos con `cantidadGeneral < cantidadMinima`
   - Usar `GET /productos/stock-bajo?umbral=10` para alertas

4. **Trazabilidad:**
   - Usar `codigoStock` para identificar cada lote
   - Registrar correctamente la `fechaVencimiento` de cada lote

## Cambios Principales en esta Actualización

1. ✨ **Nueva entidad Proveedor** con CRUD completo
2. ✨ **Relación Producto-Proveedor** (nullable, ManyToOne)
3. ✨ **Nuevo endpoint `/productos/agregar-lote`** para operaciones de recepción de mercancía
4. ✅ ProductoResponse ahora incluye información del proveedor (`proveedorId`, `proveedorNombre`)
5. ✅ Tests comprehensivos agregados (23 tests, todos pasando)

## Ejemplo de Flujo Completo

```
1. Crear Proveedor
POST /proveedores -> ID: 1

2. Crear Producto con proveedor
POST /productos/nuevo
{
  "nombre": "Ibuprofeno 400mg",
  "proveedorId": 1,
  "stocks": [...]
}
-> ID: 10, cantidadGeneral: 100

3. Recibir nuevo lote del mismo producto
POST /productos/agregar-lote
{
  "productoId": 10,
  "lotes": [{
    "cantidadUnidades": 150,
    ...
  }]
}
-> cantidadGeneral ahora: 250

4. Consultar producto
GET /productos/10
-> Incluye proveedorId: 1, proveedorNombre: "Distribuidora ABC"
```

## Migraciones de Base de Datos

El sistema usa Hibernate con `ddl-auto=update`, por lo que las nuevas tablas y columnas se crearán automáticamente al iniciar la aplicación:

1. Tabla `proveedores` será creada
2. Columna `proveedor_id` será agregada a tabla `productos`
3. Foreign key constraint será establecida automáticamente

**Nota:** En producción, considerar usar migraciones explícitas (Flyway/Liquibase) para mayor control.
