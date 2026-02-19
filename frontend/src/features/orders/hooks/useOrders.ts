import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { orderService, type CreateOrderRequest } from '../services/orderService';

export const useOrderHistory = () => {
  return useQuery({
    queryKey: ['orders'],
    queryFn: () => orderService.listOrders(),
  });
};

export const useOrder = (id: string) => {
  return useQuery({
    queryKey: ['orders', id],
    queryFn: () => orderService.getOrder(id),
    enabled: !!id,
  });
};

export const usePlaceOrder = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateOrderRequest) => {
      // Generate a unique idempotency key for this order attempt
      const idempotencyKey = self.crypto.randomUUID();
      return orderService.placeOrder(data, idempotencyKey);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};

export const useCancelOrder = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => orderService.cancelOrder(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ['orders', id] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};

export const useSettleOrder = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => orderService.settleOrder(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ['orders', id] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
};
