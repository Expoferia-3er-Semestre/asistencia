/* ══════════════════════════════════════════════════════
   escaner.js
   Lógica del escáner QR para registrar asistencia.
   Usa html5-qrcode para acceder a la cámara y leer QRs.
   Al detectar un QR hace POST a /api/eventos-acceso con
   el token y muestra feedback visual del resultado.
   ══════════════════════════════════════════════════════ */

/* Variable global del escáner para controlarlo */
let html5QrCode = null;

/* Evita procesar dos escaneos al mismo tiempo */
let procesando = false;

document.addEventListener("DOMContentLoaded", () => {
  /* Verificamos sesión antes de activar la cámara */
  verificarSesion();
  iniciarEscaner();
});

/* ── Iniciar cámara ────────────────────────────────── */

function iniciarEscaner() {
  html5QrCode = new Html5Qrcode("qr-reader");

  const config = {
    fps: 10 /* fotogramas por segundo que analiza */,
    qrbox: { width: 250, height: 250 } /* área de escaneo */,
    aspectRatio: 1.0,
  };

  html5QrCode
    .start(
      { facingMode: "environment" } /* cámara trasera del dispositivo */,
      config,
      onQRDetectado /* callback cuando detecta un QR */,
      () => {} /* ignoramos errores de frame */,
    )
    .catch((err) => {
      console.error("Error iniciando cámara:", err);
      mostrarResultado(
        "error",
        "No se pudo acceder a la cámara.",
        "Verifica los permisos del navegador.",
      );
    });
}

/* ── Callback al detectar un QR ────────────────────── */
async function onQRDetectado(token) {
  /* Si ya estamos procesando un escaneo, ignoramos el siguiente */
  if (procesando) return;
  procesando = true;

  /* Feedback inmediato mientras espera respuesta del servidor */
  mostrarResultado("cargando", "Procesando…", "");

  try {
    /* El backend espera el campo 'qrToken' en el body */
    /* Enviamos el token escaneado al backend */
    const res = await api.post("/api/eventos-acceso", { qrToken: token });
    const data = res.data;

    /* Si tipoEvento es null significa que hay un mensaje informativo
       como "Ya registró entrada y salida hoy" */
    if (!data.tipoEvento) {
      mostrarResultado("error", data.mensaje || "Sin acceso.", "");
      return;
    }

    /* El backend devuelve 'entrada' o 'salida' en tipoEvento */
    const tipo = data.tipoEvento === "entrada" ? "Entrada" : "Salida";

    /* El backend devuelve 'nombrePersonal'*/
    const nombre = data.nombrePersonal || "Empleado";

    /* El backend devuelve 'horaRegistro' como LocalTime (HH:mm:ss) */
    const hora = data.horaRegistro
      ? data.horaRegistro.substring(0, 5) /* recortamos a HH:mm */
      : new Date().toLocaleTimeString("es-VE", {
          hour: "2-digit",
          minute: "2-digit",
        });

    mostrarResultado("success", `${tipo} — ${nombre}`, hora);
  } catch (err) {
    const mensaje =
      err.response?.data?.message ||
      err.response?.data?.mensaje ||
      "Acceso no autorizado.Token inválido o expirado.";
    mostrarResultado("error", mensaje, "");
  } finally {
    /* Esperamos 3 segundos y desbloqueamos para el siguiente escaneo
       La cámara sigue activa todo el tiempo */
    setTimeout(() => {
      procesando = false;
      ocultarResultado();
    }, 3000);
  }
}

/* ── Feedback visual ───────────────────────────────── */

/* Muestra el resultado del escaneo con color según tipo */
function mostrarResultado(tipo, mensaje, hora) {
  const div = document.getElementById("escaner-resultado");
  const icono = document.getElementById("escaner-icono");
  const msgEl = document.getElementById("escaner-mensaje");
  const horaEl = document.getElementById("escaner-hora");

  /* Removemos clases anteriores */
  div.className = "escaner-resultado";

  if (tipo === "success") {
    div.classList.add("escaner-success");
    icono.textContent = "✅";
  } else if (tipo === "error") {
    div.classList.add("escaner-error");
    icono.textContent = "❌";
  } else {
    div.classList.add("escaner-cargando");
    icono.textContent = "⏳";
  }

  msgEl.textContent = mensaje;
  horaEl.textContent = hora;
  div.classList.remove("oculto");
}

/* Oculta el panel de resultado */
function ocultarResultado() {
  document.getElementById("escaner-resultado").classList.add("oculto");
}

/* Cierra sesión */
function logout() {
  localStorage.clear();
  window.location.href = "../index.html";
}
