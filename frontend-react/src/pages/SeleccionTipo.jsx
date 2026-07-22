import { useNavigate } from 'react-router-dom'
import { ACENTOS, TIPOS_DRON } from '../api/tiposDron.js'
import { cn } from '../lib/utils.js'

export function SeleccionTipo() {
  const navegar = useNavigate()

  return (
    <>
      <h1 className="mb-1.5 text-[26px] font-bold">Selecciona el tipo de servicio</h1>
      <p className="mb-8 text-sm text-ink-muted">
        Cada tipo de dron tiene límites técnicos distintos de peso y autonomía.
      </p>

      <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
        {TIPOS_DRON.map((t) => (
          <TarjetaTipo key={t.tipo} tipo={t} onSeleccionar={() => navegar(`/cotizar/${t.slug}`)} />
        ))}
      </div>
    </>
  )
}

function TarjetaTipo({ tipo, onSeleccionar }) {
  const acento = ACENTOS[tipo.acento]

  return (
    <div
      role="button"
      tabIndex={0}
      onClick={onSeleccionar}
      onKeyDown={(e) => {
        // Un div clicable debe responder también al teclado.
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault()
          onSeleccionar()
        }
      }}
      className={cn(
        'cursor-pointer rounded-lg border border-line-mid border-t-[3px] bg-surface px-6 py-8 text-center',
        'transition-transform hover:-translate-y-0.5 focus:outline-none focus-visible:ring-2 focus-visible:ring-cyan',
        acento.bordeTop,
      )}
    >
      <div
        className={cn(
          'mx-auto mb-4 h-24 w-24 overflow-hidden rounded-[10px] border-2',
          acento.borde,
        )}
      >
        <img src={tipo.imagen} alt={tipo.etiqueta} className="h-full w-full object-cover" />
      </div>

      <div className="mb-2 text-[17px] font-bold">{tipo.etiqueta}</div>
      <p className="mb-3.5 text-[13px] leading-relaxed text-ink-muted">{tipo.descripcion}</p>
      <div className="mb-4 font-mono text-[11px] text-ink-dimmer">{tipo.limites}</div>

      <div
        className={cn(
          'w-full rounded-md border bg-transparent py-2.5 font-display text-xs font-semibold uppercase tracking-[0.06em]',
          acento.borde,
          acento.texto,
        )}
      >
        Seleccionar
      </div>
    </div>
  )
}
