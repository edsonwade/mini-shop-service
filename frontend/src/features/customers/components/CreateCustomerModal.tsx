import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { Modal } from '../../../components/ui/Modal';
import { useCreateCustomer } from '../hooks/useCustomers';
import { useNotificationStore } from '../../../store/useNotificationStore';
import { Loader2, Mail } from 'lucide-react';

const customerSchema = z.object({
    email: z.string().email('Invalid email address'),
});

type CustomerSchema = z.infer<typeof customerSchema>;

interface CreateCustomerModalProps {
    isOpen: boolean;
    onClose: () => void;
}

export const CreateCustomerModal: React.FC<CreateCustomerModalProps> = ({ isOpen, onClose }) => {
    const { mutateAsync: createCustomer, isPending } = useCreateCustomer();
    const addNotification = useNotificationStore((state) => state.addNotification);

    const {
        register,
        handleSubmit,
        reset,
        formState: { errors },
    } = useForm<CustomerSchema>({
        resolver: zodResolver(customerSchema),
    });

    const onSubmit = async (data: CustomerSchema) => {
        try {
            await createCustomer(data.email);
            addNotification('Customer added successfully!', 'success');
            reset();
            onClose();
        } catch (error: any) {
            addNotification(error.message || 'Failed to add customer', 'error');
        }
    };

    return (
        <Modal
            isOpen={isOpen}
            onClose={onClose}
            title="Add New Customer"
            description="Register a new customer for your project."
        >
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
                <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
                        Email Address
                    </label>
                    <div className="relative">
                        <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                        <input
                            {...register('email')}
                            className="w-full pl-10 pr-4 py-2.5 border rounded-xl dark:bg-slate-950 dark:border-slate-800 dark:text-white"
                            placeholder="customer@domain.com"
                        />
                    </div>
                    {errors.email && <p className="mt-1 text-xs text-rose-500">{errors.email.message}</p>}
                </div>

                <button
                    type="submit"
                    disabled={isPending}
                    className="w-full flex items-center justify-center py-3 px-4 bg-slate-900 dark:bg-white dark:text-slate-900 text-white font-bold rounded-xl hover:opacity-90 transition-all disabled:opacity-50"
                >
                    {isPending && <Loader2 className="w-5 h-5 animate-spin mr-2" />}
                    Add Customer
                </button>
            </form>
        </Modal>
    );
};
