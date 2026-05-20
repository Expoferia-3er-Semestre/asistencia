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
    // Migrado a Axios: la cookie/sesión viaja sola de forma automática
    const res = await api.get("/api/cargos");
    listaCargos = res.data;
    renderTabla(listaCargos);
  } catch (err) {
    console.error("Error cargando cargos con Axios:", err);
    renderTabla([]);
  }
}

async function cargarDeptosEnModal() {
  try {
    // Migrado a Axios para mantener consistencia con los demás módulos
    const res = await api.get("/api/departamentos");
    const deptos = res.data;
    const sel = document.getElementById("form-depto");
    if (!sel) return;

    sel.innerHTML = `<option value="">Seleccionar…</option>`;

    deptos.forEach((d) => {
      // Opcional: Solo mostrar departamentos que estén activos
      if (d.activo !== false) {
        const opt = document.createElement("option");
        opt.value = d.id;
        opt.textContent = d.nombre;
        sel.appendChild(opt);
      }
    });
  } catch (err) {
    console.error("Error cargando departamentos para cargos:", err);
  }
}

/* ── Render tabla ─────────────────────────────────────── */

function renderTabla(datos) {
  const tbody = document.getElementById("tbody-cargos");
  const count = document.getElementById("tabla-count");
  if (!tbody || !count) return;

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
    .map((c) => {
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
    })
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
  if (err) {
    err.className = "feedback-msg";
    err.textContent = "";
  }
}

function editarCargo(id) {
  const cargo = listaCargos.find((x) => x.id === id);
  if (!cargo) return;

  document.getElementById("form-id").value = cargo.id;
  document.getElementById("form-nombre").value = cargo.nombre || "";
  
  const deptoId =
    cargo.departamento && typeof cargo.departamento === "object"
      ? cargo.departamento.id || ""
      : cargo.departamentoId || "";

  document.getElementById("form-depto").value = deptoId;

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
  btn.setAttribute("aria-busy", "true");
  btn.textContent = "Guardando…";

  try {
    if (esEdicion) {
      // Migrado a Axios PUT
      await api.put(`/api/cargos/${id}`, body);
    } else {
      // Migrado a Axios POST
      await api.post("/api/cargos", body);
    }
    cerrarModal();
    cargarCargos();
  } catch (err) {
    const errorMsg = err.response?.data?.message || "No se pudo guardar el cargo.";
    mostrarModalError(errorMsg);
  } finally {
    btn.removeAttribute("aria-busy");
    btn.textContent = "Guardar";
  }
}

/* ── Eliminar ─────────────────────────────────────────── */

async function eliminarCargo(id) {
  if (!confirm("¿Seguro que deseas eliminar este cargo?")) return;

  try {
    // Migrado a Axios DELETE
    await api.delete(`/api/cargos/${id}`);
    cargarCargos();
  } catch (err) {
    console.error("Error eliminando cargo:", err);
    alert("No se pudo eliminar el cargo del servidor.");
  }
}

/* ── Helpers ──────────────────────────────────────────── */

function mostrarModalError(msg) {
  const el = document.getElementById("modal-error");
  if (el) {
    el.textContent = msg;
    el.className = "feedback-msg error";
  }
}