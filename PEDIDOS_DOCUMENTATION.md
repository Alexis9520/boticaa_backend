# Documentación de la Funcionalidad de Pedidos

## Resumen

Esta funcionalidad permite agregar stock a productos existentes y automáticamente crear registros de pedidos asociados a cada lote de stock agregado. Esto facilita el seguimiento de las compras de inventario.

## Entidad Pedido

La tabla `pedidos` almacena información sobre los pedidos de stock realizados.

### Campos:

- `id` (Long): Identificador único del pedido (auto-generado)
- `id_proveedor` (Long): FK a Proveedor (nullable) - Se obtiene automáticamente del producto
- `id_producto` (Long): FK a Producto (nullable) - Se obtiene del stock agregado
- `id_stock` (Int): FK a Stock (NOT NULL) - Referencia al lote de stock del pedido
- `fecha_de_pedido` (LocalDate): Fecha en que se realizó el pedido (NOT NULL)
- `fecha_de_creacion` (LocalDateTime): Timestamp de creación automático
- `fecha_de_modificacion` (LocalDateTime): Timestamp de última modificación automático

### Relaciones:

- ManyToOne con Proveedor (nullable)
- ManyToOne con Producto (nullable)
- ManyToOne con Stock (NOT NULL)

## Endpoint: Agregar Stock con Pedido

### URL
```
POST /api/pedidos/agregar-stock
```

### Descripción
Este endpoint permite agregar uno o múltiples lotes de stock a un producto existente y automáticamente crea un registro de pedido por cada lote agregado.

### Flujo de Operación

1. Recibe los datos del stock a agregar y la fecha de pedido
2. Busca el producto por ID o código de barras
3. Agrega el/los lote(s) de stock al producto
4. Actualiza la cantidad general del producto
5. Crea un registro de pedido por cada lote agregado, incluyendo:
   - Referencia al stock creado
   - Referencia al producto
   - Referencia al proveedor (si el producto tiene uno asociado)
   - Fecha de pedido proporcionada

### Request Body

```json
{
  "stockData": {
    "productoId": 1,  // Usar ID del producto
    // O alternativamente usar código de barras:
    // "codigoBarras": "1234567890",
    "lotes": [
      {
        "codigoStock": "LOTE001",
        "cantidadUnidades": 100,
        "fechaVencimiento": "2025-12-31",
        "precioCompra": 10.50
      },
      {
        "codigoStock": "LOTE002",
        "cantidadUnidades": 150,
        "fechaVencimiento": "2026-06-30",
        "precioCompra": 11.00
      }
    ]
  },
  "fechaDePedido": "2024-11-19"
}
```

### Response - Éxito (200 OK)

```json
{
  "success": true,
  "message": "Stock agregado y pedido(s) creado(s) exitosamente"
}
```

### Response - Error (400 Bad Request)

```json
{
  "success": false,
  "message": "Error al agregar stock o crear pedido. Verifique que el producto exista y esté activo."
}
```

## Ejemplo de Uso Completo

### 1. Crear un proveedor (si no existe)
```bash
POST /api/proveedores
{
  "ruc": "20123456789",
  "razonComercial": "Distribuidora Farmacéutica ABC",
  "numero1": "987654321",
  "correo": "contacto@distribuidora.com"
}
```

### 2. Crear un producto con el proveedor
```bash
POST /api/productos/nuevo
{
  "nombre": "Paracetamol 500mg",
  "codigoBarras": "1234567890",
  "laboratorio": "Lab ABC",
  "categoria": "Analgésicos",
  "precioVentaUnd": 5.50,
  "proveedorId": 1
}
```

### 3. Agregar stock con pedido
```bash
POST /api/pedidos/agregar-stock
{
  "stockData": {
    "productoId": 1,
    "lotes": [
      {
        "codigoStock": "LOTE20241119",
        "cantidadUnidades": 500,
        "fechaVencimiento": "2026-11-19",
        "precioCompra": 3.50
      }
    ]
  },
  "fechaDePedido": "2024-11-19"
}
```

### Resultado:
- Se agrega 1 lote de stock al producto
- Se crea 1 registro en la tabla `pedidos` con:
  - id_stock: ID del stock creado
  - id_producto: 1
  - id_proveedor: 1 (del producto)
  - fecha_de_pedido: 2024-11-19
- La cantidad general del producto se actualiza sumando 500 unidades

## Casos de Uso

### 1. Recepción de mercancía con proveedor conocido
Cuando llega un pedido de un proveedor, usar este endpoint para:
- Registrar el stock recibido
- Crear automáticamente el registro de pedido
- Mantener la trazabilidad del origen del stock

### 2. Recepción de mercancía sin proveedor
Si el producto no tiene proveedor asignado:
- El campo `id_proveedor` del pedido será NULL
- Se registra igualmente el stock y el pedido

### 3. Recepción de múltiples lotes
Un solo request puede registrar múltiples lotes:
- Se crean múltiples stocks
- Se crean múltiples pedidos (uno por lote)
- Todos con la misma fecha de pedido

## Validaciones

El endpoint validará:
- ✅ Que se proporcionen tanto stockData como fechaDePedido
- ✅ Que el producto exista (por ID o código de barras)
- ✅ Que el producto esté activo
- ✅ Que se proporcione al menos un lote de stock
- ✅ Que cada lote tenga los datos requeridos (cantidad, precio de compra, etc.)

## Diferencias con otros endpoints

### vs `/api/productos/agregar-lote`
- `/api/productos/agregar-lote`: Solo agrega stock, no crea registros de pedido
- `/api/pedidos/agregar-stock`: Agrega stock Y crea registros de pedido

### vs `/api/productos/agregar-stock`
- `/api/productos/agregar-stock`: Agrega UN solo lote, no crea pedidos
- `/api/pedidos/agregar-stock`: Agrega múltiples lotes Y crea pedidos

## Consideraciones

1. **Transaccionalidad**: Todo el proceso es transaccional. Si falla alguna parte, se revierte toda la operación.

2. **Fecha de pedido**: Puede ser diferente a la fecha de creación del registro. Esto permite registrar pedidos históricos.

3. **Proveedor automático**: El proveedor se obtiene automáticamente del producto, no necesita especificarse en el request.

4. **Trazabilidad**: Cada pedido mantiene referencias al stock específico, producto y proveedor, permitiendo auditorías completas.

5. **Timestamps automáticos**: Las fechas de creación y modificación se manejan automáticamente por Hibernate.
