/* Cache local para filtrar sin volver a llamar al backend */
let listaPersonal = [];
let listaCargos = [];

function getPersonalVisibles() {
  const mostrarInactivos =
    document.getElementById("mostrar-inactivos")?.checked;
  return listaPersonal.filter((p) => mostrarInactivos || p.activo);
}

document.addEventListener("DOMContentLoaded", () => {
  verificarSesion(); // app.js — redirige si no hay token
  cargarPersonal();
  cargarDeptosEnModal();
  cargarCargosEnModal();

  const deptoSel = document.getElementById("form-depto");
  if (deptoSel) {
    deptoSel.addEventListener("change", (e) =>
      filtrarCargosPorDepto(e.target.value),
    );
  }
});

/* ── Carga ────────────── */

async function cargarPersonal() {
  try {
    const res = await api.get("/api/personal"); // Usando Axios ('api.js')
    listaPersonal = res.data;
    renderTabla(getPersonalVisibles());
  } catch (err) {
    console.error("Error cargando personal:", err);
    renderTabla([]);
  }
}

/* Llena el select de departamentos dentro del modal */
async function cargarDeptosEnModal() {
  try {
    const res = await api.get("/api/departamentos"); // Unificado a Axios
    const sel = document.getElementById("form-depto");
    if (!sel) return;

    sel.innerHTML = `<option value="">Seleccionar…</option>`;

    res.data.forEach((d) => {
      // Opcional: Solo mostrar departamentos activos al registrar nuevo personal
      if (d.activo) {
        const opt = document.createElement("option");
        opt.value = d.id;
        opt.textContent = d.nombre;
        sel.appendChild(opt);
      }
    });
  } catch (err) {
    console.error("Error cargando departamentos en modal:", err);
  }
}

/* Llena el select de cargos dentro del modal */
async function cargarCargosEnModal() {
  try {
    // Si tienes una ruta en Axios para cargos, úsala. Suponiendo getHeaders() para Fetch antiguo:
    const res = await fetch(`${API_BASE}/cargos`, { headers: getHeaders() });
    if (!res.ok) return;

    const cargos = await res.json();
    listaCargos = cargos;

    const sel = document.getElementById("form-cargo");
    if (!sel) return;

    const previous = sel.value;
    filtrarCargosPorDepto(document.getElementById("form-depto").value || "");
    if (previous) sel.value = previous;
  } catch (err) {
    console.error("Error cargando cargos:", err);
  }
}

/**
 * Llena el select de `form-cargo` filtrando por departamento
 * @param {string|number} deptoId
 */
function filtrarCargosPorDepto(deptoId) {
  const sel = document.getElementById("form-cargo");
  if (!sel) return;
  sel.innerHTML = `<option value="">Seleccionar...</option>`;

  if (!deptoId) {
    sel.disabled = true;
    return;
  }

  sel.disabled = false;
  const idNum = Number(deptoId);

  listaCargos.forEach((c) => {
    let cargoDeptoId = null;
    if (c.departamento && typeof c.departamento === "object") {
      cargoDeptoId = c.departamento.id;
    } else if (typeof c.departamentoId !== "undefined") {
      cargoDeptoId = c.departamentoId;
    } else if (typeof c.departamento !== "undefined") {
      cargoDeptoId = c.departamento;
    }

    if (Number(cargoDeptoId) === idNum) {
      const opt = document.createElement("option");
      opt.value = c.id;
      opt.textContent = c.nombre || c.descripcion || "—";
      sel.appendChild(opt);
    }
  });
}

/* ── Render tabla ─────────── */

