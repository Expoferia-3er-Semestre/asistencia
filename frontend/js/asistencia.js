document.addEventListener("DOMContentLoaded", () => {
  const token = localStorage.getItem("token");

  // Sin sesión → redirige al login
  if (!token) {
    window.location.href = "../index.html";
    return;
  }

  // Fecha de hoy como valor por defecto en todos los date inputs
  const hoy = new Date().toISOString().split("T")[0];
  document.getElementById("reg-fecha").value = hoy;
  document.getElementById("filtro-fecha-desde").value = hoy;
  document.getElementById("filtro-fecha-hasta").value = hoy;

  cargarPersonal();
  cargarDepartamentos();
});

/* ── Helpers ────────────────────────────────────────────── */

/* Cabecera de autenticación reutilizable */
function getHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${localStorage.getItem("token")}`,
  };
}

/* Muestra mensaje de éxito o error y lo oculta a los 4 s */
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
  // Primera letra mayúscula, resto minúscula
  const etiqueta = estado
    ? estado.charAt(0) + estado.slice(1).toLowerCase()
    : "—";
  return `<span class="badge ${clases[estado] || ""}">${etiqueta}</span>`;
}

/* ── Carga de selects ───────────────────────────────────── */

/* Llena el select de personal para el formulario de registro */
async function cargarPersonal() {
  try {
    const res = await fetch("http://localhost:8080/api/personal", {
      headers: getHeaders(),
    });
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
    const res = await fetch("http://localhost:8080/api/departamentos", {
      headers: getHeaders(),
    });
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

/* ── Registrar asistencia ───────────────────────────────── */

async function registrarAsistencia() {
  const personalId = document.getElementById("reg-personal").value;
  const fecha = document.getElementById("reg-fecha").value;
  const estado = document.getElementById("reg-estado").value;
  const btn = document.getElementById("btn-registrar");

  // Validación simple
  if (!personalId || !fecha || !estado) {
    mostrarFeedback(
      "feedback-registro",
      "Completa todos los campos antes de guardar.",
      "error",
    );
    return;
  }

  // Estado de carga
  btn.setAttribute("aria-busy", "true");
  btn.textContent = "Guardando…";

  try {
    const res = await fetch("http://localhost:8080/api/asistencia", {
      method: "POST",
      headers: getHeaders(),
      // Ajusta el body según lo que espere tu backend
      body: JSON.stringify({ personalId, fecha, estado }),
    });

    if (res.ok) {
      mostrarFeedback(
        "feedback-registro",
        "✅ Asistencia registrada correctamente.",
        "success",
      );
      consultarAsistencias(); // refresca la tabla automáticamente
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

/* ── Consultar asistencias ──────────────────────────────── */

async function consultarAsistencias() {
  const desde = document.getElementById("filtro-fecha-desde").value;
  const hasta = document.getElementById("filtro-fecha-hasta").value;
  const depto = document.getElementById("filtro-depto").value;
  const btn = document.getElementById("btn-consultar");

  // Construye query string solo con los filtros que tienen valor
  const params = new URLSearchParams();
  if (desde) params.append("fechaDesde", desde);
  if (hasta) params.append("fechaHasta", hasta);
  if (depto) params.append("departamentoId", depto);

  btn.setAttribute("aria-busy", "true");
  btn.textContent = "Consultando…";

  try {
    const res = await fetch(
      `http://localhost:8080/api/asistencia?${params.toString()}`,
      { headers: getHeaders() },
    );

    renderTabla(res.ok ? await res.json() : []);
  } catch (err) {
    console.error("Error consultando asistencias:", err);
    renderTabla([]);
  } finally {
    btn.removeAttribute("aria-busy");
    btn.textContent = "Consultar";
  }
}

/* ── Render de la tabla ─────────────────────────────────── */

function renderTabla(datos) {
  const tbody = document.getElementById("tbody-asistencia");
  const count = document.getElementById("tabla-count");

  count.textContent = `${datos.length} registro${datos.length !== 1 ? "s" : ""}`;

  // Sin resultados → mensaje vacío
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

  // Genera filas — ajusta los campos según tu API
  /// Para mas adelante acordarme de ajustar según los nombres reales que devuelva la API. Los campos p.nombre, p.apellido, r.personalNombre
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

/* ── Logout ─────────────────────────────────────────────── */

function logout() {
  localStorage.clear();
  window.location.href = "../index.html";
  /*"../index.html";*/
}
