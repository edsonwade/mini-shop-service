import apiClient from '../../../core/api/apiClient';

export interface Customer {
  id: string;
  name: string;
  email: string;
  kycVerified: boolean;
  tenantId: string;
}

export const customerService = {
  getCustomers: async (): Promise<Customer[]> => {
    const response = await apiClient.get('/customers');
    return response.data;
  },

  getCustomer: async (id: string): Promise<Customer> => {
    const response = await apiClient.get(`/customers/${id}`);
    return response.data;
  },

  verifyKYC: async (id: string): Promise<void> => {
    await apiClient.post(`/customers/${id}/verify-kyc`);
  },

  createCustomer: async (email: string): Promise<Customer> => {
    const response = await apiClient.post('/customers', { email });
    return response.data;
  },
};
