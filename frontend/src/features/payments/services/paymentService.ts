import apiClient from '../../../core/api/apiClient';

export interface ProcessPaymentRequest {
  orderId: string;
  amount: number;
  currency: string;
  paymentMethod: string;
}

export interface PaymentResponse {
  id: string;
  orderId: string;
  amount: number;
  status: string;
}

export const paymentService = {
  listAllPayments: async (): Promise<PaymentResponse[]> => {
    const response = await apiClient.get('/payments');
    return response.data;
  },

  getPaymentByOrderId: async (orderId: string): Promise<PaymentResponse> => {
    const response = await apiClient.get(`/payments/order/${orderId}`);
    return response.data;
  },

  processPayment: async (data: ProcessPaymentRequest): Promise<PaymentResponse> => {
    const response = await apiClient.post('/payments', data);
    return response.data;
  },

  refundOrder: async (orderId: string): Promise<void> => {
    await apiClient.post(`/payments/refund/${orderId}`);
  },

  getPaymentById: async (paymentId: string): Promise<PaymentResponse> => {
    const response = await apiClient.get(`/payments/${paymentId}`);
    return response.data;
  },
};
