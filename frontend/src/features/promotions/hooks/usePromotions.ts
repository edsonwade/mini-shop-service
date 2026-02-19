import { useMutation, useQueryClient } from '@tanstack/react-query';
import { promotionService, type CreateCouponRequest } from '../services/promotionService';

export const useCreateCoupon = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateCouponRequest) => promotionService.createCoupon(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['promotions'] });
    },
  });
};
