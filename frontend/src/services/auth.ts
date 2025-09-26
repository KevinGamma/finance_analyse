import { computed, ref } from 'vue';

import { apiClient, registerUnauthorizedHandler } from '../api/client';
import { AUTH_TOKEN_KEY } from '../constants';
import type { AuthResponse, LoginPayload, RegisterPayload, UserProfile } from '../types';

const currentUser = ref<UserProfile | null>(null);

let initializePromise: Promise<void> | null = null;
let initializationStarted = false;

registerUnauthorizedHandler(() => {
  currentUser.value = null;
  initializePromise = null;
  initializationStarted = false;
});

export function useAuthState() {
  const isAuthenticated = computed(() => currentUser.value !== null);
  return {
    currentUser,
    isAuthenticated,
    ensureAuthReady,
    login: performLogin,
    register: performRegister,
    logout: performLogout
  };
}

export function ensureAuthReady(): Promise<void> {
  if (!initializePromise) {
    initializePromise = initializeCurrentUser();
  }
  return initializePromise;
}

async function initializeCurrentUser(): Promise<void> {
  if (initializationStarted) {
    return initializePromise ?? Promise.resolve();
  }
  initializationStarted = true;

  if (typeof window === 'undefined') {
    currentUser.value = null;
    return;
  }

  const token = window.localStorage.getItem(AUTH_TOKEN_KEY);
  if (!token) {
    currentUser.value = null;
    return;
  }
  try {
    const response = await apiClient.get<UserProfile>('/auth/me');
    currentUser.value = response.data;
  } catch (error) {
    window.localStorage.removeItem(AUTH_TOKEN_KEY);
    currentUser.value = null;
  }
}

async function performLogin(payload: LoginPayload): Promise<UserProfile> {
  const response = await apiClient.post<AuthResponse>('/auth/login', payload);
  return establishSession(response.data);
}

async function performRegister(payload: RegisterPayload): Promise<UserProfile> {
  const response = await apiClient.post<AuthResponse>('/auth/register', payload);
  return establishSession(response.data);
}

function performLogout(): void {
  if (typeof window !== 'undefined') {
    window.localStorage.removeItem(AUTH_TOKEN_KEY);
  }
  currentUser.value = null;
  initializePromise = null;
  initializationStarted = false;
}

function establishSession(authResponse: AuthResponse): UserProfile {
  if (typeof window !== 'undefined') {
    window.localStorage.setItem(AUTH_TOKEN_KEY, authResponse.token);
  }
  currentUser.value = authResponse.user;
  return authResponse.user;
}
