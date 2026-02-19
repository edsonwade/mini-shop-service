import React from 'react';
import { useProducts } from '../hooks/useProducts';
import { Package, AlertCircle, ShoppingCart } from 'lucide-react';

export const ProductGrid: React.FC = () => {
    const { data: products, isLoading, isError, error } = useProducts();

    if (isLoading) {
        return (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 animate-pulse">
                {[1, 2, 3, 4, 5, 6, 7, 8].map((i) => (
                    <div key={i} className="h-64 bg-slate-100 dark:bg-slate-900 rounded-xl" />
                ))}
            </div>
        );
    }

    if (isError) {
        return (
            <div className="flex items-center p-4 text-red-500 bg-red-50 dark:bg-red-900/10 rounded-md border border-red-200">
                <AlertCircle className="w-5 h-5 mr-3" />
                Error loading products: {(error as any).message}
            </div>
        );
    }

    return (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {products?.map((product) => (
                <div
                    key={product.id}
                    className="group relative bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl p-5 hover:shadow-lg transition-all"
                >
                    <div className="aspect-square bg-slate-50 dark:bg-slate-800 rounded-lg flex items-center justify-center mb-4 group-hover:bg-primary/10 transition-colors">
                        <Package className="w-12 h-12 text-slate-300 dark:text-slate-700 group-hover:text-primary transition-colors" />
                    </div>

                    <div className="space-y-1">
                        <h3 className="font-semibold text-slate-900 dark:text-white truncate">{product.name}</h3>
                        <p className="text-sm text-slate-500 dark:text-slate-400 line-clamp-2 min-h-[40px]">
                            {product.description}
                        </p>
                    </div>

                    <div className="mt-4 flex items-center justify-between">
                        <div className="space-y-0.5">
                            <span className="text-xl font-bold text-slate-900 dark:text-white">
                                ${product.price.toFixed(2)}
                            </span>
                            <div className="text-xs text-slate-400">
                                {product.stock > 0 ? (
                                    <span className="text-emerald-500 font-medium">In Stock ({product.stock})</span>
                                ) : (
                                    <span className="text-rose-500 font-medium">Out of Stock</span>
                                )}
                            </div>
                        </div>

                        <button className="p-2 bg-slate-100 dark:bg-slate-800 rounded-lg hover:bg-primary hover:text-white transition-colors">
                            <ShoppingCart className="w-5 h-5" />
                        </button>
                    </div>
                </div>
            ))}

            {products?.length === 0 && (
                <div className="col-span-full py-20 bg-slate-50 dark:bg-slate-900/50 rounded-2xl border border-dashed border-slate-200 dark:border-slate-800 flex flex-col items-center justify-center text-center">
                    <Package className="w-16 h-16 text-slate-300 mb-4" />
                    <h3 className="text-lg font-medium dark:text-white">No products yet</h3>
                    <p className="text-slate-500 mt-1">Start by adding your first product to the catalog.</p>
                </div>
            )}
        </div>
    );
};
