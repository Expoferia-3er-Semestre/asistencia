document.addEventListener("DOMContentLoaded", () => {
  const token = localStorage.getItem("token");

  // Sin sesión → redirige al login
  if (!token) {
    window.location.href = "../index.html";
    return;
  }

  // Muestra el nombre en topbar y saludo
  const nombre =
    localStorage.getItem("nombre") || localStorage.getItem("username");
  const nombreLimpio = nombre && nombre !== "undefined" ? nombre : null;

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
  const headers = { Authorization: `Bearer ${localStorage.getItem("token")}` };
  const fecha = new Date().toISOString().split("T")[0];

  // Helper: actualiza texto de un elemento por id
  const setVal = (id, val) => {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
  };

  try {
    const [resPersonal, resAsistencia, resDeptos] = await Promise.all([
      fetch("http://localhost:8080/api/personal", { headers }),
      fetch(`http://localhost:8080/api/asistencia?fecha=${fecha}`, { headers }),
      fetch("http://localhost:8080/api/departamentos", { headers }),
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
    // Si falla la red muestra guión en los tres contadores
    console.error("Error cargando estadísticas:", err);
    ["totalPersonal", "totalAsistencias", "totalDeptos"].forEach((id) =>
      setVal(id, "—"),
    );
  }
}

/* Cierra sesión y limpia localStorage */
function logout() {
  localStorage.clear();
  window.location.href = "../index.html";
}
