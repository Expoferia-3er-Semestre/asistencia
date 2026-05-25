/* Cache local de departamentos */
let listaDeptos = [];

document.addEventListener("DOMContentLoaded", () => {
  verificarSesion(); // app.js — redirige si no hay token
  protegerModulo(["ROLE_ADMIN", "ROLE_ASISTENTE"]);
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
  const placeholder = `
      <div class="tabla-empty">
        <span>🏢</span>
        No hay departamentos registrados.
      </div>`;

  const departamentosOrdenados = datos
    .slice()
    .sort((a, b) =>
      a.nombre.localeCompare(b.nombre, "es", { sensitivity: "base" }),
    );

  if (!departamentosOrdenados.length) {
    grid.innerHTML = placeholder;
    return;
  }

  const html = departamentosOrdenados
    .map((d) => {
      const estadoClass = d.activo ? "" : "depto-card--inactive";
      return `
        <div class="depto-card ${estadoClass}" data-id="${d.id}" onclick="seleccionarDepto(${d.id})">
          <h3>${d.nombre}</h3>
          <p>${d.descripcion || "Sin descripción"}</p>
          <span class="depto-count">${d.totalPersonal ?? 0} persona${d.totalPersonal !== 1 ? "s" : ""}</span>
        </div>
      `;
    })
    .join("");

  grid.innerHTML = html;
}

/* ── Render tabla ─────────────────────────────────────── */

function renderTabla(datos) {
  const tbody = document.getElementById("tbody-deptos");
  const count = document.getElementById("tabla-count");

  count.textContent = `${datos.length} área${datos.length !== 1 ? "s" : ""}`;

  if (!datos.length) {
    tbody.innerHTML = `
      <tr>
        <td colspan="5">
          <div class="tabla-empty">
            <span>🏢</span>
            No hay áreas registradas.
          </div>
        </td>
      </tr>`;
    return;
  }

  const ordenados = [...datos].sort((a, b) => {
    if (a.activo === b.activo) {
      return a.nombre.localeCompare(b.nombre, "es", { sensitivity: "base" });
    }
    return a.activo ? -1 : 1;
  });

  tbody.innerHTML = ordenados
    .map((d) => {
      return `
    <tr data-id="${d.id}">
      <td data-label="Nombre">${d.nombre}</td>
      <td data-label="Descripción">${d.descripcion || "—"}</td>
      <td data-label="Personal">${d.totalPersonal ?? 0}</td>
      <td data-label="Acciones">
      <button class="btn-edit" onclick="editarDepto(${d.id})">Editar</button>
      </td>
      <td data-label="Estado">
        <div class="switch-cell">
          <label class="switch">
            <input type="checkbox" ${d.activo ? "checked" : ""} onchange="toggleDeptoStatus(this, ${d.activo}, ${d.id})" />
            <span class="slider"></span>
          </label>
        </div>
      </td>
    </tr>
  `;
    })
    .join("");
}

function seleccionarDepto(id) {
  const fila = document.querySelector(`#tbody-deptos tr[data-id="${id}"]`);
  if (!fila) return;

  fila.scrollIntoView({ behavior: "smooth", block: "center" });
  fila.classList.remove("depto-row--highlight");
  if (fila._highlightTimeout) {
    clearTimeout(fila._highlightTimeout);
  }

  void fila.offsetWidth;
  fila.classList.add("depto-row--highlight");
  fila._highlightTimeout = setTimeout(() => {
    fila.classList.remove("depto-row--highlight");
    fila._highlightTimeout = null;
  }, 5000);
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

/* ── Activar/Desactivar ────────────────────────────────── */

async function toggleDeptoStatus(checkbox, activo, id) {
  const confirmado = await showConfirmation(
    activo
      ? "¿Seguro que deseas desactivar este departamento?"
      : "¿Seguro que deseas activar este departamento?",
    {
      title: activo ? "Desactivar departamento" : "Activar departamento",
      confirmText: activo ? "Desactivar" : "Activar",
      cancelText: "Cancelar",
    },
  );
  if (!confirmado) {
    checkbox.checked = activo;
    return;
  }

  const action = activo ? "desactivar" : "activar";

  try {
    // Si usas tu cliente 'api' (Axios) configurado con cookies
    await api.patch(`/api/departamentos/${id}/${action}`);
    cargarDeptos();
  } catch (err) {
    const msg =
      err.response?.data?.message ||
      "No se pudo actualizar el estado del departamento.";
    alert(msg);
    checkbox.checked = activo;
  }
}

/* ── Helpers ──────────────────────────────────────────── */

function mostrarModalError(msg) {
  const el = document.getElementById("modal-error");
  el.textContent = msg;
  el.className = "feedback-msg error";
}
