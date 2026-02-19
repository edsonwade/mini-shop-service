import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { authService } from '../services/authService';
import { useAuthStore } from '../../../store/useAuthStore';
import type { AppError } from '../../../core/errors/errorMapper';

const loginSchema = z.object({
    email: z.string().email('Invalid email address'),
    password: z.string().min(8, 'Password must be at least 8 characters'),
    totpCode: z.string().length(6, 'MFA code must be 6 digits').optional().or(z.literal('')),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export const LoginForm: React.FC = () => {
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [requiresMFA, setRequiresMFA] = useState(false);

    const setAuth = useAuthStore((state) => state.setAuth);

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),
    });

    const onSubmit = async (data: LoginFormValues) => {
        setIsLoading(true);
        setError(null);
        try {
            const response = await authService.login(data);
            // Backend should return user info as well, for now we mock user or extract from JWT
            setAuth(response.accessToken, response.refreshToken, { email: data.email, roles: [] });
        } catch (err: any) {
            const appError = err as AppError;
            if (appError.code === 'MFA_REQUIRED') {
                setRequiresMFA(true);
            } else {
                setError(appError.message);
            }
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="w-full max-w-md p-8 space-y-6 bg-white rounded-lg shadow-md dark:bg-slate-900">
            <h2 className="text-3xl font-bold text-center text-slate-900 dark:text-white">Sign In</h2>
            <p className="text-sm text-center text-slate-600 dark:text-slate-400">
                Enter your credentials to access your account
            </p>

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
                <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">Email</label>
                    <input
                        {...register('email')}
                        type="email"
                        className="w-full px-4 py-2 mt-1 border rounded-md focus:ring-primary focus:border-primary dark:bg-slate-800 dark:border-slate-700"
                        placeholder="name@company.com"
                    />
                    {errors.email && <p className="mt-1 text-xs text-red-500">{errors.email.message}</p>}
                </div>

                <div>
                    <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">Password</label>
                    <input
                        {...register('password')}
                        type="password"
                        className="w-full px-4 py-2 mt-1 border rounded-md focus:ring-primary focus:border-primary dark:bg-slate-800 dark:border-slate-700"
                        placeholder="••••••••"
                    />
                    {errors.password && <p className="mt-1 text-xs text-red-500">{errors.password.message}</p>}
                </div>

                {requiresMFA && (
                    <div className="animate-in fade-in slide-in-from-top-2">
                        <label className="block text-sm font-medium text-slate-700 dark:text-slate-300">MFA Code</label>
                        <input
                            {...register('totpCode')}
                            type="text"
                            maxLength={6}
                            className="w-full px-4 py-2 mt-1 border rounded-md focus:ring-primary focus:border-primary dark:bg-slate-800 dark:border-slate-700"
                            placeholder="123456"
                        />
                        {errors.totpCode && <p className="mt-1 text-xs text-red-500">{errors.totpCode.message}</p>}
                    </div>
                )}

                {error && (
                    <div className="p-3 text-sm text-red-500 bg-red-100 rounded-md dark:bg-red-900/30 dark:text-red-400">
                        {error}
                    </div>
                )}

                <button
                    type="submit"
                    disabled={isLoading}
                    className="w-full py-2 font-semibold text-white transition-colors rounded-md bg-primary hover:bg-primary/90 disabled:opacity-50"
                >
                    {isLoading ? 'Signing In...' : 'Sign In'}
                </button>
            </form>
        </div>
    );
};
