/* asistencia.js */

document.addEventListener("DOMContentLoaded", () => {
  verificarSesion();

  // Muestra la fecha de hoy en el encabezado
  const fechaEl = document.getElementById("fecha-hoy");
  if (fechaEl) fechaEl.textContent = fechaLocal();

  cargarAsistenciaHoy();
});

/* ── Utilidad: fecha local YYYY-MM-DD sin desfase UTC ── */
function fechaLocal() {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const dia = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${dia}`;
}

/* ── Badge de estado ── */
function badgeEstado(estado) {
  const clases = {
    PRESENTE: "badge-presente",
    AUSENTE: "badge-ausente",
    TARDANZA: "badge-tardanza",
    JUSTIFICADO: "badge-justificado",
  };
  const etiqueta = estado
    ? estado.charAt(0) + estado.slice(1).toLowerCase()
    : "—";
  return `<span class="badge ${clases[estado] || ""}">${etiqueta}</span>`;
}

/* ── Formatea "2025-07-10T08:03:00" → "08:03" ── */
function formatHora(isoStr) {
  if (!isoStr) return "—";
  const d = new Date(isoStr);
  if (isNaN(d)) return isoStr;
  return d.toLocaleTimeString("es-VE", { hour: "2-digit", minute: "2-digit" });
}

/* ── Tabla del día ── */
async function cargarAsistenciaHoy() {
  const btn = document.getElementById("btn-actualizar");
  const tbody = document.getElementById("tbody-hoy");
  const count = document.getElementById("tabla-count-hoy");

  if (btn) {
    btn.setAttribute("aria-busy", "true");
    btn.textContent = "Cargando…";
  }

  try {
    const res = await api.get(`/api/asistencia?fecha=${fechaLocal()}`);
    const lista = res.data;

    if (count)
      count.textContent = `${lista.length} registro${lista.length !== 1 ? "s" : ""}`;

    if (!lista.length) {
      tbody.innerHTML = `
        <tr>
          <td colspan="4" class="tabla-empty">
            <span></span>
            Sin registros por ahora. Escanea un QR para registrar asistencia.
          </td>
        </tr>`;
      return;
    }

    tbody.innerHTML = lista
      .map(
        (r) => `
        <tr>
          <td>${r.nombrePersonal ?? r.personal?.nombre ?? "—"} ${r.apellidoPersonal ?? r.personal?.apellido ?? ""}</td>
          <td>${formatHora(r.horaEntrada ?? r.fechaHora ?? null)}</td>
          <td>${formatHora(r.horaSalida ?? null)}</td>
          <td>${badgeEstado(r.estado)}</td>
        </tr>`,
      )
      .join("");
  } catch (err) {
    console.error("Error cargando asistencia de hoy:", err);
    tbody.innerHTML = `
      <tr>
        <td colspan="4" class="tabla-empty">
          No se pudo cargar la tabla. Verifica la conexión.
        </td>
      </tr>`;
  } finally {
    if (btn) {
      btn.removeAttribute("aria-busy");
      btn.textContent = "Actualizar";
    }
  }
}
