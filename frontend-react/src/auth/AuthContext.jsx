import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { setTokenProvider } from '../api/apiClient.js'

/**
 * Sesión en memoria.
 *
 * Mismo criterio que SessionManager del cliente JavaFX: el token NO se
 * persiste (ni localStorage ni sessionStorage ni cookie). Guardarlo en
 * localStorage lo dejaría legible por cualquier script de la página, y como
 * el token es válido 24 h, quien lo lea puede actuar como el usuario.
 *
 * Limitación aceptada: al recargar la página se pierde la sesión y hay que
 * volver a entrar. Para esta demo es el intercambio correcto.
 */

const ROL_ADMINISTRADOR = 'ADMINISTRADOR'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [sesion, setSesion] = useState(null) // { token, username, rol }

  // El apiClient no conoce React: se le pasa una función que lee el estado
  // actual, de modo que cada petición use el token vigente.
  useEffect(() => {
    setTokenProvider(() => sesion?.token ?? null)
  }, [sesion])

  const guardarSesion = useCallback(({ token, username, rol }) => {
    setSesion({ token, username, rol })
  }, [])

  const cerrarSesion = useCallback(() => {
    setSesion(null)
  }, [])

  const valor = useMemo(
    () => ({
      sesion,
      token: sesion?.token ?? null,
      username: sesion?.username ?? '',
      rol: sesion?.rol ?? null,
      estaAutenticado: Boolean(sesion?.token),
      esAdministrador: sesion?.rol === ROL_ADMINISTRADOR,
      iniciales: iniciralesDe(sesion?.username),
      guardarSesion,
      cerrarSesion,
    }),
    [sesion, guardarSesion, cerrarSesion],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const contexto = useContext(AuthContext)
  if (!contexto) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>')
  }
  return contexto
}

/** Iniciales para el avatar de la cabecera, como en el diseño. */
function iniciralesDe(username) {
  if (!username) return ''
  return username
    .split(/[\s._-]+/)
    .filter(Boolean)
    .map((parte) => parte[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()
}
