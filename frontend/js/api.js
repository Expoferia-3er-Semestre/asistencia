/* api.js — instancia Axios compartida
   withCredentials: true hace que el navegador
   envíe la cookie automáticamente en cada petición */

/* Detectamos automáticamente el host para que funcione
   tanto desde la PC (localhost) como desde el teléfono (IP local) */

const BASE_URL = `http://${window.location.hostname}:8080`;

const api = axios.create({
  baseURL: BASE_URL,
  withCredentials: true,
});

/*const api = axios.create({
  baseURL: "http://127.0.0.1:8080", //http://localhost:8080
  withCredentials: true, // clave — envía la cookie httpOnly
});*/

// recuerda que origen de esta URL que se usa el frontend debe coincidir con el puerto permitido en el backend (SecurityConfig.java) asegúrate que el origen permitido este alli tambien para evitar problemas de CORS.
