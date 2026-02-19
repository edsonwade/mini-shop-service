import apiClient from '../../../core/api/apiClient';

export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  stock: number;
  tenantId: string;
}

export interface CreateProductRequest {
  name: string;
  description: string;
  price: number;
  sku: string;
  stock: number;
}

export const productService = {
  getProducts: async (): Promise<Product[]> => {
    const response = await apiClient.get('/products');
    return response.data;
  },

  getProduct: async (id: string): Promise<Product> => {
    const response = await apiClient.get(`/products/${id}`);
    return response.data;
  },

  createProduct: async (data: CreateProductRequest): Promise<Product> => {
    const response = await apiClient.post('/products', data);
    return response.data;
  },
};
