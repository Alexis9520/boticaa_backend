# Implementation Summary: Pedidos (Orders) Feature

## Overview
Implemented a new feature to track orders (pedidos) when adding stock to products. This creates a complete audit trail for inventory purchases.

## Changes Made

### 1. New Database Entity: `Pedido`
**File**: `src/main/java/quantify/BoticaSaid/model/Pedido.java`

Created a new entity with the following structure:
- `id` (Long) - Auto-generated primary key
- `proveedor` (Proveedor) - ManyToOne relationship, nullable
- `producto` (Producto) - ManyToOne relationship, nullable  
- `stock` (Stock) - ManyToOne relationship, NOT NULL
- `fechaDePedido` (LocalDate) - Order date, NOT NULL
- `fechaDeCreacion` (LocalDateTime) - Auto-generated creation timestamp
- `fechaDeModificacion` (LocalDateTime) - Auto-updated modification timestamp

### 2. New Repository: `PedidoRepository`
**File**: `src/main/java/quantify/BoticaSaid/repository/PedidoRepository.java`

Standard JPA repository interface for CRUD operations on Pedido entities.

### 3. New DTO: `AgregarStockConPedidoRequest`
**File**: `src/main/java/quantify/BoticaSaid/dto/pedido/AgregarStockConPedidoRequest.java`

Request DTO containing:
- `stockData` (AgregarLoteRequest) - Stock data to add (reuses existing DTO)
- `fechaDePedido` (LocalDate) - Order date

### 4. New Service: `PedidoService`
**File**: `src/main/java/quantify/BoticaSaid/service/PedidoService.java`

Main business logic for the feature:

**Method**: `agregarStockConPedido(AgregarStockConPedidoRequest request)`
- Validates request data
- Finds product by ID or barcode
- Adds stock lots to product (similar to `agregarLotes` but integrated)
- Updates product's total quantity
- Creates a Pedido record for each stock lot added
- Automatically links to product and supplier (if exists)
- Returns boolean indicating success/failure

**Technical Details**:
- Uses `@Transactional` to ensure atomicity
- Queries created stocks using timestamp-based filtering to avoid transient entity issues
- Handles both products with and without suppliers

### 5. New Controller: `PedidoController`
**File**: `src/main/java/quantify/BoticaSaid/controller/PedidoController.java`

REST endpoint:
- **POST** `/api/pedidos/agregar-stock`
- Accepts `AgregarStockConPedidoRequest` in request body
- Returns JSON response with success/error message
- HTTP 200 on success, HTTP 400 on error

### 6. Comprehensive Tests: `PedidoServiceTest`
**File**: `src/test/java/quantify/BoticaSaid/service/PedidoServiceTest.java`

5 test cases covering:
1. `testAgregarStockConPedidoExitoso` - Successful operation with supplier
2. `testAgregarStockConPedidoSinProveedor` - Product without supplier
3. `testAgregarStockConPedidoConCodigoBarras` - Using barcode instead of ID
4. `testAgregarStockConPedidoProductoInexistente` - Non-existent product
5. `testAgregarStockConPedidoSinDatos` - Invalid/empty request

All tests validate:
- Pedido records are created correctly
- Stock is added to products
- Product quantities are updated
- Foreign keys are set properly
- Error cases return false

### 7. Documentation
**Files**:
- `PEDIDOS_DOCUMENTATION.md` - Complete user guide with examples
- `http-tests/pedido.http` - HTTP request examples for testing

## Database Schema

### Table: `pedidos`
```sql
CREATE TABLE pedidos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_proveedor BIGINT NULL,
    id_producto BIGINT NULL,
    id_stock INT NOT NULL,
    fecha_de_pedido DATE NOT NULL,
    fecha_de_creacion DATETIME NOT NULL,
    fecha_de_modificacion DATETIME NOT NULL,
    FOREIGN KEY (id_proveedor) REFERENCES proveedores(id),
    FOREIGN KEY (id_producto) REFERENCES productos(id),
    FOREIGN KEY (id_stock) REFERENCES stock(id)
);
```

## Example Usage

