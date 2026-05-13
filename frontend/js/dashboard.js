/* dashboard.js */

document.addEventListener("DOMContentLoaded", () => {
  verificarSesion(); // app.js — redirige si no hay token

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
    const [resPersonal, resAsistencia, resDeptos] = await Promise.all([
      fetch(`${API_BASE}/personal`, { headers: getHeaders() }), // app.js
      fetch(`${API_BASE}/asistencia?fecha=${fecha}`, { headers: getHeaders() }),
      fetch(`${API_BASE}/departamentos`, { headers: getHeaders() }),
    ]);

    setVal(
      "totalPersonal",
      resPersonal.ok ? (await resPersonal.json()).length : "—",
    );
    setVal(
      "totalAsistencias",
      resAsistencia.ok ? (await resAsistencia.json()).length : "—",
    );
    setVal("totalDeptos", resDeptos.ok ? (await resDeptos.json()).length : "—");
  } catch (err) {
    console.error("Error cargando estadísticas:", err);
    ["totalPersonal", "totalAsistencias", "totalDeptos"].forEach((id) =>
      setVal(id, "—"),
    );
  }
}
