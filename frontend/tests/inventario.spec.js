import { test, expect } from "@playwright/test";
import fs from "node:fs";
import path from "node:path";

const root = path.resolve(import.meta.dirname, "../..");
const variables = Object.fromEntries(
  fs
    .readFileSync(path.join(root, ".env"), "utf8")
    .split(/\r?\n/)
    .filter((line) => /^[A-Z_]+=/.test(line))
    .map((line) => {
      const i = line.indexOf("=");
      return [line.slice(0, i), line.slice(i + 1)];
    }),
);
const evidence = path.join(root, "docs/evidencias");
fs.mkdirSync(evidence, { recursive: true });
async function login(page, username = "user") {
  await page.goto("/");
  await page.getByLabel("Usuario", { exact: true }).fill(username);
  await page
    .getByLabel("Contraseña", { exact: true })
    .fill(variables[username === "admin" ? "ADMIN_PASSWORD" : "USER_PASSWORD"]);
  await page.getByRole("button", { name: "Iniciar sesión" }).click();
  await expect(
    page.getByRole("heading", { name: "Inventario general" }),
  ).toBeVisible();
  await expect(page.getByText("Consultando información…")).toBeHidden();
}

test("USER consulta datos reales y no ve acciones de escritura", async ({
  page,
}) => {
  const pageErrors = [];
  page.on("pageerror", (error) => pageErrors.push(error.message));
  await login(page);
  await expect(page.getByRole("button", { name: "Nuevo equipo" })).toHaveCount(
    0,
  );
  await expect(
    page.getByRole("cell", { name: "UAM-001", exact: true }),
  ).toBeVisible();
  await page.screenshot({
    path: path.join(evidence, "react-user.png"),
    fullPage: true,
  });
  await page.getByRole("button", { name: "Préstamos", exact: false }).click();
  await expect(
    page.getByRole("heading", { name: "Historial de préstamos" }),
  ).toBeVisible();
  expect(pageErrors).toEqual([]);
});

test("ADMIN crea, edita, presta y devuelve desde React", async ({ page }) => {
  const code = `E2E-${Date.now()}`;
  await login(page, "admin");
  const auth = await page.request.post("/api/auth/login", {
    data: { username: "admin", password: variables.ADMIN_PASSWORD },
  });
  const token = (await auth.json()).token;
  const headers = { Authorization: `Bearer ${token}` };
  let equipoId;
  try {
    await page.getByRole("button", { name: "Nuevo equipo" }).click();
    await page.getByLabel("Código patrimonial").fill(code);
    await page.getByLabel("Nombre del equipo").fill("Equipo E2E");
    await page.getByRole("button", { name: "Guardar", exact: true }).click();
    let row = page.getByRole("row").filter({ hasText: code });
    await expect(row).toContainText("Equipo E2E");
    const equipos = await (
      await page.request.get("/api/equipos", { headers })
    ).json();
    equipoId = equipos.find((e) => e.codigoPatrimonial === code).id;
    await row.getByRole("button", { name: "Editar" }).click();
    await page.getByLabel("Nombre del equipo").fill("Equipo E2E actualizado");
    await page.getByRole("button", { name: "Guardar", exact: true }).click();
    await expect(row).toContainText("Equipo E2E actualizado");
    await row.getByRole("button", { name: "Prestar", exact: true }).click();
    await page
      .getByLabel("Nombre del solicitante")
      .fill("Instructor de prueba E2E");
    await page.getByRole("button", { name: "Guardar", exact: true }).click();
    await expect(row).toContainText("prestado");
    await page.screenshot({
      path: path.join(evidence, "react-admin-prestado.png"),
      fullPage: true,
    });
    await page.getByRole("button", { name: "Préstamos", exact: false }).click();
    const loanRow = page.getByRole("row").filter({ hasText: code });
    await loanRow.getByRole("button", { name: "Devolver" }).click();
    await expect(loanRow).toContainText("devuelto");
    await page.screenshot({
      path: path.join(evidence, "react-devolucion.png"),
      fullPage: true,
    });
    await page
      .getByRole("button", { name: "Equipos", exact: false })
      .first()
      .click();
    await expect(page.getByRole("row").filter({ hasText: code })).toContainText(
      "disponible",
    );
  } finally {
    if (equipoId) {
      const loans = await (
        await page.request.get("/api/prestamos", { headers })
      ).json();
      for (const loan of loans.filter(
        (p) => p.equipo.id === equipoId && p.estado === "ACTIVO",
      ))
        await page.request.put(`/api/prestamos/${loan.id}/devolver`, {
          headers,
        });
      await page.request.delete(`/api/equipos/${equipoId}`, { headers });
    }
  }
});

test("Actualizar consulta cambios creados externamente por HTTP", async ({
  page,
}) => {
  await login(page, "admin");
  const auth = await page.request.post("/api/auth/login", {
    data: { username: "admin", password: variables.ADMIN_PASSWORD },
  });
  const headers = { Authorization: `Bearer ${(await auth.json()).token}` };
  const code = `EXT-${Date.now()}`;
  const result = await page.request.post("/api/equipos", {
    headers,
    data: { codigoPatrimonial: code, nombre: "Alta externa", tipo: "TABLETA" },
  });
  expect(result.status()).toBe(201);
  const equipo = await result.json();
  try {
    await page.getByRole("button", { name: "Actualizar" }).click();
    await expect(page.getByRole("row").filter({ hasText: code })).toBeVisible();
  } finally {
    await page.request.delete(`/api/equipos/${equipo.id}`, { headers });
  }
});

test("Carga, lista vacía y error de red", async ({ page }) => {
  // Respuestas controladas solo para estados visuales; los demás casos consumen la API real.
  await login(page);
  let release;
  const gate = new Promise((resolve) => {
    release = resolve;
  });
  await page.route("**/api/equipos", async (route) => {
    await gate;
    await route.fulfill({ json: [] });
  });
  await page.getByRole("button", { name: "Actualizar" }).click();
  await expect(page.getByText("Consultando información…")).toBeVisible();
  await page.screenshot({
    path: path.join(evidence, "react-carga.png"),
    fullPage: true,
  });
  release();
  await expect(page.getByText("Aún no hay equipos registrados.")).toBeVisible();
  await page.screenshot({
    path: path.join(evidence, "react-vacio.png"),
    fullPage: true,
  });
  await page.unroute("**/api/equipos");
  await page.route("**/api/equipos", (route) => route.abort());
  await page.getByRole("button", { name: "Actualizar" }).click();
  await expect(page.getByRole("alert")).toContainText("No se pudo conectar");
  await page.screenshot({
    path: path.join(evidence, "react-error.png"),
    fullPage: true,
  });
});
