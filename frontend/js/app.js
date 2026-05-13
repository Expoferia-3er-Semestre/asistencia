/* ── app.js ───Funciones globales compartidas por todas las páginas.
Siempre se carga ANTES que el JS específico de cada página. ───── */

const API_BASE = "http://localhost:8080/api";

/* Cabecera JSON + token para todas las peticiones protegidas */
function getHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${localStorage.getItem("token")}`,
  };
}

/* Redirige al login si no hay token */
function verificarSesion() {
  if (!localStorage.getItem("token")) {
    window.location.href = "../index.html";
  }
}

/* Cierra sesión y limpia localStorage */
function logout() {
  localStorage.clear();
  window.location.href = "../index.html";
}

/* Nombre limpio del usuario logueado */
function getNombre() {
  const n =
    localStorage.getItem("nombre") || localStorage.getItem("username") || "";
  return n !== "undefined" ? n : "";
}
