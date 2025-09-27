<template>
  <div class="stock-page">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="12">
        <el-card class="form-card" shadow="hover">
          <template #header>
            <div class="card-title">提交股票分析</div>
          </template>
          <el-form
            ref="stockFormRef"
            :model="stockForm"
            :rules="stockRules"
            label-position="top"
            class="analysis-form"
          >
            <el-form-item label="股票代码" prop="stockCode">
              <el-input
                v-model="stockForm.stockCode"
                maxlength="10"
                placeholder="示例：AAPL"
                @keyup.enter="submitStockForm"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="stockLoading" @click="submitStockForm">综合分析</el-button>
              <el-button class="ml8" :loading="singleStockLoading" @click="submitSingleStock">结构化分析</el-button>
            </el-form-item>
          </el-form>
          <el-divider />
          <section>
            <h3 class="section-title">最新综合分析结果</h3>
            <el-empty v-if="!stockAnalysisResult" description="提交股票代码后将显示分析报告" />
            <div v-else class="analysis-result">
              <p class="analysis-result__meta">
                <strong>{{ stockAnalysisResult.stockCode }}</strong>
                <span>{{ formatDate(stockAnalysisResult.requestedAt) }}</span>
              </p>
              <el-scrollbar class="analysis-result__scroll">
                <pre>{{ toReadableText(stockAnalysisResult.analysis) }}</pre>
              </el-scrollbar>
            </div>
          </section>
          <el-divider />
          <section>
            <h3 class="section-title">结构化速览</h3>
            <el-empty v-if="!singleStockData" description="执行结构化分析以查看详细信息" />
            <StockAdvicePanel v-else :data="singleStockData" />
          </section>
