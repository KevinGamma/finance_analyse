import { createRouter, createWebHistory } from 'vue-router';

import { ensureAuthReady, useAuthState } from '../services/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/AuthView.vue'),
      meta: { public: true }
    },
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/stocks',
      name: 'stocks',
      component: () => import('../views/StockAnalysisView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/news',
      name: 'news',
      component: () => import('../views/NewsAnalysisView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/'
    }
  ]
});

router.beforeEach(async (to, from, next) => {
  await ensureAuthReady();
  const { isAuthenticated } = useAuthState();
  if (!to.meta?.public && !isAuthenticated.value) {
    return next({ name: 'login', query: { redirect: to.fullPath } });
  }
  if (to.name === 'login' && isAuthenticated.value) {
    return next({ name: 'home' });
  }
  return next();
});

export default router;
