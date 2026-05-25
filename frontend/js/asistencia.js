/* asistencia.js */

document.addEventListener("DOMContentLoaded", () => {
  verificarSesion();

  const hoy = fechaLocal();
  document.getElementById("filtro-fecha-desde").value = hoy;
  document.getElementById("filtro-fecha-hasta").value = hoy;

  const fechaEl = document.getElementById("fecha-hoy");
  if (fechaEl) fechaEl.textContent = hoy;

  cargarPersonal();
  cargarDepartamentos();
  cargarAsistenciaHoy();
});

/* ── Utilidad: fecha local YYYY-MM-DD ── */
function fechaLocal() {
  const d = new Date();
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const dia = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${dia}`;
}

/* ── Feedback visual ── */
function mostrarFeedback(id, mensaje, tipo) {
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = mensaje;
  el.className = `feedback-msg ${tipo}`;
  setTimeout(() => {
    el.className = "feedback-msg";
    el.textContent = "";
  }, 4000);
}

/* ── Badge de estado ── */
function badgeEstado(estado) {
  const clases = {
    presente: "badge-presente",
    tardanza: "badge-tardanza",
    salida_anticipada: "badge-tardanza",
    ausente: "badge-ausente",
    permiso: "badge-justificado",
    feriado: "badge-justificado",
    libre: "badge-justificado",
  };
  const etiquetas = {
    presente: "Presente",
    tardanza: "Tardanza",
    salida_anticipada: "Salida anticipada",
    ausente: "Ausente",
    permiso: "Permiso",
    feriado: "Feriado",
    libre: "Libre",
  };
  const key = estado?.toLowerCase();
  return `<span class="badge ${clases[key] || ""}">${etiquetas[key] || estado || "—"}</span>`;
}

/* ── Formatea "2025-07-10T08:03:00" → "08:03" ── */
function formatHora(isoStr) {
  if (!isoStr) return "—";
  const d = new Date(isoStr);
  if (isNaN(d)) return isoStr;
  return d.toLocaleTimeString("es-VE", { hour: "2-digit", minute: "2-digit" });
}

/* ── Render tabla genérica ── */
function renderTabla(lista) {
  const tbody = document.getElementById("tbody-asistencia");
  const count = document.getElementById("tabla-count");

  if (count)
    count.textContent = `${lista.length} registro${lista.length !== 1 ? "s" : ""}`;

  if (!lista.length) {
    tbody.innerHTML = `
      <tr>
        <td colspan="4" class="tabla-empty">
          <span></span>
          No hay registros en ese período.
        </td>
      </tr>`;
    return;
  }

  tbody.innerHTML = lista
    .map(
      (r) => `
      <tr>
        <td>${r.nombrePersonal ?? r.personal?.nombre ?? "—"} ${r.apellidoPersonal ?? r.personal?.apellido ?? ""}</td>
        <td>${r.departamento ?? r.personal?.departamento?.nombre ?? "—"}</td>
        <td>${r.fecha ?? "—"}</td>
        <td>${badgeEstado(r.estado)}</td>
      </tr>`,
    )
    .join("");
}

/* ── Carga selects ── */
async function cargarPersonal() {
  try {
    const res = await api.get("/api/personal");
    const sel = document.getElementById("filtro-personal");
    res.data.forEach((p) => {
      const opt = document.createElement("option");
      opt.value = p.id;
      opt.textContent = p.nombre
        ? `${p.nombre} ${p.apellido || ""}`.trim()
        : `ID ${p.id}`;
      sel.appendChild(opt);
    });
  } catch (err) {
    console.error("Error cargando personal:", err);
  }
}

async function cargarDepartamentos() {
  try {
    const res = await api.get("/api/departamentos");
    const sel = document.getElementById("filtro-depto");
    res.data.forEach((d) => {
      const opt = document.createElement("option");
      opt.value = d.id;
      opt.textContent = d.nombre;
      sel.appendChild(opt);
    });
  } catch (err) {
    console.error("Error cargando departamentos:", err);
  }
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

/* ── Consultar asistencias históricas ── */
async function consultarAsistencias() {
  const desde = document.getElementById("filtro-fecha-desde").value;
  const hasta = document.getElementById("filtro-fecha-hasta").value;
  const depto = document.getElementById("filtro-depto").value;
  const estado = document.getElementById("filtro-estado").value;
  const personal = document.getElementById("filtro-personal").value;
  const btn = document.getElementById("btn-consultar");

  if (!desde || !hasta) {
    mostrarFeedback(
      "feedback-consulta",
      "Selecciona las fechas para consultar.",
      "error",
    );
    return;
  }

  const params = { fechaDesde: desde, fechaHasta: hasta };
  if (depto) params.departamentoId = depto;
  if (estado) params.estado = estado;
  if (personal) params.personalId = personal;

  btn.setAttribute("aria-busy", "true");
  btn.textContent = "Consultando…";

  try {
    const res = await api.get("/api/asistencia", { params });
    renderTabla(res.data);
  } catch (err) {
    mostrarFeedback(
      "feedback-consulta",
      err.response?.data?.message || "Error consultando.",
      "error",
    );
    renderTabla([]);
  } finally {
    btn.removeAttribute("aria-busy");
    btn.textContent = "Consultar";
  }
}

/* ── Limpiar filtros ── */
function limpiarFiltros() {
  const hoy = fechaLocal();
  document.getElementById("filtro-fecha-desde").value = hoy;
  document.getElementById("filtro-fecha-hasta").value = hoy;
  document.getElementById("filtro-depto").value = "";
  document.getElementById("filtro-estado").value = "";
  document.getElementById("filtro-personal").value = "";
  consultarAsistencias();
}
