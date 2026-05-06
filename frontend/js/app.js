// app.js - Sistema de navegación SPA con JWT

class App {
  constructor() {
    this.token = localStorage.getItem("token");
    this.currentUser = null;
    this.init();
  }

  init() {
    // Configurar Axios con interceptor para JWT
    this.setupAxios();

    // Cargar módulo inicial según hash o token
    const initialModule = this.getInitialModule();
    this.loadModule(initialModule, {}, true);

    // Manejar navegación del browser
    window.addEventListener("popstate", (e) => {
      const moduleName =
        e.state?.module || this.getHashModule() || this.getInitialModule();
      this.loadModule(moduleName, {}, true);
    });

    window.addEventListener("hashchange", () => {
      const moduleName = this.getHashModule() || this.getInitialModule();
      this.loadModule(moduleName, {}, true);
    });
  }

  getHashModule() {
    return window.location.hash ? window.location.hash.substring(1) : null;
  }

  getInitialModule() {
    const hashModule = this.getHashModule();
    if (hashModule) {
      return hashModule;
    }
    return this.token ? "dashboard" : "login";
  }

  setupAxios() {
    // 1. Configura la URL base para no repetirla en cada fetch
    axios.defaults.baseURL = "http://localhost:8080";

    // Interceptor para agregar token a todas las requests
    axios.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem("token");
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      },
    );

    // Interceptor para manejar errores de autenticación
    axios.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response && error.response.status === 401) {
          this.logout();
        }
        return Promise.reject(error);
      },
    );
  }

  async loadModule(moduleName, data = {}, replace = false) {
    try {
      const response = await fetch(`modules/${moduleName}.html`);
      if (!response.ok) throw new Error(`Error: ${response.status}`);

      const html = await response.text();
      const appContainer = document.getElementById("app");

      // 1. Insertamos el HTML
      appContainer.innerHTML = html;

      // Procesamos los scripts internos
      const inlineScripts = appContainer.querySelectorAll("script");
      inlineScripts.forEach((oldScript) => {
        const newScript = document.createElement("script");
        if (oldScript.src) newScript.src = oldScript.src;
        newScript.textContent = oldScript.textContent;
        oldScript.parentNode.replaceChild(newScript, oldScript);
      });

      // ESPERA CRUCIAL: Dejamos que el DOM se asiente
      setTimeout(() => {
        const scriptFunction =
          window[
            `init${moduleName.charAt(0).toUpperCase() + moduleName.slice(1)}`
          ];
        if (scriptFunction) {
          console.log(`Ejecutando init para: ${moduleName}`);
          scriptFunction.call(this, data);
        }
      }, 50); // 50ms son suficientes para que el DOM esté disponible

      // ... resto de tu lógica de historial ...
    } catch (error) {
      console.error("Error loading module:", error);
    }
  }

  showError(message) {
    const alertHtml = `
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
    document.getElementById("app").insertAdjacentHTML("afterbegin", alertHtml);
  }

  showSuccess(message) {
    const alertHtml = `
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        `;
    document.getElementById("app").insertAdjacentHTML("afterbegin", alertHtml);
  }

  logout() {
    localStorage.removeItem("token");
    this.token = null;
    this.currentUser = null;
    this.loadModule("login");
  }
}

// Inicializar la aplicación cuando el DOM esté listo
document.addEventListener("DOMContentLoaded", () => {
  window.app = new App();
});
