/* dashboard.js */

document.addEventListener("DOMContentLoaded", () => {
  verificarSesion(); // app.js

  // Muestra nombre en topbar y saludo
  const nombreLimpio = getNombre(); // app.js
  const saludoEl = document.getElementById("bienvenida-texto");
  const topbarNomEl = document.getElementById("topbar-nombre");

  if (saludoEl) {
    saludoEl.textContent = nombreLimpio
      ? `¡Hola, ${nombreLimpio}! Aquí el resumen de hoy.`
      : "Aquí el resumen de hoy.";
  }

  if (topbarNomEl && nombreLimpio) {
    topbarNomEl.textContent = nombreLimpio;
  }

  cargarEstadisticas();
});

/* Carga los 3 contadores desde el backend */
async function cargarEstadisticas() {
  const fecha = new Date().toISOString().split("T")[0];

  // Actualiza el texto de un elemento por id
  const setVal = (id, val) => {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
  };

  try {
    /* api de api.js — cookie viaja sola, sin headers manuales */
    const [resPersonal, resAsistencia, resDeptos] = await Promise.all([
      api.get("/api/personal"),
      api.get(`/api/asistencia?fecha=${fecha}`),
      api.get("/api/departamentos"),
    ]);

    setVal("totalPersonal", resPersonal.data.length);
    setVal("totalAsistencias", resAsistencia.data.length);
    setVal("totalDeptos", resDeptos.data.length);
  } catch (err) {
    console.error("Error cargando estadísticas:", err);
    ["totalPersonal", "totalAsistencias", "totalDeptos"].forEach((id) =>
      setVal(id, "—"),
    );
  }
}
