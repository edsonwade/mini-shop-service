import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { customerService } from '../services/customerService';

export const useCustomers = () => {
  return useQuery({
    queryKey: ['customers'],
    queryFn: customerService.getCustomers,
  });
};

export const useVerifyKYC = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => customerService.verifyKYC(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customers'] });
    },
  });
};

export const useCreateCustomer = () => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (email: string) => customerService.createCustomer(email),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['customers'] });
    },
  });
};
