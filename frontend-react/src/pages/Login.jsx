import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { login } from '../api/apiClient.js'
import { useAuth } from '../auth/AuthContext.jsx'
import { Alerta, Aviso, Boton, Campo, CampoConEtiqueta, FondoAcceso, Marca } from '../components/ui.jsx'

export function Login() {
  const [usuario, setUsuario] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  const { guardarSesion } = useAuth()
  const navegar = useNavigate()
  const [parametros] = useSearchParams()

  // El registro deja aquí su mensaje de éxito al redirigir.
  const aviso = parametros.get('registrado')

  const enviar = async (evento) => {
    evento.preventDefault()
    setError('')

    if (!usuario.trim() || !password) {
      setError('Ingresa usuario y contraseña.')
      return
    }

    setEnviando(true)
    try {
      const respuesta = await login(usuario.trim(), password)
      guardarSesion(respuesta)
      // Un administrador entra directo al catálogo, como en el diseño.
      navegar(respuesta.rol === 'ADMINISTRADOR' ? '/admin' : '/tipos', { replace: true })
    } catch (e) {
      setError(e.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden">
      <FondoAcceso acento="cyan" />

      <form
        onSubmit={enviar}
        className="relative w-[420px] max-w-[90vw] rounded-[10px] border border-line-mid bg-surface px-10 py-11 shadow-[0_30px_70px_rgba(0,0,0,0.55)]"
      >
        <div className="mb-8">
          <Marca />
        </div>

        <h1 className="mb-1.5 text-[22px] font-semibold">Iniciar sesión</h1>
        <p className="mb-6 text-[13px] text-ink-muted">
          Accede para cotizar o gestionar la flota de drones.
        </p>

        <div className="mb-4 empty:mb-0">
          {aviso && !error && <Aviso>{aviso}</Aviso>}
          <Alerta>{error}</Alerta>
        </div>

        <div className="flex flex-col gap-4">
          <CampoConEtiqueta etiqueta="Usuario" id="usuario">
            <Campo
              id="usuario"
              autoComplete="username"
              placeholder="tu.usuario"
              value={usuario}
              onChange={(e) => setUsuario(e.target.value)}
            />
          </CampoConEtiqueta>

          <CampoConEtiqueta etiqueta="Contraseña" id="password">
            <Campo
              id="password"
              type="password"
              autoComplete="current-password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </CampoConEtiqueta>

          <Boton type="submit" className="mt-2" disabled={enviando}>
            {enviando ? 'Ingresando…' : 'Ingresar'}
          </Boton>
        </div>

        <div className="mt-6 border-t border-line pt-5 text-center text-[13px] text-ink-muted">
          ¿No tienes cuenta?{' '}
          <Link to="/registro" className="text-cyan no-underline hover:brightness-110">
            Regístrate
          </Link>
        </div>
      </form>
    </div>
  )
}
