import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Modal } from '../../../components/ui/Modal';
import { useCreateProduct } from '../hooks/useProducts';
import { useNotificationStore } from '../../../store/useNotificationStore';
import { Loader2, Package, Tag, DollarSign, ListOrdered } from 'lucide-react';

const productSchema = z.object({
    name: z.string().min(2, 'Name is required'),
    description: z.string().min(5, 'Description is required'),
    price: z.number().min(0.01, 'Price must be greater than 0'),
    sku: z.string().min(3, 'SKU is required'),
    stock: z.number().int().min(0, 'Stock cannot be negative'),
});

type ProductSchema = z.infer<typeof productSchema>;

interface CreateProductModalProps {
    isOpen: boolean;
    onClose: () => void;
}

export const CreateProductModal: React.FC<CreateProductModalProps> = ({ isOpen, onClose }) => {
    const createProduct = useCreateProduct();
    const addNotification = useNotificationStore((state) => state.addNotification);

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm<ProductSchema>({
        resolver: zodResolver(productSchema),
        defaultValues: { stock: 0 },
    });

    const onSubmit = async (data: ProductSchema) => {
        try {
            await createProduct.mutateAsync(data);
            addNotification('Product created successfully!', 'success');
            reset();
            onClose();
        } catch (error: any) {
            addNotification(error.message || 'Failed to create product', 'error');
        }
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title="Add New Product"
            description="List a new product in your SaaS catalog."
        >
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                    <div className="col-span-2">
                        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">Name</label>
                        <div className="relative">
                            <Package className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                            <input
                                {...register('name')}
                                className="w-full pl-10 pr-4 py-2 border rounded-lg dark:bg-slate-950 dark:border-slate-800 dark:text-white"
                                placeholder="Premium Widget"
                            />
                        </div>
                        {errors.name && <p className="mt-1 text-xs text-rose-500">{errors.name.message}</p>}
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">Price</label>
                        <div className="relative">
                            <DollarSign className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                            <input
                                {...register('price', { valueAsNumber: true })}
                                type="number"
                                step="0.01"
                                className="w-full pl-10 pr-4 py-2 border rounded-lg dark:bg-slate-950 dark:border-slate-800 dark:text-white"
                                placeholder="49.99"
                            />
                        </div>
                        {errors.price && <p className="mt-1 text-xs text-rose-500">{errors.price.message}</p>}
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">SKU</label>
                        <div className="relative">
                            <Tag className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                            <input
                                {...register('sku')}
                                className="w-full pl-10 pr-4 py-2 border rounded-lg dark:bg-slate-950 dark:border-slate-800 dark:text-white"
                                placeholder="WIDG-001"
                            />
                        </div>
                        {errors.sku && <p className="mt-1 text-xs text-rose-500">{errors.sku.message}</p>}
                    </div>
                </div>

                <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">Stock Quantity</label>
                    <div className="relative">
                        <ListOrdered className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                        <input
                            {...register('stock', { valueAsNumber: true })}
                            type="number"
                            className="w-full pl-10 pr-4 py-2 border rounded-lg dark:bg-slate-950 dark:border-slate-800 dark:text-white"
                            placeholder="100"
                        />
                    </div>
                    {errors.stock && <p className="mt-1 text-xs text-rose-500">{errors.stock.message}</p>}
                </div>

                <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1">Description</label>
                    <textarea
                        {...register('description')}
                        rows={3}
                        className="w-full px-4 py-2 border rounded-lg dark:bg-slate-950 dark:border-slate-800 dark:text-white"
                        placeholder="High-quality widget for..."
                    />
                    {errors.description && <p className="mt-1 text-xs text-rose-500">{errors.description.message}</p>}
                </div>

                <button
                    type="submit"
                    disabled={createProduct.isPending}
                    className="w-full flex items-center justify-center py-2.5 px-4 bg-primary text-white font-semibold rounded-lg hover:bg-primary/90 transition-all disabled:opacity-50"
                >
                    {createProduct.isPending ? <Loader2 className="w-5 h-5 animate-spin mr-2" /> : null}
                    Create Product
                </button>
            </form>
        </Modal>
    );
};
