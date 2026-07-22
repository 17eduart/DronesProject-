import { clsx } from 'clsx'
import { twMerge } from 'tailwind-merge'

/**
 * Une clases condicionales resolviendo conflictos de Tailwind (la última gana).
 * Es el helper estándar de shadcn/ui, y lo usan también los componentes
 * propios de este proyecto.
 */
export function cn(...inputs) {
  return twMerge(clsx(inputs))
}
