import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth/AuthContext.jsx'
import { RutaProtegida } from './auth/RutaProtegida.jsx'
import { AppShell } from './components/AppShell.jsx'
import { AdminCatalogo } from './pages/AdminCatalogo.jsx'
import { Cotizacion } from './pages/Cotizacion.jsx'
import { Factura } from './pages/Factura.jsx'
import { Login } from './pages/Login.jsx'
import { Registro } from './pages/Registro.jsx'
import { SeleccionTipo } from './pages/SeleccionTipo.jsx'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Rutas />
      </BrowserRouter>
    </AuthProvider>
  )
}

function Rutas() {
  const { estaAutenticado, esAdministrador } = useAuth()

  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/registro" element={<Registro />} />

      {/* Todo lo que cuelga de aquí exige sesión y comparte cabecera y
          barra lateral. */}
      <Route
        element={
          <RutaProtegida>
            <AppShell />
          </RutaProtegida>
        }
      >
        <Route path="/tipos" element={<SeleccionTipo />} />
        <Route path="/cotizar/:slug" element={<Cotizacion />} />
        <Route path="/factura" element={<Factura />} />
        <Route
          path="/admin"
          element={
            <RutaProtegida soloAdmin>
              <AdminCatalogo />
            </RutaProtegida>
          }
        />
      </Route>

      {/* Raíz y rutas desconocidas: al sitio que corresponda según la sesión. */}
      <Route
        path="*"
        element={
          <Navigate to={!estaAutenticado ? '/login' : esAdministrador ? '/admin' : '/tipos'} replace />
        }
      />
    </Routes>
  )
}
