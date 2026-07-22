import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from 'node:path'

// Tailwind v4 se integra como plugin de Vite; ya no se usa PostCSS ni
// tailwind.config.js: el tema vive en src/styles/theme.css con @theme.
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': path.resolve(process.cwd(), './src'),
    },
  },
  server: {
    // El backend permite CORS exactamente desde este origen (SecurityConfig).
    port: 5173,
    strictPort: true,
  },
})
