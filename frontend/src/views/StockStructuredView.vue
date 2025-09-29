<template>
  <div class="stock-structured">
    <el-card class="form-card" shadow="hover">
      <template #header><div class="card-title">结构化分析</div></template>
      <el-form ref="stockFormRef" :model="stockForm" :rules="stockRules" label-position="top" class="analysis-form">
        <el-form-item label="股票代码" prop="stockCode">
          <el-input v-model="stockForm.stockCode" maxlength="10" placeholder="示例：AAPL" @keyup.enter="submit" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="submit">生成结构化分析</el-button>
        </el-form-item>
      </el-form>
      <el-divider />
      <section>
        <h3 class="section-title">结构化速览</h3>
        <el-empty v-if="!data" description="提交股票代码后将显示结构化要点" />
        <template v-else>
          <StockAdvicePanel :data="data" />
          <div class="kline-wrap">
            <h4 class="section-sub-title">提取的K线</h4>
            <el-empty v-if="!candles.length" description="无时间序列" />
            <KLineChart v-else :candles="candles" :height="420" />
          </div>
        </template>
      </section>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';
import { analyzeStock, toExtractedJson, extractTimeSeries } from '../api/stockAnalysis';
import type { ExtractedJson, SingleStockApiResponse, TimeSeriesCandle } from '../types';
import { extractErrorMessage } from '../utils/errors';
import StockAdvicePanel from '../components/StockAdvicePanel.vue';
import KLineChart from '../components/KLineChart.vue';

interface StockFormModel { stockCode: string }

const stockFormRef = ref<FormInstance>();
const stockForm = reactive<StockFormModel>({ stockCode: '' });
const loading = ref(false);
const data = ref<ExtractedJson | null>(null);
const candles = ref<TimeSeriesCandle[]>([]);

const stockRules = reactive<FormRules<StockFormModel>>({
  stockCode: [
    { required: true, message: '请输入股票代码', trigger: 'blur' },
    { pattern: /^[A-Za-z]{1,10}$/, message: '仅支持 1-10 位英文字母的股票代码', trigger: 'blur' }
  ]
});

async function submit() {
  if (!stockFormRef.value) return;
  try { await stockFormRef.value.validate(); } catch { return; }
  loading.value = true;
  try {
    const code = stockForm.stockCode.trim().toUpperCase();
    const resp = await analyzeStock({ stockCode: code, analysisType: 'STRUCTURED' });
    data.value = toExtractedJson(resp.analysis as SingleStockApiResponse);
    candles.value = extractTimeSeries(resp.analysis as SingleStockApiResponse);
    ElMessage.success(`已生成 ${code} 的结构化分析`);
  } catch (err) {
    data.value = null; candles.value = [];
    ElMessage.error(extractErrorMessage(err));
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.stock-structured { display: flex; flex-direction: column; gap: 16px; }
.form-card { border-radius: 16px; }
.card-title { font-weight: 700; font-size: 18px; color: #32475b; }
.analysis-form { margin-bottom: 16px; }
.section-title { margin: 0 0 12px; font-size: 16px; font-weight: 600; color: #3a8ee6; }
.section-sub-title { margin: 12px 0; font-size: 14px; font-weight: 600; color: #32475b; }
.kline-wrap { margin-top: 12px; }
</style>
