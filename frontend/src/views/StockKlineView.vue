<template>
  <div class="stock-kline">
    <el-card class="kline-card" shadow="hover">
      <template #header>
        <div class="kline-card-header">
          <div class="left">
            <h3 class="card-title">K线图查看</h3>
            <p v-if="klineMetaDetails" class="kline-meta">{{ klineMetaDetails }}</p>
          </div>
          <el-form class="kline-form" inline size="small" @submit.prevent>
            <el-form-item>
              <el-input v-model="form.symbol" maxlength="10" placeholder="示例：AAPL" @keyup.enter="load" />
            </el-form-item>
            <el-form-item>
              <el-select v-model="form.interval" placeholder="时间间隔" style="width: 120px">
                <el-option v-for="item in intervals" :key="item.value" :label="item.label" :value="item.value" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="load">获取K线</el-button>
            </el-form-item>
          </el-form>
        </div>
      </template>
      <div class="kline-body">
        <KLineChart v-if="candles.length" :candles="candles" :height="560" />
        <el-empty v-else description="请输入股票代码并选择时间间隔以查看K线图" />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import KLineChart from '../components/KLineChart.vue';
import { fetchIntradaySeries, mapIntradayToCandles } from '../api/stockAnalysis';
import type { IntradayInterval, TimeSeriesCandle } from '../types';
import { extractErrorMessage } from '../utils/errors';

const intervals = [
  { label: '1分钟', value: '1min' },
  { label: '5分钟', value: '5min' },
  { label: '15分钟', value: '15min' },
  { label: '30分钟', value: '30min' },
  { label: '60分钟', value: '60min' }
] as const;

const form = reactive({ symbol: '', interval: '1min' as IntradayInterval });
const loading = ref(false);
const candles = ref<TimeSeriesCandle[]>([]);
const lastRefreshed = ref<string | null>(null);
const timezone = ref<string | null>(null);

const klineMetaDetails = computed(() => {
  const parts: string[] = [];
  if (lastRefreshed.value) parts.push('最后刷新：' + fmt(lastRefreshed.value));
  if (timezone.value) parts.push('时区：' + timezone.value);
  return parts.join(' | ');
});

function fmt(ts: string) {
  const d = new Date(ts.replace(' ', 'T'));
  if (Number.isNaN(d.getTime())) return ts;
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
}

async function load() {
  const symbol = form.symbol.trim().toUpperCase();
  if (!symbol) { ElMessage.warning('请输入股票代码'); return; }
  if (!/^[A-Za-z.\-]{1,20}$/.test(symbol)) { ElMessage.warning('股票代码格式不正确'); return; }
  loading.value = true;
  try {
    const series = await fetchIntradaySeries(symbol, form.interval);
    lastRefreshed.value = series.lastRefreshed ?? null;
    timezone.value = series.timezone ?? null;
    candles.value = mapIntradayToCandles(series);
    if (!candles.value.length) ElMessage.info('未获取到该区间的K线数据');
  } catch (e) {
    candles.value = [];
    ElMessage.error(extractErrorMessage(e));
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.stock-kline { display: flex; flex-direction: column; gap: 16px; }
.kline-card { border-radius: 16px; }
.kline-card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { margin: 0; font-weight: 700; font-size: 18px; color: #32475b; }
.kline-meta { margin: 6px 0 0; color: #6b7a90; font-size: 13px; }
</style>
