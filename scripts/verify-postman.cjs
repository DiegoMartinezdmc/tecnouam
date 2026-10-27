const fs = require("node:fs");
const path = require("node:path");
const root = path.resolve(__dirname, "..");
const newman = require(path.join(root, ".tools/qa/node_modules/newman"));
const env = Object.fromEntries(
  fs
    .readFileSync(path.join(root, ".env"), "utf8")
    .split(/\r?\n/)
    .filter((line) => /^[A-Z_]+=/.test(line))
    .map((line) => {
      const i = line.indexOf("=");
      return [line.slice(0, i), line.slice(i + 1)];
    }),
);
newman.run(
  {
    collection: require(
      path.join(root, "postman/TecnoUAM.postman_collection.json"),
    ),
    envVar: [
      { key: "adminPassword", value: env.ADMIN_PASSWORD },
      { key: "userPassword", value: env.USER_PASSWORD },
    ],
    reporters: [],
  },
  (error, summary) => {
    if (error) {
      console.error(error.message);
      process.exitCode = 1;
      return;
    }
    const report = {
      fecha: new Date().toISOString(),
      herramienta: "Newman - colección Postman contra HTTP local",
      solicitudes: summary.run.executions.map((e) => ({
        nombre: e.item.name,
        estado: e.response?.code,
        pruebas: e.assertions?.map((a) => ({
          nombre: a.assertion,
          correcto: !a.error,
        })),
      })),
      fallos: summary.run.failures.map((f) => ({
        prueba: f.source?.name,
        mensaje: f.error?.message,
      })),
      estadisticas: summary.run.stats,
    };
    const directory = path.join(root, "docs/evidencias");
    fs.mkdirSync(directory, { recursive: true });
    fs.writeFileSync(
      path.join(directory, "resultados-api.json"),
      JSON.stringify(report, null, 2),
    );
    console.log(
      `Solicitudes: ${report.solicitudes.length}. Fallos: ${report.fallos.length}.`,
    );
    if (report.fallos.length) {
      console.log(JSON.stringify(report.fallos));
      process.exitCode = 1;
    }
  },
);
