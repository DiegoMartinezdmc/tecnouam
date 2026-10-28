import { useEffect, useState } from 'react'
import axios from 'axios'
import './App.css'

const api = axios.create({ baseURL: '/api', timeout: 10000 })

function Login({ onLogin, error }) {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [localError, setLocalError] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()
    setSubmitting(true)
    setLocalError('')
    try {
      const response = await api.post('/auth/login', { username, password })
      if (typeof response.data?.token !== 'string' || !response.data.token) {
        throw new Error('El servidor no entregó un token válido.')
      }
      onLogin(response.data.token, username)
      setPassword('')
    } catch (requestError) {
      setLocalError(
        requestError.response?.status === 401
          ? 'Usuario o contraseña incorrectos.'
          : requestError.message === 'El servidor no entregó un token válido.'
            ? requestError.message
            : 'No se pudo conectar con la API. Comprueba que esté en ejecución.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="login-layout">
      <div className="brand-mark" aria-hidden="true">T</div>
      <section className="login-card" aria-labelledby="login-title">
        <p className="eyebrow">TECNOUAM · INVENTARIO</p>
        <h1 id="login-title">Accede al inventario</h1>
        <p className="muted">Inicia sesión para consultar el estado actual de los equipos tecnológicos.</p>
        <form onSubmit={handleSubmit}>
          <label htmlFor="username">Usuario</label>
          <input id="username" name="username" autoComplete="username" value={username}
            onChange={(event) => setUsername(event.target.value)} required />
          <label htmlFor="password">Contraseña</label>
          <input id="password" name="password" type="password" autoComplete="current-password"
            value={password} onChange={(event) => setPassword(event.target.value)} required />
          {(localError || error) && <p className="form-error" role="alert">{localError || error}</p>}
          <button className="primary-button" type="submit" disabled={submitting}>
            {submitting ? 'Ingresando…' : 'Iniciar sesión'}
          </button>
        </form>
      </section>
      <p className="login-footer">Consulta segura de equipos y préstamos</p>
    </main>
  )
}

function EquipoTable({ equipos }) {
  return (
    <div className="table-scroll">
      <table>
        <thead>
          <tr>
            <th scope="col">Código patrimonial</th>
            <th scope="col">Nombre</th>
            <th scope="col">Tipo</th>
            <th scope="col">Estado</th>
          </tr>
        </thead>
        <tbody>
          {equipos.map((equipo) => (
            <tr key={equipo.id}>
              <td className="code-cell">{equipo.codigoPatrimonial}</td>
              <td className="name-cell">{equipo.nombre}</td>
              <td>{equipo.tipo}</td>
              <td>
                <span className={'status ' + (equipo.estado === 'DISPONIBLE' ? 'status-available' : 'status-loaned')}>
                  <span className="status-dot" aria-hidden="true" />
                  {equipo.estado === 'DISPONIBLE' ? 'Disponible' : 'Prestado'}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function App() {
  const [token, setToken] = useState(() => sessionStorage.getItem('tecnouam_token'))
  const [username, setUsername] = useState(() => sessionStorage.getItem('tecnouam_username') || '')
  const [authError, setAuthError] = useState('')
  const [equipos, setEquipos] = useState([])
  const [loading, setLoading] = useState(() => Boolean(sessionStorage.getItem('tecnouam_token')))
  const [error, setError] = useState('')
  const [refreshKey, setRefreshKey] = useState(0)
  const [updatedAt, setUpdatedAt] = useState(null)

  useEffect(() => {
    if (!token) return undefined
    const controller = new AbortController()
    let active = true

    async function loadEquipos() {
      setLoading(true)
      setError('')
      try {
        const response = await api.get('/equipos', {
          headers: { Authorization: 'Bearer ' + token },
          signal: controller.signal,
        })
        if (!Array.isArray(response.data)) throw new Error('Respuesta inesperada de la API')
        if (active) {
          setEquipos(response.data)
          setUpdatedAt(new Date())
        }
      } catch (requestError) {
        if (!active || axios.isCancel(requestError)) return
        if (requestError.response?.status === 401) {
          sessionStorage.removeItem('tecnouam_token')
          sessionStorage.removeItem('tecnouam_username')
          setToken(null)
          setAuthError('Tu sesión venció. Inicia sesión nuevamente.')
        } else {
          setError('No se pudo cargar la lista de equipos. Comprueba la conexión e inténtalo de nuevo.')
        }
      } finally {
        if (active) setLoading(false)
      }
    }

    loadEquipos()
    return () => {
      active = false
      controller.abort()
    }
  }, [token, refreshKey])

  function handleLogin(newToken, newUsername) {
    sessionStorage.setItem('tecnouam_token', newToken)
    sessionStorage.setItem('tecnouam_username', newUsername)
    setAuthError('')
    setLoading(true)
    setUsername(newUsername)
    setToken(newToken)
  }

  function handleLogout() {
    sessionStorage.removeItem('tecnouam_token')
    sessionStorage.removeItem('tecnouam_username')
    setToken(null)
    setEquipos([])
    setUpdatedAt(null)
  }

  if (!token) return <Login onLogin={handleLogin} error={authError} />

  const disponibles = equipos.filter((equipo) => equipo.estado === 'DISPONIBLE').length
  const prestados = equipos.filter((equipo) => equipo.estado === 'PRESTADO').length
  const showStats = !loading && !error

  return (
    <div className="app-shell">
      <header className="site-header">
        <div className="header-brand"><div className="brand-mark small" aria-hidden="true">T</div><span>TecnoUAM</span></div>
        <div className="header-user"><span className="user-label">{username}</span><button type="button" className="text-button" onClick={handleLogout}>Cerrar sesión</button></div>
      </header>

      <main className="dashboard">
        <div className="heading-row">
          <div>
            <p className="eyebrow">CONTROL DE ACTIVOS</p>
            <h1>Equipos tecnológicos</h1>
            <p className="muted">Consulta la disponibilidad actual del inventario.</p>
          </div>
          <button className="refresh-button" type="button" onClick={() => setRefreshKey((key) => key + 1)} disabled={loading}>
            <span aria-hidden="true">↻</span> {loading ? 'Actualizando…' : 'Actualizar lista'}
          </button>
        </div>

        <section className="summary" aria-label="Resumen del inventario">
          <div className="summary-item"><span>Total de equipos</span><strong>{showStats ? equipos.length : '—'}</strong></div>
          <div className="summary-item"><span>Disponibles</span><strong>{showStats ? disponibles : '—'}</strong></div>
          <div className="summary-item"><span>Prestados</span><strong>{showStats ? prestados : '—'}</strong></div>
        </section>

        <section className="inventory-card" aria-labelledby="inventory-title">
          <div className="card-heading">
            <div><h2 id="inventory-title">Inventario</h2><p className="muted">Datos obtenidos de la API de TecnoUAM</p></div>
            {updatedAt && !loading && !error && <span className="updated-at">Actualizado a las {updatedAt.toLocaleTimeString('es-GT', { hour: '2-digit', minute: '2-digit' })}</span>}
          </div>
          {loading ? (
            <div className="state-panel" role="status"><span className="spinner" aria-hidden="true" />Cargando equipos…</div>
          ) : error ? (
            <div className="state-panel error-panel" role="alert"><strong>No fue posible consultar el inventario</strong><span>{error}</span><button type="button" className="inline-button" onClick={() => setRefreshKey((key) => key + 1)}>Reintentar</button></div>
          ) : equipos.length === 0 ? (
            <div className="state-panel empty-panel"><span className="empty-icon" aria-hidden="true">▦</span><strong>Aún no hay equipos registrados</strong><span>Los equipos que registre un administrador aparecerán aquí al actualizar la lista.</span></div>
          ) : (
            <EquipoTable equipos={equipos} />
          )}
        </section>
      </main>
    </div>
  )
}

export default App
