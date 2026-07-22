import { useCallback, useEffect, useState } from 'react'
import { actualizarDron, crearDron, eliminarDron, obtenerDrones } from '../api/apiClient.js'
import { TIPOS_DRON } from '../api/tiposDron.js'
import {
  Alerta,
  Aviso,
  BadgeTipo,
  Boton,
  Campo,
  CampoConEtiqueta,
  Selector,
  dinero,
} from '../components/ui.jsx'
import { cn } from '../lib/utils.js'

const COLUMNAS = 'grid-cols-[1fr_1fr_1.3fr_1fr_1fr_1fr_0.9fr_1.1fr]'

const FORMULARIO_VACIO = {
  codigo: '',
  tipo: 'LIVIANO',
  modelo: '',
  distancia_km: '',
  peso_maximo: '',
  horas_vuelo: '',
}

export function AdminCatalogo() {
  const [catalogo, setCatalogo] = useState([])
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState('')
  const [aviso, setAviso] = useState('')

  const [formVisible, setFormVisible] = useState(false)
  const [editando, setEditando] = useState(null) // código en edición, o null si es alta
  const [form, setForm] = useState(FORMULARIO_VACIO)
  const [errorForm, setErrorForm] = useState('')
  const [guardando, setGuardando] = useState(false)

  const recargar = useCallback(async () => {
    setCargando(true)
    try {
      setCatalogo(await obtenerDrones())
      setError('')
    } catch (e) {
      setError(e.message)
    } finally {
      setCargando(false)
    }
  }, [])

  useEffect(() => {
    recargar()
  }, [recargar])

  const abrirAlta = () => {
    setForm(FORMULARIO_VACIO)
    setEditando(null)
    setErrorForm('')
    setFormVisible(true)
  }

  const abrirEdicion = (dron) => {
    setForm({
      codigo: dron.codigo,
      tipo: dron.tipo,
      modelo: dron.modelo,
      distancia_km: String(dron.distancia_km ?? ''),
      peso_maximo: String(dron.peso_maximo ?? ''),
      horas_vuelo: String(dron.horas_vuelo ?? ''),
    })
    setEditando(dron.codigo)
    setErrorForm('')
    setFormVisible(true)
  }

  const cerrarForm = () => {
    setFormVisible(false)
    setErrorForm('')
  }

  const cambiar = (campo) => (e) => setForm((f) => ({ ...f, [campo]: e.target.value }))

  const guardar = async (evento) => {
    evento.preventDefault()
    setErrorForm('')

    if (!form.codigo.trim() || !form.modelo.trim()) {
      setErrorForm('Código y modelo son obligatorios.')
      return
    }

    const numeros = {
      distancia_km: Number(form.distancia_km),
      peso_maximo: Number(form.peso_maximo),
      horas_vuelo: Number(form.horas_vuelo),
    }
    if (Object.values(numeros).some((v) => !Number.isFinite(v) || v <= 0)) {
      setErrorForm('Completa los valores numéricos con cifras mayores a 0.')
      return
    }

    const cuerpo = {
      codigo: form.codigo.trim(),
      modelo: form.modelo.trim(),
      ...numeros,
    }

    setGuardando(true)
    try {
      if (editando) {
        // El tipo va el de la fila, no el del formulario: ver nota en el <select>.
        await actualizarDron(form.tipo, editando, cuerpo)
        setAviso(`Dron ${editando} actualizado.`)
      } else {
        await crearDron(form.tipo, cuerpo)
        setAviso(`Dron ${cuerpo.codigo} agregado.`)
      }
      setFormVisible(false)
      await recargar()
    } catch (e) {
      // Mensaje real del backend: límites de la subclase, tipo inválido, o 403.
      setErrorForm(e.message)
    } finally {
      setGuardando(false)
    }
  }

  const borrar = async (dron) => {
    // Acción destructiva: se confirma antes de llamar a la API.
    const seguro = window.confirm(
      `¿Eliminar el dron ${dron.codigo} (${dron.modelo})?\nEsta acción no se puede deshacer.`,
    )
    if (!seguro) return

    try {
      await eliminarDron(dron.codigo)
      setAviso(`Dron ${dron.codigo} eliminado.`)
      if (editando === dron.codigo) setFormVisible(false)
      await recargar()
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-[26px] font-bold">Catálogo de drones</h1>
          <p className="mt-1 text-sm text-ink-muted">
            Administra los modelos disponibles para cotización.
          </p>
        </div>
        <Boton variante="admin" onClick={abrirAlta} className="whitespace-nowrap py-2.5 text-xs">
          + Nuevo dron
        </Boton>
      </div>

      <div className="mb-4 flex flex-col gap-2 empty:mb-0">
        <Alerta>{error}</Alerta>
        {!error && <Aviso>{aviso}</Aviso>}
      </div>

      <div className="flex items-start gap-6">
        <div className="flex-1 overflow-hidden rounded-lg border border-line-mid bg-surface">
          <div
            className={cn(
              'grid gap-3.5 border-b border-line bg-sunken px-5 py-3',
              'font-mono text-[11px] uppercase tracking-[0.06em] text-ink-dim',
              COLUMNAS,
            )}
          >
            <div>Código</div>
            <div>Tipo</div>
            <div>Modelo</div>
            <div>Dist. máx (km)</div>
            <div>Peso máx (kg)</div>
            <div>Vuelo (min)</div>
            <div>Tarifa</div>
            <div>Acciones</div>
          </div>

          {cargando && (
            <div className="px-5 py-8 text-center font-mono text-xs text-ink-faint">
              Cargando catálogo…
            </div>
          )}

          {!cargando && catalogo.length === 0 && (
            <div className="px-5 py-8 text-center font-mono text-xs text-ink-faint">
              No hay drones en el catálogo todavía.
            </div>
          )}

          {catalogo.map((d) => (
            <div
              key={d.codigo}
              className={cn(
                'grid items-center gap-3.5 border-b border-line-row px-5 py-3.5 text-[13px]',
                COLUMNAS,
              )}
            >
              <div className="font-mono">{d.codigo}</div>
              <div>
                <BadgeTipo tipo={d.tipo} />
              </div>
              <div>{d.modelo}</div>
              <div className="font-mono">{d.distancia_km}</div>
              <div className="font-mono">{d.peso_maximo}</div>
              <div className="font-mono">{d.horas_vuelo}</div>
              <div className="font-mono">{dinero(d.costo_base)}</div>
              <div className="flex gap-2.5 font-mono text-xs">
                <button
                  type="button"
                  onClick={() => abrirEdicion(d)}
                  className="cursor-pointer text-cyan hover:brightness-110"
                >
                  Editar
                </button>
                <button
                  type="button"
                  onClick={() => borrar(d)}
                  className="cursor-pointer text-danger hover:brightness-110"
                >
                  Eliminar
                </button>
              </div>
            </div>
          ))}
        </div>

        {formVisible && (
          <form
            onSubmit={guardar}
            className="w-[340px] shrink-0 rounded-lg border border-line-mid border-l-[3px] border-l-amber bg-surface p-6"
          >
            <div className="mb-4.5 text-base font-bold">
              {editando ? 'Editar dron' : 'Nuevo dron'}
            </div>

            <div className="mb-3.5 empty:mb-0">
              <Alerta>{errorForm}</Alerta>
            </div>

            <div className="flex flex-col gap-3.5">
              <CampoConEtiqueta etiqueta="Código" id="f-codigo">
                <Campo
                  id="f-codigo"
                  value={form.codigo}
                  onChange={cambiar('codigo')}
                  disabled={Boolean(editando)}
                />
              </CampoConEtiqueta>

              <CampoConEtiqueta etiqueta="Tipo" id="f-tipo">
                {/* Al editar queda bloqueado: el tipo es el discriminador de la
                    fila en la herencia SINGLE_TABLE, igual que el código es su
                    clave primaria. Cambiarlo en un PUT hace que Hibernate falle
                    con DuplicateKeyException y la API devuelva 500. Para cambiar
                    de tipo hay que eliminar el dron y crearlo de nuevo. */}
                <Selector
                  id="f-tipo"
                  value={form.tipo}
                  onChange={cambiar('tipo')}
                  disabled={Boolean(editando)}
                >
                  {TIPOS_DRON.map((t) => (
                    <option key={t.tipo} value={t.tipo}>
                      {t.tipo}
                    </option>
                  ))}
                </Selector>
              </CampoConEtiqueta>

              <CampoConEtiqueta etiqueta="Modelo" id="f-modelo">
                <Campo id="f-modelo" value={form.modelo} onChange={cambiar('modelo')} />
              </CampoConEtiqueta>

              <CampoConEtiqueta etiqueta="Distancia máxima (km)" id="f-dist">
                <Campo
                  id="f-dist"
                  type="number"
                  step="any"
                  value={form.distancia_km}
                  onChange={cambiar('distancia_km')}
                />
              </CampoConEtiqueta>

              <CampoConEtiqueta etiqueta="Peso máximo (kg)" id="f-peso">
                <Campo
                  id="f-peso"
                  type="number"
                  step="any"
                  value={form.peso_maximo}
                  onChange={cambiar('peso_maximo')}
                />
              </CampoConEtiqueta>

              <CampoConEtiqueta etiqueta="Horas de vuelo (min)" id="f-horas">
                <Campo
                  id="f-horas"
                  type="number"
                  step="any"
                  value={form.horas_vuelo}
                  onChange={cambiar('horas_vuelo')}
                />
              </CampoConEtiqueta>

              {/* El diseño incluía un campo "Costo base ($/km)", pero DronRequest
                  no lo acepta: el backend lo fija en el constructor de cada
                  subclase (0.50 / 6.00 / 15.00). Mostrarlo editable sería
                  ofrecer un control que no hace nada. Se muestra en la tabla,
                  que sí lo devuelve GET /dron. */}

              <div className="mt-2 flex gap-2.5">
                <Boton variante="contorno" onClick={cerrarForm} className="flex-1 py-2.5 text-xs">
                  Cancelar
                </Boton>
                <Boton
                  variante="admin"
                  type="submit"
                  className="flex-1 py-2.5 text-xs"
                  disabled={guardando}
                >
                  {guardando ? 'Guardando…' : 'Guardar'}
                </Boton>
              </div>
            </div>
          </form>
        )}
      </div>
    </>
  )
}
