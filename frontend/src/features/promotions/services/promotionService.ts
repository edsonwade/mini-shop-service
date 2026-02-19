import apiClient from '../../../core/api/apiClient';

export interface CreateCouponRequest {
  code: string;
  discountAmount: number;
  currency: string;
  expiryDate: string; // ISO Instant string
}

export interface PromotionResponse {
  id: string;
  code: string;
  discountAmount: number;
  active: boolean;
}

export const promotionService = {
  createCoupon: async (data: CreateCouponRequest): Promise<PromotionResponse> => {
    const response = await apiClient.post('/promotions/coupons', data);
    return response.data;
  },
};
