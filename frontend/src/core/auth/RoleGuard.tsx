import React from 'react';
import { useAuthStore } from '../../store/useAuthStore';

interface RoleGuardProps {
    allowedRoles: string[];
    children: React.ReactNode;
    fallback?: React.ReactNode;
}

export const RoleGuard: React.FC<RoleGuardProps> = ({ allowedRoles, children, fallback = null }) => {
    const { user } = useAuthStore();

    const hasRole = user && user.roles.some((role) => allowedRoles.includes(role));

    if (!hasRole) {
        return <>{fallback}</>;
    }

    return <>{children}</>;
};
