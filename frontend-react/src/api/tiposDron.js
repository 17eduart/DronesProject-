/**
 * Traducción entre lo que ve el usuario y lo que entiende el backend.
 *
 * OJO: la interfaz habla de "Carga pesada", pero DronServicio solo reconoce
 * los strings LIVIANO, CARGA y EMERGENCIA. Mandar "PESADO" hace que el switch
 * lance IllegalArgumentException y la API responda 400. Esa traducción vive
 * SOLO en este archivo; ninguna pantalla debe escribir el tipo a mano.
 */

export const TIPO_LIVIANO = 'LIVIANO'
export const TIPO_CARGA = 'CARGA'
export const TIPO_EMERGENCIA = 'EMERGENCIA'

/** Los tres tipos, en el orden en que se muestran en la selección. */
export const TIPOS_DRON = [
  {
    tipo: TIPO_LIVIANO,
    // "pesado" no aparece nunca como slug: la ruta del tipo de carga usa
    // "carga", que ya coincide con el valor del backend.
    slug: 'liviano',
    etiqueta: 'Carga liviana',
    descripcion: 'Paquetes pequeños y entregas rápidas de corta duración.',
    limites: '≤ 5 kg · ≤ 120 min',
    imagen: '/img/DroneLiviano.jpg',
    acento: 'cyan',
  },
  {
    tipo: TIPO_CARGA,
    slug: 'carga',
    etiqueta: 'Carga pesada',
    descripcion: 'Transporte de mayor volumen y distancias extendidas.',
    limites: '< 30 kg · ≤ 300 min',
    imagen: '/img/DronePesado.jpg',
    acento: 'amber',
  },
  {
    tipo: TIPO_EMERGENCIA,
    slug: 'emergencia',
    etiqueta: 'Emergencia',
    descripcion: 'Respuesta prioritaria para insumos médicos y rescate.',
    limites: '≤ 15 kg · ≤ 180 min',
    imagen: '/img/DroneEmergencia.png',
    acento: 'danger',
  },
]

export function tipoDesdeSlug(slug) {
  return TIPOS_DRON.find((t) => t.slug === slug) ?? null
}

export function tipoDesdeValor(tipo) {
  return TIPOS_DRON.find((t) => t.tipo === tipo) ?? null
}

/** Etiqueta legible de un tipo del backend ("CARGA" -> "Carga pesada"). */
export function etiquetaDeTipo(tipo) {
  return tipoDesdeValor(tipo)?.etiqueta ?? tipo ?? ''
}

/** Clases de acento por tipo, para no repetir condicionales en cada pantalla. */
export const ACENTOS = {
  cyan: {
    texto: 'text-cyan',
    borde: 'border-cyan',
    bordeTop: 'border-t-cyan',
    fondoSuave: 'bg-cyan/15',
    bordeSuave: 'border-cyan/40',
    sombra: 'shadow-[0_0_22px_var(--color-cyan)]/35',
  },
  amber: {
    texto: 'text-amber',
    borde: 'border-amber',
    bordeTop: 'border-t-amber',
    fondoSuave: 'bg-amber/15',
    bordeSuave: 'border-amber/40',
    sombra: 'shadow-[0_0_22px_var(--color-amber)]/35',
  },
  danger: {
    texto: 'text-danger',
    borde: 'border-danger',
    bordeTop: 'border-t-danger',
    fondoSuave: 'bg-danger/15',
    bordeSuave: 'border-danger/40',
    sombra: 'shadow-[0_0_22px_var(--color-danger)]/35',
  },
}

/** Acento asociado a un tipo del backend, con fallback seguro. */
export function acentoDeTipo(tipo) {
  return ACENTOS[tipoDesdeValor(tipo)?.acento ?? 'cyan']
}
