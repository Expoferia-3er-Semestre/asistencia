/* asistencia.js */

document.addEventListener("DOMContentLoaded", () => {
  verificarSesion(); // app.js — redirige si no hay token

  // Fecha de hoy por defecto en los tres inputs de fecha
  const hoy = new Date().toISOString().split("T")[0];
  document.getElementById("reg-fecha").value = hoy;
  document.getElementById("filtro-fecha-desde").value = hoy;
  document.getElementById("filtro-fecha-hasta").value = hoy;

  cargarPersonal();
  cargarDepartamentos();
});

/* ── Feedback visual ──────────────────────────────────── */

/* Muestra mensaje de éxito o error y lo oculta a los 4s */
function mostrarFeedback(id, mensaje, tipo) {
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = mensaje;
  el.className = `feedback-msg ${tipo}`; // 'success' | 'error'
  setTimeout(() => {
    el.className = "feedback-msg";
    el.textContent = "";
  }, 4000);
}

/* Devuelve el HTML del badge según el estado */
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

/* ── Carga de selects ─────────────────────────────────── */

/* Llena el select de personal para el formulario de registro */
async function cargarPersonal() {
  try {
    const res = await fetch(`${API_BASE}/personal`, { headers: getHeaders() }); // app.js
    if (!res.ok) return;

    const lista = await res.json();
    const sel = document.getElementById("reg-personal");

    lista.forEach((p) => {
      const opt = document.createElement("option");
      opt.value = p.id;
      // Ajusta los campos según tu modelo de Personal
      opt.textContent = p.nombre
        ? `${p.nombre} ${p.apellido || ""}`.trim()
        : `ID ${p.id}`;
      sel.appendChild(opt);
    });
  } catch (err) {
    console.error("Error cargando personal:", err);
  }
}

/* Llena el select de departamentos para el filtro */
async function cargarDepartamentos() {
  try {
    const res = await fetch(`${API_BASE}/departamentos`, {
      headers: getHeaders(),
    }); // app.js
    if (!res.ok) return;

    const lista = await res.json();
    const sel = document.getElementById("filtro-depto");

    lista.forEach((d) => {
      const opt = document.createElement("option");
      opt.value = d.id;
      opt.textContent = d.nombre;
      sel.appendChild(opt);
    });
  } catch (err) {
    console.error("Error cargando departamentos:", err);
  }
}

/* ── Registrar asistencia ─────────────────────────────── */

async function registrarAsistencia() {
  const personalId = document.getElementById("reg-personal").value;
  const fecha = document.getElementById("reg-fecha").value;
  const estado = document.getElementById("reg-estado").value;
  const btn = document.getElementById("btn-registrar");

  if (!personalId || !fecha || !estado) {
    mostrarFeedback(
      "feedback-registro",
      "Completa todos los campos antes de guardar.",
      "error",
    );
    return;
  }

  btn.setAttribute("aria-busy", "true");
  btn.textContent = "Guardando…";

  try {
    const res = await fetch(`${API_BASE}/asistencia`, {
      method: "POST",
      headers: getHeaders(), // app.js
      // Ajusta el body según lo que espere tu backend
      body: JSON.stringify({ personalId, fecha, estado }),
    });

    if (res.ok) {
      mostrarFeedback(
        "feedback-registro",
        "Asistencia registrada correctamente.",
        "success",
      );
      consultarAsistencias(); // refresca la tabla
    } else {
      const err = await res.json().catch(() => ({}));
      mostrarFeedback(
        "feedback-registro",
        err.message || "No se pudo registrar la asistencia.",
        "error",
      );
    }
  } catch {
    mostrarFeedback(
      "feedback-registro",
      "Error de conexión con el servidor.",
      "error",
    );
  } finally {
    btn.removeAttribute("aria-busy");
    btn.textContent = "Guardar";
  }
}

/* ── Consultar asistencias ────────────────────────────── */

async function consultarAsistencias() {
  const desde = document.getElementById("filtro-fecha-desde").value;
  const hasta = document.getElementById("filtro-fecha-hasta").value;
  const depto = document.getElementById("filtro-depto").value;
  const btn = document.getElementById("btn-consultar");

  // Validación — ambas fechas son obligatorias
  if (!desde || !hasta) {
    mostrarFeedback(
      "feedback-consulta",
      "Selecciona las fechas para consultar.",
      "error",
    );
    return;
  }

  const params = new URLSearchParams();
  params.append("fechaDesde", desde);
  params.append("fechaHasta", hasta);
  if (depto) params.append("departamentoId", depto);

  btn.setAttribute("aria-busy", "true");
  btn.textContent = "Consultando…";

  try {
    const res = await fetch(`${API_BASE}/asistencia?${params.toString()}`, {
      headers: getHeaders(), // app.js
    });
    renderTabla(res.ok ? await res.json() : []);
  } catch (err) {
    console.error("Error consultando asistencias:", err);
    mostrarFeedback(
      "feedback-consulta",
      "Error de conexión con el servidor.",
      "error",
    );
    renderTabla([]);
  } finally {
    btn.removeAttribute("aria-busy");
    btn.textContent = "Consultar";
  }
}

/* ── Render tabla ─────────────────────────────────────── */

function renderTabla(datos) {
  const tbody = document.getElementById("tbody-asistencia");
  const count = document.getElementById("tabla-count");

  count.textContent = `${datos.length} registro${datos.length !== 1 ? "s" : ""}`;

  if (!datos.length) {
    tbody.innerHTML = `
      <tr>
        <td colspan="4">
          <div class="tabla-empty">
            <span>🔍</span>
            No se encontraron registros para los filtros seleccionados.
          </div>
        </td>
      </tr>`;
    return;
  }

  // Recordatorio mas adelante: ajustar los campos según los nombres reales que devuelva la API
  tbody.innerHTML = datos
    .map((r) => {
      const nombre = r.personalNombre || r.nombre || `ID ${r.personalId}`;
      const depto = r.departamento || r.deptoNombre || "—";
      const fecha = r.fecha || "—";
      const estado = r.estado || "—";

      return `
      <tr>
        <td data-label="Nombre">${nombre}</td>
        <td data-label="Departamento">${depto}</td>
        <td data-label="Fecha">${fecha}</td>
        <td data-label="Estado">${badgeEstado(estado)}</td>
      </tr>`;
    })
    .join("");
}
