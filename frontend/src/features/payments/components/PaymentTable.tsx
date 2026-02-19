import React from 'react';
import { usePayments } from '../hooks/usePayments';
import { CreditCard, CheckCircle2, RefreshCcw, Landmark, Loader2 } from 'lucide-react';

export const PaymentTable: React.FC = () => {
    const { data: payments, isLoading } = usePayments();

    if (isLoading) {
        return (
            <div className="flex flex-col items-center justify-center p-12 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800">
                <Loader2 className="w-8 h-8 text-primary animate-spin mb-4" />
                <p className="text-slate-500">Loading transactions...</p>
            </div>
        );
    }

    if (!payments || payments.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center p-12 bg-white dark:bg-slate-900 rounded-xl border border-slate-200 dark:border-slate-800 text-center">
                <div className="w-16 h-16 bg-slate-100 dark:bg-slate-800 rounded-full flex items-center justify-center mb-4">
                    <Landmark className="w-8 h-8 text-slate-400" />
                </div>
                <h3 className="font-semibold text-lg dark:text-white">No transactions</h3>
                <p className="text-slate-500 max-w-xs mt-2">Payments will appear here once orders are processed.</p>
            </div>
        );
    }

    return (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl overflow-hidden shadow-sm">
            <div className="px-6 py-4 border-b border-slate-200 dark:border-slate-800 flex justify-between items-center bg-slate-50/50 dark:bg-slate-800/20">
                <div className="flex items-center">
                    <CreditCard className="w-5 h-5 text-primary mr-2" />
                    <h3 className="font-semibold dark:text-white">Financial Transactions</h3>
                </div>
            </div>
            <div className="overflow-x-auto">
                <table className="w-full text-sm text-left">
                    <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 uppercase text-xs font-semibold">
                        <tr>
                            <th className="px-6 py-3">Transaction ID</th>
                            <th className="px-6 py-3">Order ID</th>
                            <th className="px-6 py-3">Amount</th>
                            <th className="px-6 py-3">Status</th>
                            <th className="px-6 py-3 text-right">Method</th>
                        </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 dark:divide-slate-800">
                        {payments.map((payment) => (
                            <tr key={payment.id} className="hover:bg-slate-50 dark:hover:bg-slate-800/50 transition-colors">
                                <td className="px-6 py-4 font-mono text-xs text-slate-500">
                                    {payment.id.substring(0, 13)}
                                </td>
                                <td className="px-6 py-4 font-mono text-xs dark:text-slate-300">
                                    {payment.orderId.substring(0, 13)}
                                </td>
                                <td className="px-6 py-4 font-semibold dark:text-white">
                                    {new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(payment.amount)}
                                </td>
                                <td className="px-6 py-4">
                                    <div className="flex items-center text-xs font-medium">
                                        {payment.status === 'COMPLETED' ? (
                                            <span className="inline-flex items-center px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400">
                                                <CheckCircle2 className="w-3 h-3 mr-1" />
                                                Settled
                                            </span>
                                        ) : (
                                            <span className="inline-flex items-center px-2 py-0.5 rounded-full bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-400">
                                                <RefreshCcw className="w-3 h-3 mr-1 animate-spin-slow" />
                                                Processing
                                            </span>
                                        )}
                                    </div>
                                </td>
                                <td className="px-6 py-4 text-right text-slate-500 font-medium uppercase text-[10px] tracking-wider">
                                    Credit Card
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};
