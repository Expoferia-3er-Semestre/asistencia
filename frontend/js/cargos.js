/* Cache local de cargos */
let listaCargos = [];

document.addEventListener("DOMContentLoaded", () => {
  verificarSesion(); // app.js — redirige si no hay token
  cargarCargos();
  cargarDeptosEnModal();
});

/* ── Carga ────────────────────────────────────────────── */

async function cargarCargos() {
  try {
    const res = await fetch(`${API_BASE}/cargos`, {
      headers: getHeaders(),
    });
    if (!res.ok) throw new Error("Sin respuesta del servidor");

    listaCargos = await res.json();
    renderTabla(listaCargos);
  } catch (err) {
    console.error("Error cargando cargos:", err);
    renderTabla([]);
  }
}

async function cargarDeptosEnModal() {
  try {
    const res = await fetch(`${API_BASE}/departamentos`, {
      headers: getHeaders(),
    });
    if (!res.ok) throw new Error("Sin respuesta del servidor");

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
    console.error("Error cargando departamentos para cargos:", err);
  }
}

/* ── Render tabla ─────────────────────────────────────── */

function renderTabla(datos) {
  const tbody = document.getElementById("tbody-cargos");
  const count = document.getElementById("tabla-count");

  count.textContent = `${datos.length} cargo${datos.length !== 1 ? "s" : ""}`;

  if (!datos.length) {
    tbody.innerHTML = `
      <tr>
        <td colspan="3">
          <div class="tabla-empty">
            <span>🧾</span>
            No hay cargos registrados.
          </div>
        </td>
      </tr>`;
    return;
  }

  tbody.innerHTML = datos
    .map(
      (c) => {
        const departamento =
          c.departamento && typeof c.departamento === "object"
            ? c.departamento.nombre || "—"
            : c.departamento || "—";

        return `
      <tr>
        <td data-label="Cargo">${c.nombre || "—"}</td>
        <td data-label="Departamento">${departamento}</td>
        <td data-label="Acciones">
          <button class="btn-edit" onclick="editarCargo(${c.id})">Editar</button>
          <button class="btn-delete" onclick="eliminarCargo(${c.id})">Eliminar</button>
        </td>
      </tr>`;
      },
    )
    .join("");
}

/* ── Modal ────────────────────────────────────────────── */

function abrirModal(titulo = "Agregar cargo") {
  document.getElementById("modal-titulo").textContent = titulo;
  document.getElementById("modal-overlay").classList.add("active");
  document.getElementById("modal-cargo").classList.add("active");
}

function cerrarModal() {
  document.getElementById("modal-overlay").classList.remove("active");
  document.getElementById("modal-cargo").classList.remove("active");
  limpiarFormulario();
}

function limpiarFormulario() {
  document.getElementById("form-id").value = "";
  document.getElementById("form-nombre").value = "";
  document.getElementById("form-depto").value = "";
  const err = document.getElementById("modal-error");
  err.className = "feedback-msg";
  err.textContent = "";
}

function editarCargo(id) {
  const cargo = listaCargos.find((x) => x.id === id);
  if (!cargo) return;

  document.getElementById("form-id").value = cargo.id;
  document.getElementById("form-nombre").value = cargo.nombre || "";
  document.getElementById("form-depto").value =
    cargo.departamento && typeof cargo.departamento === "object"
      ? cargo.departamento.id || ""
      : cargo.departamentoId || "";

  abrirModal("Editar cargo");
}

/* ── Guardar (crear o editar) ─────────────────────────── */

async function guardarCargo() {
  const id = document.getElementById("form-id").value;
  const nombre = document.getElementById("form-nombre").value.trim();
  const departamentoId = document.getElementById("form-depto").value;
  const btn = document.getElementById("btn-guardar");

  if (!nombre) {
    mostrarModalError("El nombre del cargo es obligatorio.");
    return;
  }

  if (!departamentoId) {
    mostrarModalError("Selecciona un departamento para el cargo.");
    return;
  }

  const body = {
    nombre,
    departamentoId: Number(departamentoId),
  };
  const esEdicion = !!id;
  const url = esEdicion ? `${API_BASE}/cargos/${id}` : `${API_BASE}/cargos`;
  const method = esEdicion ? "PUT" : "POST";

  btn.setAttribute("aria-busy", "true");
  btn.textContent = "Guardando…";

  try {
    const res = await fetch(url, {
      method,
      headers: getHeaders(),
      body: JSON.stringify(body),
    });

    if (res.ok) {
      cerrarModal();
      cargarCargos();
    } else {
      const err = await res.json().catch(() => ({}));
      mostrarModalError(err.message || "No se pudo guardar el cargo.");
    }
  } catch {
    mostrarModalError("Error de conexión con el servidor.");
  } finally {
    btn.removeAttribute("aria-busy");
    btn.textContent = "Guardar";
  }
}

/* ── Eliminar ─────────────────────────────────────────── */

async function eliminarCargo(id) {
  if (!confirm("¿Seguro que deseas eliminar este cargo?")) return;

  try {
    const res = await fetch(`${API_BASE}/cargos/${id}`, {
      method: "DELETE",
      headers: getHeaders(),
    });

    if (res.ok) {
      cargarCargos();
    } else {
      alert("No se pudo eliminar el cargo.");
    }
  } catch {
    alert("Error de conexión con el servidor.");
  }
}

/* ── Helpers ──────────────────────────────────────────── */

function mostrarModalError(msg) {
  const el = document.getElementById("modal-error");
  el.textContent = msg;
  el.className = "feedback-msg error";
}
