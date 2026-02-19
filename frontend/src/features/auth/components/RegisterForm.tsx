import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { authService } from '../services/authService';
import { useAuthStore } from '../../../store/useAuthStore';
import { useNotificationStore } from '../../../store/useNotificationStore';
import { useNavigate, Link } from 'react-router-dom';
import { Loader2, UserPlus, Building2 } from 'lucide-react';

const registerSchema = z.object({
    email: z.string().email('Invalid email address'),
    password: z.string().min(8, 'Password must be at least 8 characters'),
    tenantId: z.string().min(1, 'Project/Tenant ID is required'),
});

type RegisterSchema = z.infer<typeof registerSchema>;

export const RegisterForm: React.FC = () => {
    const navigate = useNavigate();
    const setAuth = useAuthStore((state) => state.setAuth);
    const addNotification = useNotificationStore((state) => state.addNotification);
    const [isLoading, setIsLoading] = React.useState(false);

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<RegisterSchema>({
        resolver: zodResolver(registerSchema),
    });

    const onSubmit = async (data: RegisterSchema) => {
        setIsLoading(true);
        try {
            const response = await authService.register(data);
            setAuth(response.accessToken, response.refreshToken, { email: data.email, roles: [], tenantId: data.tenantId });
            addNotification('Account created successfully!', 'success');
            navigate('/');
        } catch (error: any) {
            addNotification(error.message || 'Registration failed', 'error');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="w-full max-w-md p-8 bg-white rounded-2xl shadow-xl dark:bg-slate-900 border border-slate-200 dark:border-slate-800">
            <div className="flex flex-col items-center mb-8">
                <div className="p-3 mb-4 bg-primary/10 rounded-full">
                    <UserPlus className="w-8 h-8 text-primary" />
                </div>
                <h2 className="text-2xl font-bold dark:text-white">Create Account</h2>
                <p className="text-slate-500 text-sm mt-2">Join the SaaS platform today</p>
            </div>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
                <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
                        Project / Tenant ID
                    </label>
                    <div className="relative">
                        <Building2 className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                        <input
                            {...register('tenantId')}
                            className="w-full pl-10 pr-4 py-2 border rounded-lg focus:ring-2 focus:ring-primary/20 dark:bg-slate-950 dark:border-slate-800 dark:text-white"
                            placeholder="my-company"
                        />
                    </div>
                    {errors.tenantId && <p className="mt-1 text-xs text-rose-500">{errors.tenantId.message}</p>}
                </div>

                <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
                        Email Address
                    </label>
                    <input
                        {...register('email')}
                        className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-primary/20 dark:bg-slate-950 dark:border-slate-800 dark:text-white"
                        placeholder="name@company.com"
                    />
                    {errors.email && <p className="mt-1 text-xs text-rose-500">{errors.email.message}</p>}
                </div>

                <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-1.5">
                        Password
                    </label>
                    <input
                        {...register('password')}
                        type="password"
                        className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-primary/20 dark:bg-slate-950 dark:border-slate-800 dark:text-white"
                        placeholder="••••••••"
                    />
                    {errors.password && <p className="mt-1 text-xs text-rose-500">{errors.password.message}</p>}
                </div>

                <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full flex items-center justify-center py-2.5 px-4 bg-primary text-white font-semibold rounded-lg hover:bg-primary/90 focus:ring-4 focus:ring-primary/20 transition-all disabled:opacity-50"
                >
                    {isLoading ? <Loader2 className="w-5 h-5 animate-spin" /> : 'Create SaaS Account'}
                </button>
            </form>

            <div className="mt-6 text-center text-sm">
                <span className="text-slate-500">Already have an account?</span>{' '}
                <Link to="/login" className="text-primary font-medium hover:underline">
                    Sign In
                </Link>
            </div>
        </div>
    );
};
