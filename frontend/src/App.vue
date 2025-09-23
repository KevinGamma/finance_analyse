<template>
  <div class="app-shell">
    <el-container>
      <el-header class="app-header">
        <div class="app-header__content">
          <h1>金融分析平台</h1>
          <p>借助 n8n 工作流快速获取股票洞见</p>
        </div>
      </el-header>
      <el-main>
        <el-row :gutter="20">
          <el-col :xs="24" :lg="12">
            <el-card class="form-card">
              <template #header>
                <div class="card-title">股票即时分析</div>
              </template>
              <el-form
                ref="formRef"
                :model="form"
                :rules="rules"
                label-position="top"
                class="analysis-form"
              >
                <el-form-item label="股票代码" prop="stockCode">
                  <el-input
                    v-model="form.stockCode"
                    maxlength="4"
                    placeholder="请输入四位股票代码，例如 AAPL"
                    @keyup.enter="submitForm"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="loading" @click="submitForm">
                    开始分析
                  </el-button>
                </el-form-item>
              </el-form>
              <el-divider />
              <section>
                <h2 class="section-title">最新分析结果</h2>
                <el-empty v-if="!analysisResult" description="请先执行分析以查看结果" />
                <div v-else class="analysis-result">
                  <p class="analysis-result__meta">
                    <strong>{{ analysisResult.stockCode }}</strong>
                    <span>{{ formatDate(analysisResult.requestedAt) }}</span>
                  </p>
                  <el-scrollbar class="analysis-result__scroll">
                    <pre>{{ toReadableText(analysisResult.analysis) }}</pre>
                  </el-scrollbar>
                </div>
              </section>
            </el-card>
          </el-col>
          <el-col :xs="24" :lg="12">
            <el-card class="history-card">
              <template #header>
                <div class="card-title">历史分析记录</div>
              </template>
              <el-table
                v-if="history.length"
                :data="history"
                :loading="historyLoading"
                border
                height="520"
              >
                <el-table-column prop="stockCode" label="股票" width="120" />
                <el-table-column label="请求时间" width="200">
                  <template #default="scope">
                    {{ formatDate(scope.row.requestedAt) }}
                  </template>
                </el-table-column>
                <el-table-column label="分析摘要" min-width="220" align="center">
                  <template #default="scope">
                    <el-button type="primary" link @click="openDetail(scope.row)">
                      查看详情
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-empty v-else description="暂无分析记录" />
            </el-card>
          </el-col>
        </el-row>
      </el-main>
    </el-container>

    <el-dialog
      v-model="detailDialogVisible"
      width="640px"
      :destroy-on-close="true"
      @close="handleDetailClose"
    >
      <template #header>
        <div class="dialog-header">
          <span>分析详情</span>
          <span v-if="selectedHistory">
            （{{ selectedHistory.stockCode }} · {{ formatDate(selectedHistory.requestedAt) }}）
          </span>
        </div>
      </template>
      <div v-if="selectedHistory" class="dialog-body">
        <el-scrollbar max-height="400px">
          <pre>{{ toReadableText(selectedHistory.analysis) }}</pre>
        </el-scrollbar>
      </div>
      <template #footer>
        <el-button type="primary" @click="handleDetailClose">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import type { AxiosError } from 'axios';

import { analyzeStock, fetchHistory } from './api/stockAnalysis';
import type { StockAnalysisRequest, StockAnalysisResponse } from './types';

const formRef = ref<FormInstance>();
const form = reactive<StockAnalysisRequest>({ stockCode: '' });
const loading = ref(false);
const historyLoading = ref(false);
const analysisResult = ref<StockAnalysisResponse | null>(null);
const history = ref<StockAnalysisResponse[]>([]);
const detailDialogVisible = ref(false);
const selectedHistory = ref<StockAnalysisResponse | null>(null);

const rules = reactive<FormRules<StockAnalysisRequest>>({
  stockCode: [
    { required: true, message: '请输入股票代码', trigger: 'blur' },
    {
      pattern: /^[A-Za-z]{4}$/,
      message: '仅支持四位字母代码，例如 AAPL',
      trigger: 'blur'
    }
  ]
});

async function submitForm() {
  if (!formRef.value) {
    return;
  }
  try {
    await formRef.value.validate();
  } catch (validationError) {
    return;
  }

  loading.value = true;
  try {
    const payload = { stockCode: form.stockCode.trim().toUpperCase() };
    const response = await analyzeStock(payload);
    analysisResult.value = response;
    form.stockCode = '';
    ElMessage.success(`已获取 ${response.stockCode} 的分析结果`);
    await refreshHistory();
  } catch (error) {
    ElMessage.error(extractErrorMessage(error));
  } finally {
    loading.value = false;
  }
}

function openDetail(record: StockAnalysisResponse) {
  selectedHistory.value = record;
  detailDialogVisible.value = true;
}

function handleDetailClose() {
  detailDialogVisible.value = false;
  selectedHistory.value = null;
}

async function refreshHistory() {
  historyLoading.value = true;
  try {
    history.value = await fetchHistory();
  } catch (error) {
    ElMessage.warning(extractErrorMessage(error));
  } finally {
    historyLoading.value = false;
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
  if (!value) {
    return '暂无返回数据';
  }
  if (typeof value === 'string') {
    return removeDelimiters(value.trim());
  }
  try {
    const text = JSON.stringify(value, null, 2);
    return removeDelimiters(text);
  } catch (error) {
    return removeDelimiters(String(value));
  }
}

function toPreview(value: unknown) {
  const readable = toReadableText(value).replace(/\s+/g, ' ').trim();
  return readable.length > 60 ? `${readable.slice(0, 57)}...` : readable;
}

function removeDelimiters(content: string) {
  if (!content) {
    return content;
  }
  return content
    .replace(/[{}\[\]"]/g, '')
    .replace(/\n\s*\n/g, '\n')
    .trim();
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
    return '请求超时，请稍后重试';
  }
  if (axiosError.message?.toLowerCase().includes('timeout')) {
    return '请求超时，请稍后重试';
  }
  return '请求失败，请稍后重试';
}

onMounted(async () => {
  await refreshHistory();
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
</style>