M         </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card class="history-card" shadow="hover">
          <template #header>
            <div class="card-title">近期分析记录</div>
          </template>
          <el-table
            v-if="stockHistory.length"
            :data="stockHistory"
            :loading="stockHistoryLoading"
            border
            height="520"
          >
            <el-table-column prop="stockCode" label="股票" width="140" />
            <el-table-column prop="analysisType" label="类型" width="140">
              <template #default="scope">
                {{ formatAnalysisType(scope.row.analysisType) }}
              </template>
            </el-table-column>
            <el-table-column label="生成时间" width="200">
              <template #default="scope">
                {{ formatDate(scope.row.requestedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="详情" min-width="180" align="center">
              <template #default="scope">
                <el-button type="primary" link @click="openStockDetail(scope.row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无历史记录" />
        </el-card>
      </el-col>
    </el-row>

    <!-- Full-width K-line row -->
    <el-row v-if="klineCandles.length" class="kline-row">
      <el-col :span="24">
        <el-card class="kline-card" shadow="hover">
          <template #header>
            <div class="kline-card-header">
              <div class="left">
                <h3 class="card-title kline-title">K线走势 ({{ singleStockData?.stock?.code || stockAnalysisResult?.stockCode || '当前' }})</h3>
              </div>
              <div class="right">
                <el-button size="small" text type="primary" @click="openKlineLarge(klineCandles)">全屏</el-button>
              </div>
            </div>
          </template>
          <KLineChart :candles="klineCandles" :height="520" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="stockDetailDialogVisible" width="92%" :show-close="false" @close="handleStockDetailClose">
      <template #header>
        <div class="dialog-header">
          <span>分析详情：{{ selectedStockHistory?.stockCode }}</span>
          <el-button text type="primary" @click="handleStockDetailClose">关闭</el-button>
        </div>
      </template>
      <div class="dialog-body" v-if="selectedStockHistory">
        <p class="dialog-meta">
          {{ formatAnalysisType(selectedStockHistory.analysisType) }} · {{ formatDate(selectedStockHistory.requestedAt) }}
        </p>
        <template v-if="selectedStockHistory.analysisType === 'STRUCTURED' && selectedStructuredHistoryData">
          <StockAdvicePanel :data="selectedStructuredHistoryData" />
          <div class="dialog-divider"></div>
          <div class="kline-header">
            <h4 class="section-sub-title">K线图</h4>
            <el-button v-if="selectedHistoryCandles.length" size="small" text type="primary" @click="openKlineLarge(selectedHistoryCandles)">全屏</el-button>
          </div>
          <el-empty v-if="!selectedHistoryCandles.length" description="无时间序列" />
          <KLineChart v-else :candles="selectedHistoryCandles" :height="460" />
        </template>
        <pre v-else>{{ toReadableText(selectedStockHistory.analysis) }}</pre>
      </div>
    </el-dialog>

    <!-- Fullscreen enlarged K-line Dialog -->
    <el-dialog v-model="klineDialogVisible" fullscreen :show-close="true" @close="closeKlineLarge">
      <template #header>
        <div class="dialog-header">
          <span>K线全屏视图</span>
          <el-button text type="primary" @click="closeKlineLarge">关闭</el-button>
        </div>
      </template>
      <div class="kline-full-wrapper" v-if="largeCandles.length">
        <KLineChart :candles="largeCandles" :height="calcFullscreenHeight()" />
      </div>
      <el-empty v-else description="暂无数据" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';

import StockAdvicePanel from '../components/StockAdvicePanel.vue';
import KLineChart from '../components/KLineChart.vue';
import { analyzeStock, fetchHistory, toExtractedJson, extractTimeSeries } from '../api/stockAnalysis';
import type {
  ExtractedJson,
  SingleStockApiResponse,
  StockAnalysisResponse,
  TimeSeriesCandle
} from '../types';
import { formatDate, toReadableText } from '../utils/display';
import { extractErrorMessage } from '../utils/errors';

interface StockFormModel {
  stockCode: string;
}

const stockFormRef = ref<FormInstance>();
const stockForm = reactive<StockFormModel>({ stockCode: '' });
const stockLoading = ref(false);
const singleStockLoading = ref(false);
const stockHistoryLoading = ref(false);
const stockAnalysisResult = ref<StockAnalysisResponse | null>(null);
const singleStockData = ref<ExtractedJson | null>(null);
const klineCandles = ref<TimeSeriesCandle[]>([]);
const stockHistory = ref<StockAnalysisResponse[]>([]);
const stockDetailDialogVisible = ref(false);
const selectedStockHistory = ref<StockAnalysisResponse | null>(null);
const largeCandles = ref<TimeSeriesCandle[]>([]);
const klineDialogVisible = ref(false);

const analysisTypeLabel: Record<StockAnalysisResponse['analysisType'], string> = {
  COMPREHENSIVE: '综合分析',
  STRUCTURED: '结构化分析'
};

const selectedStructuredHistoryData = computed<ExtractedJson | null>(() => {
  if (!selectedStockHistory.value || selectedStockHistory.value.analysisType !== 'STRUCTURED') {
    return null;
  }
  return toExtractedJson(selectedStockHistory.value.analysis as SingleStockApiResponse);
});

const selectedHistoryCandles = computed<TimeSeriesCandle[]>(() => {
  if (!selectedStockHistory.value || selectedStockHistory.value.analysisType !== 'STRUCTURED') return [];
  try {
    return extractTimeSeries(selectedStockHistory.value.analysis as SingleStockApiResponse);
  } catch {
    return [];
  }
});

const stockRules = reactive<FormRules<StockFormModel>>({
  stockCode: [
    { required: true, message: '请输入股票代码', trigger: 'blur' },
    {
      pattern: /^[A-Za-z]{1,10}$/,
      message: '仅支持 1-10 位英文字母的股票代码',
      trigger: 'blur'
    }
  ]
});

async function submitStockForm() {
  if (!stockFormRef.value) {
    return;
  }
  try {
    await stockFormRef.value.validate();
  } catch {
    return;
  }
  stockLoading.value = true;
  try {
    const code = stockForm.stockCode.trim().toUpperCase();
    const response = await analyzeStock({ stockCode: code, analysisType: 'COMPREHENSIVE' });
    stockAnalysisResult.value = response;
    stockForm.stockCode = '';
    ElMessage.success(`已生成 ${response.stockCode} 的综合分析`);
    await refreshStockHistory();
  } catch (error) {
    ElMessage.error(extractErrorMessage(error));
  } finally {
    stockLoading.value = false;
  }
}

async function submitSingleStock() {
  if (!stockFormRef.value) {
    return;
  }
  try {
    await stockFormRef.value.validate();
  } catch {
    return;
  }
  singleStockLoading.value = true;
  try {
    const code = stockForm.stockCode.trim().toUpperCase();
    const response = await analyzeStock({ stockCode: code, analysisType: 'STRUCTURED' });
    singleStockData.value = toExtractedJson(response.analysis as SingleStockApiResponse);
    klineCandles.value = extractTimeSeries(response.analysis as SingleStockApiResponse);
    ElMessage.success(`已生成 ${code} 的结构化分析`);
    await refreshStockHistory();
  } catch (error) {
    singleStockData.value = null;
    klineCandles.value = [];
    ElMessage.error(extractErrorMessage(error));
  } finally {
    singleStockLoading.value = false;
  }
}

function openStockDetail(record: StockAnalysisResponse) {
  selectedStockHistory.value = record;
  stockDetailDialogVisible.value = true;
}

function handleStockDetailClose() {
  stockDetailDialogVisible.value = false;
  selectedStockHistory.value = null;
}

function openKlineLarge(candles: TimeSeriesCandle[]) {
  largeCandles.value = candles.slice();
  klineDialogVisible.value = true;
  // slight delay to ensure dialog renders before chart init (handled internally by chart watch)
}

function closeKlineLarge() {
  klineDialogVisible.value = false;
  largeCandles.value = [];
}

async function refreshStockHistory() {
  stockHistoryLoading.value = true;
  try {
    stockHistory.value = await fetchHistory();
  } catch (error) {
    ElMessage.warning(extractErrorMessage(error));
  } finally {
    stockHistoryLoading.value = false;
  }
}

function formatAnalysisType(type: StockAnalysisResponse['analysisType']) {
  return analysisTypeLabel[type] ?? type;
}

function calcFullscreenHeight() {
  return Math.max(window.innerHeight - 140, 500); // dynamic height
}

onMounted(async () => {
  await refreshStockHistory();
});
</script>

<style scoped>
.stock-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-card,
.history-card {
  height: 100%;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(50, 71, 91, 0.12);
  border: none;
  background: #fff;
}

.card-title {
  font-weight: 700;
  font-size: 18px;
  color: #32475b;
  letter-spacing: 0.5px;
}

.analysis-form {
  margin-bottom: 16px;
}

.el-form-item__label {
  font-weight: 600;
  color: #32475b;
}

.el-input__wrapper {
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(50, 71, 91, 0.08);
}

.el-button {
  border-radius: 8px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.ml8 {
  margin-left: 8px;
}

.analysis-result {
  background: #f7f9fc;
  border-radius: 10px;
  padding: 16px 14px;
  box-shadow: 0 1px 4px rgba(50, 71, 91, 0.08);
  margin-bottom: 8px;
}

.analysis-result__meta {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-bottom: 8px;
  color: #3a8ee6;
  font-weight: 500;
}

.analysis-result__scroll {
  max-height: 260px;
  background: #f0f4fa;
  border-radius: 6px;
  padding: 8px 10px;
}

.analysis-result pre {
  margin: 0;
  font-family: 'Fira Code', Consolas, monospace;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 15px;
  color: #222;
}

.section-title {
  margin: 0 0 12px;
  font-size: 17px;
  font-weight: 600;
  color: #3a8ee6;
  letter-spacing: 0.5px;
}

.section-sub-title {
  margin: 16px 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: #32475b;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 700;
  font-size: 17px;
  color: #32475b;
}

.dialog-body pre {
  margin: 0;
  font-family: 'Fira Code', Consolas, monospace;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 15px;
  color: #222;
}

.dialog-meta {
  margin-bottom: 16px;
  color: #6b7a90;
}

.dialog-divider {
  height: 1px;
  background: #e0e6ed;
  margin: 16px 0;
}

.kline-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
}

.kline-row { margin-top: 8px; }
.kline-card { border-radius: 16px; }
.kline-card-header { display: flex; justify-content: space-between; align-items: center; }
.kline-title { margin: 0; }
.kline-full-wrapper :deep(.kline-chart) { height: 100%; }

@media (max-width: 900px) {
  .stock-page {
    gap: 16px;
  }
}
</style>
