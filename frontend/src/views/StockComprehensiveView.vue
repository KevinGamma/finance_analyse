<template>
  <div class="stock-comprehensive">
    <el-card class="form-card" shadow="hover">
      <template #header>
        <div class="card-title">综合分析</div>
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
          <el-button type="primary" :loading="stockLoading" @click="submitStockForm">生成综合分析</el-button>
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
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { analyzeStock } from '../api/stockAnalysis';
import type { StockAnalysisResponse } from '../types';
import { formatDate, toReadableText } from '../utils/display';
import { extractErrorMessage } from '../utils/errors';

interface StockFormModel { stockCode: string }

const stockFormRef = ref<FormInstance>();
const stockForm = reactive<StockFormModel>({ stockCode: '' });
const stockLoading = ref(false);
const stockAnalysisResult = ref<StockAnalysisResponse | null>(null);

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
  if (!stockFormRef.value) return;
  try { await stockFormRef.value.validate(); } catch { return; }
  stockLoading.value = true;
  try {
    const code = stockForm.stockCode.trim().toUpperCase();
    const response = await analyzeStock({ stockCode: code, analysisType: 'COMPREHENSIVE' });
    stockAnalysisResult.value = response;
    stockForm.stockCode = '';
    ElMessage.success(`已生成 ${response.stockCode} 的综合分析`);
  } catch (error) {
    ElMessage.error(extractErrorMessage(error));
  } finally {
    stockLoading.value = false;
  }
}
</script>

<style scoped>
.stock-comprehensive {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.form-card { border-radius: 16px; }
.card-title { font-weight: 700; font-size: 18px; color: #32475b; }
.analysis-form { margin-bottom: 16px; }
.section-title { margin: 0 0 12px; font-size: 16px; font-weight: 600; color: #3a8ee6; }
.analysis-result { background: #f7f9fc; border-radius: 10px; padding: 16px 14px; }
.analysis-result__meta { display: flex; justify-content: space-between; font-size: 14px; margin-bottom: 8px; color: #3a8ee6; font-weight: 500; }
.analysis-result__scroll { max-height: 320px; background: #f0f4fa; border-radius: 6px; padding: 8px 10px; }
.analysis-result pre { margin: 0; font-family: 'Fira Code', Consolas, monospace; white-space: pre-wrap; word-break: break-word; font-size: 15px; color: #222; }
</style>
