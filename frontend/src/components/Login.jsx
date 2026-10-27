import React from "react";

export default function Login({
  login,
  credentials,
  setCredentials,
  busy,
  error,
}) {
  return (
    <main className="login">
      <section className="login-story">
        <div className="brand">
          T<span>U</span> <b>TecnoUAM</b>
        </div>
        <div>
          <p className="eyebrow">CENTRO DE CAPACITACIÓN</p>
          <h1>
            Tecnología lista.
            <br />
            Aprendizaje en marcha.
          </h1>
          <p>
            Gestiona el inventario y los préstamos de equipos desde un solo
            lugar.
          </p>
        </div>
        <small>Equipamiento tecnológico · Universidad Americana · Javier Espinoza</small>
      </section>
      <section className="login-form">
        <form onSubmit={login}>
          <p className="eyebrow">BIENVENIDO</p>
          <h2>Accede a tu espacio</h2>
          <p className="muted">
            Ingresa con tu cuenta institucional de demostración.
          </p>
          <label>
            Usuario
            <input
              autoComplete="username"
              required
              value={credentials.username}
              onChange={(e) =>
                setCredentials({ ...credentials, username: e.target.value })
              }
            />
          </label>
          <label>
            Contraseña
            <input
              type="password"
              autoComplete="current-password"
              required
              value={credentials.password}
              onChange={(e) =>
                setCredentials({ ...credentials, password: e.target.value })
              }
            />
          </label>
          {error && (
            <p role="alert" className="error">
              {error}
            </p>
          )}
          <button disabled={busy}>
            {busy ? "Validando…" : "Iniciar sesión →"}
          </button>
          <small className="muted">
            Consulta con USER · Gestión completa con ADMIN
          </small>
        </form>
      </section>
    </main>
  );
}
