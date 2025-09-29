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
    // Stock analysis module: home + split pages
    {
      path: '/stocks',
      name: 'stocks-home',
      component: () => import('../views/StockHomeView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/stocks/comprehensive',
      name: 'stocks-comprehensive',
      component: () => import('../views/StockComprehensiveView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/stocks/structured',
      name: 'stocks-structured',
      component: () => import('../views/StockStructuredView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/stocks/kline',
      name: 'stocks-kline',
      component: () => import('../views/StockKlineView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/stocks/history',
      name: 'stocks-history',
      component: () => import('../views/StockHistoryView.vue'),
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
