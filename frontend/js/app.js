/* ── app.js ───Funciones globales compartidas por todas las páginas.
Siempre se carga ANTES que el JS específico de cada página. ───── */

/* Verifica sesión activa preguntándole al backend
   Si la cookie no existe o expiró → backend devuelve 401 → redirige al login */
async function verificarSesion() {
  try {
    await api.get("/api/auth/me"); // api viene de api.js
  } catch {
    localStorage.clear();
    window.location.href = "../index.html";
  }
}

/* Cierra sesión — le dice al backend que limpie la cookie (MaxAge=0) */
async function logout() {
  try {
    await api.post("/api/auth/logout");
  } catch {
    /* Si falla igual redirigimos */
  } finally {
    localStorage.clear();
    window.location.href = "../index.html";
  }
}

/* Nombre limpio del usuario logueado - seguira en localStorage porque no es sensible */
function getNombre() {
  const n =
    localStorage.getItem("nombre") || localStorage.getItem("username") || "";
  return n !== "undefined" ? n : "";
}
