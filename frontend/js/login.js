/* Función principal que se ejecuta al hacer click
   en "Iniciar sesión" o presionar Enter */
async function iniciarSesion() {
  /* Obtenemos los valores .trim() elimina espacios en blanco al inicio y al final */
  const usuario = document.getElementById("usuario").value.trim();
  const password = document.getElementById("password").value.trim();
  const errorMsg = document.getElementById("error-msg");
  const btn = document.getElementById("btn-login");

  // Validación básica
  if (!usuario || !password) {
    mostrarError("Por favor completa todos los campos.");
    return;
  }

  // Estado de carga
  btn.setAttribute("aria-busy", "true");
  btn.textContent = "Ingresando...";
  errorMsg.style.display = "none";

  try {
    /* api viene de api.js — withCredentials:true hace que el navegador
       guarde la cookie httpOnly que manda el backend */
    const res = await api.post("/api/auth/login", {
      nombreUsuario: usuario,
      password: password,
    });

    /* El token ya NO se guarda — viaja solo en la cookie httpOnly
       Solo guardamos el nombre para mostrarlo en el dashboard */
    const nombre =
      res.data.nombreUsuario || res.data.username || res.data.nombre || usuario;
    localStorage.setItem("nombre", nombre);

    // Redirigir según rol despues de la expoferia, por ahora vamos directo al dashboard
    window.location.href = "../modules/dashboard.html";
    
  } catch (err) {
    /* Axios lanza error si el status no es 2xx */
    if (err.response?.status === 401 || err.response?.status === 403) {
      mostrarError("Usuario o contraseña incorrectos.");
    } else {
      mostrarError("No se pudo conectar con el servidor. Intenta de nuevo.");
    }
  } finally {
    btn.removeAttribute("aria-busy");
    btn.textContent = "Iniciar sesión";
  }
}

/* Muestra el mensaje de error en el párrafo #error-msg */
function mostrarError(msg) {
  const errorMsg = document.getElementById("error-msg");
  errorMsg.textContent = msg;
  errorMsg.style.display = "block";
}

/* Oculta el mensaje de error */
function ocultarError() {
  const el = document.getElementById("error-msg");
  el.style.display = "none";
  el.textContent   = "";
}

// Login con Enter
document.addEventListener("keydown", (e) => {
  if (e.key === "Enter") iniciarSesion();
});
