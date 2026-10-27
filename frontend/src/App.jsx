import Badge from "./components/Badge";
import Login from "./components/Login";
import React, { useEffect, useState } from "react";
import { api, errorMessage } from "./api";

const emptyEquipo = { codigoPatrimonial: "", nombre: "", tipo: "PORTATIL" };
const today = () => new Date().toLocaleDateString("en-CA");
const types = {
  PORTATIL: "Portátil",
  PROYECTOR: "Proyector",
  TABLETA: "Tableta",
};
export default function App() {
  const [session, setSession] = useState(null);
  const [credentials, setCredentials] = useState({
    username: "",
    password: "",
  });
  const [equipos, setEquipos] = useState([]),
    [prestamos, setPrestamos] = useState([]);
  const [tab, setTab] = useState("equipos"),
    [search, setSearch] = useState("");
  const [loading, setLoading] = useState(false),
    [busy, setBusy] = useState(false);
  const [error, setError] = useState(""),
    [notice, setNotice] = useState("");
  const [modal, setModal] = useState(null),
    [form, setForm] = useState({});
  const admin = session?.role === "ADMIN";
  async function load() {
    setLoading(true);
    setError("");
    try {
      const [e, p] = await Promise.all([
        api.get("/equipos"),
        api.get("/prestamos"),
      ]);
      setEquipos(e.data);
      setPrestamos(p.data);
    } catch (e) {
      setError(errorMessage(e));
      if (e.response?.status === 401) logout();
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    if (session) load();
  }, [session]);
  function logout() {
    delete api.defaults.headers.common.Authorization;
    setSession(null);
    setEquipos([]);
    setPrestamos([]);
    setModal(null);
  }
  async function login(event) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const { data } = await api.post("/auth/login", credentials);
      api.defaults.headers.common.Authorization = `Bearer ${data.token}`;
      setSession(data);
      setCredentials({ username: "", password: "" });
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }
  function open(kind, equipo) {
    setError("");
    setModal({ kind, id: equipo?.id });
    setForm(
      kind === "prestamo"
        ? {
            equipoId: equipo.id,
            solicitante: "",
            fechaPrestamo: today(),
            fechaPrevistaDevolucion: today(),
          }
        : equipo
          ? {
              codigoPatrimonial: equipo.codigoPatrimonial,
              nombre: equipo.nombre,
              tipo: equipo.tipo,
            }
          : { ...emptyEquipo },
    );
  }
  async function mutate(action, message) {
    setBusy(true);
    setError("");
    setNotice("");
    try {
      await action();
      setModal(null);
      setNotice(message);
      await load();
    } catch (e) {
      setError(errorMessage(e));
      if (e.response?.status === 401) logout();
    } finally {
      setBusy(false);
    }
  }
  function save(event) {
    event.preventDefault();
    mutate(
      () =>
        modal.kind === "prestamo"
          ? api.post("/prestamos", form)
          : modal.id
            ? api.put(`/equipos/${modal.id}`, form)
            : api.post("/equipos", form),
      "Registro guardado correctamente.",
    );
  }
  const field = (key, label, props = {}) => (
    <label>
      {label}
      <input
        required
        value={form[key] ?? ""}
        onChange={(e) => setForm({ ...form, [key]: e.target.value })}
        {...props}
      />
    </label>
  );
  if (!session)
    return (
      <Login
        login={login}
        credentials={credentials}
        setCredentials={setCredentials}
        busy={busy}
        error={error}
      />
    );
  const filtered = equipos.filter((e) =>
    `${e.nombre} ${e.codigoPatrimonial} ${types[e.tipo]}`
      .toLowerCase()
      .includes(search.toLowerCase()),
  );
  return (
    <div className="shell">
      <aside>
        <div className="brand">
          T<span>U</span> <b>TecnoUAM</b>
        </div>
        <p className="eyebrow">GESTIÓN ACADÉMICA</p>
        <nav>
          <button
            className={tab === "equipos" ? "active" : ""}
            onClick={() => setTab("equipos")}
          >
            ▦ Equipos
          </button>
          <button
            className={tab === "prestamos" ? "active" : ""}
            onClick={() => setTab("prestamos")}
          >
            ⇄ Préstamos
          </button>
        </nav>
        <div className="aside-footer">
          Centro de capacitación
          <br />
          <b>Universidad Americana</b>
        </div>
      </aside>
      <div className="workspace">
        <header>
          <span>
            Inventario /{" "}
            <b>{tab === "equipos" ? "Equipos tecnológicos" : "Préstamos"}</b>
          </span>
          <div>
            <span className="avatar">{session.username[0].toUpperCase()}</span>{" "}
            {session.username} <small>{session.role}</small>
            <button className="text" onClick={logout}>
              Salir
            </button>
          </div>
        </header>
        <main>
          <div className="page-heading">
            <div>
              <p className="eyebrow">CONTROL DE EQUIPAMIENTO</p>
              <h1>
                {tab === "equipos"
                  ? "Equipos tecnológicos"
                  : "Préstamos de equipos"}
              </h1>
              <p className="muted">
                Disponibilidad y movimientos conectados con tu inventario.
              </p>
            </div>
            {admin && (
              <button
                onClick={() => {
                  setTab("equipos");
                  open("equipo");
                }}
              >
                + Nuevo equipo
              </button>
            )}
          </div>
          <section className="stats">
            <article>
              <span>Equipos registrados</span>
              <strong>{equipos.length}</strong>
              <small>Inventario total</small>
            </article>
            <article>
              <span>Disponibles</span>
              <strong>
                {equipos.filter((e) => e.estado === "DISPONIBLE").length}
              </strong>
              <small>Listos para un nuevo préstamo</small>
            </article>
            <article>
              <span>Préstamos activos</span>
              <strong>
                {prestamos.filter((p) => p.estado === "ACTIVO").length}
              </strong>
              <small>Equipos en uso</small>
            </article>
          </section>
          {error && (
            <p className="error" role="alert">
              {error}
            </p>
          )}
          {notice && (
            <p className="success" role="status">
              {notice}
            </p>
          )}
          <section className="panel">
            <div className="toolbar">
              <div>
                <h2>
                  {tab === "equipos"
                    ? "Inventario general"
                    : "Historial de préstamos"}
                </h2>
                <small className="muted">
                  {admin
                    ? "Administra los recursos del centro"
                    : "Acceso de consulta"}
                </small>
              </div>
              <div className="tools">
                {tab === "equipos" && (
                  <input
                    aria-label="Buscar equipo"
                    placeholder="Buscar código o equipo…"
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                  />
                )}
                <button
                  className="secondary"
                  disabled={loading || busy}
                  onClick={load}
                >
                  ↻ Actualizar
                </button>
              </div>
            </div>
            {loading ? (
              <p className="empty" role="status">
                Consultando información…
              </p>
            ) : tab === "equipos" ? (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>Código</th>
                      <th>Equipo</th>
                      <th>Tipo</th>
                      <th>Estado</th>
                      {admin && <th>Acciones</th>}
                    </tr>
                  </thead>
                  <tbody>
                    {filtered.map((e) => (
                      <tr key={e.id}>
                        <td className="code">{e.codigoPatrimonial}</td>
                        <td>
                          <b>{e.nombre}</b>
                        </td>
                        <td>{types[e.tipo]}</td>
                        <td>
                          <Badge value={e.estado} />
                        </td>
                        {admin && (
                          <td className="actions">
                            <button
                              disabled={busy || e.estado !== "DISPONIBLE"}
                              className="text"
                              onClick={() => open("prestamo", e)}
                            >
                              Prestar
                            </button>
                            <button
                              disabled={busy}
                              className="text"
                              onClick={() => open("equipo", e)}
                            >
                              Editar
                            </button>
                            <button
                              disabled={busy}
                              className="text danger"
                              onClick={() =>
                                setModal({ kind: "eliminar", id: e.id })
                              }
                            >
                              Eliminar
                            </button>
                          </td>
                        )}
                      </tr>
                    ))}
                  </tbody>
                </table>
                {!filtered.length && (
                  <p className="empty">
                    {equipos.length
                      ? "No hay coincidencias para tu búsqueda."
                      : "Aún no hay equipos registrados."}
                  </p>
                )}
              </div>
            ) : (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>Equipo</th>
                      <th>Solicitante</th>
                      <th>Préstamo</th>
                      <th>Devolución prevista</th>
                      <th>Estado</th>
                      {admin && <th>Acciones</th>}
                    </tr>
                  </thead>
                  <tbody>
                    {prestamos.map((p) => (
                      <tr key={p.id}>
                        <td>
                          <b>{p.equipo.nombre}</b>
                          <small className="block muted">
                            {p.equipo.codigoPatrimonial}
                          </small>
                        </td>
                        <td>{p.solicitante}</td>
                        <td>{p.fechaPrestamo}</td>
                        <td>{p.fechaPrevistaDevolucion}</td>
                        <td>
                          <Badge value={p.estado} />
                        </td>
                        {admin && (
                          <td>
                            <button
                              disabled={busy || p.estado !== "ACTIVO"}
                              className="text"
                              onClick={() =>
                                mutate(
                                  () => api.put(`/prestamos/${p.id}/devolver`),
                                  "Devolución registrada.",
                                )
                              }
                            >
                              Devolver
                            </button>
                          </td>
                        )}
                      </tr>
                    ))}
                  </tbody>
                </table>
                {!prestamos.length && (
                  <p className="empty">Aún no hay préstamos registrados.</p>
                )}
              </div>
            )}
            <footer className="panel-footer">
              Información obtenida de la API · Usa Actualizar para consultar
              cambios externos.
            </footer>
          </section>
        </main>
      </div>
      {modal && (
        <div className="overlay">
          <section
            className="dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="modal-title"
          >
            <h2 id="modal-title">
              {modal.kind === "eliminar"
                ? "Eliminar equipo"
                : modal.kind === "prestamo"
                  ? "Registrar préstamo"
                  : modal.id
                    ? "Editar equipo"
                    : "Nuevo equipo"}
            </h2>
            {modal.kind === "eliminar" ? (
              <>
                {error && (
                  <p className="error" role="alert">
                    {error}
                  </p>
                )}
                <p>
                  Se eliminará el equipo y su historial devuelto. Un préstamo
                  activo impedirá la eliminación.
                </p>
                <div className="dialog-actions">
                  <button
                    className="secondary"
                    disabled={busy}
                    onClick={() => setModal(null)}
                  >
                    Cancelar
                  </button>
                  <button
                    disabled={busy}
                    onClick={() =>
                      mutate(
                        () => api.delete(`/equipos/${modal.id}`),
                        "Equipo eliminado.",
                      )
                    }
                  >
                    Confirmar eliminación
                  </button>
                </div>
              </>
            ) : (
              <form onSubmit={save}>
                {modal.kind === "prestamo" ? (
                  <>
                    {field("solicitante", "Nombre del solicitante", {
                      maxLength: 120,
                    })}
                    {field("fechaPrestamo", "Fecha de préstamo", {
                      type: "date",
                    })}
                    {field("fechaPrevistaDevolucion", "Devolución prevista", {
                      type: "date",
                      min: form.fechaPrestamo,
                    })}
                  </>
                ) : (
                  <>
                    {field("codigoPatrimonial", "Código patrimonial", {
                      maxLength: 40,
                      pattern: "[A-Za-z0-9_-]+",
                    })}
                    {field("nombre", "Nombre del equipo", { maxLength: 120 })}
                    <label>
                      Tipo
                      <select
                        value={form.tipo}
                        onChange={(e) =>
                          setForm({ ...form, tipo: e.target.value })
                        }
                      >
                        {Object.entries(types).map(([v, label]) => (
                          <option key={v} value={v}>
                            {label}
                          </option>
                        ))}
                      </select>
                    </label>
                  </>
                )}
                {error && (
                  <p className="error" role="alert">
                    {error}
                  </p>
                )}
                <div className="dialog-actions">
                  <button
                    type="button"
                    className="secondary"
                    disabled={busy}
                    onClick={() => setModal(null)}
                  >
                    Cancelar
                  </button>
                  <button disabled={busy}>
                    {busy ? "Guardando…" : "Guardar"}
                  </button>
                </div>
              </form>
            )}
          </section>
        </div>
      )}
    </div>
  );
}
