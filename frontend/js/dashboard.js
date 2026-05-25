/* dashboard.js */

document.addEventListener("DOMContentLoaded", () => {
  verificarSesion(); // app.js
  protegerModulo(["ROLE_ADMIN", "ROLE_ASISTENTE"]);

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
  cargarEstadisticasSemana();
});

/* Carga los 3 contadores desde el backend */
/* Carga los 3 contadores desde el backend de forma independiente */
async function cargarEstadisticas() {
  // Obtener la fecha local real en formato YYYY-MM-DD sin desfase UTC
  const d = new Date();
  const año = d.getFullYear();
  const mes = String(d.getMonth() + 1).padStart(2, "0");
  const dia = String(d.getDate()).padStart(2, "0");
  const fechaLocal = `${año}-${mes}-${dia}`;

  // Actualiza el texto de un elemento por id
  const setVal = (id, val) => {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
  };

  try {
    /* Usamos Promise.allSettled para que un fallo en un endpoint no rompa los demás */
    const resultados = await Promise.allSettled([
      api.get("/api/personal"),
      api.get(`/api/asistencia?fecha=${fechaLocal}`),
      api.get("/api/departamentos"),
    ]);

    // 1. Procesar Personal
    if (resultados[0].status === "fulfilled") {
      setVal("totalPersonal", resultados[0].value.data.length);
    } else {
      console.error("Error en /api/personal:", resultados[0].reason);
      setVal("totalPersonal", "0");
    }

    // 2. Procesar Asistencias de hoy
    if (resultados[1].status === "fulfilled") {
      setVal("totalAsistencias", resultados[1].value.data.length);
    } else {
      console.error("Error en /api/asistencia:", resultados[1].reason);
      setVal("totalAsistencias", "0");
    }

    // 3. Procesar Departamentos
    if (resultados[2].status === "fulfilled") {
      setVal("totalDeptos", resultados[2].value.data.length);
    } else {
      console.error("Error en /api/departamentos:", resultados[2].reason);
      setVal("totalDeptos", "0");
    }
  } catch (err) {
    console.error("Error crítico en bloque de estadísticas:", err);
    ["totalPersonal", "totalAsistencias", "totalDeptos"].forEach((id) =>
      setVal(id, "—"),
    );
  }
}

/* ── Semana actual ── */
function getSemanaActual() {
  const hoy = new Date();
  const diaSemana = hoy.getDay() || 7; // domingo=0 → 7
  const lunes = new Date(hoy);
  lunes.setDate(hoy.getDate() - (diaSemana - 1));
  const domingo = new Date(lunes);
  domingo.setDate(lunes.getDate() + 6);
  const fmt = (d) => d.toISOString().split("T")[0];
  return { desde: fmt(lunes), hasta: fmt(domingo) };
}

async function cargarEstadisticasSemana() {
  const { desde, hasta } = getSemanaActual();

  // Actualiza el link "Ver detalle "
  const link = document.getElementById("link-ver-detalle");
  if (link)
    link.href = `asistencia.html?fechaDesde=${desde}&fechaHasta=${hasta}`;

  const setVal = (id, val) => {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
  };

  try {
    const res = await api.get("/api/asistencia/estadisticas", {
      params: { fechaDesde: desde, fechaHasta: hasta },
    });
    const data = res.data;
    setVal("semana-presentes", data.presente ?? "—");
    setVal("semana-ausentes", data.ausente ?? "—");
    setVal("semana-tardanzas", data.tardanza ?? "—");
    setVal("semana-permisos", data.permiso ?? "—");
  } catch (err) {
    console.error("Error cargando estadisticas de semana:", err);
    [
      "semana-presentes",
      "semana-ausentes",
      "semana-tardanzas",
      "semana-permisos",
    ].forEach((id) => setVal(id, "—"));
  }
}
