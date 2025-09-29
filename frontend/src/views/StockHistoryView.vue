<template>
  <div class="stock-history">
    <el-card class="history-card" shadow="hover">
      <template #header>
        <div class="card-title">
          近期分析记录
        </div>
      </template>

      <div class="toolbar">
        <el-form inline label-position="left">
          <el-form-item label="筛选类型">
            <el-select v-model="typeFilter" style="width: 180px">
              <el-option label="全部" value="ALL" />
              <el-option label="综合分析" value="COMPREHENSIVE" />
              <el-option label="结构化分析" value="STRUCTURED" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :loading="loading" @click="load">刷新</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table v-if="filteredRows.length" :data="filteredRows" :loading="loading" border height="560">
        <el-table-column prop="stockCode" label="股票" width="160" />
        <el-table-column prop="analysisType" label="类型" width="140">
          <template #default="scope">{{ label(scope.row.analysisType) }}</template>
        </el-table-column>
        <el-table-column label="生成时间" width="220">
          <template #default="scope">{{ formatDate(scope.row.requestedAt) }}</template>
        </el-table-column>
        <el-table-column label="详情" min-width="180" align="center">
          <template #default="scope">
            <el-button type="primary" link @click="openDetail(scope.row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else :description="loading ? '加载中...' : '暂无历史记录'" />
    </el-card>

    <el-dialog v-model="detailVisible" width="70%" :show-close="false">
      <template #header>
        <div class="dialog-header">
          <span>分析详情：{{ current?.stockCode }}</span>
          <el-button text type="primary" @click="detailVisible=false">关闭</el-button>
        </div>
      </template>
      <div v-if="current" class="dialog-body">
        <p class="dialog-meta">{{ label(current.analysisType) }} · {{ formatDate(current.requestedAt) }}</p>
        <template v-if="current.analysisType==='STRUCTURED' && structured">
          <StockAdvicePanel :data="structured" />
          <div class="dialog-divider"></div>
          <div class="kline-header">
            <h4 class="section-sub-title">K线图</h4>
          </div>
          <el-empty v-if="!candles.length" description="无时间序列" />
          <KLineChart v-else :candles="candles" :height="420" />
        </template>
        <pre v-else>{{ toReadableText(current.analysis) }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue';
import { fetchHistory, toExtractedJson, extractTimeSeries } from '../api/stockAnalysis';
import StockAdvicePanel from '../components/StockAdvicePanel.vue';
import KLineChart from '../components/KLineChart.vue';
import type { ExtractedJson, SingleStockApiResponse, StockAnalysisResponse, TimeSeriesCandle } from '../types';
import { formatDate, toReadableText } from '../utils/display';

const loading = ref(false);
const rows = ref<StockAnalysisResponse[]>([]);
const current = ref<StockAnalysisResponse | null>(null);
const detailVisible = ref(false);
const typeFilter = ref<'ALL' | 'COMPREHENSIVE' | 'STRUCTURED'>('ALL');

const filteredRows = computed(() => {
  if (typeFilter.value === 'ALL') return rows.value;
  return rows.value.filter(r => r.analysisType === typeFilter.value);
});

const structured = computed<ExtractedJson | null>(() => {
  if (!current.value || current.value.analysisType!=='STRUCTURED') return null;
  return toExtractedJson(current.value.analysis as SingleStockApiResponse);
});

const candles = computed<TimeSeriesCandle[]>(() => {
  if (!current.value || current.value.analysisType!=='STRUCTURED') return [];
  try { return extractTimeSeries(current.value.analysis as SingleStockApiResponse); } catch { return []; }
});

const typeLabel: Record<StockAnalysisResponse['analysisType'], string> = {
  COMPREHENSIVE: '综合分析', STRUCTURED: '结构化分析'
};

function label(t: StockAnalysisResponse['analysisType']) { return typeLabel[t] ?? t; }

async function load() {
  loading.value = true;
  try { rows.value = await fetchHistory(); } finally { loading.value = false; }
}

function openDetail(row: StockAnalysisResponse) { current.value = row; detailVisible.value = true; }

onMounted(load);
</script>

<style scoped>
.stock-history { display: flex; flex-direction: column; gap: 16px; }
.history-card { border-radius: 16px; }
.card-title { font-weight: 700; font-size: 18px; color: #32475b; }
.toolbar { padding: 8px 0 12px; }
.dialog-header { display: flex; justify-content: space-between; align-items: center; font-weight: 700; font-size: 16px; color: #32475b; }
.dialog-meta { margin-bottom: 12px; color: #6b7a90; }
.dialog-divider { height: 1px; background: #e0e6ed; margin: 12px 0; }
.section-sub-title { margin: 8px 0 12px; font-size: 14px; font-weight: 600; color: #32475b; }
</style>
