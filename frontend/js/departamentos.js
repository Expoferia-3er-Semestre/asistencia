/* Cache local de departamentos */
let listaDeptos = [];

document.addEventListener("DOMContentLoaded", () => {
  verificarSesion(); // app.js — redirige si no hay token
  cargarDeptos();
});

/* ── Carga ────────────────────────────────────────────── */

async function cargarDeptos() {
  try {
    const res = await api.get("/api/departamentos"); // cookie viaja sola
    listaDeptos = res.data;

    renderCards(listaDeptos);
    renderTabla(listaDeptos);
  } catch (err) {
    console.error("Error cargando departamentos:", err);
    renderTabla([]);
  }
}

/* ── Render tarjetas resumen ──────────────────────────── */

function renderCards(datos) {
  const grid = document.getElementById("deptos-grid");

  if (!datos.length) {
    grid.innerHTML = `
      <div class="tabla-empty">
        <span>🏢</span>
        No hay departamentos registrados.
      </div>`;
    return;
  }

  grid.innerHTML = datos
    .map(
      (d) => `
    <div class="depto-card">
      <h3>${d.nombre}</h3>
      <p>${d.descripcion || "Sin descripción"}</p>
      <span class="depto-count">${d.totalPersonal ?? 0} persona${d.totalPersonal !== 1 ? "s" : ""}</span>
    </div>
  `,
    )
    .join("");
}

/* ── Render tabla ─────────────────────────────────────── */

function renderTabla(datos) {
  const tbody = document.getElementById("tbody-deptos");
  const count = document.getElementById("tabla-count");

  count.textContent = `${datos.length} área${datos.length !== 1 ? "s" : ""}`;

  if (!datos.length) {
    tbody.innerHTML = `
      <tr>
        <td colspan="4">
          <div class="tabla-empty">
            <span>🏢</span>
            No hay áreas registradas.
          </div>
        </td>
      </tr>`;
    return;
  }

  tbody.innerHTML = datos
    .map(
      (d) => `
    <tr>
      <td data-label="Nombre">${d.nombre}</td>
      <td data-label="Descripción">${d.descripcion || "—"}</td>
      <td data-label="Personal">${d.totalPersonal ?? 0}</td>
      <td data-label="Acciones">
        <button class="btn-edit"   onclick="editarDepto(${d.id})">Editar</button>
        <button class="btn-delete" onclick="eliminarDepto(${d.id})">Eliminar</button>
      </td>
    </tr>
  `,
    )
    .join("");
}

/* ── Modal ────────────────────────────────────────────── */

function abrirModal(titulo = "Agregar departamento") {
  document.getElementById("modal-titulo").textContent = titulo;
  document.getElementById("modal-overlay").classList.add("active");
  document.getElementById("modal-depto").classList.add("active");
}

function cerrarModal() {
  document.getElementById("modal-overlay").classList.remove("active");
  document.getElementById("modal-depto").classList.remove("active");
  limpiarFormulario();
}

function limpiarFormulario() {
  document.getElementById("form-id").value = "";
  document.getElementById("form-nombre").value = "";
  document.getElementById("form-descripcion").value = "";
  const err = document.getElementById("modal-error");
  err.className = "feedback-msg";
  err.textContent = "";
}

/* Precarga el formulario con los datos del departamento a editar */
function editarDepto(id) {
  const d = listaDeptos.find((x) => x.id === id);
  if (!d) return;

  document.getElementById("form-id").value = d.id;
  document.getElementById("form-nombre").value = d.nombre || "";
  document.getElementById("form-descripcion").value = d.descripcion || "";

  abrirModal("Editar departamento");
}

/* ── Guardar (crear o editar) ─────────────────────────── */

async function guardarDepto() {
  const id = document.getElementById("form-id").value;
  const nombre = document.getElementById("form-nombre").value.trim();
  const descripcion = document.getElementById("form-descripcion").value.trim();
  const btn = document.getElementById("btn-guardar");

  if (!nombre) {
    mostrarModalError("El nombre del área es obligatorio.");
    return;
  }

  const body = { nombre, descripcion };
  const esEdicion = !!id;

  btn.setAttribute("aria-busy", "true");
  btn.textContent = "Guardando…";

  try {
    /* cookie viaja sola — sin headers manuales */
    if (esEdicion) {
      await api.put(`/api/departamentos/${id}`, body);
    } else {
      await api.post("/api/departamentos", body);
    }
    cerrarModal();
    cargarDeptos();
  } catch (err) {
    const msg = err.response?.data?.message || "No se pudo guardar el área.";
    mostrarModalError(msg);
  } finally {
    btn.removeAttribute("aria-busy");
    btn.textContent = "Guardar";
  }
}

/* ── Eliminar ─────────────────────────────────────────── */

async function eliminarDepto(id) {
  if (!confirm("¿Seguro que deseas eliminar esta área?")) return;

  try {
    await api.delete(`/api/departamentos/${id}`); // cookie viaja sola
    cargarDeptos();
  } catch {
    alert("No se pudo eliminar el área.");
  }
}

/* ── Helpers ──────────────────────────────────────────── */

function mostrarModalError(msg) {
  const el = document.getElementById("modal-error");
  el.textContent = msg;
  el.className = "feedback-msg error";
}
