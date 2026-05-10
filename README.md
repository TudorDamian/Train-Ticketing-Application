# Train Ticketing Application

A full-stack Java + React application for managing train schedules, routes, bookings, and delay notifications.

## Overview

- Backend: Spring Boot 4, PostgreSQL, Flyway migrations.
- Frontend: React + Vite (proxying `/api` to the backend).
- Email notifications: optional (disabled by default, logs instead).

## Preloaded Data

The database migration `traintickets/src/main/resources/db/migration/V1__init.sql` seeds:

- Stations (e.g., BUC, PLV, BRV, SIB, CLJ, ORD, TIM, IAS, CTA)
- Trains (IR/IC/R/RE)
- Routes and route stops
- Trips on `2026-06-15` and `2026-06-16`

These IDs are used in the examples below.

## Run With Docker Compose

From the repo root:

```bash
docker compose --env-file frontend/.env up --build -d
```

Services:

- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080/api`
- Postgres: `localhost:5432`

## Environment Variables

The backend reads values from `.env` if present:

- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`
- `DB_HOST` (default `localhost`), `DB_PORT` (default `5432`)
- `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`
- `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS_ENABLE`
- `MAIL_ENABLED` (default `false`)
- `MAIL_FROM` (default `no-reply@traintickets.local`)

The frontend uses:

- `VITE_API_BASE_URL` (default `/api`)

## API Error Format

All errors follow a consistent shape:

```json
{
  "timestamp": "2026-05-09T18:25:12Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Not enough seats available for this trip segment",
  "details": []
}
```

## Functional Examples

Base URL below is `http://localhost:8080/api`.

### 1) Find connections between stations

**Request** (direct or 1-changeover connections):

```bash
curl "http://localhost:8080/api/trips/search?from=BUC&to=CLJ&date=2026-06-15"
```

**Response** (direct):

```json
{
  "connections": [
    {
      "segments": [
        {
          "tripId": 1,
          "train": "IR 1745",
          "from": "Bucuresti Nord",
          "to": "Cluj-Napoca",
          "departureTime": "2026-06-15T08:00:00",
          "arrivalTime": "2026-06-15T12:45:00"
        }
      ],
      "totalDurationMinutes": 285,
      "changeovers": 0
    }
  ]
}
```

**Response** (1 changeover example, BUC -> TIM):

```json
{
  "connections": [
    {
      "segments": [
        {
          "tripId": 1,
          "train": "IR 1745",
          "from": "Bucuresti Nord",
          "to": "Cluj-Napoca",
          "departureTime": "2026-06-15T08:00:00",
          "arrivalTime": "2026-06-15T12:45:00"
        },
        {
          "tripId": 2,
          "train": "IC 581",
          "from": "Cluj-Napoca",
          "to": "Timisoara Nord",
          "departureTime": "2026-06-15T13:35:00",
          "arrivalTime": "2026-06-15T17:40:00"
        }
      ],
      "totalDurationMinutes": 580,
      "changeovers": 1
    }
  ]
}
```

If no connection exists, the response is:

```json
{
  "connections": []
}
```

### 2) Book tickets (prevents overbooking)

**Request**:

```bash
curl -X POST "http://localhost:8080/api/bookings" \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "ana.popa@example.com",
    "tripId": 1,
    "fromStationId": 1,
    "toStationId": 5,
    "ticketCount": 2,
    "passengerNames": ["Ana Popa", "Mihai Popa"]
  }'
```

**Response**:

```json
{
  "id": 100,
  "customerEmail": "ana.popa@example.com",
  "tripId": 1,
  "fromStation": "Bucuresti Nord",
  "toStation": "Cluj-Napoca",
  "ticketCount": 2,
  "status": "CONFIRMED",
  "createdAt": "2026-05-09T18:30:00Z",
  "tickets": [
    { "id": 1000, "passengerName": "Ana Popa" },
    { "id": 1001, "passengerName": "Mihai Popa" }
  ]
}
```

**Overbooking error**:

```json
{
  "timestamp": "2026-05-09T18:31:10Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Not enough seats available for this trip segment",
  "details": []
}
```

A confirmation email is sent when `MAIL_ENABLED=true` and SMTP settings are valid.

### 3) Get booking by id / by customer email

**Request**:

```bash
curl "http://localhost:8080/api/bookings/100"
```

**Request**:

```bash
curl "http://localhost:8080/api/bookings?email=ana.popa@example.com"
```

**Response** (array for the email endpoint):

```json
[
  {
    "id": 100,
    "customerEmail": "ana.popa@example.com",
    "tripId": 1,
    "fromStation": "Bucuresti Nord",
    "toStation": "Cluj-Napoca",
    "ticketCount": 2,
    "status": "CONFIRMED",
    "createdAt": "2026-05-09T18:30:00Z",
    "tickets": [
      { "id": 1000, "passengerName": "Ana Popa" },
      { "id": 1001, "passengerName": "Mihai Popa" }
    ]
  }
]
```

