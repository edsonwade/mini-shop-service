import React from 'react';
import { useCustomers, useVerifyKYC } from '../hooks/useCustomers';
import { CheckCircle, XCircle, Loader2, UserCheck } from 'lucide-react';

export const CustomerTable: React.FC = () => {
    const { data: customers, isLoading, isError, error } = useCustomers();
    const { mutate: verifyKYC, isPending: isVerifying } = useVerifyKYC();

    if (isLoading) {
        return (
            <div className="space-y-4 animate-pulse">
                <div className="h-10 bg-slate-200 dark:bg-slate-800 rounded-md w-full" />
                {[1, 2, 3, 4, 5].map((i) => (
                    <div key={i} className="h-16 bg-slate-100 dark:bg-slate-900 rounded-md w-full" />
                ))}
            </div>
        );
    }

    if (isError) {
        return (
            <div className="p-4 text-red-500 bg-red-50 dark:bg-red-900/10 rounded-md border border-red-200 dark:border-red-800">
                Error loading customers: {(error as any).message}
            </div>
        );
    }

    return (
        <div className="overflow-x-auto bg-white border rounded-lg dark:bg-slate-900 dark:border-slate-800">
            <table className="w-full text-sm text-left border-collapse">
                <thead className="bg-slate-50 dark:bg-slate-800/50 text-slate-500 dark:text-slate-400 font-medium">
                    <tr>
                        <th className="px-6 py-4 border-b dark:border-slate-800">Name</th>
                        <th className="px-6 py-4 border-b dark:border-slate-800">Email</th>
                        <th className="px-6 py-4 border-b dark:border-slate-800">Status</th>
                        <th className="px-6 py-4 border-b dark:border-slate-800">Tenant</th>
                        <th className="px-6 py-4 border-b dark:border-slate-800 text-right">Actions</th>
                    </tr>
                </thead>
                <tbody className="divide-y divide-slate-200 dark:divide-slate-800">
                    {customers?.map((customer) => (
                        <tr key={customer.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/30 transition-colors">
                            <td className="px-6 py-4 dark:text-white font-medium">{customer.name}</td>
                            <td className="px-6 py-4 text-slate-600 dark:text-slate-400">{customer.email}</td>
                            <td className="px-6 py-4">
                                {customer.kycVerified ? (
                                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-emerald-100 text-emerald-800 dark:bg-emerald-900/30 dark:text-emerald-400">
                                        <CheckCircle className="w-3 h-3 mr-1" />
                                        Verified
                                    </span>
                                ) : (
                                    <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-amber-100 text-amber-800 dark:bg-amber-900/30 dark:text-amber-400">
                                        <XCircle className="w-3 h-3 mr-1" />
                                        Pending KYC
                                    </span>
                                )}
                            </td>
                            <td className="px-6 py-4 text-slate-500 text-xs font-mono">{customer.tenantId}</td>
                            <td className="px-6 py-4 text-right">
                                {!customer.kycVerified && (
                                    <button
                                        onClick={() => verifyKYC(customer.id)}
                                        disabled={isVerifying}
                                        className="inline-flex items-center px-3 py-1 text-xs font-semibold text-primary border border-primary rounded-md hover:bg-primary hover:text-white transition-all disabled:opacity-50"
                                    >
                                        {isVerifying ? (
                                            <Loader2 className="w-3 h-3 mr-1 animate-spin" />
                                        ) : (
                                            <UserCheck className="w-3 h-3 mr-1" />
                                        )}
                                        Verify KYC
                                    </button>
                                )}
                            </td>
                        </tr>
                    ))}
                    {customers?.length === 0 && (
                        <tr>
                            <td colSpan={5} className="px-6 py-12 text-center text-slate-500">
                                No customers found.
                            </td>
                        </tr>
                    )}
                </tbody>
            </table>
        </div>
    );
};
