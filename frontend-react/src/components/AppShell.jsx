import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext.jsx'
import { Marca } from './ui.jsx'
import { cn } from '../lib/utils.js'

/**
 * Cabecera fija de 64px + barra lateral de 220px, como en el handoff.
 *
 * Todo lleva la clase no-print: al imprimir la factura solo debe salir el
 * documento, no la navegación.
 */
export function AppShell() {
  const { username, iniciales, esAdministrador, cerrarSesion } = useAuth()
  const navegar = useNavigate()

  const salir = () => {
    cerrarSesion()
    navegar('/login', { replace: true })
  }

  return (
    <div className="min-h-screen bg-bg text-ink">
      <header className="no-print fixed inset-x-0 top-0 z-10 flex h-16 items-center justify-between border-b border-line bg-chrome px-7">
        <Marca compacta />

        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2.5">
            <div className="flex h-8 w-8 items-center justify-center rounded-full border border-line-stronger bg-avatar font-mono text-xs font-semibold">
              {iniciales}
            </div>
            <div>
              <div className="text-[13px] font-semibold leading-tight">{username}</div>
              <div
                className={cn(
                  'font-mono text-[10px] uppercase tracking-[0.08em]',
                  esAdministrador ? 'text-amber' : 'text-cyan',
                )}
              >
                {esAdministrador ? 'Administrador' : 'Cliente'}
              </div>
            </div>
          </div>

          <button
            type="button"
            onClick={salir}
            className="cursor-pointer rounded-md border border-line-strong bg-transparent px-3.5 py-2 font-mono text-xs text-ink-muted transition hover:border-line-stronger hover:text-ink"
          >
            Cerrar sesión
          </button>
        </div>
      </header>

      <nav className="no-print fixed bottom-0 left-0 top-16 z-9 w-[220px] border-r border-line bg-chrome py-6">
        <EnlaceLateral to="/tipos" acento="cyan">
          Nueva cotización
        </EnlaceLateral>
        {esAdministrador && (
          <EnlaceLateral to="/admin" acento="amber">
            Catálogo de drones
          </EnlaceLateral>
        )}
      </nav>

      <main className="ml-[220px] mt-16 max-w-[1180px] p-10">
        <Outlet />
      </main>
    </div>
  )
}

function EnlaceLateral({ to, acento, children }) {
  const color = acento === 'amber' ? 'bg-amber' : 'bg-cyan'
  const borde = acento === 'amber' ? 'border-l-amber' : 'border-l-cyan'

  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        cn(
          'flex items-center gap-2.5 border-l-[3px] px-6 py-3 text-[13px] font-medium transition',
          isActive
            ? cn(borde, 'bg-raised text-ink')
            : 'border-l-transparent text-ink-muted hover:bg-raised/60 hover:text-ink',
        )
      }
    >
      <span className={cn('inline-block h-2 w-2 rounded-sm', color)} />
      {children}
    </NavLink>
  )
}
