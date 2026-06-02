# Cómo Agregar un Nuevo POJO

Guía paso a paso para agregar un nuevo tipo de datos al sistema SSE.

## Paso 1: Crear el POJO

Crea una nueva clase en `src/main/java/com/servicenow/poc/sse/model/` que implemente `DataModel`:

```java
package com.servicenow.poc.sse.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MiNuevoDato implements DataModel {
    private String campo1;
    private Integer campo2;
    private Long timestamp;
    
    @Override
    public String getDataType() {
        return "mi_nuevo_dato";  // Identificador único
    }
}
```

**Requisitos:**
- ✅ Implementar interfaz `DataModel`
- ✅ Tener constructor sin argumentos (para Jackson)
- ✅ Método `getDataType()` con identificador único
- ✅ Usar `@Data` de Lombok (o getters/setters manuales)

## Paso 2: Registrar en DataTypeConfig

Edita `src/main/java/com/servicenow/poc/sse/config/DataTypeConfig.java`:

```java
@PostConstruct
public void registerDataTypes() {
    registry.register("simple", SimpleData.class);
    registry.register("sensor", SensorData.class);
    registry.register("user_activity", UserActivity.class);
    registry.register("mi_nuevo_dato", MiNuevoDato.class);  // ← Agregar aquí
    
    registry.setActiveDataType(defaultDataType);
}
```

## Paso 3: Configurar tipo activo

Edita `src/main/resources/application.yml`:

```yaml
app:
  data:
    file: data.json
    type: mi_nuevo_dato  # ← Cambiar aquí
```

## Paso 4: Crear archivo JSON de ejemplo

Crea `data.json` en la raíz del proyecto:

```json
{
  "campo1": "valor ejemplo",
  "campo2": 123,
  "timestamp": 1717358400000
}
```

## Paso 5: Reiniciar aplicación

```bash
mvn spring-boot:run
```

El backend:
- Detectará el nuevo tipo configurado
- Cargará el JSON usando el POJO específico
- Validará que el JSON coincida con la estructura del POJO

## Cambiar entre tipos en runtime

### Opción 1: Variable de entorno
```bash
export APP_DATA_TYPE=sensor
mvn spring-boot:run
```

### Opción 2: Argumento JVM
```bash
mvn spring-boot:run -Dapp.data.type=user_activity
```

### Opción 3: Editar application.yml
```yaml
app:
  data:
    type: simple  # Cambiar aquí
```

## Ejemplo completo: OrderData

```java
// 1. Crear POJO
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderData implements DataModel {
    private String orderId;
    private String customerId;
    private Double totalAmount;
    private String status;
    private List<String> items;
    private Long timestamp;
    
    @Override
    public String getDataType() {
        return "order";
    }
}

// 2. Registrar en DataTypeConfig
registry.register("order", OrderData.class);

// 3. Configurar en application.yml
app:
  data:
    type: order

// 4. Crear data.json
{
  "orderId": "ORD-001",
  "customerId": "CUST-123",
  "totalAmount": 299.99,
  "status": "pending",
  "items": ["item1", "item2"],
  "timestamp": 1717358400000
}
```

## Ventajas del patrón

✅ **Type-safe:** Compilador valida estructura  
✅ **Autocomplete:** IDE sugiere campos  
✅ **Validación:** Jackson valida JSON contra POJO  
✅ **Extensible:** Agregar tipos sin modificar core  
✅ **Documentado:** POJO documenta estructura esperada  

## Troubleshooting

### Error: "Data type not registered"
→ Verificar que el tipo esté registrado en `DataTypeConfig`

### Error: "Failed to create default instance"
→ Verificar que el POJO tenga constructor sin argumentos

### Error: JSON parsing exception
→ Verificar que el JSON coincida con los campos del POJO

### Error: "No active data type set"
→ Verificar configuración en `application.yml`
