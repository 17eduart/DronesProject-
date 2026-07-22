import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { registrar } from '../api/apiClient.js'
import { Alerta, Boton, Campo, CampoConEtiqueta, FondoAcceso } from '../components/ui.jsx'

export function Registro() {
  const [usuario, setUsuario] = useState('')
  const [password, setPassword] = useState('')
  const [password2, setPassword2] = useState('')
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  const navegar = useNavigate()

  const enviar = async (evento) => {
    evento.preventDefault()
    setError('')

    if (!usuario.trim() || !password || !password2) {
      setError('Completa todos los campos.')
      return
    }
    // Validación local: no gastamos una petición para algo que se comprueba aquí.
    if (password !== password2) {
      setError('Las contraseñas no coinciden.')
      return
    }

    setEnviando(true)
    try {
      // El backend crea siempre CLIENTE; el rol no se manda desde aquí.
      await registrar(usuario.trim(), password)
      navegar(
        `/login?registrado=${encodeURIComponent(`Usuario ${usuario.trim()} registrado. Ya puedes iniciar sesión.`)}`,
        { replace: true },
      )
    } catch (e) {
      setError(e.message)
    } finally {
      setEnviando(false)
    }
  }

  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden">
      <FondoAcceso acento="amber" velocidad="animate-scanline-slow" />

      <form
        onSubmit={enviar}
        className="relative w-[440px] max-w-[90vw] rounded-[10px] border border-line-mid bg-surface p-10 shadow-[0_30px_70px_rgba(0,0,0,0.55)]"
      >
        <h1 className="mb-1.5 text-[22px] font-semibold">Crear cuenta</h1>
        <p className="mb-5 text-[13px] text-ink-muted">
          Regístrate como cliente para cotizar servicios de dron.
        </p>

        <div className="mb-4 empty:mb-0">
          <Alerta>{error}</Alerta>
        </div>

        <div className="flex flex-col gap-3.5">
          <CampoConEtiqueta etiqueta="Usuario" id="reg-usuario">
            <Campo
              id="reg-usuario"
              autoComplete="username"
              placeholder="tu.usuario"
              value={usuario}
              onChange={(e) => setUsuario(e.target.value)}
            />
          </CampoConEtiqueta>

          <div className="flex gap-3">
            <div className="flex-1">
              <CampoConEtiqueta etiqueta="Contraseña" id="reg-pass">
                <Campo
                  id="reg-pass"
                  type="password"
                  autoComplete="new-password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </CampoConEtiqueta>
            </div>
            <div className="flex-1">
              <CampoConEtiqueta etiqueta="Confirmar" id="reg-pass2">
                <Campo
                  id="reg-pass2"
                  type="password"
                  autoComplete="new-password"
                  placeholder="••••••••"
                  value={password2}
                  onChange={(e) => setPassword2(e.target.value)}
                />
              </CampoConEtiqueta>
            </div>
          </div>

          <Boton type="submit" variante="admin" className="mt-2" disabled={enviando}>
            {enviando ? 'Creando…' : 'Crear cuenta'}
          </Boton>
        </div>

        <div className="mt-5 border-t border-line pt-4 text-center text-[13px] text-ink-muted">
          ¿Ya tienes cuenta?{' '}
          <Link to="/login" className="text-cyan no-underline hover:brightness-110">
            Inicia sesión
          </Link>
        </div>
      </form>
    </div>
  )
}
