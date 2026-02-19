import React from 'react';
import * as Dialog from '@radix-ui/react-dialog';
import { X } from 'lucide-react';

interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title: string;
    description?: string;
    children: React.ReactNode;
}

export const Modal: React.FC<ModalProps> = ({ isOpen, onClose, title, description, children }) => {
    return (
        <Dialog.Root open={isOpen} onOpenChange={onClose}>
            <Dialog.Portal>
                <Dialog.Overlay className="fixed inset-0 bg-black/50 backdrop-blur-sm z-[1000] animate-in fade-in duration-200" />
                <Dialog.Content className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-lg bg-white dark:bg-slate-900 rounded-2xl shadow-2xl p-6 z-[1001] border border-slate-200 dark:border-slate-800 animate-in zoom-in-95 fade-in duration-200">
                    <div className="flex justify-between items-center mb-4">
                        <div>
                            <Dialog.Title className="text-xl font-bold dark:text-white">{title}</Dialog.Title>
                            {description && (
                                <Dialog.Description className="text-sm text-slate-500 mt-1">
                                    {description}
                                </Dialog.Description>
                            )}
                        </div>
                        <Dialog.Close className="p-2 text-slate-400 hover:text-slate-600 dark:hover:text-white transition-colors">
                            <X className="w-5 h-5" />
                        </Dialog.Close>
                    </div>
                    {children}
                </Dialog.Content>
            </Dialog.Portal>
        </Dialog.Root>
    );
};