function renderTabla(datos) {
  const tbody = document.getElementById("tbody-personal");
  const count = document.getElementById("tabla-count");
  if (!tbody || !count) return;

  count.textContent = `${datos.length} registro${datos.length !== 1 ? "s" : ""}`;

  if (!datos.length) {
    tbody.innerHTML = `
      <tr>
        <td colspan="6">
          <div class="tabla-empty">
            <span>👥</span>
            No hay personal registrado.
          </div>
        </td>
      </tr>`;
    return;
  }

  // Ordenar: Activos primero, luego alfabéticamente por Nombre + Apellido
  const ordenados = [...datos].sort((a, b) => {
    if (a.activo === b.activo) {
      const nombreA = `${a.nombre || ""} ${a.apellido || ""}`
        .trim()
        .toLowerCase();
      const nombreB = `${b.nombre || ""} ${b.apellido || ""}`
        .trim()
        .toLowerCase();
      return nombreA.localeCompare(nombreB, "es", { sensitivity: "base" });
    }
    return a.activo ? -1 : 1;
  });

  tbody.innerHTML = ordenados
    .map((p) => {
      const cargo =
        typeof p.cargo === "object"
          ? p.cargo?.nombre || p.cargo?.descripcion || "—"
          : p.cargo || "—";

      const depto =
        typeof p.departamento === "object"
          ? p.departamento?.nombre || "—"
          : p.departamentoNombre || p.departamento || "—";

      return `
      <tr>
        <td data-label="Nombre">${p.nombre || ""} ${p.apellido || ""}</td>
        <td data-label="Cédula">${p.cedula || "—"}</td>
        <td data-label="Cargo">${cargo}</td>
        <td data-label="Departamento">${depto}</td>
        <td data-label="Acciones">
<button class="btn-edit" onclick="editarPersonal(${p.id})">Editar</button>

        <td data-label="Estado">
          <div class="switch-cell">
            <label class="switch">
              <input type="checkbox" ${p.activo ? "checked" : ""} onchange="togglePersonalStatus(this, ${p.activo}, ${p.id})" />
              <span class="slider"></span>
            </label>
          </div>
        </td>
        <td data-label="QR" style="text-align:center;">
  <button class="btn-qr" onclick="verQR(${p.id}, '${p.nombre || ""} ${p.apellido || ""}')">Ver QR</button>
</td>
      </tr>`;
    })
    .join("");
}

/* ── Buscador local ───────────────────────────────────── */

function filtrarTabla() {
  const q = document.getElementById("buscador").value.toLowerCase();
  const filtrados = getPersonalVisibles().filter((p) =>
    `${p.nombre || ""} ${p.apellido || ""} ${p.cedula || ""} ${p.departamentoNombre || ""}`
      .toLowerCase()
      .includes(q),
  );
  renderTabla(filtrados);
}

/* ── Modal ────────────────────────────────────────────── */

function abrirModal(titulo = "Agregar personal") {
  document.getElementById("modal-titulo").textContent = titulo;
  document.getElementById("modal-overlay").classList.add("active");
  document.getElementById("modal-personal").classList.add("active");
}

function cerrarModal() {
  document.getElementById("modal-overlay").classList.remove("active");
  document.getElementById("modal-personal").classList.remove("active");
  limpiarFormulario();
}

function limpiarFormulario() {
  [
    "form-id",
    "form-nombre",
    "form-apellido",
    "form-telefono",
    "form-correo",
    "form-cedula",
    "form-cargo",
  ].forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.value = "";
  });

  const deptoEl = document.getElementById("form-depto");
  if (deptoEl) deptoEl.value = "";

  filtrarCargosPorDepto("");
  const err = document.getElementById("modal-error");
  if (err) {
    err.className = "feedback-msg";
    err.textContent = "";
  }
}

function editarPersonal(id) {
  const p = listaPersonal.find((x) => x.id === id);
  if (!p) return;

  const cargoId =
    typeof p.cargo === "object" ? p.cargo?.id || "" : p.cargoId || "";
  const deptoId =
    typeof p.departamento === "object"
      ? p.departamento?.id || ""
      : p.departamentoId || "";

  document.getElementById("form-id").value = p.id;
  document.getElementById("form-nombre").value = p.nombre || "";
  document.getElementById("form-apellido").value = p.apellido || "";
  document.getElementById("form-telefono").value = p.telefono || "";
  document.getElementById("form-correo").value = p.correo || "";
  document.getElementById("form-cedula").value = p.cedula || "";
  document.getElementById("form-depto").value = deptoId;

  filtrarCargosPorDepto(deptoId);
  document.getElementById("form-cargo").value = cargoId;

  abrirModal("Editar personal");
}

/* ── Guardar (crear o editar) ─────────────────────────── */