### Request:
```json
POST /api/pedidos/agregar-stock
{
  "stockData": {
    "productoId": 1,
    "lotes": [
      {
        "codigoStock": "LOTE001",
        "cantidadUnidades": 100,
        "fechaVencimiento": "2025-12-31",
        "precioCompra": 10.50
      }
    ]
  },
  "fechaDePedido": "2024-11-19"
}
```

### Response:
```json
{
  "success": true,
  "message": "Stock agregado y pedido(s) creado(s) exitosamente"
}
```

### What Happens:
1. Stock lot is added to product ID 1
2. Product's `cantidadGeneral` increases by 100
3. A new `Pedido` record is created with:
   - Reference to the new stock
   - Reference to product ID 1
   - Reference to the product's supplier (if exists)
   - Order date: 2024-11-19
   - Auto-generated creation/modification timestamps

## Test Results
- **Total tests**: 41 (36 existing + 5 new)
- **Passed**: 41
- **Failed**: 0
- **Code coverage**: Comprehensive coverage of new code

## Security Analysis
- **CodeQL scan**: 0 vulnerabilities found
- No SQL injection risks (using JPA/Hibernate)
- Proper input validation in service layer
- Transactional consistency maintained

## Design Decisions

### 1. Timestamp-based Stock Retrieval
After saving the product (which cascades to stocks), we retrieve the newly created stocks using a timestamp-based query. This avoids the "transient entity" issue that would occur if trying to save Pedido entities referencing unsaved Stock entities.

### 2. Reusing Existing DTOs
The `AgregarLoteRequest` DTO is reused for consistency with the existing `agregarLotes` endpoint, making the API more intuitive.

### 3. Automatic Supplier Assignment
The supplier is automatically obtained from the product rather than being provided in the request, reducing redundancy and ensuring data consistency.

### 4. Nullable Foreign Keys
While `id_stock` is NOT NULL (a pedido must reference a stock), `id_proveedor` and `id_producto` are nullable to handle edge cases, though in practice they should always have values.

### 5. One Pedido per Stock Lot
Each stock lot gets its own Pedido record, providing granular tracking of orders at the lot level.

## Integration Points

### Existing Code Modified:
- None (zero breaking changes)

### Existing Code Used:
- `ProductoRepository.findByIdWithStocks()`
- `ProductoRepository.findByCodigoBarrasWithStocks()`
- `StockRepository.findByFechaCreacionBetweenWithProducto()`
- `AgregarLoteRequest` DTO
- Producto, Stock, and Proveedor entities

## Migration Notes

The database table will be created automatically by Hibernate when the application starts (ddl-auto=update).

For production environments, consider creating an explicit migration script:
```sql
CREATE TABLE IF NOT EXISTS pedidos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_proveedor BIGINT NULL,
    id_producto BIGINT NULL,
    id_stock INT NOT NULL,
    fecha_de_pedido DATE NOT NULL,
    fecha_de_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_de_modificacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedido_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedores(id),
    CONSTRAINT fk_pedido_producto FOREIGN KEY (id_producto) REFERENCES productos(id),
    CONSTRAINT fk_pedido_stock FOREIGN KEY (id_stock) REFERENCES stock(id)
);

CREATE INDEX idx_pedido_proveedor ON pedidos(id_proveedor);
CREATE INDEX idx_pedido_producto ON pedidos(id_producto);
CREATE INDEX idx_pedido_stock ON pedidos(id_stock);
CREATE INDEX idx_pedido_fecha ON pedidos(fecha_de_pedido);
```

## Future Enhancements (Not in Scope)

Potential improvements for future iterations:
1. GET endpoints to query pedidos
2. Update/cancel pedido functionality
3. Pedido status tracking (pending, received, cancelled)
4. Integration with accounting/billing systems
5. Reports for purchase analysis by supplier/date
6. Automatic reorder point notifications

## Compliance

✅ Meets all requirements from problem statement:
- ✅ New "pedidos" table created
- ✅ All specified fields present with correct nullability
- ✅ Endpoint receives stock object and fecha_de_pedido
- ✅ Creates pedido record(s)
- ✅ Links to product from stock
- ✅ Links to supplier from product
- ✅ Adds stock to product (calls agregarLotes logic)
- ✅ All tests pass
- ✅ No breaking changes
