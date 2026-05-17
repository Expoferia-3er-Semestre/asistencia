/* ── app.js ───Funciones globales compartidas por todas las páginas.
Siempre se carga ANTES que el JS específico de cada página. ───── */

const API_BASE = "http://localhost:8080/api";

/* Cabecera JSON + token para todas las peticiones protegidas */
function getHeaders() {
  return {
    "Content-Type": "application/json",
    Authorization: `Bearer ${localStorage.getItem("token")}`,
  };
}

/* Redirige al login si no hay token */
function verificarSesion() {
  if (!localStorage.getItem("token")) {
    window.location.href = "../index.html";
  }
}

/* Cierra sesión y limpia localStorage */
function logout() {
  localStorage.clear();
  window.location.href = "../index.html";
}

/* Nombre limpio del usuario logueado */
function getNombre() {
  const n =
    localStorage.getItem("nombre") || localStorage.getItem("username") || "";
  return n !== "undefined" ? n : "";
}

function createConfirmModal() {
  if (document.getElementById("confirm-overlay")) return;

  const overlay = document.createElement("div");
  overlay.className = "modal-overlay";
  overlay.id = "confirm-overlay";

  const modal = document.createElement("div");
  modal.className = "modal";
  modal.id = "confirm-modal";
  modal.setAttribute("role", "dialog");
  modal.setAttribute("aria-modal", "true");
  modal.setAttribute("aria-labelledby", "confirm-titulo");
  modal.innerHTML = `
    <div class="modal-header">
      <h3 id="confirm-titulo" class="confirm-title">Confirmar acción</h3>
      <button class="modal-close" type="button" aria-label="Cerrar" id="confirm-close">✕</button>
    </div>
    <div class="modal-body">
      <p class="confirm-message">¿Estás seguro?</p>
    </div>
    <div class="modal-footer">
      <button class="btn-secondary" type="button" id="confirm-cancel">Cancelar</button>
      <button class="btn-primary" type="button" id="confirm-ok">Confirmar</button>
    </div>`;

  overlay.appendChild(modal);
  modal.addEventListener("click", (event) => event.stopPropagation());
  document.body.appendChild(overlay);
}

function showConfirmation(message, options = {}) {
  createConfirmModal();

  const { title = "Confirmar acción", confirmText = "Confirmar", cancelText = "Cancelar" } = options;
  const overlay = document.getElementById("confirm-overlay");
  const modal = document.getElementById("confirm-modal");
  const titleEl = modal.querySelector(".confirm-title");
  const messageEl = modal.querySelector(".confirm-message");
  const okButton = document.getElementById("confirm-ok");
  const cancelButton = document.getElementById("confirm-cancel");
  const closeButton = document.getElementById("confirm-close");

  titleEl.textContent = title;
  messageEl.textContent = message;
  okButton.textContent = confirmText;
  cancelButton.textContent = cancelText;

  overlay.classList.add("active");
  modal.classList.add("active");

  return new Promise((resolve) => {
    function cleanup(result) {
      overlay.classList.remove("active");
      modal.classList.remove("active");
      okButton.removeEventListener("click", onConfirm);
      cancelButton.removeEventListener("click", onCancel);
      closeButton.removeEventListener("click", onCancel);
      overlay.removeEventListener("click", onCancel);
      document.removeEventListener("keydown", onKeyDown);
      resolve(result);
    }

    function onConfirm() {
      cleanup(true);
    }

    function onCancel() {
      cleanup(false);
    }

    function onKeyDown(event) {
      if (event.key === "Escape") {
        event.preventDefault();
        cleanup(false);
      }
    }

    okButton.addEventListener("click", onConfirm);
    cancelButton.addEventListener("click", onCancel);
    closeButton.addEventListener("click", onCancel);
    overlay.addEventListener("click", onCancel);
    document.addEventListener("keydown", onKeyDown);
  });
}
