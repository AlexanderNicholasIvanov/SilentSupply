import { Routes, Route } from 'react-router-dom'
import { WebSocketProvider } from './contexts/WebSocketContext'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import DashboardPage from './pages/DashboardPage'
import CatalogPage from './pages/CatalogPage'
import ProductDetailPage from './pages/ProductDetailPage'
import MyProductsPage from './pages/MyProductsPage'
import ProductFormPage from './pages/ProductFormPage'
import OrdersPage from './pages/OrdersPage'
import OrderDetailPage from './pages/OrderDetailPage'
import MessagesPage from './pages/MessagesPage'
import ConversationPage from './pages/ConversationPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route
        element={
          <ProtectedRoute>
            <WebSocketProvider>
              <Layout />
            </WebSocketProvider>
          </ProtectedRoute>
        }
      >
        <Route path="/" element={<DashboardPage />} />
        <Route path="/catalog" element={<CatalogPage />} />
        <Route path="/catalog/:id" element={<ProductDetailPage />} />
        <Route path="/orders" element={<OrdersPage />} />
        <Route path="/orders/:id" element={<OrderDetailPage />} />
        <Route path="/my-products" element={<MyProductsPage />} />
        <Route path="/my-products/new" element={<ProductFormPage />} />
        <Route path="/my-products/:id/edit" element={<ProductFormPage />} />
        <Route path="/messages" element={<MessagesPage />} />
        <Route path="/messages/:id/*" element={<ConversationPage />} />
      </Route>
    </Routes>
  )
}
