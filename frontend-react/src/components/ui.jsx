import { cn } from '../lib/utils.js'

/**
 * Primitivos de interfaz derivados del sistema de diseño.
 *
 * Las clases salen de los tokens definidos en styles/index.css (@theme), no
 * de valores sueltos: si mañana cambia un color del diseño, se cambia en el
 * tema y se propaga aquí.
 */

/** Etiqueta de campo: mono, versalitas, tenue. */
export function Etiqueta({ children, htmlFor }) {
  return (
    <label
      htmlFor={htmlFor}
      className="mb-1.5 block font-mono text-[11px] uppercase tracking-[0.1em] text-ink-dim"
    >
      {children}
    </label>
  )
}

const CLASES_CAMPO =
  'w-full rounded-md border border-line-mid bg-sunken px-3.5 py-3 font-mono text-sm text-ink ' +
  'outline-none transition-colors placeholder:text-ink-faint ' +
  'focus:border-cyan/70 disabled:cursor-not-allowed disabled:opacity-50'

export function Campo({ className, ...props }) {
  return <input className={cn(CLASES_CAMPO, className)} {...props} />
}

export function Selector({ className, children, ...props }) {
  return (
    <select className={cn(CLASES_CAMPO, className)} {...props}>
      {children}
    </select>
  )
}

/** Campo con su etiqueta, que es como aparecen siempre en el diseño. */
export function CampoConEtiqueta({ etiqueta, id, children }) {
  return (
    <div>
      <Etiqueta htmlFor={id}>{etiqueta}</Etiqueta>
      {children}
    </div>
  )
}

const VARIANTES_BOTON = {
  primario:
    'bg-cyan text-ink-inverse font-bold shadow-[0_0_20px_color-mix(in_oklch,var(--color-cyan)_25%,transparent)] hover:brightness-110',
  admin:
    'bg-amber text-ink-inverse font-bold shadow-[0_0_20px_color-mix(in_oklch,var(--color-amber)_25%,transparent)] hover:brightness-110',
  contorno:
    'bg-transparent border border-line-strong text-ink-muted hover:border-line-stronger hover:text-ink',
}

export function Boton({ variante = 'primario', className, type = 'button', ...props }) {
  return (
    <button
      type={type}
      className={cn(
        'cursor-pointer rounded-md px-5 py-3 font-display text-[13px] uppercase tracking-[0.08em]',
        'transition disabled:cursor-not-allowed disabled:opacity-50',
        VARIANTES_BOTON[variante],
        className,
      )}
      {...props}
    />
  )
}

/** Bloque de error en rojo, con el mismo estilo en todas las pantallas. */
export function Alerta({ children }) {
  if (!children) return null
  return (
    <div
      role="alert"
      className="rounded-md border border-danger/40 bg-danger/20 px-3 py-2.5 font-mono text-[13px] text-danger-ink"
    >
      {children}
    </div>
  )
}

/** Mensaje de éxito; el diseño no lo define, reusa la forma de Alerta en verde. */
export function Aviso({ children }) {
  if (!children) return null
  return (
    <div className="rounded-md border border-cyan/40 bg-cyan/12 px-3 py-2.5 font-mono text-[13px] text-cyan">
      {children}
    </div>
  )
}

export function Tarjeta({ className, children }) {
  return (
    <div className={cn('rounded-lg border border-line-mid bg-surface', className)}>{children}</div>
  )
}

/** Marca EPN + nombre del sistema, en la cabecera y en las pantallas de acceso. */
export function Marca({ compacta = false }) {
  const tamano = compacta ? 'h-8 w-8' : 'h-10 w-10'
  return (
    <div className="flex items-center gap-3">
      <div
        className={cn(
          tamano,
          'flex shrink-0 items-center justify-center overflow-hidden rounded-full border border-cyan/50 bg-[#f4f6f8]',
          !compacta && 'shadow-[0_0_14px_color-mix(in_oklch,var(--color-cyan)_20%,transparent)]',
        )}
      >
        <img src="/img/Buho-EPN.png" alt="EPN" className="h-[78%] w-[78%] object-contain" />
      </div>
      <div>
        <div
          className={cn(
            'font-display font-bold leading-none tracking-[0.06em]',
            compacta ? 'text-sm' : 'text-[17px]',
          )}
        >
          DRONESPROJECT
        </div>
        {!compacta && (
          <div className="mt-0.5 font-mono text-[11px] tracking-[0.12em] text-ink-dim">
            EPN · SISTEMA DE COTIZACIÓN
          </div>
        )}
      </div>
    </div>
  )
}

/**
 * Fondo de las pantallas de acceso: retícula + línea de escaneo.
 * Ambas capas son decorativas y no deben capturar clics.
 */
export function FondoAcceso({ acento = 'cyan', velocidad = 'animate-scanline' }) {
  const color = acento === 'amber' ? 'var(--color-amber)' : 'var(--color-cyan)'
  return (
    <>
      <div className="pointer-events-none absolute inset-0 grid-backdrop" aria-hidden />
      <div
        className={cn('pointer-events-none absolute inset-x-0 h-0.5', velocidad)}
        aria-hidden
        style={{ background: `linear-gradient(90deg, transparent, ${color}, transparent)` }}
      />
    </>
  )
}

/** Badge del tipo de dron en la tabla del catálogo. */
export function BadgeTipo({ tipo }) {
  const estilos = {
    LIVIANO: 'bg-cyan/15 text-cyan border-cyan/40',
    CARGA: 'bg-amber/15 text-amber border-amber/40',
    EMERGENCIA: 'bg-danger/15 text-danger border-danger/40',
  }
  return (
    <span
      className={cn(
        'rounded border px-2 py-0.5 font-mono text-[11px]',
        estilos[tipo] ?? 'border-line-mid text-ink-muted',
      )}
    >
      {tipo}
    </span>
  )
}

/** Formatea dinero como el diseño: $12.34 */
export const dinero = (n) =>
  typeof n === 'number' && Number.isFinite(n) ? `$${n.toFixed(2)}` : '—'
