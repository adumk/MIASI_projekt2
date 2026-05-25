import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import AppLayout from './components/AppLayout';
import PrivateRoute from './components/PrivateRoute';
import { AuthProvider, useAuth } from './context/AuthContext';
import AdminDashboardPage from './pages/admin/AdminDashboardPage';
import AdminFleetPage from './pages/admin/AdminFleetPage';
import AdminMaintenancePage from './pages/admin/AdminMaintenancePage';
import AdminTariffsPage from './pages/admin/AdminTariffsPage';
import EmployeeFleetPage from './pages/employee/EmployeeFleetPage';
import EmployeeDeskPage from './pages/employee/EmployeeDeskPage';
import FleetPage from './pages/FleetPage';
import LoginPage from './pages/LoginPage';
import ProfilePage from './pages/ProfilePage';
import RegisterPage from './pages/RegisterPage';
import RentalsPage from './pages/RentalsPage';

function HomeRedirect() {
  const { isAuthenticated, isLoading, homePath } = useAuth();
  if (isLoading) return null;
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  return <Navigate to={homePath} replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route element={<AppLayout />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/home" element={<HomeRedirect />} />

            <Route element={<PrivateRoute roles={['CUSTOMER']} />}>
              <Route index element={<FleetPage />} />
              <Route path="/rentals" element={<RentalsPage />} />
              <Route path="/profile" element={<ProfilePage />} />
            </Route>

            <Route element={<PrivateRoute roles={['EMPLOYEE']} />}>
              <Route path="/employee" element={<EmployeeDeskPage />} />
              <Route path="/employee/fleet" element={<EmployeeFleetPage />} />
            </Route>

            <Route element={<PrivateRoute roles={['ADMIN']} />}>
              <Route path="/admin" element={<AdminDashboardPage />} />
              <Route path="/admin/fleet" element={<AdminFleetPage />} />
              <Route path="/admin/tariffs" element={<AdminTariffsPage />} />
              <Route path="/admin/maintenance" element={<AdminMaintenancePage />} />
            </Route>

            <Route path="*" element={<HomeRedirect />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
