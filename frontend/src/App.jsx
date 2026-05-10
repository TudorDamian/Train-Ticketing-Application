import { NavLink, Route, Routes } from 'react-router-dom'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { api } from './api/client'
import './App.css'

const today = new Date().toISOString().slice(0, 10)

function formatDateTime(value) {
  if (!value) return 'Not set'
  return new Intl.DateTimeFormat(undefined, {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value))
}

function minutesLabel(minutes) {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (!hours) return `${mins} min`
  if (!mins) return `${hours} h`
  return `${hours} h ${mins} min`
}

function useReferenceData() {
  const [data, setData] = useState({ stations: [], routes: [], trains: [] })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  async function load() {
    setLoading(true)
    setError('')
    try {
      const [stations, routes, trains] = await Promise.all([
        api.getStations(),
        api.getRoutes(),
        api.getTrains(),
      ])
      setData({ stations, routes, trains })
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    // Data fetching is the external synchronization this hook owns.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    load()
  }, [])

  return { ...data, loading, error, reload: load }
}

function useTimedStatus(timeoutMs = 5000) {
  const [status, setStatus] = useState({ type: '', message: '' })

  useEffect(() => {
    if (!status.message) return undefined
    const timeoutId = window.setTimeout(
      () => setStatus({ type: '', message: '' }),
      timeoutMs,
    )
    return () => window.clearTimeout(timeoutId)
  }, [status, timeoutMs])

  return {
    status,
    clearStatus: useCallback(() => setStatus({ type: '', message: '' }), []),
    showError: useCallback(
      (message) => setStatus({ type: 'error', message }),
      [],
    ),
    showSuccess: useCallback(
      (message) => setStatus({ type: 'success', message }),
      [],
    ),
  }
}

function StatusMessage({ type = 'info', children }) {
  if (!children) return null
  return <div className={`status ${type}`}>{children}</div>
}

function EmptyState({ title, children }) {
  return (
    <div className="empty-state">
      <strong>{title}</strong>
      <span>{children}</span>
    </div>
  )
}

function AppShell() {
  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Train Ticketing Application</p>
          <h1>Rail operations and booking desk</h1>
        </div>
        <nav aria-label="Main navigation">
          <NavLink to="/">Search</NavLink>
          <NavLink to="/booking">Booking</NavLink>
          <NavLink to="/admin">Admin</NavLink>
        </nav>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<SearchPage />} />
          <Route path="/booking" element={<BookingLookupPage />} />
          <Route path="/admin" element={<AdminPage />} />
        </Routes>
      </main>
    </div>
  )
}

