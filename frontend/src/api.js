import axios from "axios";
export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "/api",
  timeout: 15000,
});
export const errorMessage = (error) =>
  error.response?.data?.detail ||
  (error.response?.status === 403
    ? "Tu cuenta solo tiene permisos de consulta."
    : error.response?.status === 401
      ? "Sesión vencida o credenciales incorrectas."
      : "No se pudo conectar con el servidor.");
