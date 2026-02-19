import React from 'react';
import { useOrderHistory, useCancelOrder, useSettleOrder } from '../hooks/useOrders';
import { ShoppingBag, CheckCircle2, XCircle, Loader2 } from 'lucide-react';

export const OrderTable: React.FC = () => {
    const { data: orders, isLoading } = useOrderHistory();
    const cancelOrder = useCancelOrder();
    const settleOrder = useSettleOrder();

    if (isLoading) {
        return (
            <div className="flex flex-col items-center justify-center p-12 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800">
                <Loader2 className="w-8 h-8 text-primary animate-spin mb-4" />
                <p className="text-slate-500">Loading orders...</p>
            </div>
        );
    }

    if (!orders || orders.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center p-12 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 text-center">
                <div className="w-16 h-16 bg-slate-100 dark:bg-slate-800 rounded-full flex items-center justify-center mb-4">
                    <ShoppingBag className="w-8 h-8 text-slate-400" />
                </div>
                <h3 className="font-semibold text-lg dark:text-white">No orders yet</h3>
                <p className="text-slate-500 max-w-xs mt-2">Start your marketplace by creating your first customer order.</p>
            </div>
        );
    }

    return (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl overflow-hidden shadow-sm">
            <div className="px-6 py-4 border-b border-slate-200 dark:border-slate-800 flex justify-between items-center">
                <h3 className="font-semibold dark:text-white">Order History</h3>
                <span className="text-xs px-2 py-1 bg-slate-100 dark:bg-slate-800 text-slate-500 rounded-md font-medium">
                    {orders.length} Total
                </span>
            </div>
            <div className="overflow-x-auto">
                <table className="w-full text-sm text-left">
                    <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 uppercase text-xs font-semibold">
                        <tr>
                            <th className="px-6 py-3 whitespace-nowrap">Order ID</th>
                            <th className="px-6 py-3">Customer</th>
                            <th className="px-6 py-3">Status</th>
                            <th className="px-6 py-4">Items</th>
                            <th className="px-6 py-3 text-right">Actions</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                        {orders.map((order) => (
                            <tr key={order.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                                <td className="px-6 py-4 font-mono text-xs dark:text-slate-300 whitespace-nowrap">
                                    {order.id.substring(0, 13)}...
                                </td>
                                <td className="px-6 py-4 dark:text-white font-medium">
                                    {order.customerId}
                                </td>
                                <td className="px-6 py-4">
                                    <div className="flex items-center">
                                        <div className={`w-2 h-2 rounded-full mr-2 ${order.status === 'COMPLETED' ? 'bg-emerald-500' :
                                            order.status === 'CANCELLED' ? 'bg-rose-500' : 'bg-amber-500'
                                            }`} />
                                        <span className="capitalize dark:text-slate-300">{order.status.toLowerCase()}</span>
                                    </div>
                                </td>
                                <td className="px-6 py-4 text-slate-500">
                                    {order.items.length} product(s)
                                </td>
                                <td className="px-6 py-4 text-right">
                                    <div className="flex justify-end space-x-2">
                                        <button
                                            onClick={() => settleOrder.mutate(order.id)}
                                            disabled={order.status !== 'PENDING' || settleOrder.isPending}
                                            className="p-1.5 text-emerald-600 hover:bg-emerald-50 dark:hover:bg-emerald-900/20 rounded-md transition-colors disabled:opacity-30"
                                            title="Settle Order"
                                        >
                                            <CheckCircle2 className="w-4 h-4" />
                                        </button>
                                        <button
                                            onClick={() => cancelOrder.mutate(order.id)}
                                            disabled={order.status !== 'PENDING' || cancelOrder.isPending}
                                            className="p-1.5 text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-900/20 rounded-md transition-colors disabled:opacity-30"
                                            title="Cancel Order"
                                        >
                                            <XCircle className="w-4 h-4" />
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};
