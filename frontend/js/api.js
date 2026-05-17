/* api.js — instancia Axios compartida
   withCredentials: true hace que el navegador
   envíe la cookie automáticamente en cada petición */

const api = axios.create({
  baseURL: "http://localhost:8080",
  withCredentials: true, // clave — envía la cookie httpOnly
});
