import apiClient from '../../../core/api/apiClient';

export interface OrderItemRequest {
  productId: string;
  quantity: number;
}

export interface OrderItemResponse {
  productId: string;
  sku: string;
  quantity: number;
  unitPrice: number;
}

export interface CreateOrderRequest {
  tenantId: string;
  customerId: string;
  items: OrderItemRequest[];
  couponCode?: string;
}

export interface OrderResponse {
  id: string;
  customerId: string;
  status: string;
  totalAmount: number;
  currency: string;
  createdAt: string;
  items: OrderItemResponse[];
}

export const orderService = {
  placeOrder: async (data: CreateOrderRequest, idempotencyKey: string): Promise<OrderResponse> => {
    const response = await apiClient.post('/orders', data, {
      headers: {
        'X-Idempotency-Key': idempotencyKey,
      },
    });
    return response.data;
  },

  getOrder: async (id: string): Promise<OrderResponse> => {
    const response = await apiClient.get(`/orders/${id}`);
    return response.data;
  },

  cancelOrder: async (id: string): Promise<void> => {
    await apiClient.post(`/orders/${id}/cancel`);
  },

  settleOrder: async (id: string): Promise<void> => {
    await apiClient.post(`/orders/${id}/settle`);
  },

  listOrders: async (): Promise<OrderResponse[]> => {
    const response = await apiClient.get('/orders');
    return response.data;
  },
};
