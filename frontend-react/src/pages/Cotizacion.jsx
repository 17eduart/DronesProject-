import { useEffect, useMemo, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { crearPedido, obtenerDrones, obtenerFactura } from '../api/apiClient.js'
import { ACENTOS, tipoDesdeSlug } from '../api/tiposDron.js'
import { Alerta, Boton, Campo, CampoConEtiqueta, Selector, Tarjeta } from '../components/ui.jsx'
import { cn } from '../lib/utils.js'

export function Cotizacion() {
  const { slug } = useParams()
  const navegar = useNavigate()

  const tipo = tipoDesdeSlug(slug)

  const [catalogo, setCatalogo] = useState([])
  const [cargando, setCargando] = useState(true)
  const [codigoElegido, setCodigoElegido] = useState('')
  const [distancia, setDistancia] = useState('')
  const [peso, setPeso] = useState('')
  const [horas, setHoras] = useState('')
  const [error, setError] = useState('')
  const [enviando, setEnviando] = useState(false)

  // GET /dron devuelve el catálogo completo: el filtro por tipo se hace aquí
  // porque el endpoint no acepta filtro.
  useEffect(() => {
    if (!tipo) return
    let vigente = true

    setCargando(true)
    obtenerDrones()
      .then((drones) => {
        if (!vigente) return
        setCatalogo(drones.filter((d) => d.tipo === tipo.tipo))
      })
      .catch((e) => vigente && setError(e.message))
      .finally(() => vigente && setCargando(false))

    // Evita escribir estado si el usuario cambió de pantalla antes de que
    // llegara la respuesta.
    return () => {
      vigente = false
    }
  }, [tipo])

  const seleccionado = useMemo(
    () => catalogo.find((d) => d.codigo === codigoElegido) ?? null,
    [catalogo, codigoElegido],
  )

  if (!tipo) {
    return (
      <>
        <Alerta>El tipo de servicio «{slug}» no existe.</Alerta>
        <Link to="/tipos" className="mt-4 inline-block font-mono text-xs text-cyan">
          ← Volver a los tipos
        </Link>
      </>
    )
  }

  const acento = ACENTOS[tipo.acento]

  const enviar = async (evento) => {
    evento.preventDefault()
    setError('')

    if (!seleccionado) {
      setError('Selecciona un modelo de dron.')
      return
    }

    const numeros = { distancia: Number(distancia), peso: Number(peso), horas: Number(horas) }
    const invalido = Object.entries(numeros).find(([, v]) => !Number.isFinite(v) || v <= 0)
    if (invalido) {
      setError('Ingresa valores numéricos válidos mayores a 0.')
      return
    }

    setEnviando(true)
    try {
      const pedido = await crearPedido({
        codigoDron: seleccionado.codigo,
        distanciaSolicitada: numeros.distancia,
        pesoSolicitado: numeros.peso,
        horasSolicitadas: numeros.horas,
      })
      const factura = await obtenerFactura(pedido.id)

      // La factura se pasa por estado de navegación: ya la tenemos, no hay
      // razón para que la pantalla siguiente la vuelva a pedir.
      navegar('/factura', { state: { pedido, factura }, replace: true })
    } catch (e) {
      // Si el backend rechazó por capacidad excedida (400), aquí llega su
      // mensaje real: "El peso solicitado (50.0 kg) supera la capacidad…".
      setError(e.message)
      setEnviando(false)
    }
  }

  return (
    <>
      <Link
        to="/tipos"
        className="mb-3.5 inline-block font-mono text-xs text-ink-muted hover:text-ink"
      >
        ← Cambiar tipo de servicio
      </Link>

      <h1 className="mb-1 text-[26px] font-bold">Cotizar: {tipo.etiqueta}</h1>
      <p className="mb-7 text-sm text-ink-muted">
        Completa los datos del vuelo para generar tu cotización.
      </p>

      <div className="grid grid-cols-1 items-start gap-6 lg:grid-cols-[1.3fr_1fr]">
        <Tarjeta className="p-7">
          <form onSubmit={enviar} className="flex flex-col gap-4.5">
            <CampoConEtiqueta etiqueta="Modelo de dron" id="modelo">
              <Selector
                id="modelo"
                value={codigoElegido}
                onChange={(e) => setCodigoElegido(e.target.value)}
                disabled={cargando}
              >
                <option value="">
                  {cargando ? 'Cargando catálogo…' : 'Selecciona un modelo…'}
                </option>
                {catalogo.map((d) => (
                  <option key={d.codigo} value={d.codigo}>
                    {d.modelo} · {d.codigo}
                  </option>
                ))}
              </Selector>
            </CampoConEtiqueta>

            <CampoConEtiqueta etiqueta="Distancia a recorrer (km)" id="distancia">
              <Campo
                id="distancia"
                type="number"
                min="0"
                step="any"
                placeholder="0"
                value={distancia}
                onChange={(e) => setDistancia(e.target.value)}
              />
            </CampoConEtiqueta>

            <CampoConEtiqueta etiqueta="Peso a cargar (kg)" id="peso">
              <Campo
                id="peso"
                type="number"
                min="0"
                step="any"
                placeholder="0"
                value={peso}
                onChange={(e) => setPeso(e.target.value)}
              />
            </CampoConEtiqueta>

            <CampoConEtiqueta etiqueta="Horas de vuelo (min)" id="horas">
              <Campo
                id="horas"
                type="number"
                min="0"
                step="any"
                placeholder="0"
                value={horas}
                onChange={(e) => setHoras(e.target.value)}
              />
            </CampoConEtiqueta>

            <Alerta>{error}</Alerta>

            <Boton type="submit" className="mt-1.5" disabled={enviando || cargando}>
              {enviando ? 'Generando…' : 'Generar cotización'}
            </Boton>
          </form>
        </Tarjeta>

        <div className="rounded-lg border border-line bg-sunken p-6">
          <div className={cn('mb-4 h-[110px] overflow-hidden rounded-md border-2', acento.borde)}>
            <img src={tipo.imagen} alt={tipo.etiqueta} className="h-full w-full object-cover" />
          </div>

          <div className="mb-4 font-mono text-[11px] uppercase tracking-[0.1em] text-cyan">
            Telemetría del modelo
          </div>

          {seleccionado ? (
            <dl className="flex flex-col gap-3.5 font-mono">
              <Telemetria etiqueta="Código" valor={seleccionado.codigo} />
              <Telemetria etiqueta="Alcance máx." valor={`${seleccionado.distancia_km} km`} />
              <Telemetria etiqueta="Peso máx." valor={`${seleccionado.peso_maximo} kg`} />
              <Telemetria etiqueta="Autonomía máx." valor={`${seleccionado.horas_vuelo} min`} ultimo />
            </dl>
          ) : (
            <p className="font-mono text-xs text-ink-faint">
              Selecciona un modelo para ver sus límites técnicos.
            </p>
          )}
        </div>
      </div>
    </>
  )
}

function Telemetria({ etiqueta, valor, ultimo = false }) {
  return (
    <div
      className={cn(
        'flex items-baseline justify-between',
        !ultimo && 'border-b border-line-soft pb-2.5',
      )}
    >
      <dt className="text-xs text-ink-dim">{etiqueta}</dt>
      <dd className="text-[13px]">{valor}</dd>
    </div>
  )
}
