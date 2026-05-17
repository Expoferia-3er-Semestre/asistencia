/* Cache local para filtrar sin volver a llamar al backend */
let listaPersonal = [];

document.addEventListener("DOMContentLoaded", () => {
  verificarSesion(); // app.js — redirige si no hay token
  cargarPersonal();
  cargarDeptosEnModal();
});

/* ── Carga ────────────── */

async function cargarPersonal() {
  try {
    const res = await fetch(`${API_BASE}/personal`, { headers: getHeaders() }); // app.js
    if (!res.ok) throw new Error("Sin respuesta del servidor");

    listaPersonal = await res.json();
    renderTabla(listaPersonal);
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

  tbody.innerHTML = datos
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
  const filtrados = listaPersonal.filter((p) =>
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
    "form-cedula",
    "form-cargo",
  ].forEach((id) => (document.getElementById(id).value = ""));
  document.getElementById("form-depto").value = "";
  const err = document.getElementById("modal-error");
  err.className = "feedback-msg";
  err.textContent = "";
}

/* Precarga el formulario con datos del personal a editar */
function editarPersonal(id) {
  const p = listaPersonal.find((x) => x.id === id);
  if (!p) return;

  // Extrae cargo limpio — puede venir como objeto o string
  const cargoVal =
    typeof p.cargo === "object"
      ? p.cargo?.nombre || p.cargo?.descripcion || ""
      : p.cargo || "";

  // Extrae id de departamento para seleccionar en el select
  const deptoId =
    typeof p.departamento === "object"
      ? p.departamento?.id || ""
      : p.departamentoId || "";

  document.getElementById("form-id").value = p.id;
  document.getElementById("form-nombre").value = p.nombre || "";
  document.getElementById("form-apellido").value = p.apellido || "";
  document.getElementById("form-cedula").value = p.cedula || "";
  document.getElementById("form-cargo").value = cargoVal;
  document.getElementById("form-depto").value = deptoId;

  abrirModal("Editar personal");
}

/* ── Guardar (crear o editar) ─────────────────────────── */

async function guardarPersonal() {
  const id = document.getElementById("form-id").value;
  const nombre = document.getElementById("form-nombre").value.trim();
  const apellido = document.getElementById("form-apellido").value.trim();
  const cedula = document.getElementById("form-cedula").value.trim();
  const cargo = document.getElementById("form-cargo").value.trim();
  const deptoId = document.getElementById("form-depto").value;
  const btn = document.getElementById("btn-guardar");

  if (!nombre || !apellido || !cedula) {
    mostrarModalError("Nombre, apellido y cédula son obligatorios.");
    return;
  }

  const body = {
    nombre,
    apellido,
    cedula,
    cargo,
    departamentoId: deptoId || null,
  };
  const esEdicion = !!id;
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

/* ── Eliminar ─────────────────────────────────────────── */

async function togglePersonalStatus(checkbox, activo, id) {
  const confirmado = activo
    ? confirm("¿Seguro que deseas desactivar este empleado?")
    : confirm("¿Seguro que deseas reactivar este empleado?");
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
