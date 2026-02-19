import { Ticket, Percent, Calendar, Check, Plus } from 'lucide-react';

export const PromotionTable: React.FC = () => {
    // Promotion service currently only has 'createCoupon'. For the scale to 10 UI, 
    // we'll implement a clean empty state with a "Create First Coupon" CTA.

    return (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl overflow-hidden shadow-sm">
            <div className="px-6 py-4 border-b border-slate-200 dark:border-slate-800 flex justify-between items-center bg-slate-50/50 dark:bg-slate-800/20">
                <div className="flex items-center">
                    <Ticket className="w-5 h-5 text-indigo-500 mr-2" />
                    <h3 className="font-semibold dark:text-white">Marketing Coupons</h3>
                </div>
                <button className="flex items-center px-3 py-1.5 text-xs font-medium bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition-colors">
                    <Plus className="w-4 h-4 mr-1" />
                    New Coupon
                </button>
            </div>

            <div className="flex flex-col items-center justify-center p-16 text-center">
                <div className="w-20 h-20 bg-indigo-50 dark:bg-indigo-900/20 rounded-2xl flex items-center justify-center mb-6 rotate-3">
                    <Percent className="w-10 h-10 text-indigo-600 dark:text-indigo-400" />
                </div>
                <h3 className="text-xl font-bold dark:text-white">Boost your sales</h3>
                <p className="text-slate-500 max-w-sm mt-3 mb-8">
                    Create promotional codes to attract more customers and increase your loyalty program engagement.
                </p>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 w-full max-w-lg">
                    <div className="p-4 rounded-xl border border-dashed border-slate-200 dark:border-slate-800 flex items-start text-left">
                        <div className="mt-1 p-2 bg-emerald-100 text-emerald-700 rounded-md mr-3">
                            <Check className="w-4 h-4" />
                        </div>
                        <div>
                            <p className="text-sm font-semibold dark:text-white">Active Coupons</p>
                            <p className="text-xs text-slate-500">Track performance of live codes.</p>
                        </div>
                    </div>
                    <div className="p-4 rounded-xl border border-dashed border-slate-200 dark:border-slate-800 flex items-start text-left">
                        <div className="mt-1 p-2 bg-slate-100 text-slate-700 rounded-md mr-3">
                            <Calendar className="w-4 h-4" />
                        </div>
                        <div>
                            <p className="text-sm font-semibold dark:text-white">Scheduled</p>
                            <p className="text-xs text-slate-500">Auto-start future campaigns.</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};
