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
            {{ selectedStockHistory.stockCode }} · {{ formatDate(selectedStockHistory.requestedAt) }}
          </span>
        </div>
      </template>
      <div v-if="selectedStockHistory" class="dialog-body">
        <el-scrollbar max-height="400px">
          <pre>{{ toReadableText(selectedStockHistory.analysis) }}</pre>
        </el-scrollbar>
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
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import type { AxiosError } from 'axios';

import { analyzeStock, fetchHistory as fetchStockHistory } from './api/stockAnalysis';
import { analyzeNews, fetchNewsHistory } from './api/newsAnalysis';
import type {
  NewsAnalysisRequest,
  NewsAnalysisResponse,
  StockAnalysisRequest,
  StockAnalysisResponse
} from './types';

const stockFormRef = ref<FormInstance>();
const stockForm = reactive<StockAnalysisRequest>({ stockCode: '' });
const stockLoading = ref(false);
const stockHistoryLoading = ref(false);
const stockAnalysisResult = ref<StockAnalysisResponse | null>(null);
const stockHistory = ref<StockAnalysisResponse[]>([]);
const stockDetailDialogVisible = ref(false);
const selectedStockHistory = ref<StockAnalysisResponse | null>(null);

const newsFormRef = ref<FormInstance>();
const newsForm = reactive<NewsAnalysisRequest>({ keyword: '' });
const newsLoading = ref(false);
const newsHistoryLoading = ref(false);
const newsAnalysisResult = ref<NewsAnalysisResponse | null>(null);
const newsHistory = ref<NewsAnalysisResponse[]>([]);
const newsDetailDialogVisible = ref(false);
const selectedNewsHistory = ref<NewsAnalysisResponse | null>(null);

const stockRules = reactive<FormRules<StockAnalysisRequest>>({
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
    const payload = { stockCode: stockForm.stockCode.trim().toUpperCase() };
    const response = await analyzeStock(payload);
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
  if (value === null || value === undefined) {
    return '暂无分析内容';
  }
  if (Array.isArray(value)) {
    const merged = value
      .map((item) => {
        if (typeof item === 'string') {
          return item;
        }
        if (item && typeof item === 'object') {
          const maybeText = (item as Record<string, unknown>).text;
          if (typeof maybeText === 'string') {
            return maybeText;
          }
        }
        return JSON.stringify(item, null, 2);
      })
      .filter((item) => Boolean(item))
      .join('\n\n');
    return formatMultilineContent(merged);
  }
  if (typeof value === 'object') {
    const maybeText = (value as Record<string, unknown>).text;
    if (typeof maybeText === 'string') {
      return formatMultilineContent(maybeText);
    }
    return formatMultilineContent(JSON.stringify(value, null, 2));
  }
  if (typeof value === 'string') {
    return formatMultilineContent(value);
  }
  return String(value);
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
