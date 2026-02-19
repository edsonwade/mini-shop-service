import React from 'react';
import { useForm, useFieldArray } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Modal } from '../../../components/ui/Modal';
import { usePlaceOrder } from '../hooks/useOrders';
import { useProducts } from '../../products/hooks/useProducts';
import { useCustomers } from '../../customers/hooks/useCustomers';
import { useAuthStore } from '../../../store/useAuthStore';
import { useNotificationStore } from '../../../store/useNotificationStore';
import { Loader2, ShoppingCart, Plus, Trash2, User } from 'lucide-react';

const orderItemSchema = z.object({
    productId: z.string().min(1, 'Product is required'),
    quantity: z.number().int().min(1, 'Quantity must be at least 1'),
});

const orderSchema = z.object({
    customerId: z.string().min(1, 'Customer is required'),
    items: z.array(orderItemSchema).min(1, 'At least one item is required'),
});

type OrderSchema = z.infer<typeof orderSchema>;

interface CreateOrderModalProps {
    isOpen: boolean;
    onClose: () => void;
}

export const CreateOrderModal: React.FC<CreateOrderModalProps> = ({ isOpen, onClose }) => {
    const { user } = useAuthStore();
    const placeOrder = usePlaceOrder();
    const { data: products } = useProducts();
    const { data: customers } = useCustomers();
    const addNotification = useNotificationStore((state) => state.addNotification);

    const {
        register,
        control,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm<OrderSchema>({
        resolver: zodResolver(orderSchema),
        defaultValues: { items: [{ productId: '', quantity: 1 }] },
    });

    const { fields, append, remove } = useFieldArray({
        control,
        name: 'items',
    });

    const onSubmit = async (data: OrderSchema) => {
        if (!user?.tenantId) {
            addNotification('Tenant context missing. Please re-login.', 'error');
            return;
        }

        try {
            await placeOrder.mutateAsync({
                ...data,
                tenantId: user.tenantId,
            });
            addNotification('Order placed successfully!', 'success');
            reset();
            onClose();
        } catch (error: any) {
            addNotification(error.message || 'Failed to place order', 'error');
        }
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title="Create New Order"
            description="Orchestrate a new fulfillment order for a customer."
        >
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
                        Customer
                    </label>
                    <div className="relative">
                        <User className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                        <select
                            {...register('customerId')}
                            className="w-full pl-10 pr-4 py-2 border rounded-xl dark:bg-slate-950 dark:border-slate-800 dark:text-white appearance-none"
                        >
                            <option value="">Select a customer...</option>
                            {customers?.map((c) => (
                                <option key={c.id} value={c.id}>{c.email}</option>
                            ))}
                        </select>
                    </div>
                    {errors.customerId && <p className="mt-1 text-xs text-rose-500">{errors.customerId.message}</p>}
                </div>

                <div className="space-y-3">
                    <div className="flex justify-between items-center">
                        <label className="text-sm font-medium text-slate-700 dark:text-slate-300 flex items-center">
                            <ShoppingCart className="w-4 h-4 mr-2" />
                            Order Items
                        </label>
                        <button
                            type="button"
                            onClick={() => append({ productId: '', quantity: 1 })}
                            className="text-xs flex items-center text-primary font-semibold hover:underline"
                        >
                            <Plus className="w-3 h-3 mr-1" />
                            Add Item
                        </button>
                    </div>

                    <div className="max-h-[30vh] overflow-y-auto space-y-3 pr-2">
                        {fields.map((field, index) => (
                            <div key={field.id} className="flex gap-2 items-start">
                                <div className="flex-1">
                                    <select
                                        {...register(`items.${index}.productId`)}
                                        className="w-full px-3 py-2 border rounded-lg dark:bg-slate-950 dark:border-slate-800 dark:text-white text-sm"
                                    >
                                        <option value="">Choose product...</option>
                                        {products?.map((p) => (
                                            <option key={p.id} value={p.id}>{p.name} (${p.price})</option>
                                        ))}
                                    </select>
                                </div>
                                <div className="w-24">
                                    <input
                                        {...register(`items.${index}.quantity`, { valueAsNumber: true })}
                                        type="number"
                                        className="w-full px-3 py-2 border rounded-lg dark:bg-slate-950 dark:border-slate-800 dark:text-white text-sm"
                                        placeholder="Qty"
                                    />
                                </div>
                                <button
                                    type="button"
                                    onClick={() => remove(index)}
                                    className="p-2 text-slate-400 hover:text-rose-500 transition-colors"
                                >
                                    <Trash2 className="w-4 h-4" />
                                </button>
                            </div>
                        ))}
                    </div>
                    {errors.items && <p className="text-xs text-rose-500">{errors.items.message}</p>}
                </div>

                <button
                    type="submit"
                    disabled={placeOrder.isPending}
                    className="w-full flex items-center justify-center py-3 px-4 bg-primary text-white font-bold rounded-xl hover:bg-primary/90 transition-all disabled:opacity-50 shadow-lg shadow-primary/20"
                >
                    {placeOrder.isPending ? <Loader2 className="w-5 h-5 animate-spin mr-2" /> : null}
                    Place Order
                </button>
            </form>
        </Modal>
    );
};
