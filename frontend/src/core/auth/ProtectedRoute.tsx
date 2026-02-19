import React from 'react';
import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../../store/useAuthStore';

interface ProtectedRouteProps {
    allowedRoles?: string[];
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ allowedRoles }) => {
    const { accessToken, user } = useAuthStore();
    const location = useLocation();

    if (!accessToken) {
        // Redirect to login but save the current location they were trying to access
        return <Navigate to="/login" state={{ from: location }} replace />;
    }

    if (allowedRoles && user && !user.roles.some((role) => allowedRoles.includes(role))) {
        // Role not authorized - redirect to unauthorized or home
        return <Navigate to="/unauthorized" replace />;
    }

    return <Outlet />;
};
