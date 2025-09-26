<template>
  <div class="app-shell">
    <el-container>
      <el-header class="app-header">
        <div class="app-header__content">
          <h1>财经智能分析平台</h1>
          <p>整合 n8n 获取股票与新闻的即时洞察</p>
        </div>
      </el-header>
      <el-main>
        <el-row :gutter="20">
          <el-col :xs="24" :lg="12">
            <el-card class="form-card">
              <template #header>
                <div class="card-title">股票实时分析</div>
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
                    maxlength="4"
                    placeholder="请输入四位股票代码，例如 AAPL"
                    @keyup.enter="submitStockForm"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="stockLoading" @click="submitStockForm">
                    开始分析
                  </el-button>
                  <el-button class="ml8" :loading="singleStockLoading" @click="submitSingleStock">
                    结构化分析
                  </el-button>
                </el-form-item>
              </el-form>
              <el-divider />
              <section>
                <h2 class="section-title">最新分析结果</h2>
                <el-empty v-if="!stockAnalysisResult" description="提交股票代码以获取分析" />
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
                <h2 class="section-title">结构化分析面板</h2>
                <el-empty v-if="!singleStockData" description="点击上方“结构化分析”获取可视化面板" />
                <StockAdvicePanel v-else :data="singleStockData" />
              </section>
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-card class="history-card">
              <template #header>
                <div class="card-title">股票历史记录</div>
              </template>
              <el-table
                v-if="stockHistory.length"
                :data="stockHistory"
                :loading="stockHistoryLoading"
                border
                height="520"
              >
                <el-table-column prop="stockCode" label="股票" width="120" />
                <el-table-column prop="analysisType" label="分析类型" width="140">
                  <template #default="scope">
                    {{ formatAnalysisType(scope.row.analysisType) }}
                  </template>
                </el-table-column>
                <el-table-column label="分析时间" width="200">
                  <template #default="scope">
                    {{ formatDate(scope.row.requestedAt) }}
                  </template>
                </el-table-column>
                <el-table-column label="分析摘要" min-width="220" align="center">
                  <template #default="scope">
                    <el-button type="primary" link @click="openStockDetail(scope.row)">
                      查看详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-else description="暂无分析记录" />
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="section-gap">
          <el-col :xs="24" :lg="12">
            <el-card class="form-card">
              <template #header>
                <div class="card-title">新闻关键字分析</div>
              </template>
              <el-form
                ref="newsFormRef"
                :model="newsForm"
                :rules="newsRules"
                label-position="top"
                class="analysis-form"
              >
                <el-form-item label="关键字" prop="keyword">
                  <el-input
                    v-model="newsForm.keyword"
                    maxlength="60"
                    placeholder="请输入要跟踪的新闻关键字"
                    @keyup.enter="submitNewsForm"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="newsLoading" @click="submitNewsForm">
                    获取新闻分析
                  </el-button>
                </el-form-item>
              </el-form>
              <el-divider />
              <section>
                <h2 class="section-title">最新新闻洞察</h2>
                <el-empty v-if="!newsAnalysisResult" description="提交关键字以获取新闻分析" />
                <div v-else class="analysis-result">
                  <p class="analysis-result__meta">
                    <strong>{{ newsAnalysisResult.keyword }}</strong>
                    <span>{{ formatDate(newsAnalysisResult.requestedAt) }}</span>
                  </p>
                  <el-scrollbar class="analysis-result__scroll">
                    <pre>{{ toReadableText(newsAnalysisResult.analysis) }}</pre>
                  </el-scrollbar>
                </div>
              </section>
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-card class="history-card">
              <template #header>
                <div class="card-title">新闻分析历史</div>
              </template>
              <el-table
                v-if="newsHistory.length"
                :data="newsHistory"
                :loading="newsHistoryLoading"
                border
                height="520"
              >
                <el-table-column prop="keyword" label="关键字" width="160" />
                <el-table-column label="分析时间" width="200">
                  <template #default="scope">
                    {{ formatDate(scope.row.requestedAt) }}
                  </template>
                </el-table-column>
                <el-table-column label="分析摘要" min-width="220" align="center">
                  <template #default="scope">
                    <el-button type="primary" link @click="openNewsDetail(scope.row)">
                      查看详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-else description="暂无新闻分析记录" />
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>

    <el-dialog
      v-model="stockDetailDialogVisible"
      width="640px"
      :destroy-on-close="true"
      @close="handleStockDetailClose"
    >
      <template #header>
        <div class="dialog-header">
          <span>股票分析详情</span>
          <span v-if="selectedStockHistory">
            {{ selectedStockHistory.stockCode }} · {{ formatDate(selectedStockHistory.requestedAt) }} · {{ formatAnalysisType(selectedStockHistory.analysisType) }}
          </span>
        </div>
      </template>
      <div v-if="selectedStockHistory" class="dialog-body">
        <template v-if="selectedStockHistory.analysisType === 'COMPREHENSIVE'">
          <el-scrollbar max-height="400px">
            <pre>{{ toReadableText(selectedStockHistory.analysis) }}</pre>
          </el-scrollbar>
        </template>
        <template v-else>
          <StockAdvicePanel :data="selectedStructuredHistoryData" />
        </template>
      </div>
      <template #footer>
        <el-button type="primary" @click="handleStockDetailClose">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="newsDetailDialogVisible"
      width="640px"
      :destroy-on-close="true"
      @close="handleNewsDetailClose"
    >
      <template #header>
        <div class="dialog-header">
          <span>新闻分析详情</span>
          <span v-if="selectedNewsHistory">
            {{ selectedNewsHistory.keyword }} · {{ formatDate(selectedNewsHistory.requestedAt) }}
          </span>
        </div>
      </template>
      <div v-if="selectedNewsHistory" class="dialog-body">
        <el-scrollbar max-height="400px">
          <pre>{{ toReadableText(selectedNewsHistory.analysis) }}</pre>
        </el-scrollbar>
      </div>
      <template #footer>
        <el-button type="primary" @click="handleNewsDetailClose">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import type { AxiosError } from 'axios';

