/**
 * Cliente HTTP de la API de drones.
 *
 * Réplica del ApiClient.java del cliente JavaFX: mismos endpoints, mismo
 * criterio de errores. Usa fetch nativo — axios no aportaría nada aquí y sería
 * una dependencia más que mantener.
 *
 * El token NO se guarda aquí ni en localStorage: lo inyecta AuthContext con
 * setTokenProvider, igual que SessionManager alimentaba al ApiClient de Java.
 */

const BASE_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

/**
 * De dónde sale el token en cada petición. Es una función y no un valor para
 * que siempre lea el estado actual de React en lugar de una copia congelada al
 * cargar el módulo.
 */
let tokenProvider = () => null

export function setTokenProvider(fn) {
  tokenProvider = fn
}

/** Error de la API con el mensaje ya listo para mostrar al usuario. */
export class ApiError extends Error {
  constructor(status, message, options) {
    super(message, options)
    this.name = 'ApiError'
    this.status = status
  }
}

const MENSAJES_GENERICOS = {
  401: 'Credenciales inválidas o sesión expirada.',
  403: 'No tiene permisos para esta operación.',
  404: 'El recurso solicitado no existe.',
}

/**
 * Saca el texto del campo "Mensaje" del JSON de error de GlobalException.
 * Si el cuerpo no tiene ese formato (por ejemplo un 404 sin cuerpo, o una
 * página de error del contenedor), cae a un genérico por código.
 */
function mensajeDeError(status, cuerpo) {
  if (cuerpo && typeof cuerpo === 'object' && typeof cuerpo.Mensaje === 'string') {
    return cuerpo.Mensaje
  }
  return MENSAJES_GENERICOS[status] ?? `Error del servidor (HTTP ${status}).`
}

async function leerCuerpo(respuesta) {
  const texto = await respuesta.text()
  if (!texto) return null
  try {
    return JSON.parse(texto)
  } catch {
    return texto
  }
}

/**
 * @param {string} ruta
 * @param {{ method?: string, body?: unknown, conToken?: boolean }} opciones
 */
async function peticion(ruta, { method = 'GET', body, conToken = true } = {}) {
  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  if (conToken) {
    const token = tokenProvider()
    if (!token) {
      throw new ApiError(401, 'No hay sesión activa. Vuelva a iniciar sesión.')
    }
    headers.Authorization = `Bearer ${token}`
  }

  let respuesta
  try {
    respuesta = await fetch(`${BASE_URL}${ruta}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch (causa) {
    // fetch solo rechaza por fallo de red o CORS; un 4xx/5xx llega como
    // respuesta normal y se maneja abajo.
    throw new ApiError(
      0,
      `No se pudo conectar con el servidor. ¿Está levantado en ${BASE_URL}?`,
      { cause: causa },
    )
  }

  const cuerpo = await leerCuerpo(respuesta)

  if (!respuesta.ok) {
    throw new ApiError(respuesta.status, mensajeDeError(respuesta.status, cuerpo))
  }
  return cuerpo
}

/** Codifica un valor que va dentro de la ruta (código escrito por el usuario). */
const segmento = (valor) => encodeURIComponent(valor ?? '')

// ---------------------------------------------------------------------------
// Endpoints públicos
// ---------------------------------------------------------------------------

export function login(username, password) {
  return peticion('/login', { method: 'POST', body: { username, password }, conToken: false })
}

export function registrar(username, password) {
  // El backend siempre crea CLIENTE; el rol no se manda desde aquí.
  return peticion('/registro', { method: 'POST', body: { username, password }, conToken: false })
}

// ---------------------------------------------------------------------------
// Catálogo
// ---------------------------------------------------------------------------

export function obtenerDrones() {
  return peticion('/dron')
}

export function crearDron(tipo, dron) {
  return peticion(`/dron/${segmento(tipo)}`, { method: 'POST', body: dron })
}

export function actualizarDron(tipo, codigo, dron) {
  return peticion(`/dron/${segmento(tipo)}/${segmento(codigo)}`, { method: 'PUT', body: dron })
}

export function eliminarDron(codigo) {
  // Responde 204 sin cuerpo.
  return peticion(`/dron/${segmento(codigo)}`, { method: 'DELETE' })
}

// ---------------------------------------------------------------------------
// Pedidos y factura
// ---------------------------------------------------------------------------

export function crearPedido(pedido) {
  return peticion('/pedidos', { method: 'POST', body: pedido })
}

export function obtenerFactura(pedidoId) {
  return peticion(`/pedidos/${segmento(pedidoId)}/factura`)
}
