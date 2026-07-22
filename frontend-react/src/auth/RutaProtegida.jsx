import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext.jsx'

/**
 * Envuelve una ruta que exige sesión, y opcionalmente rol de administrador.
 *
 * Esto es UX, NO seguridad: quien manda es el backend, que responde 401/403
 * según el rol del token. Ocultar una ruta aquí solo evita que el usuario
 * llegue a una pantalla que igual le rebotaría; nada impide llamar a la API
 * por fuera del navegador.
 */
export function RutaProtegida({ soloAdmin = false, children }) {
  const { estaAutenticado, esAdministrador } = useAuth()
  const ubicacion = useLocation()

  if (!estaAutenticado) {
    // replace evita que el botón "atrás" devuelva a la pantalla protegida.
    return <Navigate to="/login" replace state={{ desde: ubicacion.pathname }} />
  }

  if (soloAdmin && !esAdministrador) {
    return <Navigate to="/tipos" replace />
  }

  return children
}
