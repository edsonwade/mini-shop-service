import { Routes, Route, Navigate } from 'react-router-dom';
import { LoginForm } from './features/auth/components/LoginForm';
import { RegisterForm } from './features/auth/components/RegisterForm';
import { ProtectedRoute } from './core/auth/ProtectedRoute';
import { AppShell } from './components/layout/AppShell';
import {
  DashboardView,
  CustomersView,
  ProductsView,
  OrdersView,
  PaymentsView,
  CouponsView,
  UnauthorizedView
} from './features/dashboard/views/DashboardViews';

function App() {
  return (
    <Routes>
      {/* Public Routes */}
      <Route path="/login" element={
        <div className="flex items-center justify-center min-h-screen bg-slate-100 dark:bg-slate-950 px-4">
          <LoginForm />
        </div>
      } />
      <Route path="/register" element={
        <div className="flex items-center justify-center min-h-screen bg-slate-100 dark:bg-slate-950 px-4">
          <RegisterForm />
        </div>
      } />
      <Route path="/unauthorized" element={<UnauthorizedView />} />

      {/* Protected SaaS Application Routes */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppShell />}>
          <Route index element={<DashboardView />} />
          <Route path="customers" element={<CustomersView />} />
          <Route path="products" element={<ProductsView />} />
          <Route path="orders" element={<OrdersView />} />
          <Route path="payments" element={<PaymentsView />} />
          <Route path="coupons" element={<CouponsView />} />
        </Route>
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