import { analyzeStock, fetchHistory as fetchStockHistory, toExtractedJson } from './api/stockAnalysis';
import { analyzeNews, fetchNewsHistory } from './api/newsAnalysis';
import type {
  NewsAnalysisRequest,
  NewsAnalysisResponse,
  StockAnalysisResponse,
  SingleStockApiResponse,
  ExtractedJson
} from './types';
import StockAdvicePanel from './components/StockAdvicePanel.vue';

interface StockFormModel { stockCode: string }

const stockFormRef = ref<FormInstance>();
const stockForm = reactive<StockFormModel>({ stockCode: '' });
const stockLoading = ref(false);
const singleStockLoading = ref(false);
const stockHistoryLoading = ref(false);
const stockAnalysisResult = ref<StockAnalysisResponse | null>(null);
const singleStockData = ref<ExtractedJson | null>(null);
const stockHistory = ref<StockAnalysisResponse[]>([]);
const stockDetailDialogVisible = ref(false);
const selectedStockHistory = ref<StockAnalysisResponse | null>(null);

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

function formatAnalysisType(type: StockAnalysisResponse['analysisType']) {
  return analysisTypeLabel[type] ?? type;
}

const newsFormRef = ref<FormInstance>();
const newsForm = reactive<NewsAnalysisRequest>({ keyword: '' });
const newsLoading = ref(false);
const newsHistoryLoading = ref(false);
const newsAnalysisResult = ref<NewsAnalysisResponse | null>(null);
const newsHistory = ref<NewsAnalysisResponse[]>([]);
const newsDetailDialogVisible = ref(false);
const selectedNewsHistory = ref<NewsAnalysisResponse | null>(null);

const stockRules = reactive<FormRules<StockFormModel>>({
  stockCode: [
    { required: true, message: '请输入股票代码', trigger: 'blur' },
    {
      pattern: /^[A-Za-z]{4}$/,
      message: '仅支持四位英文字母，例如 AAPL',
      trigger: 'blur'
    }
  ]
});

const newsRules = reactive<FormRules<NewsAnalysisRequest>>({
  keyword: [{ required: true, message: '请输入关键字', trigger: 'blur' }]
});

async function submitStockForm() {
  if (!stockFormRef.value) {
    return;
  }
  try {
    await stockFormRef.value.validate();
  } catch (validationError) {
    return;
  }

  stockLoading.value = true;
  try {
    const code = stockForm.stockCode.trim().toUpperCase();
    const response = await analyzeStock({ stockCode: code, analysisType: 'COMPREHENSIVE' });
    stockAnalysisResult.value = response;
    stockForm.stockCode = '';
    ElMessage.success(`已获取 ${response.stockCode} 的分析结果`);
    await refreshStockHistory();
  } catch (error) {
    ElMessage.error(extractErrorMessage(error));
  } finally {
    stockLoading.value = false;
  }
}

async function submitSingleStock() {
  if (!stockFormRef.value) return;
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
    ElMessage.success(`已获取 ${code} 的结构化分析`);
    await refreshStockHistory();
  } catch (error) {
    singleStockData.value = null;
    ElMessage.error(extractErrorMessage(error));
  } finally {
    singleStockLoading.value = false;
  }
}

