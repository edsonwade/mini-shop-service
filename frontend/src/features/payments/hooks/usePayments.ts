import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { paymentService, type ProcessPaymentRequest } from '../services/paymentService';

export const usePayments = () => {
  return useQuery({
    queryKey: ['payments'],
    queryFn: paymentService.listAllPayments,
  });
};

export const usePaymentByOrder = (orderId: string) => {
  return useQuery({
    queryKey: ['payments', 'order', orderId],
    queryFn: () => paymentService.getPaymentByOrderId(orderId),
    enabled: !!orderId,
  });
};

export const useProcessPayment = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: ProcessPaymentRequest) => paymentService.processPayment(data),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['payments'] });
      queryClient.invalidateQueries({ queryKey: ['payments', 'order', data.orderId] });
      queryClient.invalidateQueries({ queryKey: ['orders', data.orderId] });
    },
  });
};

export const useRefundOrder = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (orderId: string) => paymentService.refundOrder(orderId),
    onSuccess: (_, orderId) => {
      queryClient.invalidateQueries({ queryKey: ['payments'] });
      queryClient.invalidateQueries({ queryKey: ['payments', 'order', orderId] });
      queryClient.invalidateQueries({ queryKey: ['orders', orderId] });
    },
  });
};
