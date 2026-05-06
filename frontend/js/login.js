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
    /* Enviamos las credenciales al backend como JSON mediante una petición POST */
    const response = await fetch("http://localhost:8080/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ nombreUsuario: usuario, password: password }),
    });

    /* Si el servidor responde con error (401, 403, etc.) mostramos mensaje de credenciales incorrectas */
    if (!response.ok) {
      mostrarError("Usuario o contraseña incorrectos.");
      return;
    }

    /* Convertimos la respuesta del servidor a objeto JavaScript
       data contendrá: { token, rol, nombre } */
    const data = await response.json();

    // Guardar token y datos del usuario
    localStorage.setItem("token", data.token);
    localStorage.setItem("rol", data.roles[0]);
    // El backend devuelve 'nombreUsuario' en la respuesta (ver JwtResponse.java)
    const nombreUsuario = data.nombreUsuario || data.username || data.nombre || usuario;
    localStorage.setItem("nombre", nombreUsuario);
    console.log("Token guardado:", data.token ? "OK" : "FALLO");
    console.log("Nombre guardado:", nombreUsuario);

    // Redirigir según rol despues de la expoferia, por ahora vamos directo al dashboard
    window.location.href = "../modules/dashboard.html";
  } catch (error) {
    /* Si hay un error de red (servidor apagado, sin internet) mostramos un mensaje al usuario */
    mostrarError("No se pudo conectar con el servidor. Intenta de nuevo.");
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

// Login con Enter
document.addEventListener("keydown", (e) => {
  if (e.key === "Enter") iniciarSesion();
});
