import React from 'react';
import { useNotificationStore, type Notification } from '../../store/useNotificationStore';
import { CheckCircle, XCircle, AlertCircle, Info, X } from 'lucide-react';

const icons = {
    success: <CheckCircle className="w-5 h-5 text-emerald-500" />,
    error: <XCircle className="w-5 h-5 text-rose-500" />,
    warning: <AlertCircle className="w-5 h-5 text-amber-500" />,
    info: <Info className="w-5 h-5 text-blue-500" />,
};

const Toast: React.FC<{ notification: Notification }> = ({ notification }) => {
    const remove = useNotificationStore((state) => state.removeNotification);

    return (
        <div className="flex items-center w-full max-w-xs p-4 space-x-4 text-slate-500 bg-white rounded-lg shadow-xl dark:text-slate-400 dark:bg-slate-900 border border-slate-200 dark:border-slate-800 animate-in fade-in slide-in-from-right-4 duration-300">
            <div className="flex-shrink-0">
                {icons[notification.type]}
            </div>
            <div className="text-sm font-normal text-slate-900 dark:text-slate-100">{notification.message}</div>
            <button
                type="button"
                onClick={() => remove(notification.id)}
                className="ms-auto -mx-1.5 -my-1.5 bg-white text-slate-400 hover:text-slate-900 rounded-lg focus:ring-2 focus:ring-slate-300 p-1.5 hover:bg-slate-100 inline-flex items-center justify-center h-8 w-8 dark:text-slate-500 dark:hover:text-white dark:bg-slate-900 dark:hover:bg-slate-800"
            >
                <span className="sr-only">Close</span>
                <X className="w-4 h-4" />
            </button>
        </div>
    );
};

export const ToastContainer: React.FC = () => {
    const notifications = useNotificationStore((state) => state.notifications);

    return (
        <div className="fixed bottom-5 right-5 z-[9999] flex flex-col gap-3">
            {notifications.map((n) => (
                <Toast key={n.id} notification={n} />
            ))}
        </div>
    );
};
