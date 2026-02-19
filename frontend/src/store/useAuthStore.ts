import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: {
    email: string;
    roles: string[];
    tenantId?: string;
  } | null;
  tenantId: string | null;
  setAuth: (accessToken: string, refreshToken: string, user: any) => void;
  setTenantId: (tenantId: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      accessToken: null,
      refreshToken: null,
      user: null,
      tenantId: null, // Should be initialized from discovery or env if possible

      setAuth: (accessToken, refreshToken, user) =>
        set({ accessToken, refreshToken, user }),

      setTenantId: (tenantId) => set({ tenantId }),

      logout: () =>
        set({
          accessToken: null,
          refreshToken: null,
          user: null,
        }),
    }),
    {
      name: 'auth-storage',
    }
  )
);
