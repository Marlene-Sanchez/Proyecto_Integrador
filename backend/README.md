# Reports API

Backend minimal para la app Android de reportes geolocalizados.

Requisitos:
- Node.js 18+
- Docker (opcional) y docker-compose para Postgres

Desarrollo local (sin docker):
1. `npm install`
2. Configurar `DATABASE_URL` si usas Postgres local
3. `npm start`

Con Docker:
`docker-compose up --build`

Endpoints:
- POST /auth/register { email, password }
- POST /auth/login { email, password } -> { token }
- GET /reports
- GET /reports/:id
- POST /reports (multipart, Authorization: Bearer <token>)

Las imágenes se sirven desde /uploads/<filename> y `image_url` en la respuesta apunta a la URL completa.