### 4) Admin: manage stations

**Create station**:

```bash
curl -X POST "http://localhost:8080/api/admin/stations" \
  -H "Content-Type: application/json" \
  -d '{ "name": "Arad", "code": "ARAD" }'
```

**Response**:

```json
{ "id": 10, "name": "Arad", "code": "ARAD" }
```

**Update station**:

```bash
curl -X PUT "http://localhost:8080/api/admin/stations/10" \
  -H "Content-Type: application/json" \
  -d '{ "name": "Arad Central", "code": "ARAD" }'
```

**Delete station**:

```bash
curl -X DELETE "http://localhost:8080/api/admin/stations/10"
```

### 5) Admin: manage trains

**Create train**:

```bash
curl -X POST "http://localhost:8080/api/admin/trains" \
  -H "Content-Type: application/json" \
  -d '{ "name": "IR 2201", "capacity": 150 }'
```

**Response**:

```json
{ "id": 6, "name": "IR 2201", "capacity": 150 }
```

### 6) Admin: manage routes

**Create route**:

```bash
curl -X POST "http://localhost:8080/api/admin/routes" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Arad to Cluj-Napoca",
    "stops": [
      { "stationId": 10, "stopOrder": 1, "arrivalOffsetMinutes": 0, "departureOffsetMinutes": 0 },
      { "stationId": 5, "stopOrder": 2, "arrivalOffsetMinutes": 210, "departureOffsetMinutes": 210 }
    ]
  }'
```

**Response**:

```json
{
  "id": 6,
  "name": "Arad to Cluj-Napoca",
  "stops": [
    {
      "id": 16,
      "station": { "id": 10, "name": "Arad", "code": "ARAD" },
      "stopOrder": 1,
      "arrivalOffsetMinutes": 0,
      "departureOffsetMinutes": 0
    },
    {
      "id": 17,
      "station": { "id": 5, "name": "Cluj-Napoca", "code": "CLJ" },
      "stopOrder": 2,
      "arrivalOffsetMinutes": 210,
      "departureOffsetMinutes": 210
    }
  ]
}
```

### 7) Admin: manage trips (train schedules)

**Create trip**:

```bash
curl -X POST "http://localhost:8080/api/admin/trips" \
  -H "Content-Type: application/json" \
  -d '{
    "trainId": 6,
    "routeId": 6,
    "departureDateTime": "2026-06-20T08:15:00"
  }'
```

**Response**:

```json
{
  "id": 9,
  "trainId": 6,
  "train": "IR 2201",
  "routeId": 6,
  "route": "Arad to Cluj-Napoca",
  "departureDateTime": "2026-06-20T08:15:00",
  "status": "SCHEDULED",
  "delayMinutes": 0
}
```

### 8) Admin: view bookings for a trip

**Request**:

```bash
curl "http://localhost:8080/api/admin/trips/1/bookings"
```

**Response**:

```json
[
  {
    "id": 100,
    "customerEmail": "ana.popa@example.com",
    "tripId": 1,
    "fromStation": "Bucuresti Nord",
    "toStation": "Cluj-Napoca",
    "ticketCount": 2,
    "status": "CONFIRMED",
    "createdAt": "2026-05-09T18:30:00Z",
    "tickets": [
      { "id": 1000, "passengerName": "Ana Popa" },
      { "id": 1001, "passengerName": "Mihai Popa" }
    ]
  }
]
```

### 9) Admin: apply delay and notify customers

**Request**:

```bash
curl -X POST "http://localhost:8080/api/admin/trips/1/delay" \
  -H "Content-Type: application/json" \
  -d '{ "delayMinutes": 15, "reason": "Track maintenance" }'
```

**Response**:

```json
{
  "id": 1,
  "tripId": 1,
  "delayMinutes": 15,
  "reason": "Track maintenance",
  "createdAt": "2026-05-09T18:35:00Z",
  "notifiedBookings": 1
}
```

### 10) Delete resources

All delete endpoints return `204 No Content` on success:

- `DELETE /api/admin/stations/{id}`
- `DELETE /api/admin/trains/{id}`
- `DELETE /api/admin/routes/{id}`
- `DELETE /api/admin/trips/{id}`
- `DELETE /api/admin/bookings/{id}`

## Frontend Notes

The frontend calls `/api/*` and relies on the Vite proxy in `frontend/vite.config.js` to reach the backend when running locally.

## Project Structure

- `frontend/`: React UI
- `traintickets/`: Spring Boot backend
- `compose.yaml`: Docker Compose for local dev

