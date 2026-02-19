import React from 'react';
import { CustomerTable } from '../../customers/components/CustomerTable';
import { ProductGrid } from '../../products/components/ProductGrid';
import { OrderTable } from '../../orders/components/OrderTable';
import { PaymentTable } from '../../payments/components/PaymentTable';
import { PromotionTable } from '../../promotions/components/PromotionTable';
import { CreateProductModal } from '../../products/components/CreateProductModal';
import { CreateCustomerModal } from '../../customers/components/CreateCustomerModal';
import { CreateOrderModal } from '../../orders/components/CreateOrderModal';

export const DashboardView: React.FC = () => (
    <div className="space-y-4">
        <h2 className="text-2xl font-bold dark:text-white">Overview</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="p-6 bg-white rounded-xl shadow-sm border border-slate-200 dark:bg-slate-900 dark:border-slate-800">
                <p className="text-sm font-medium text-slate-500">Total Sales</p>
                <p className="text-3xl font-bold dark:text-white">$24,500.00</p>
            </div>
            <div className="p-6 bg-white rounded-xl shadow-sm border border-slate-200 dark:bg-slate-900 dark:border-slate-800">
                <p className="text-sm font-medium text-slate-500">Active Customers</p>
                <p className="text-3xl font-bold dark:text-white">1,234</p>
            </div>
            <div className="p-6 bg-white rounded-xl shadow-sm border border-slate-200 dark:bg-slate-900 dark:border-slate-800">
                <p className="text-sm font-medium text-slate-500">Products Stock</p>
                <p className="text-3xl font-bold dark:text-white">567</p>
            </div>
        </div>
    </div>
);

export const CustomersView: React.FC = () => {
    const [isCreateModalOpen, setIsCreateModalOpen] = React.useState(false);

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-2xl font-bold dark:text-white">Customers Management</h2>
                <button
                    onClick={() => setIsCreateModalOpen(true)}
                    className="px-4 py-2 bg-slate-900 dark:bg-white dark:text-slate-900 text-white font-bold rounded-lg hover:opacity-90 transition-all"
                >
                    Add Customer
                </button>
            </div>
            <CustomerTable />
            <CreateCustomerModal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
            />
        </div>
    );
};

export const ProductsView: React.FC = () => {
    const [isCreateModalOpen, setIsCreateModalOpen] = React.useState(false);

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-2xl font-bold dark:text-white">Product Catalog</h2>
                <button
                    onClick={() => setIsCreateModalOpen(true)}
                    className="px-4 py-2 bg-primary text-white rounded-lg font-medium hover:bg-primary/90 transition-colors"
                >
                    Add Product
                </button>
            </div>
            <ProductGrid />
            <CreateProductModal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
            />
        </div>
    );
};

export const OrdersView: React.FC = () => {
    const [isCreateModalOpen, setIsCreateModalOpen] = React.useState(false);

    return (
        <div className="space-y-6">
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-2xl font-bold dark:text-white">Fulfillment Center</h2>
                    <p className="text-slate-500 text-sm">Manage and track all customer orders</p>
                </div>
                <button
                    onClick={() => setIsCreateModalOpen(true)}
                    className="px-4 py-2 bg-primary text-white rounded-lg font-medium hover:bg-primary/90 transition-colors"
                >
                    New Order
                </button>
            </div>
            <OrderTable />
            <CreateOrderModal
                isOpen={isCreateModalOpen}
                onClose={() => setIsCreateModalOpen(false)}
            />
        </div>
    );
};

export const PaymentsView: React.FC = () => (
    <div className="space-y-6">
        <div className="flex justify-between items-center">
            <div>
                <h2 className="text-2xl font-bold dark:text-white">Revenue & Settlements</h2>
                <p className="text-slate-500 text-sm">Monitor financial health and transaction history</p>
            </div>
        </div>
        <PaymentTable />
    </div>
);

export const CouponsView: React.FC = () => (
    <div className="space-y-6">
        <div className="flex justify-between items-center">
            <div>
                <h2 className="text-2xl font-bold dark:text-white">Growth & Promotions</h2>
                <p className="text-slate-500 text-sm">Create marketing campaigns and discount codes</p>
            </div>
        </div>
        <PromotionTable />
    </div>
);

export const UnauthorizedView: React.FC = () => (
    <div className="flex flex-col items-center justify-center min-h-[50vh] text-center">
        <h2 className="text-4xl font-bold text-red-500">403</h2>
        <p className="mt-2 text-xl font-semibold dark:text-white">Access Denied</p>
        <p className="mt-1 text-slate-500">You do not have permission to view this page.</p>
    </div>
);