function SearchPage() {
  const { stations, loading, error } = useReferenceData()
  const [criteria, setCriteria] = useState({
    from: 'BUC',
    to: 'TIM',
    date: today,
  })
  const [connections, setConnections] = useState([])
  const [searching, setSearching] = useState(false)
  const [searchError, setSearchError] = useState('')
  const [selectedSegment, setSelectedSegment] = useState(null)
  const stationByName = useMemo(
    () => new Map(stations.map((station) => [station.name, station])),
    [stations],
  )

  async function searchTrips(event) {
    event.preventDefault()
    setSearching(true)
    setSearchError('')
    setSelectedSegment(null)
    try {
      const result = await api.searchTrips(criteria)
      setConnections(result.connections ?? [])
    } catch (err) {
      setConnections([])
      setSearchError(err.message)
    } finally {
      setSearching(false)
    }
  }

  function stationOptions() {
    return stations.map((station) => (
      <option key={station.id} value={station.code}>
        {station.name} ({station.code})
      </option>
    ))
  }

  return (
    <section className="page-grid">
      <div className="search-stack">
        <div className="panel search-panel">
          <div className="section-heading">
            <p className="eyebrow">Customer flow</p>
            <h2>Find a trip</h2>
          </div>
          <StatusMessage type="error">{error}</StatusMessage>
          <form className="form-grid" onSubmit={searchTrips}>
            <label>
              From
              <select
                value={criteria.from}
                onChange={(event) =>
                  setCriteria({ ...criteria, from: event.target.value })
                }
                disabled={loading}
              >
                {stationOptions()}
              </select>
            </label>
            <label>
              To
              <select
                value={criteria.to}
                onChange={(event) =>
                  setCriteria({ ...criteria, to: event.target.value })
                }
                disabled={loading}
              >
                {stationOptions()}
              </select>
            </label>
            <label>
              Travel date
              <input
                type="date"
                value={criteria.date}
                onChange={(event) =>
                  setCriteria({ ...criteria, date: event.target.value })
                }
              />
            </label>
            <button className="primary" type="submit" disabled={searching || loading}>
              {searching ? 'Searching...' : 'Search trips'}
            </button>
          </form>
        </div>

        <BookingForm
          key={
            selectedSegment
              ? `${selectedSegment.tripId}-${selectedSegment.from}-${selectedSegment.to}`
              : 'empty-booking'
          }
          segment={selectedSegment}
        />
      </div>

      <div className="panel results-panel">
        <div className="section-heading">
          <p className="eyebrow">Available connections</p>
          <h2>Results</h2>
        </div>
        <StatusMessage type="error">{searchError}</StatusMessage>
        {searching && <StatusMessage>Loading matching trips...</StatusMessage>}
        {!searching && !connections.length && !searchError && (
          <EmptyState title="No connections loaded">
            Search by station code and date to see direct trips and one-changeover
            connections from the backend.
          </EmptyState>
        )}
        <div className="connection-list">
          {connections.map((connection, index) => (
            <article className="connection-card" key={`${index}-${connection.totalDurationMinutes}`}>
              <div className="connection-summary">
                <strong>{minutesLabel(connection.totalDurationMinutes)}</strong>
                <span>
                  {connection.changeovers === 0
                    ? 'Direct'
                    : `${connection.changeovers} changeover`}
                </span>
              </div>
              <div className="segment-list">
                {connection.segments.map((segment, segmentIndex) => {
                  const fromStation = stationByName.get(segment.from)
                  const toStation = stationByName.get(segment.to)
                  return (
                    <div className="segment" key={`${segment.tripId}-${segmentIndex}`}>
                      <div>
                        <strong>{segment.from}</strong>
                        <span>{formatDateTime(segment.departureTime)}</span>
                      </div>
                      <div className="track-line" aria-hidden="true" />
                      <div>
                        <strong>{segment.to}</strong>
                        <span>{formatDateTime(segment.arrivalTime)}</span>
                      </div>
                      <div className="segment-meta">
                        <span>{segment.train}</span>
                        <span>Trip #{segment.tripId}</span>
                      </div>
                      <button
                        type="button"
                        className="secondary"
                        onClick={() =>
                          setSelectedSegment({
                            ...segment,
                            fromStationId: fromStation?.id ?? '',
                            toStationId: toStation?.id ?? '',
                          })
                        }
                      >
                        Book segment
                      </button>
                    </div>
                  )
                })}
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  )
}

function BookingForm({ segment }) {
  const selectedSegment = segment
  const [form, setForm] = useState({
    customerEmail: '',
    ticketCount: 1,
    passengerNames: '',
  })
  const [result, setResult] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const { status, clearStatus, showError } = useTimedStatus()
  const passengerNames = form.passengerNames
    .split('\n')
    .map((name) => name.trim())
    .filter(Boolean)
  const ticketCount = Number(form.ticketCount)
  const passengerCountMatches =
    ticketCount > 0 && passengerNames.length === ticketCount
  const canConfirmBooking =
    form.customerEmail.trim() &&
    form.passengerNames.trim() &&
    passengerCountMatches &&
    !submitting

  async function submit(event) {
    event.preventDefault()
    if (!segment) return
    if (!form.customerEmail.trim() || !form.passengerNames.trim()) {
      showError('Customer email and passenger names are required.')
      return
    }
    if (!passengerCountMatches) {
      showError(`Enter exactly ${ticketCount} passenger name(s).`)
      return
    }
    setSubmitting(true)
    clearStatus()
    setResult(null)
    try {
      const booking = await api.createBooking({
        customerEmail: form.customerEmail,
        tripId: selectedSegment.tripId,
        fromStationId: Number(selectedSegment.fromStationId),
        toStationId: Number(selectedSegment.toStationId),
        ticketCount: Number(form.ticketCount),
        passengerNames,
      })
      setResult(booking)
    } catch (err) {
      showError(err.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="panel booking-panel">
      <div className="section-heading">
        <p className="eyebrow">Reservation</p>
        <h2>Book tickets</h2>
      </div>
      {!segment && (
        <EmptyState title="Select a segment">
          Use “Book segment” from a search result to prepare the booking request.
        </EmptyState>
      )}
      {selectedSegment && (
        <form className="form-grid" onSubmit={submit}>
          <div className="selected-trip">
            <strong>
              {selectedSegment.from} to {selectedSegment.to}
            </strong>
            <span>
              {selectedSegment.train} · Trip #{selectedSegment.tripId}
            </span>
          </div>
          <label>
            Customer email
            <input
              type="email"
              required
              placeholder="customer@example.com"
              value={form.customerEmail}
              onChange={(event) =>
                setForm({ ...form, customerEmail: event.target.value })
              }
            />
          </label>
          <label>
            Tickets
            <input
              type="number"
              min="1"
              required
              value={form.ticketCount}
              onChange={(event) => {
                const nextTicketCount = Math.max(1, Number(event.target.value) || 1)
                setForm({ ...form, ticketCount: nextTicketCount })
              }}
            />
          </label>
          <label>
            From station
            <input value={selectedSegment.from} readOnly />
          </label>
          <label>
            To station
            <input value={selectedSegment.to} readOnly />
          </label>
          <label className="wide">
            Passenger names
            <textarea
              rows="4"
              required
              placeholder={'Ana Popescu\nMihai Popescu'}
              value={form.passengerNames}
              onChange={(event) =>
                setForm({ ...form, passengerNames: event.target.value })
              }
            />
          </label>
          {form.passengerNames.trim() && !passengerCountMatches && (
            <div className="wide">
              <StatusMessage type="error">
              Enter exactly {ticketCount} passenger name(s), one per line.
              </StatusMessage>
            </div>
          )}
          <button className="primary" type="submit" disabled={!canConfirmBooking}>
            {submitting ? 'Booking...' : 'Confirm booking'}
          </button>
        </form>
      )}
      <StatusMessage type={status.type}>{status.message}</StatusMessage>
      {result && <BookingReceipt booking={result} />}
    </div>
  )
}

function BookingReceipt({ booking }) {
  return (
    <div className="receipt">
      <div>
        <span>Booking</span>
        <strong>#{booking.id}</strong>
      </div>
      <div>
        <span>Status</span>
        <strong>{booking.status}</strong>
      </div>
      <div>
        <span>Route</span>
        <strong>
          {booking.fromStation} to {booking.toStation}
        </strong>
      </div>
      <ul>
        {booking.tickets?.map((ticket) => (
          <li key={ticket.id}>
            Ticket #{ticket.id} {ticket.passengerName && `· ${ticket.passengerName}`}
          </li>
        ))}
      </ul>
    </div>
  )
}

function BookingLookupPage() {
  const [email, setEmail] = useState('')
  const [bookings, setBookings] = useState([])
  const [hasSearched, setHasSearched] = useState(false)
  const [loading, setLoading] = useState(false)
  const { status, clearStatus, showError } = useTimedStatus()

  async function lookup(event) {
    event.preventDefault()
    setLoading(true)
    clearStatus()
    setBookings([])
    setHasSearched(true)
    try {
      setBookings(await api.getBookingsByEmail(email))
    } catch (err) {
      showError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="single-column">
      <div className="panel">
        <div className="section-heading">
          <p className="eyebrow">Booking desk</p>
          <h2>Find an existing booking</h2>
        </div>
        <form className="inline-form" onSubmit={lookup}>
          <label>
            Customer email
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
            />
          </label>
          <button className="primary" disabled={loading} type="submit">
            {loading ? 'Loading...' : 'Lookup'}
          </button>
        </form>
        <StatusMessage type={status.type}>{status.message}</StatusMessage>
        {!hasSearched && !status.message && (
          <EmptyState title="No booking selected">
            Enter a customer email to list every booking made with that address.
          </EmptyState>
        )}
        {hasSearched && !loading && !status.message && !bookings.length && (
          <EmptyState title="No bookings found">
            No bookings were returned for {email}.
          </EmptyState>
        )}
        {!!bookings.length && (
          <div className="booking-list">
            {bookings.map((booking) => (
              <BookingReceipt key={booking.id} booking={booking} />
            ))}
          </div>
        )}
      </div>
    </section>
  )
}

function AdminPage() {
  const { stations, routes, trains, loading, error, reload } = useReferenceData()
  const [tripRefreshKey, setTripRefreshKey] = useState(0)

  return (
    <section className="admin-layout">
      <div className="panel admin-overview">
        <div className="section-heading">
          <p className="eyebrow">Operations</p>
          <h2>Backend resources</h2>
        </div>
        <StatusMessage type="error">{error}</StatusMessage>
        {loading ? (
          <StatusMessage>Loading resources...</StatusMessage>
        ) : (
          <div className="metric-grid">
            <div>
              <strong>{stations.length}</strong>
              <span>Stations</span>
            </div>
            <div>
              <strong>{routes.length}</strong>
              <span>Routes</span>
            </div>
            <div>
              <strong>{trains.length}</strong>
              <span>Trains</span>
            </div>
          </div>
        )}
      </div>
      <div className="admin-stations">
        <StationAdmin stations={stations} reload={reload} />
      </div>
      <div className="admin-routes">
        <RouteAdmin stations={stations} routes={routes} reload={reload} />
      </div>
      <div className="admin-trains">
        <TrainAdmin trains={trains} reload={reload} />
      </div>
      <div className="admin-trips">
        <TripAdmin trains={trains} routes={routes} refreshKey={tripRefreshKey} />
      </div>
      <div className="admin-delays">
        <DelayAdmin onDelayApplied={() => setTripRefreshKey((key) => key + 1)} />
      </div>
      <div className="admin-manifest">
        <TripBookingsAdmin />
      </div>
    </section>
  )
}

function StationAdmin({ stations, reload }) {
  const [form, setForm] = useState({ name: '', code: '' })
  const [editingId, setEditingId] = useState(null)
  const { status, clearStatus, showError, showSuccess } = useTimedStatus()

  async function submit(event) {
    event.preventDefault()
    clearStatus()
    try {
      const body = { ...form, code: form.code.toUpperCase() }
      if (editingId) {
        await api.updateStation(editingId, body)
        showSuccess('Station updated.')
      } else {
        await api.createStation(body)
        showSuccess('Station created.')
      }
      setForm({ name: '', code: '' })
      setEditingId(null)
      reload()
    } catch (err) {
      showError(err.message)
    }
  }

  async function remove(id) {
    clearStatus()
    try {
      await api.deleteStation(id)
      showSuccess('Station removed.')
      reload()
    } catch (err) {
      showError(err.message)
    }
  }

  function edit(station) {
    setEditingId(station.id)
    setForm({ name: station.name, code: station.code })
  }

  return (
    <AdminCard title="Stations" eyebrow="Reference data">
      <form className="form-grid compact" onSubmit={submit}>
        <label>
          Name
          <input
            value={form.name}
            onChange={(event) => setForm({ ...form, name: event.target.value })}
            required
          />
        </label>
        <label>
          Code
          <input
            value={form.code}
            maxLength="16"
            onChange={(event) => setForm({ ...form, code: event.target.value })}
            required
          />
        </label>
        <button className="primary" type="submit">
          {editingId ? 'Save station' : 'Add station'}
        </button>
        {editingId && (
          <button
            className="secondary"
            type="button"
            onClick={() => {
              setEditingId(null)
              setForm({ name: '', code: '' })
            }}
          >
            Cancel
          </button>
        )}
      </form>
      <StatusMessage type={status.type}>{status.message}</StatusMessage>
      <EditableList
        items={stations}
        render={(station) => `${station.name} (${station.code})`}
        onEdit={edit}
        onDelete={remove}
      />
    </AdminCard>
  )
}

function TrainAdmin({ trains, reload }) {
  const [form, setForm] = useState({ name: '', capacity: 80 })
  const [editingId, setEditingId] = useState(null)
  const { status, clearStatus, showError, showSuccess } = useTimedStatus()

  async function submit(event) {
    event.preventDefault()
    clearStatus()
    try {
      const body = { name: form.name, capacity: Number(form.capacity) }
      if (editingId) {
        await api.updateTrain(editingId, body)
        showSuccess('Train updated.')
      } else {
        await api.createTrain(body)
        showSuccess('Train created.')
      }
      setForm({ name: '', capacity: 80 })
      setEditingId(null)
      reload()
    } catch (err) {
      showError(err.message)
    }
  }

  async function remove(id) {
    clearStatus()
    try {
      await api.deleteTrain(id)
      showSuccess('Train removed.')
      reload()
    } catch (err) {
      showError(err.message)
    }
  }

  function edit(train) {
    setEditingId(train.id)
    setForm({ name: train.name, capacity: train.capacity })
  }

  return (
    <AdminCard title="Trains" eyebrow="Fleet">
      <form className="form-grid compact" onSubmit={submit}>
        <label>
          Name
          <input
            value={form.name}
            onChange={(event) => setForm({ ...form, name: event.target.value })}
            required
          />
        </label>
        <label>
          Capacity
          <input
            type="number"
            min="1"
            value={form.capacity}
            onChange={(event) =>
              setForm({ ...form, capacity: event.target.value })
            }
            required
          />
        </label>
        <button className="primary" type="submit">
          {editingId ? 'Save train' : 'Add train'}
        </button>
        {editingId && (
          <button
            className="secondary"
            type="button"
            onClick={() => {
              setEditingId(null)
              setForm({ name: '', capacity: 80 })
            }}
          >
            Cancel
          </button>
        )}
      </form>
      <StatusMessage type={status.type}>{status.message}</StatusMessage>
      <EditableList
        items={trains}
        render={(train) => `${train.name} · ${train.capacity} seats`}
        onEdit={edit}
        onDelete={remove}
      />
    </AdminCard>
  )
}

function RouteAdmin({ stations, routes, reload }) {
  const [form, setForm] = useState({
    name: '',
    stops: [
      { stationId: '', arrivalOffsetMinutes: 0, departureOffsetMinutes: 0 },
      { stationId: '', arrivalOffsetMinutes: 120, departureOffsetMinutes: 120 },
    ],
  })
  const [editingId, setEditingId] = useState(null)
  const { status, clearStatus, showError, showSuccess } = useTimedStatus()

  function routePayload() {
    return {
      name: form.name,
      stops: form.stops.map((stop, index) => ({
        stationId: Number(stop.stationId || stations[index]?.id || stations[0]?.id),
        stopOrder: index + 1,
        arrivalOffsetMinutes: Number(stop.arrivalOffsetMinutes),
        departureOffsetMinutes: Number(stop.departureOffsetMinutes),
      })),
    }
  }

  async function submit(event) {
    event.preventDefault()
    clearStatus()
    try {
      const body = routePayload()
      const stationIds = body.stops.map((stop) => stop.stationId)
      if (new Set(stationIds).size !== stationIds.length) {
        showError('A route cannot contain the same station more than once.')
        return
      }
      if (editingId) {
        await api.updateRoute(editingId, body)
        showSuccess('Route updated.')
      } else {
        await api.createRoute(body)
        showSuccess('Route created.')
      }
      setEditingId(null)
      setForm({
        name: '',
        stops: [
          { stationId: '', arrivalOffsetMinutes: 0, departureOffsetMinutes: 0 },
          { stationId: '', arrivalOffsetMinutes: 120, departureOffsetMinutes: 120 },
        ],
      })
      reload()
    } catch (err) {
      showError(err.message)
    }
  }

  function updateStop(index, field, value) {
    setForm({
      ...form,
      stops: form.stops.map((stop, stopIndex) =>
        stopIndex === index ? { ...stop, [field]: value } : stop,
      ),
    })
  }

  function edit(route) {
    setEditingId(route.id)
    setForm({
      name: route.name,
      stops: route.stops.map((stop) => ({
        stationId: stop.station.id,
        arrivalOffsetMinutes: stop.arrivalOffsetMinutes,
        departureOffsetMinutes: stop.departureOffsetMinutes,
      })),
    })
  }

  async function remove(id) {
    clearStatus()
    try {
      await api.deleteRoute(id)
      showSuccess('Route removed.')
      reload()
    } catch (err) {
      showError(err.message)
    }
  }

  return (
    <AdminCard title="Routes" eyebrow="Network">
      <p className="helper-text">
        Define the ordered stops of one route. Changeovers are not added here; the
        search API creates them by combining separate scheduled trips that meet at a
        common station.
      </p>
      <form className="form-grid compact" onSubmit={submit}>
        <label className="wide">
          Route name
          <input
            value={form.name}
            onChange={(event) => setForm({ ...form, name: event.target.value })}
            required
          />
        </label>
        {form.stops.map((stop, index) => (
          <div className="stop-row" key={index}>
            <label>
              Stop {index + 1}
              <select
                value={stop.stationId || stations[index]?.id || stations[0]?.id || ''}
                onChange={(event) => updateStop(index, 'stationId', event.target.value)}
              >
                {stations.map((station) => (
                  <option key={station.id} value={station.id}>{station.name}</option>
                ))}
              </select>
            </label>
            <label>
              Arrival offset
              <input
                type="number"
                min="0"
                value={stop.arrivalOffsetMinutes}
                onChange={(event) =>
                  updateStop(index, 'arrivalOffsetMinutes', event.target.value)
                }
                required
              />
            </label>
            <label>
              Departure offset
              <input
                type="number"
                min="0"
                value={stop.departureOffsetMinutes}
                onChange={(event) =>
                  updateStop(index, 'departureOffsetMinutes', event.target.value)
                }
                required
              />
            </label>
            {form.stops.length > 2 && (
              <button
                className="secondary"
                type="button"
                onClick={() =>
                  setForm({
                    ...form,
                    stops: form.stops.filter((_, stopIndex) => stopIndex !== index),
                  })
                }
              >
                Remove stop
              </button>
            )}
          </div>
        ))}
        <button
          className="secondary"
          type="button"
          onClick={() =>
            setForm({
              ...form,
              stops: [
                ...form.stops,
                {
                  stationId: stations[form.stops.length]?.id || stations[0]?.id || '',
                  arrivalOffsetMinutes: 0,
                  departureOffsetMinutes: 0,
                },
              ],
            })
          }
        >
          Add stop
        </button>
        <button className="primary" type="submit">
          {editingId ? 'Save route' : 'Add route'}
        </button>
        {editingId && (
          <button
            className="secondary"
            type="button"
            onClick={() => {
              setEditingId(null)
              setForm({
                name: '',
                stops: [
                  { stationId: '', arrivalOffsetMinutes: 0, departureOffsetMinutes: 0 },
                  { stationId: '', arrivalOffsetMinutes: 120, departureOffsetMinutes: 120 },
                ],
              })
            }}
          >
            Cancel
          </button>
        )}
      </form>
      <StatusMessage type={status.type}>{status.message}</StatusMessage>
      <EditableList
        items={routes}
        render={(route) =>
          `${route.name} · ${route.stops.map((stop) => stop.station.code).join(' to ')}`
        }
        onEdit={edit}
        onDelete={remove}
      />
    </AdminCard>
  )
}

function TripAdmin({ trains, routes, refreshKey }) {
  const [form, setForm] = useState({ trainId: '', routeId: '', departureDateTime: '' })
  const [trips, setTrips] = useState([])
  const [editingId, setEditingId] = useState(null)
  const { status, clearStatus, showError, showSuccess } = useTimedStatus()
  const trainId = form.trainId || trains[0]?.id || ''
  const routeId = form.routeId || routes[0]?.id || ''

  const loadTrips = useCallback(async () => {
    try {
      setTrips(await api.getTrips())
    } catch (err) {
      showError(err.message)
    }
  }, [showError])

  useEffect(() => {
    // Data fetching is the external synchronization this admin panel owns.
    // eslint-disable-next-line react-hooks/set-state-in-effect
    loadTrips()
  }, [loadTrips, refreshKey])

  async function submit(event) {
    event.preventDefault()
    clearStatus()
    try {
      const body = {
        trainId: Number(trainId),
        routeId: Number(routeId),
        departureDateTime: form.departureDateTime,
      }
      if (editingId) {
        await api.updateTrip(editingId, body)
        showSuccess('Trip updated.')
      } else {
        const created = await api.createTrip(body)
        showSuccess(`Created trip #${created.id}.`)
      }
      setEditingId(null)
      setForm({ trainId: '', routeId: '', departureDateTime: '' })
      loadTrips()
    } catch (err) {
      showError(err.message)
    }
  }

  function edit(trip) {
    setEditingId(trip.id)
    setForm({
      trainId: trip.trainId,
      routeId: trip.routeId,
      departureDateTime: trip.departureDateTime,
    })
  }

  async function remove(id) {
    clearStatus()
    try {
      await api.deleteTrip(id)
      showSuccess('Trip removed.')
      loadTrips()
    } catch (err) {
      showError(err.message)
    }
  }

  return (
    <AdminCard title="Trips" eyebrow="Scheduling">
      <form className="form-grid compact" onSubmit={submit}>
        <label>
          Train
          <select
            value={trainId}
            onChange={(event) => setForm({ ...form, trainId: event.target.value })}
          >
            {trains.map((train) => (
              <option key={train.id} value={train.id}>{train.name}</option>
            ))}
          </select>
        </label>
        <label>
          Route
          <select
            value={routeId}
            onChange={(event) => setForm({ ...form, routeId: event.target.value })}
          >
            {routes.map((route) => (
              <option key={route.id} value={route.id}>{route.name}</option>
            ))}
          </select>
        </label>
        <label>
          Departure
          <input
            type="datetime-local"
            value={form.departureDateTime}
            onChange={(event) =>
              setForm({ ...form, departureDateTime: event.target.value })
            }
            required
          />
        </label>
        <button className="primary" type="submit">
          {editingId ? 'Save trip' : 'Create trip'}
        </button>
        {editingId && (
          <button
            className="secondary"
            type="button"
            onClick={() => {
              setEditingId(null)
              setForm({ trainId: '', routeId: '', departureDateTime: '' })
            }}
          >
            Cancel
          </button>
        )}
      </form>
      <StatusMessage type={status.type}>{status.message}</StatusMessage>
      <EditableList
        items={trips}
        render={(trip) =>
          `#${trip.id} · ${trip.train} · ${trip.route} · ${formatDateTime(trip.departureDateTime)} · ${trip.status}${trip.delayMinutes ? ` · ${trip.delayMinutes} min delay` : ''}`
        }
        onEdit={edit}
        onDelete={remove}
      />
    </AdminCard>
  )
}

function DelayAdmin({ onDelayApplied }) {
  const [form, setForm] = useState({ tripId: '', delayMinutes: 15, reason: '' })
  const { status, clearStatus, showError, showSuccess } = useTimedStatus()

  async function submit(event) {
    event.preventDefault()
    clearStatus()
    try {
      const delay = await api.applyDelay(form.tripId, {
          delayMinutes: Number(form.delayMinutes),
          reason: form.reason,
        })
      showSuccess(
        `Delay applied to trip #${delay.tripId}: ${delay.delayMinutes} minute(s). ${delay.notifiedBookings} booking notification(s) logged.`,
      )
      onDelayApplied()
    } catch (err) {
      showError(err.message)
    }
  }

  return (
    <AdminCard title="Delays" eyebrow="Service updates">
      <form className="form-grid compact" onSubmit={submit}>
        <label>
          Trip ID
          <input
            type="number"
            min="1"
            value={form.tripId}
            onChange={(event) => setForm({ ...form, tripId: event.target.value })}
            required
          />
        </label>
        <label>
          Delay minutes
          <input
            type="number"
            min="0"
            value={form.delayMinutes}
            onChange={(event) =>
              setForm({ ...form, delayMinutes: event.target.value })
            }
            required
          />
        </label>
        <label className="wide">
          Reason
          <input
            value={form.reason}
            onChange={(event) => setForm({ ...form, reason: event.target.value })}
            required
          />
        </label>
        <button className="primary" type="submit">Apply delay</button>
      </form>
      <StatusMessage type={status.type}>{status.message}</StatusMessage>
    </AdminCard>
  )
}

function TripBookingsAdmin() {
  const [tripId, setTripId] = useState('')
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(false)
  const { status, clearStatus, showError, showSuccess } = useTimedStatus()

  async function loadBookings() {
    setLoading(true)
    clearStatus()
    setBookings([])
    try {
      setBookings(await api.getTripBookings(tripId))
    } catch (err) {
      showError(err.message)
    } finally {
      setLoading(false)
    }
  }

  async function submit(event) {
    event.preventDefault()
    clearStatus()
    await loadBookings()
  }

  async function removeBooking(id) {
    clearStatus()
    try {
      await api.deleteBooking(id)
      setBookings((currentBookings) =>
        currentBookings.filter((booking) => booking.id !== id),
      )
      showSuccess(`Booking #${id} removed.`)
    } catch (err) {
      showError(err.message)
    }
  }

  return (
    <AdminCard title="Trip bookings" eyebrow="Manifest">
      <form className="inline-form" onSubmit={submit}>
        <label>
          Trip ID
          <input
            type="number"
            min="1"
            value={tripId}
            onChange={(event) => setTripId(event.target.value)}
            required
          />
        </label>
        <button className="primary" type="submit" disabled={loading}>
          {loading ? 'Loading...' : 'Load'}
        </button>
      </form>
      <StatusMessage type={status.type}>{status.message}</StatusMessage>
      {!bookings.length && !status.message && (
        <EmptyState title="No manifest loaded">
          Enter a trip id to list confirmed bookings for that trip.
        </EmptyState>
      )}
      <EditableList
        items={bookings}
        render={(booking) =>
          `#${booking.id} · ${booking.customerEmail} · ${booking.ticketCount} ticket(s)`
        }
        onDelete={removeBooking}
      />
    </AdminCard>
  )
}

function AdminCard({ title, eyebrow, children }) {
  return (
    <div className="panel admin-card">
      <div className="section-heading">
        <p className="eyebrow">{eyebrow}</p>
        <h2>{title}</h2>
      </div>
      {children}
    </div>
  )
}

function EditableList({ items, render, onEdit, onDelete }) {
  if (!items?.length) {
    return <EmptyState title="Empty">No records returned by the API.</EmptyState>
  }
  return (
    <ul className="editable-list">
      {items.map((item) => (
        <li key={item.id}>
          <span>{render(item)}</span>
          <div className="row-actions">
            {onEdit && (
              <button className="secondary" type="button" onClick={() => onEdit(item)}>
                Edit
              </button>
            )}
            <button className="danger" type="button" onClick={() => onDelete(item.id)}>
              Remove
            </button>
          </div>
        </li>
      ))}
    </ul>
  )
}

export default AppShell
