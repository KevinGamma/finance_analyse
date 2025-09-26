<template>
  <div class="home-page">
    <section class="hero">
      <h2>{{ currentUser?.username }}，欢迎回来，请选择要执行的操作</h2>
      <p>将每日工作流程拆分为专注的股票分析与新闻分析。</p>
    </section>
    <el-row :gutter="20" class="option-grid">
      <el-col :xs="24" :md="12">
        <el-card class="option-card" shadow="hover">
          <div class="card-body">
            <h3>股票分析</h3>
            <p>提交新的分析任务或查看近期的结构化结果。</p>
            <el-button type="primary" @click="goToStocks">进入股票分析</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card class="option-card" shadow="hover">
          <div class="card-body">
            <h3>新闻分析</h3>
            <p>追踪重点主题的新闻热度与情绪变化。</p>
            <el-button type="primary" plain @click="goToNews">进入新闻分析</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

import { useAuthState } from '../services/auth';

const router = useRouter();
const { currentUser } = useAuthState();

const goToStocks = () => router.push({ name: 'stocks' });
const goToNews = () => router.push({ name: 'news' });
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.hero {
  background: #ffffff;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 12px 32px rgba(50, 71, 91, 0.12);
}

.hero h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #32475b;
}

.hero p {
  margin: 12px 0 0;
  font-size: 16px;
  color: #6b7a90;
}

.option-grid {
  margin-top: 8px;
}

.option-card {
  border-radius: 16px;
  min-height: 220px;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-body h3 {
  margin: 0;
  font-size: 22px;
  color: #1f2d3d;
}

.card-body p {
  margin: 0;
  color: #6b7a90;
}

.el-button {
  align-self: flex-start;
}
</style>
