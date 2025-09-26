<template>
  <div class="news-page">
    <el-row :gutter="20">
      <el-col :xs="24" :lg="12">
        <el-card class="form-card" shadow="hover">
          <template #header>
            <div class="card-title">执行新闻分析</div>
          </template>
          <el-form
            ref="newsFormRef"
            :model="newsForm"
            :rules="newsRules"
            label-position="top"
            class="analysis-form"
          >
            <el-form-item label="关键词" prop="keyword">
              <el-input
                v-model="newsForm.keyword"
                maxlength="60"
                placeholder="示例：人工智能"
                @keyup.enter="submitNewsForm"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="newsLoading" @click="submitNewsForm">分析新闻</el-button>
            </el-form-item>
          </el-form>
          <el-divider />
          <section>
            <h3 class="section-title">最新分析结果</h3>
            <el-empty v-if="!newsAnalysisResult" description="提交关键词后将显示分析结果" />
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
        <el-card class="history-card" shadow="hover">
          <template #header>
            <div class="card-title">近期新闻分析</div>
          </template>
          <el-table
            v-if="newsHistory.length"
            :data="newsHistory"
            :loading="newsHistoryLoading"
            border
            height="520"
          >
            <el-table-column prop="keyword" label="关键词" width="200" />
            <el-table-column label="生成时间" width="200">
              <template #default="scope">
                {{ formatDate(scope.row.requestedAt) }}
              </template>
            </el-table-column>
            <el-table-column label="详情" min-width="180" align="center">
              <template #default="scope">
                <el-button type="primary" link @click="openNewsDetail(scope.row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else description="暂无新闻历史" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="newsDetailDialogVisible" width="60%" :show-close="false" @close="handleNewsDetailClose">
      <template #header>
        <div class="dialog-header">
          <span>新闻洞察：{{ selectedNewsHistory?.keyword }}</span>
          <el-button text type="primary" @click="handleNewsDetailClose">关闭</el-button>
        </div>
      </template>
      <div class="dialog-body" v-if="selectedNewsHistory">
        <p class="dialog-meta">{{ formatDate(selectedNewsHistory.requestedAt) }}</p>
        <pre>{{ toReadableText(selectedNewsHistory.analysis) }}</pre>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';

import { analyzeNews, fetchNewsHistory } from '../api/newsAnalysis';
import type { NewsAnalysisRequest, NewsAnalysisResponse } from '../types';
import { formatDate, toReadableText } from '../utils/display';
import { extractErrorMessage } from '../utils/errors';

const newsFormRef = ref<FormInstance>();
const newsForm = reactive<NewsAnalysisRequest>({ keyword: '' });
const newsLoading = ref(false);
const newsHistoryLoading = ref(false);
const newsAnalysisResult = ref<NewsAnalysisResponse | null>(null);
const newsHistory = ref<NewsAnalysisResponse[]>([]);
const newsDetailDialogVisible = ref(false);
const selectedNewsHistory = ref<NewsAnalysisResponse | null>(null);

const newsRules = reactive<FormRules<NewsAnalysisRequest>>({
  keyword: [{ required: true, message: '请输入关键词', trigger: 'blur' }]
});

async function submitNewsForm() {
  if (!newsFormRef.value) {
    return;
  }
  try {
    await newsFormRef.value.validate();
  } catch {
    return;
  }
  newsLoading.value = true;
  try {
    const payload = { keyword: newsForm.keyword.trim() };
    const response = await analyzeNews(payload);
    newsAnalysisResult.value = response;
    newsForm.keyword = '';
    ElMessage.success(`已生成关于“${response.keyword}”的新闻分析`);
    await refreshNewsHistory();
  } catch (error) {
    ElMessage.error(extractErrorMessage(error));
  } finally {
    newsLoading.value = false;
  }
}

function openNewsDetail(record: NewsAnalysisResponse) {
  selectedNewsHistory.value = record;
  newsDetailDialogVisible.value = true;
}

function handleNewsDetailClose() {
  newsDetailDialogVisible.value = false;
  selectedNewsHistory.value = null;
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

onMounted(async () => {
  await refreshNewsHistory();
});
</script>

<style scoped>
.news-page {
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

.section-title {
  margin: 0 0 12px;
  font-size: 17px;
  font-weight: 600;
  color: #3a8ee6;
  letter-spacing: 0.5px;
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

.analysis-result pre,
.dialog-body pre {
  margin: 0;
  font-family: 'Fira Code', Consolas, monospace;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 15px;
  color: #222;
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 700;
  font-size: 17px;
  color: #32475b;
}

.dialog-meta {
  margin-bottom: 16px;
  color: #6b7a90;
}

@media (max-width: 900px) {
  .news-page {
    gap: 16px;
  }
}
</style>
