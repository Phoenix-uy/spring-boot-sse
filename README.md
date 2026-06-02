# Server-Sent Events Demo con Spring Boot

POC de comunicación en tiempo real usando Server-Sent Events (SSE) con JSON file-based.

## Arquitectura

- **Backend:** Spring Boot 3.2.5 + WebFlux (SSE reactivo)
- **Frontend:** React 18 + Vite (separado del backend)
- **Datos:** Archivo JSON (`data.json`) modificable manualmente
- **Comunicación:** SSE para push servidor→cliente cuando JSON cambia

## Estructura

```
backend/
├── src/main/java/com/servicenow/poc/sse/
│   ├── SseDemoApplication.java      # Main class
│   ├── config/
│   │   └── WebConfig.java           # CORS config
│   ├── controller/
│   │   ├── DataController.java      # REST endpoint (GET)
│   │   └── SseController.java       # SSE streaming endpoint
│   └── service/
│       └── DataService.java         # FileWatcher + broadcast
├── src/main/resources/
│   └── application.yml              # Config
└── data.json                        # Archivo de datos (auto-creado)

frontend/
├── src/
│   ├── App.jsx                      # Componente principal
│   ├── App.css                      # Estilos
│   ├── main.jsx                     # Entry point
│   └── index.css                    # Estilos globales
├── index.html
├── vite.config.js                   # Proxy a backend
└── package.json
```

## Cómo funciona

1. **Backend lee** `data.json` al iniciar (crea uno por defecto si no existe)
2. **FileWatcher** detecta modificaciones en `data.json`
3. **Cuando JSON cambia**, backend:
   - Recarga datos del archivo
   - Emite evento SSE a todos los clientes conectados
4. **Frontend recibe** evento automáticamente y actualiza UI

### Flujo de datos

```
data.json modificado manualmente
        ↓
FileWatcher detecta cambio
        ↓
DataService.loadDataFromFile()
        ↓
sink.tryEmitNext(newData)
        ↓
    ┌───────┴───────┐
    ↓               ↓
Cliente 1       Cliente N
actualiza UI    actualiza UI
```

## Endpoints

### SSE Stream
- `GET /api/sse/stream` - Stream de eventos (text/event-stream)

### REST API
- `GET /api/data` - Obtener datos actuales del JSON

## Ejecutar

### Backend (puerto 8080)

```bash
cd server-side-events
mvn spring-boot:run
```

### Frontend (puerto 3000)

```bash
cd frontend
npm install
npm run dev
```

Abrir navegador en: http://localhost:3000

## Probar

1. Ejecutar backend y frontend
2. Abrir http://localhost:3000 en múltiples pestañas
3. Editar `data.json` en el directorio raíz del backend:
   ```json
   {
     "message": "Hello from file!",
     "counter": 42,
     "timestamp": 1234567890
   }
   ```
4. Guardar archivo
5. Ver actualización automática en todas las pestañas

## Formato del JSON - POJOs Adaptables

Backend usa **patrón Registry** con POJOs específicos type-safe. Incluye 3 tipos predefinidos:

### SimpleData (por defecto)
```json
{
  "message": "Hello SSE",
  "counter": 42,
  "timestamp": 1717358400000
}
```

### SensorData
```json
{
  "sensorId": "SENSOR-001",
  "temperature": 23.5,
  "humidity": 65.2,
  "status": "active",
  "timestamp": 1717358400000
}
```

### UserActivity
```json
{
  "userId": "user123",
  "action": "login",
  "tags": ["web", "mobile"],
  "timestamp": 1717358400000
}
```

### Cambiar tipo activo

Editar `application.yml`:
```yaml
app:
  data:
    type: sensor  # simple | sensor | user_activity
```

Ver `HOW_TO_ADD_NEW_POJO.md` para agregar tipos personalizados.

## Componentes clave

### DataTypeRegistry
- **Patrón Registry:** Registra POJOs específicos por tipo
- **Type-safe:** Validación en compile-time
- **Extensible:** Agregar tipos sin modificar core
- **Configuración:** Tipo activo vía `application.yml`

### DataService
- **FileWatcher:** Detecta cambios en `data.json` con `WatchService`
- **Thread-safety:** `AtomicReference<DataModel>` para acceso concurrente
- **Broadcast:** `Sinks.Many.multicast()` para múltiples clientes
- **Debounce:** 100ms delay para evitar múltiples eventos por guardado
- **Deserialización:** Jackson deserializa JSON → POJO específico

### SseController
- `Flux<ServerSentEvent<DataModel>>` para streaming type-safe
- Keep-alive cada 30s para evitar timeout
- Event type "data-update"

### Frontend (React)
- EventSource API para SSE
- Renderizado dinámico de cualquier estructura JSON
- Log de eventos en tiempo real
- Reconexión automática

## Configuración

### Backend (`application.yml`)

```yaml
app:
  data:
    file: data.json  # Ruta relativa al directorio de ejecución
```

Para usar ruta absoluta:
```yaml
app:
  data:
    file: C:/path/to/custom-data.json
```

### Frontend (`vite.config.js`)

Proxy configurado para `/api/*` → `http://localhost:8080`

## Notas técnicas

- **Patrón Registry:** POJOs específicos registrados dinámicamente
- **Type-safety:** Jackson valida JSON contra POJO en deserialización
- **FileWatcher:** Usa Java NIO `WatchService` (nativo del OS)
- **Debounce:** 100ms sleep después de detectar cambio (algunos editores guardan múltiples veces)
- **Backpressure:** Manejado por Reactor con `onBackpressureBuffer()`
- **Memory leaks:** Evitados con `@PreDestroy` cleanup
- **CORS:** Configurado para desarrollo (ajustar en producción)
- **Extensibilidad:** Agregar POJOs sin modificar DataService/Controllers

## Producción

Para producción:
- Cambiar CORS a dominios específicos
- Agregar autenticación/autorización
- Considerar persistencia en DB + file sync
- Configurar timeouts apropiados
- Build frontend: `npm run build` → servir desde backend o CDN
