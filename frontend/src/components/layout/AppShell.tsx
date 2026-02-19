import React from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../store/useAuthStore';
import { LogOut, Package, Users, Home, ShoppingCart, CreditCard, Ticket } from 'lucide-react';

import { ToastContainer } from '../ui/Toast';

export const AppShell: React.FC = () => {
    const { user, logout } = useAuthStore();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <div className="flex h-screen bg-slate-50 dark:bg-slate-950">
            <ToastContainer />
            {/* Sidebar */}
            <aside className="w-64 bg-white border-r border-slate-200 dark:bg-slate-900 dark:border-slate-800">
                <div className="flex flex-col h-full">
                    <div className="flex items-center h-16 px-6 border-b border-slate-200 dark:border-slate-800">
                        <span className="text-xl font-bold text-primary">MiniMarket</span>
                    </div>

                    <nav className="flex-1 px-4 py-6 space-y-2 overflow-y-auto">
                        <Link to="/" className="flex items-center px-4 py-2 transition-colors rounded-md text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800">
                            <Home className="w-5 h-5 mr-3" />
                            Overview
                        </Link>
                        <Link to="/customers" className="flex items-center px-4 py-2 transition-colors rounded-md text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800">
                            <Users className="w-5 h-5 mr-3" />
                            Customers
                        </Link>
                        <Link to="/products" className="flex items-center px-4 py-2 transition-colors rounded-md text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800">
                            <Package className="w-5 h-5 mr-3" />
                            Products
                        </Link>
                        <Link to="/orders" className="flex items-center px-4 py-2 transition-colors rounded-md text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800">
                            <ShoppingCart className="w-5 h-5 mr-3" />
                            Orders
                        </Link>
                        <Link to="/payments" className="flex items-center px-4 py-2 transition-colors rounded-md text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800">
                            <CreditCard className="w-5 h-5 mr-3" />
                            Payments
                        </Link>
                        <Link to="/coupons" className="flex items-center px-4 py-2 transition-colors rounded-md text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800">
                            <Ticket className="w-5 h-5 mr-3" />
                            Promotions
                        </Link>
                    </nav>

                    <div className="p-4 border-t border-slate-200 dark:border-slate-800">
                        <div className="flex items-center px-4 py-2 mb-4">
                            <div className="flex items-center justify-center w-8 h-8 rounded-full bg-primary/20 text-primary">
                                {user?.email?.[0].toUpperCase()}
                            </div>
                            <div className="ml-3 overflow-hidden">
                                <p className="text-sm font-medium truncate dark:text-white">{user?.email}</p>
                                <p className="text-xs truncate text-slate-500">{user?.roles.join(', ')}</p>
                            </div>
                        </div>
                        <button
                            onClick={handleLogout}
                            className="flex items-center w-full px-4 py-2 text-sm text-red-500 transition-colors rounded-md hover:bg-red-50 dark:hover:bg-red-900/10"
                        >
                            <LogOut className="w-4 h-4 mr-3" />
                            Sign Out
                        </button>
                    </div>
                </div>
            </aside>

            {/* Main Content */}
            <main className="flex-1 overflow-auto">
                <header className="flex items-center justify-between h-16 px-8 bg-white border-b border-slate-200 dark:bg-slate-900 dark:border-slate-800">
                    <h1 className="text-lg font-semibold text-slate-900 dark:text-white">Dashboard</h1>
                </header>

                <div className="p-8">
                    <Outlet />
                </div>
            </main>
        </div>
    );
};
