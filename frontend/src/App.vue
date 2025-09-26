<template>
  <div class="app-shell">
    <el-container>
      <el-header class="app-header">
        <div class="app-header__content">
          <RouterLink class="brand" to="/">财经分析门户</RouterLink>
          <div class="spacer" />
          <template v-if="isAuthenticated">
            <nav class="nav-links">
              <RouterLink class="nav-link" :class="linkClass('home')" to="/">首页</RouterLink>
              <RouterLink class="nav-link" :class="linkClass('stocks')" to="/stocks">股票分析</RouterLink>
              <RouterLink class="nav-link" :class="linkClass('news')" to="/news">新闻分析</RouterLink>
            </nav>
            <el-divider direction="vertical" class="nav-divider" />
            <div class="user-meta">
              <span class="user-name">{{ currentUser?.username }}</span>
              <el-button link type="primary" @click="handle退出登录">退出登录</el-button>
            </div>
          </template>
          <RouterLink v-else class="nav-link" to="/login">登录 / 注册</RouterLink>
        </div>
      </el-header>
      <el-main class="app-main">
        <RouterView />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { useRoute, useRouter } from 'vue-router';

import { useAuthState } from './services/auth';

const route = useRoute();
const router = useRouter();

const { currentUser, isAuthenticated, logout } = useAuthState();

const linkClass = (name: string) => ({ active: route.name === name });

const handle退出登录 = () => {
  logout();
  router.push({ name: 'login' });
};
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e3e9f6 100%);
}

.app-header {
  background: linear-gradient(90deg, #1f2d3d 60%, #32475b 100%);
  color: #fff;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(31, 45, 61, 0.15);
  padding: 0 32px;
}

.app-header__content {
  display: flex;
  align-items: center;
  width: 100%;
  gap: 16px;
}

.brand {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  text-decoration: none;
}

.spacer {
  flex: 1 1 auto;
}

.nav-links {
  display: flex;
  gap: 16px;
}

.nav-link {
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  font-weight: 600;
  letter-spacing: 0.3px;
  transition: opacity 0.2s;
}

.nav-link:hover {
  opacity: 1;
}

.nav-link.active {
  color: #ffffff;
  border-bottom: 2px solid #fff;
  padding-bottom: 2px;
}

.nav-divider {
  height: 24px;
  margin: 0 12px;
  border-color: rgba(255, 255, 255, 0.3);
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.85);
}

.user-name {
  font-weight: 600;
}

.app-main {
  padding: 32px 12px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.el-button.is-link {
  color: #ffd56b;
}

@media (max-width: 900px) {
  .app-header {
    padding: 0 16px;
  }
  .nav-links {
    gap: 12px;
  }
  .app-main {
    padding: 24px 8px;
  }
}
</style>
