<template>
  <div class="stock-home">
    <section class="hero">
      <h2>股票分析功能</h2>
      <p>请选择要使用的功能模块。</p>
    </section>

    <el-row :gutter="20" class="icon-grid">
      <el-col :xs="24" :md="12" :lg="6">
        <el-card class="icon-card" shadow="hover" @click="go('stocks-comprehensive')">
          <div class="icon">🧠</div>
          <h3>综合分析</h3>
          <p>根据您提交的股票代码，生成综合分析报告。</p>
          <el-button type="primary" text @click.stop="go('stocks-comprehensive')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12" :lg="6">
        <el-card class="icon-card" shadow="hover" @click="go('stocks-structured')">
          <div class="icon">🧩</div>
          <h3>结构化分析</h3>
          <p>获取结构化要点与建议，便于快速决策。</p>
          <el-button type="primary" text @click.stop="go('stocks-structured')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12" :lg="6">
        <el-card class="icon-card" shadow="hover" @click="go('stocks-kline')">
          <div class="icon">📈</div>
          <h3>K线图查看</h3>
          <p>选择股票与时间粒度，查看K线与成交量。</p>
          <el-button type="primary" text @click.stop="go('stocks-kline')">进入</el-button>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12" :lg="6">
        <el-card class="icon-card" shadow="hover" @click="go('stocks-history')">
          <div class="icon">🕒</div>
          <h3>历史记录</h3>
          <p>在这里浏览您的近期分析记录，查看历史详情。</p>
          <el-button type="primary" text @click.stop="go('stocks-history')">进入</el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

const router = useRouter();
const go = (name: string) => router.push({ name });
</script>

<style scoped>
.stock-home { display: flex; flex-direction: column; gap: 24px; }
.hero { background: #fff; padding: 24px; border-radius: 16px; box-shadow: 0 8px 24px rgba(50,71,91,0.12); }
.hero h2 { margin: 0; font-size: 24px; font-weight: 700; color: #32475b; }
.hero p { margin: 10px 0 0; color: #6b7a90; }
.icon-grid { margin-top: 8px; }
.icon-card { border-radius: 16px; min-height: 200px; display: flex; flex-direction: column; gap: 10px; align-items: flex-start; cursor: pointer; }
.icon { width: 64px; height: 64px; display: grid; place-items: center; font-size: 36px; border-radius: 16px; background: #f0f4fa; }
.icon-card h3 { margin: 0; font-size: 20px; color: #1f2d3d; }
.icon-card p { margin: 0; color: #6b7a90; }
</style>