async function submitNewsForm() {
  if (!newsFormRef.value) {
    return;
  }
  try {
    await newsFormRef.value.validate();
  } catch (validationError) {
    return;
  }

  newsLoading.value = true;
  try {
    const payload = { keyword: newsForm.keyword.trim() };
    const response = await analyzeNews(payload);
    newsAnalysisResult.value = response;
    newsForm.keyword = '';
    ElMessage.success(`已获取“${response.keyword}”的新闻分析`);
    await refreshNewsHistory();
  } catch (error) {
    ElMessage.error(extractErrorMessage(error));
  } finally {
    newsLoading.value = false;
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

function openNewsDetail(record: NewsAnalysisResponse) {
  selectedNewsHistory.value = record;
  newsDetailDialogVisible.value = true;
}

function handleNewsDetailClose() {
  newsDetailDialogVisible.value = false;
  selectedNewsHistory.value = null;
}

async function refreshStockHistory() {
  stockHistoryLoading.value = true;
  try {
    stockHistory.value = await fetchStockHistory();
  } catch (error) {
    ElMessage.warning(extractErrorMessage(error));
  } finally {
    stockHistoryLoading.value = false;
  }
}

async function refreshNewsHistory() {
  newsHistoryLoading.value = true;
  try {
    newsHistory.value = await fetchNewsHistory();
  } catch (error) {
    ElMessage.warning(extractErrorMessage(error));
  } finally {
    newsHistoryLoading.value = false;
  }
}

function formatDate(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString();
}

function toReadableText(value: unknown) {
  const result = renderValue(value);
  return result || '暂无分析结果';
}

function renderValue(value: unknown): string {
  if (value === null || value === undefined) {
    return '';
  }
  if (typeof value === 'string') {
    const trimmed = value.trim();
    if (trimmed && isJsonLike(trimmed)) {
      try {
        return renderValue(JSON.parse(trimmed));
      } catch (error) {
        return formatMultilineContent(value);
      }
    }
    return formatMultilineContent(value);
  }
  if (typeof value === 'number' || typeof value === 'boolean') {
    return String(value);
  }
  if (Array.isArray(value)) {
    const items = value
      .map((item) => renderValue(item))
      .filter((item) => item.trim().length > 0);
    return items.join('\n\n');
  }
  if (typeof value === 'object') {
    const record = value as Record<string, unknown>;
    const maybeText = record.text;
    if (typeof maybeText === 'string') {
      return formatMultilineContent(maybeText);
    }
    const entries = Object.entries(record)
      .map(([key, val]) => formatEntry(key, val))
      .filter((item) => item.trim().length > 0);
    return entries.join('\n\n');
  }
  return '';
}

function formatEntry(key: string, value: unknown): string {
  const rendered = renderValue(value);
  if (!rendered) {
    return '';
  }
  const label = prettifyKey(key);
  if (rendered.includes('\n')) {
    return `${label}:\n${indentMultiline(rendered)}`;
  }
  return `${label}: ${rendered}`;
}

function prettifyKey(key: string): string {
  return key
    .replace(/[_-]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .replace(/\b\w/g, (match) => match.toUpperCase());
}

function indentMultiline(value: string): string {
  return value
    .split('\n')
    .map((line) => (line.trim() ? `  ${line}` : line))
    .join('\n');
}

function isJsonLike(text: string): boolean {
  const trimmed = text.trim();
  return (
    (trimmed.startsWith('{') && trimmed.endsWith('}')) ||
    (trimmed.startsWith('[') && trimmed.endsWith(']'))
  );
}

function formatMultilineContent(content: string) {
  return content.replace(/\\n/g, '\n').trim();
}

function extractErrorMessage(error: unknown) {
  if (typeof error === 'string' && error.trim()) {
    return error;
  }
  const axiosError = error as AxiosError<{ message?: string }>;
  if (axiosError.response?.data?.message) {
    return axiosError.response.data.message;
  }
  if (axiosError.code === 'ECONNABORTED') {
    return '请求超时，请稍后再试';
  }
  if (axiosError.message?.toLowerCase().includes('timeout')) {
    return '请求超时，请稍后再试';
  }
  return '请求失败，请稍后再试';
}

onMounted(async () => {
  await Promise.all([refreshStockHistory(), refreshNewsHistory()]);
});
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.app-header {
  background: #1f2d3d;
  color: #fff;
  display: flex;
  align-items: center;
}

.app-header__content h1 {
  margin: 0;
  font-size: 24px;
}

.app-header__content p {
  margin: 4px 0 0;
  opacity: 0.8;
}

.el-main {
  padding: 24px;
}

.form-card,
.history-card {
  height: 100%;
}

.card-title {
  font-weight: 600;
}

.analysis-form {
  margin-bottom: 16px;
}

.ml8 { margin-left: 8px; }

.analysis-result {
  background-color: #f7f9fc;
  border-radius: 8px;
  padding: 12px;
}

.analysis-result__meta {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
  margin-bottom: 8px;
}

.analysis-result__scroll {
  max-height: 260px;
}

.analysis-result pre,
.analysis-tooltip {
  margin: 0;
  font-family: 'Fira Code', Consolas, monospace;
  white-space: pre-wrap;
  word-break: break-word;
}

.analysis-preview {
  display: inline-block;
  max-width: 100%;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.analysis-tooltip {
  max-width: 360px;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.dialog-body pre {
  margin: 0;
  font-family: 'Fira Code', Consolas, monospace;
  white-space: pre-wrap;
  word-break: break-word;
}

.section-title {
  margin: 0 0 12px;
  font-size: 16px;
}

.section-gap {
  margin-top: 24px;
}
</style>


