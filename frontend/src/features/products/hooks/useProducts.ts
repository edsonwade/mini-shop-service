import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { productService, type CreateProductRequest } from '../services/productService';

export const useProducts = () => {
  return useQuery({
    queryKey: ['products'],
    queryFn: productService.getProducts,
  });
};

export const useCreateProduct = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: CreateProductRequest) => productService.createProduct(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
    },
  });
};
