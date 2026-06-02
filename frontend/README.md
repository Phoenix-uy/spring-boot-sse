# Frontend - SSE Demo

React + Vite frontend para Server-Sent Events demo.

## Instalación

```bash
npm install
```

## Desarrollo

```bash
npm run dev
```

Abre http://localhost:3000

## Build

```bash
npm run build
```

Output en `dist/`

## Características

- ✅ Conexión SSE automática a backend
- ✅ Renderizado dinámico de cualquier JSON
- ✅ Log de eventos en tiempo real
- ✅ Reconexión automática si se pierde conexión
- ✅ UI moderna con gradientes y animaciones
- ✅ Responsive design

## Proxy

Vite proxy configurado:
- `/api/*` → `http://localhost:8080`

Backend debe estar corriendo en puerto 8080.
