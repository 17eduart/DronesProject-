import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { etiquetaDeTipo } from '../api/tiposDron.js'
import { dinero } from '../components/ui.jsx'

/**
 * Factura del pedido recién creado.
 *
 * Recibe pedido + factura por el estado de navegación: la pantalla anterior ya
 * los pidió a la API y no tiene sentido volver a consultarlos. Si alguien
 * entra directo a /factura (por ejemplo recargando), no hay nada que mostrar y
 * se vuelve a la selección de tipo.
 *
 * Todos los números salen del backend. El desglose que muestra
 * (base + distancia + peso) es el que calcula FacturaResponse, no una fórmula
 * replicada aquí: duplicar el cálculo en el cliente es garantizar que algún
 * día muestre algo distinto de lo que se cobró.
 */
export function Factura() {
  const { state } = useLocation()
  const navegar = useNavigate()

  if (!state?.factura || !state?.pedido) {
    return <Navigate to="/tipos" replace />
  }

  const { pedido, factura } = state
  const fecha = formatearFecha(factura.fechaEmision)

  return (
    <>
      <div className="no-print mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-[26px] font-bold">Cotización generada</h1>
          <div className="mt-1 font-mono text-xs text-ink-dim">
            {factura.numeroFactura} · {fecha}
          </div>
        </div>

        <div className="flex gap-3">
          <button
            type="button"
            onClick={() => navegar('/tipos')}
            className="cursor-pointer rounded-md border border-line-strong bg-transparent px-4.5 py-2.5 font-display text-xs font-semibold uppercase tracking-[0.05em] text-ink-muted transition hover:text-ink"
          >
            Nueva cotización
          </button>
          <button
            type="button"
            onClick={() => window.print()}
            className="cursor-pointer rounded-md bg-cyan px-4.5 py-2.5 font-display text-xs font-bold uppercase tracking-[0.05em] text-ink-inverse transition hover:brightness-110"
          >
            Descargar PDF
          </button>
        </div>
      </div>

      <div className="print-area max-w-[680px] rounded-lg border border-line-mid bg-surface p-8">
        {/* Solo visible al imprimir: la cabecera de la app no sale en el papel. */}
        <div className="mb-6 hidden print:block">
          <div className="text-lg font-bold">DRONESPROJECT · EPN</div>
          <div className="font-mono text-xs">
            {factura.numeroFactura} · {fecha}
          </div>
        </div>

        <div className="mb-5 flex justify-between border-b border-line pb-5">
          <div>
            <div className="mb-1 font-mono text-[11px] uppercase tracking-[0.1em] text-ink-dim">
              Cliente
            </div>
            <div className="text-[15px] font-semibold">{factura.nombreCliente}</div>
          </div>
          <div className="text-right">
            <div className="mb-1 font-mono text-[11px] uppercase tracking-[0.1em] text-ink-dim">
              Dron
            </div>
            <div className="text-[15px] font-semibold">{pedido.modeloDron}</div>
            <div className="font-mono text-xs text-ink-dim">
              {pedido.codigoDron} · {etiquetaDeTipo(pedido.tipoDron)}
            </div>
          </div>
        </div>

        <div className="mb-6 flex gap-6 font-mono text-xs text-ink-muted">
          <div>
            Distancia: <span className="text-ink">{pedido.distanciaSolicitada} km</span>
          </div>
          <div>
            Peso: <span className="text-ink">{pedido.pesoSolicitado} kg</span>
          </div>
          <div>
            Vuelo: <span className="text-ink">{pedido.horasSolicitadas} min</span>
          </div>
        </div>

        <div className="flex flex-col gap-3 font-mono text-sm">
          <Linea etiqueta="Costo base" valor={dinero(factura.costoBase)} />
          <Linea etiqueta="Componente distancia" valor={dinero(factura.componenteDistancia)} />
          <div className="border-b border-line pb-4">
            <Linea etiqueta="Componente peso" valor={dinero(factura.componentePeso)} />
          </div>

          <div className="mt-1.5 flex items-center justify-between rounded-md border border-cyan/40 bg-cyan/12 px-4 py-3.5">
            <span className="font-display font-bold">TOTAL</span>
            <span className="text-lg font-bold text-cyan">{dinero(factura.total)}</span>
          </div>
        </div>
      </div>
    </>
  )
}

function Linea({ etiqueta, valor }) {
  return (
    <div className="flex justify-between">
      <span className="text-ink-muted">{etiqueta}</span>
      <span>{valor}</span>
    </div>
  )
}

/** La API entrega ISO-8601 (LocalDateTime); se muestra en formato local. */
function formatearFecha(iso) {
  if (!iso) return ''
  const fecha = new Date(iso)
  if (Number.isNaN(fecha.getTime())) return iso
  return fecha.toLocaleDateString('es-EC', { year: 'numeric', month: 'short', day: 'numeric' })
}
