# Provider (Proveedor) API Changes

## Overview
Se ha implementado soporte para proveedores (suppliers) con una relación muchos-a-muchos con productos. Ahora cada producto puede tener múltiples proveedores asociados.

Este cambio permite:
- Registrar múltiples proveedores en el sistema
- Asociar varios proveedores a cada producto
- Mantener un registro del precio de compra por proveedor
- Marcar un proveedor como principal para cada producto

## Nuevos Endpoints de Proveedores

### 1. Crear Proveedor
**POST** `/proveedores`
```json
{
  "nombre": "Proveedor ABC",
  "ruc": "20123456789",
  "telefono": "+51999888777",
  "email": "contacto@proveedor.com",
  "direccion": "Av. Los Proveedores 123",
  "contacto": "Juan Pérez"
}
```

### 2. Listar Proveedores (con paginación)
**GET** `/proveedores?q=ABC&activo=true&page=0&size=10`

Parámetros opcionales:
- `q`: búsqueda por nombre o RUC
- `activo`: filtrar por estado (true/false)
- `page`: número de página (default: 0)
- `size`: tamaño de página (default: 10)

### 3. Obtener Proveedores Activos
**GET** `/proveedores/activos`

### 4. Obtener Proveedor por ID
**GET** `/proveedores/{id}`

### 5. Actualizar Proveedor
**PUT** `/proveedores/{id}`
```json
{
  "nombre": "Proveedor ABC Actualizado",
  "ruc": "20123456789",
  "telefono": "+51999888777",
  "email": "nuevo@proveedor.com",
  "direccion": "Nueva Dirección",
  "contacto": "María García"
}
```

### 6. Eliminar Proveedor (borrado lógico)
**DELETE** `/proveedores/{id}`

## Cambios en Endpoints de Productos

### ProductoRequest (Crear/Actualizar)
Se añadió un campo opcional `proveedorIds`:

```json
{
  "nombre": "Paracetamol 500mg",
  "codigoBarras": "7501234567890",
  "categoria": "Analgésicos",
  "laboratorio": "Laboratorio XYZ",
  "precioVentaUnd": 5.50,
  "proveedorIds": [1, 2, 3],  // <-- NUEVO: Lista de IDs de proveedores
  "stocks": [...]
}
```

**Nota importante**: 
- Si `proveedorIds` se omite o está vacío, se mantienen los proveedores existentes
- Si se proporciona una lista, se reemplazarán todos los proveedores del producto

### ProductoResponse
Ahora incluye información de proveedores:

```json
{
  "id": 1,
  "nombre": "Paracetamol 500mg",
  "codigoBarras": "7501234567890",
  "categoria": "Analgésicos",
  "laboratorio": "Laboratorio XYZ",
  "precioVentaUnd": 5.50,
  "stocks": [...],
  "proveedores": [  // <-- NUEVO
    {
      "id": 1,
      "nombre": "Proveedor ABC",
      "ruc": "20123456789",
      "precioCompra": 3.50,
      "esPrincipal": false
    },
    {
      "id": 2,
      "nombre": "Proveedor XYZ",
      "ruc": "20987654321",
      "precioCompra": 3.20,
      "esPrincipal": true
    }
  ]
}
```

## Endpoints de Productos NO Modificados

Los siguientes endpoints continúan funcionando exactamente igual:
- `GET /productos` - Listar productos con paginación
- `GET /productos/{id}` - Obtener producto por ID
- `GET /productos/codigo-barras/{codigo}` - Obtener por código de barras
- `GET /productos/buscar` - Buscar productos
- `GET /productos/stock-bajo` - Productos con stock bajo
- `DELETE /productos/{id}` - Eliminar producto
- `POST /productos/agregar-stock` - Agregar stock

## Migración para Frontend

### Cambios Mínimos Requeridos:
1. **Opcional**: Mostrar la lista de proveedores en la interfaz de productos
2. **Opcional**: Permitir seleccionar proveedores al crear/editar productos
3. **Sin cambios obligatorios**: Todos los endpoints existentes siguen funcionando

### Recomendaciones:
1. Agregar una nueva sección "Proveedores" en el menú
2. Crear un componente para gestionar proveedores (CRUD)
3. En el formulario de productos, agregar un selector múltiple de proveedores
4. Mostrar los proveedores asociados en la vista de detalle del producto

## Base de Datos

### Nuevas Tablas:
1. **proveedores**: Información de proveedores
2. **producto_proveedor**: Tabla intermedia para la relación muchos-a-muchos

Las tablas se crearán automáticamente al iniciar la aplicación gracias a JPA/Hibernate.