async function guardarPersonal() {
  const id = document.getElementById("form-id").value;
  const nombre = document.getElementById("form-nombre").value.trim();
  const apellido = document.getElementById("form-apellido").value.trim();
  const telefono = document.getElementById("form-telefono").value.trim();
  const correo = document.getElementById("form-correo").value.trim();
  const cedula = document.getElementById("form-cedula").value.trim();
  const cargoId = document.getElementById("form-cargo").value;
  const cargoName = cargoId
    ? document.getElementById("form-cargo").selectedOptions?.[0]?.textContent ||
      null
    : null;
  const deptoId = document.getElementById("form-depto").value;
  const btn = document.getElementById("btn-guardar");

  if (!nombre || !apellido || !cedula) {
    mostrarModalError("Nombre, apellido y cédula son obligatorios.");
    return;
  }

  const esEdicion = !!id;
  const existing = esEdicion
    ? listaPersonal.find((x) => String(x.id) === String(id))
    : null;

  const body = {
    nombre,
    apellido,
    cedula,
    telefono,
    correo,
    cargo: cargoName,
    cargoId: cargoId ? Number(cargoId) : null,
    departamentoId: deptoId ? Number(deptoId) : null,
    fechaIngreso:
      esEdicion && existing?.fechaIngreso
        ? existing.fechaIngreso
        : new Date().toISOString().split("T")[0],
  };

  if (esEdicion && existing && typeof existing.activo !== "undefined") {
    body.activo = existing.activo;
  } else if (!esEdicion) {
    body.activo = true;
  }

  btn.setAttribute("aria-busy", "true");
  btn.textContent = "Guardando…";

  try {
    if (esEdicion) {
      await api.put(`/api/personal/${id}`, body);
    } else {
      await api.post("/api/personal", body);
    }
    cerrarModal();
    cargarPersonal();
  } catch (err) {
    mostrarModalError("Error de conexión con el servidor.");
  } finally {
    btn.removeAttribute("aria-busy");
    btn.textContent = "Guardar";
  }
}

/* ── Activar/Desactivar ─────────────────────────────────────────── */

async function togglePersonalStatus(checkbox, activo, id) {
  const confirmado = await showConfirmation(
    activo
      ? "¿Seguro que deseas desactivar este empleado?"
      : "¿Seguro que deseas reactivar este empleado?",
    {
      title: activo ? "Desactivar empleado" : "Reactivar empleado",
      confirmText: activo ? "Desactivar" : "Activar",
      cancelText: "Cancelar",
    },
  );
  if (!confirmado) {
    checkbox.checked = activo;
    return;
  }

  const action = activo ? "desactivar" : "activar"; // Asegúrate de si tu backend usa endpoints semánticos

  try {
    // Intentamos manejarlo con la instancia Axios para consistencia de sesión/cookies
    if (activo) {
      await api.delete(`/api/personal/${id}`);
    } else {
      await api.patch(`/api/personal/${id}/reactivar`);
    }
    cargarPersonal();
  } catch (err) {
    alert("No se pudo actualizar el estado del registro.");
    checkbox.checked = activo;
  }
}

async function eliminarPersonal(id) {
  return togglePersonalStatus(id, true);
}

/* ── Modal QR ─────────────────────────────────────────────── */

/* Abre el modal QR, obtiene el token del backend y dibuja el QR */
async function verQR(id, nombre) {
  /* Limpiamos el canvas anterior y mostramos el modal */
  document.getElementById("qr-canvas").innerHTML = "";
  document.getElementById("qr-nombre-empleado").textContent =
    nombre.trim() || "Empleado";
  document.getElementById("qr-status").textContent = "Generando QR…";

  /* Activamos el modal */
  document.getElementById("modal-qr-overlay").classList.add("active");
  document.getElementById("modal-qr").classList.add("active");

  try {
    /* Solicitamos el token QR al backend */
    const res = await api.get(`/api/personal/${id}/qr-token`);
    const token = res.data?.token || res.data;

    if (!token) {
      document.getElementById("qr-status").textContent =
        "No se pudo obtener el token QR.";
      return;
    }

    /* Limpiamos el mensaje y dibujamos el QR con el token JWT */
    document.getElementById("qr-status").textContent = "";
    new QRCode(document.getElementById("qr-canvas"), {
      text: token, // el contenido del QR es el token JWT completo
      width: 220,
      height: 220,
      colorDark: "#2f5a8a", // azul institucional
      colorLight: "#ffffff",
      correctLevel: QRCode.CorrectLevel.M,
    });

    /* Ocultamos el texto del token que qrcode.js agrega como atributo title */
    setTimeout(() => {
      const canvas = document.getElementById("qr-canvas");
      canvas.removeAttribute("title");
    }, 200);
  } catch (err) {
    console.error("Error obteniendo QR:", err);
    document.getElementById("qr-status").textContent =
      "Error al generar el QR. Intenta de nuevo.";
  }
}

/* Cierra el modal QR y limpia el canvas */
function cerrarModalQR() {
  document.getElementById("modal-qr-overlay").classList.remove("active");
  document.getElementById("modal-qr").classList.remove("active");
  document.getElementById("qr-canvas").innerHTML = "";
  document.getElementById("qr-status").textContent = "";
}

/* ── Helpers ──────────────────── */

function mostrarModalError(msg) {
  const el = document.getElementById("modal-error");
  if (el) {
    el.textContent = msg;
    el.className = "feedback-msg error";
  }
}
