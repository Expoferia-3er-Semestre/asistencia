/* api.js — instancia Axios compartida */

const NGROK_URL = "https://agile-dawdler-factor.ngrok-free.dev";
const LOCAL_URL = "http://localhost:8080"; // O tu IP local si lo prefieres

// Evaluamos el host actual de la barra de direcciones
const BASE_URL = window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1"
  ? LOCAL_URL 
  : NGROK_URL;

const api = axios.create({
  baseURL: BASE_URL,
  withCredentials: true,
});

// Condición inteligente: Solo añade la cabecera si la URL base es la de Ngrok
if (BASE_URL === NGROK_URL) {
  api.defaults.headers.common["Ngrok-Skip-Browser-Warning"] = "true";
}