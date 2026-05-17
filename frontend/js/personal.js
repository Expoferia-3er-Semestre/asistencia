/* Cache local para filtrar sin volver a llamar al backend */
let listaPersonal = [];
let listaCargos = [];

function getPersonalVisibles() {
  const mostrarInactivos = document.getElementById("mostrar-inactivos")?.checked;
  return listaPersonal.filter((p) => mostrarInactivos || p.activo);
}

document.addEventListener("DOMContentLoaded", () => {
  verificarSesion(); // app.js — redirige si no hay token
  cargarPersonal();
  cargarDeptosEnModal();
  cargarCargosEnModal();
  // Al cambiar departamento en el modal, filtrar cargos disponibles
  const deptoSel = document.getElementById("form-depto");
  if (deptoSel) {
    deptoSel.addEventListener("change", (e) => filtrarCargosPorDepto(e.target.value));
  }
});

/* ── Carga ────────────── */

async function cargarPersonal() {
  try {
    const res = await fetch(`${API_BASE}/personal`, { headers: getHeaders() }); // app.js
    if (!res.ok) throw new Error("Sin respuesta del servidor");

    listaPersonal = await res.json();
    renderTabla(getPersonalVisibles());
  } catch (err) {
    console.error("Error cargando personal:", err);
    renderTabla([]);
  }
}

