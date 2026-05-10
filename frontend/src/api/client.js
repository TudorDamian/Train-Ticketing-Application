const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers ?? {}),
    },
    ...options,
  })

  if (response.status === 204) return null

  const contentType = response.headers.get('content-type') ?? ''
  const payload = contentType.includes('application/json')
    ? await response.json()
    : await response.text()

  if (!response.ok) {
    throw new ApiError(payload)
  }

  return payload
}

export class ApiError extends Error {
  constructor(payload) {
    if (typeof payload === 'string') {
      super(cleanErrorMessage(payload || 'Backend API is unreachable'))
      this.payload = payload
      return
    }

    const details = Array.isArray(payload?.details) ? payload.details.join(' ') : ''
    super(cleanErrorMessage([payload?.message, details].filter(Boolean).join(' ') || 'Request failed'))
    this.payload = payload
  }
}

function cleanErrorMessage(message) {
  const lowerMessage = message.toLowerCase()
  const databaseTerms = [
    'could not execute statement',
    'constraint',
    'foreign key',
    'duplicate key',
    'sql',
  ]

  if (databaseTerms.some((term) => lowerMessage.includes(term))) {
    return 'This change conflicts with existing records. Remove dependent records first or choose different values.'
  }

  return message
}

export const api = {
  getStations: () => request('/stations'),
  createStation: (body) =>
      request('/admin/stations', {method: 'POST', body: JSON.stringify(body)}),
  updateStation: (id, body) =>
      request(`/admin/stations/${id}`, {method: 'PUT', body: JSON.stringify(body)}),
  deleteStation: (id) => request(`/admin/stations/${id}`, {method: 'DELETE'}),

  getTrains: () => request('/trains'),
  createTrain: (body) =>
      request('/admin/trains', {method: 'POST', body: JSON.stringify(body)}),
  updateTrain: (id, body) =>
      request(`/admin/trains/${id}`, {method: 'PUT', body: JSON.stringify(body)}),
  deleteTrain: (id) => request(`/admin/trains/${id}`, {method: 'DELETE'}),

  getRoutes: () => request('/routes'),
  createRoute: (body) =>
      request('/admin/routes', {method: 'POST', body: JSON.stringify(body)}),
  updateRoute: (id, body) =>
      request(`/admin/routes/${id}`, {method: 'PUT', body: JSON.stringify(body)}),
  deleteRoute: (id) => request(`/admin/routes/${id}`, {method: 'DELETE'}),

  getTrips: () => request('/admin/trips'),
  createTrip: (body) =>
      request('/admin/trips', {method: 'POST', body: JSON.stringify(body)}),
  updateTrip: (id, body) =>
      request(`/admin/trips/${id}`, {method: 'PUT', body: JSON.stringify(body)}),
  deleteTrip: (id) => request(`/admin/trips/${id}`, {method: 'DELETE'}),
  searchTrips: ({from, to, date}) =>
      request(
          `/trips/search?from=${encodeURIComponent(from)}&to=${encodeURIComponent(
              to,
          )}&date=${encodeURIComponent(date)}`,
      ),

  createBooking: (body) =>
      request('/bookings', {method: 'POST', body: JSON.stringify(body)}),
  getBooking: (id) => request(`/bookings/${id}`),
  getBookingsByEmail: (email) =>
      request(`/bookings?email=${encodeURIComponent(email)}`),
  getTripBookings: (tripId) => request(`/admin/trips/${tripId}/bookings`),
  deleteBooking: (id) => request(`/admin/bookings/${id}`, {method: 'DELETE'}),

  applyDelay: (tripId, body) =>
      request(`/admin/trips/${tripId}/delay`, {
        method: 'POST',
        body: JSON.stringify(body),
      }),
}