/* Llena el select de departamentos dentro del modal */
async function cargarDeptosEnModal() {
  try {
    const res = await fetch(`${API_BASE}/departamentos/activos`, {
      headers: getHeaders(),
    }); // app.js
    if (!res.ok) return;

    const deptos = await res.json();
    const sel = document.getElementById("form-depto");
    sel.innerHTML = `<option value="">Seleccionar…</option>`;

    deptos.forEach((d) => {
      const opt = document.createElement("option");
      opt.value = d.id;
      opt.textContent = d.nombre;
      sel.appendChild(opt);
    });
  } catch (err) {
    console.error("Error cargando departamentos:", err);
  }
}

  /* Llena el select de cargos dentro del modal */
  async function cargarCargosEnModal() {
    try {
      const res = await fetch(`${API_BASE}/cargos`, { headers: getHeaders() });
      if (!res.ok) return;

      const cargos = await res.json();
      listaCargos = cargos;
      const sel = document.getElementById("form-cargo");
      const previous = sel.value;
      // Populate according to currently selected departamento (if any)
      filtrarCargosPorDepto(document.getElementById("form-depto").value || "");
      if (previous) sel.value = previous;
    } catch (err) {
      console.error("Error cargando cargos:", err);
    }
  }

  /**
   * Llena el select de `form-cargo` filtrando por departamento (si se pasa)
   * @param {string|number} deptoId
   */
  function filtrarCargosPorDepto(deptoId) {
    const sel = document.getElementById("form-cargo");
    if (!sel) return;
    sel.innerHTML = `<option value="">Seleccionar...</option>`;

    if (!deptoId){
      sel.disabled = true;
      return;
    }

    sel.disabled = false;

    const idNum = deptoId ? Number(deptoId) : null;
    listaCargos.forEach((c) => {
      // Determinar departamentoId del cargo (puede venir como objeto o campo directo)
      let cargoDeptoId = null;
      if (c.departamento && typeof c.departamento === "object") {
        cargoDeptoId = c.departamento.id;
      } else if (typeof c.departamentoId !== "undefined") {
        cargoDeptoId = c.departamentoId;
      } else if (typeof c.departamento !== "undefined") {
        cargoDeptoId = c.departamento;
      }

      if (!idNum || Number(cargoDeptoId) === idNum) {
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

  const ordenados = [...datos].sort((a, b) => {
    if (a.activo === b.activo) {
      const nombreA = `${a.nombre || ""} ${a.apellido || ""}`.trim().toLowerCase();
      const nombreB = `${b.nombre || ""} ${b.apellido || ""}`.trim().toLowerCase();
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
        </td>
        <td data-label="Estado">
          <div class="switch-cell">
            <label class="switch">
              <input type="checkbox" ${p.activo ? "checked" : ""} onchange="togglePersonalStatus(this, ${p.activo}, ${p.id})" />
              <span class="slider"></span>
            </label>
          </div>
        </td>
      </tr>`;
    })
    .join("");
}
/* ── Buscador local ───────────────────────────────────── */

function filtrarTabla() {
  const q = document.getElementById("buscador").value.toLowerCase();
  const filtrados = getPersonalVisibles().filter((p) =>
    `${p.nombre} ${p.apellido} ${p.cedula} ${p.departamentoNombre || ""}`
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
  ].forEach((id) => (document.getElementById(id).value = ""));
  document.getElementById("form-depto").value = "";
  // Restaurar listado completo o vacío de cargos
  filtrarCargosPorDepto("");
  const err = document.getElementById("modal-error");
  err.className = "feedback-msg";
  err.textContent = "";
}

/* Precarga el formulario con datos del personal a editar */
function editarPersonal(id) {
  const p = listaPersonal.find((x) => x.id === id);
  if (!p) return;

  // Extrae cargo limpio — puede venir como objeto o string
  const cargoId =
    typeof p.cargo === "object" ? p.cargo?.id || "" : p.cargoId || "";

  // Extrae id de departamento para seleccionar en el select
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
  // Filtrar cargos según el departamento y luego seleccionar el cargo actual
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
    ? document.getElementById("form-cargo").selectedOptions?.[0]?.textContent || null
    : null;
  const deptoId = document.getElementById("form-depto").value;
  const btn = document.getElementById("btn-guardar");

  if (!nombre || !apellido || !cedula) {
    mostrarModalError("Nombre, apellido y cédula son obligatorios.");
    return;
  }

  const esEdicion = !!id;

  const existing = esEdicion ? listaPersonal.find((x) => String(x.id) === String(id)) : null;

  const body = {
    nombre,
    apellido,
    cedula,
    telefono,
    correo,
    cargo: cargoName,
    cargoId: cargoId ? Number(cargoId) : null,
    departamentoId: deptoId ? Number(deptoId) : null,
    fechaIngreso: (esEdicion && existing?.fechaIngreso) 
    ? existing.fechaIngreso 
    : new Date().toISOString().split('T')[0],
  };
  // Si es edición, preservar el estado 'activo' del registro existente;
  // si es creación, establecer activo = true por defecto
  if (esEdicion) {
    const existing = listaPersonal.find((x) => String(x.id) === String(id));
    if (existing && typeof existing.activo !== "undefined") {
      body.activo = existing.activo;
    }
  } else {
    body.activo = true;
  }
  const url = esEdicion ? `${API_BASE}/personal/${id}` : `${API_BASE}/personal`;
  const method = esEdicion ? "PUT" : "POST";

  btn.setAttribute("aria-busy", "true");
  btn.textContent = "Guardando…";

  try {
    const res = await fetch(url, {
      method,
      headers: getHeaders(), // app.js
      body: JSON.stringify(body),
    });

    if (res.ok) {
      cerrarModal();
      cargarPersonal(); // refresca la tabla
    } else {
      const err = await res.json().catch(() => ({}));
      mostrarModalError(err.message || "No se pudo guardar el registro.");
    }
  } catch {
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

  const url = activo
    ? `${API_BASE}/personal/${id}`
    : `${API_BASE}/personal/${id}/reactivar`;
  const method = activo ? "DELETE" : "PATCH";

  try {
    const res = await fetch(url, {
      method,
      headers: getHeaders(), // app.js
    });

    if (res.ok) {
      cargarPersonal();
    } else {
      const err = await res.json().catch(() => ({}));
      alert(err.message || "No se pudo actualizar el estado.");
      checkbox.checked = activo;
    }
  } catch {
    alert("Error de conexión con el servidor.");
    checkbox.checked = activo;
  }
}

async function eliminarPersonal(id) {
  return togglePersonalStatus(id, true);
}

/* ── Helpers ──────────────────── */

function mostrarModalError(msg) {
  const el = document.getElementById("modal-error");
  el.textContent = msg;
  el.className = "feedback-msg error";
}
