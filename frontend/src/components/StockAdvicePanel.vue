<template>
  <div class="stock-advice-panel" v-if="data">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="block-card">
          <template #header>
            <div class="card-hd">
              <span>基础信息</span>
              <el-tag size="small" type="info" v-if="data.timezone">{{ data.timezone }}</el-tag>
            </div>
          </template>
          <div class="title">
            <span class="code">{{ data.stock?.code || '-' }}</span>
            <span class="name">{{ data.stock?.name || '' }}</span>
          </div>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="市场">{{ data.stock?.market || '-' }}</el-descriptions-item>
            <el-descriptions-item label="货币">{{ data.stock?.currency || '-' }}</el-descriptions-item>
            <el-descriptions-item label="行业">{{ data.stock?.industry || '-' }}</el-descriptions-item>
            <el-descriptions-item label="板块">{{ data.stock?.sector || '-' }}</el-descriptions-item>
            <el-descriptions-item label="日期" :span="2">{{ formatDate(data.date) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="block-card">
          <template #header>
            <div class="card-hd">
              <span>价格概览</span>
              <el-tag size="small" :type="priceChangeTagType">{{ changeText }}</el-tag>
            </div>
          </template>
          <el-descriptions :column="3" size="small" border>
            <el-descriptions-item label="开盘">{{ fmtNum(data.price_data?.open) }}</el-descriptions-item>
            <el-descriptions-item label="收盘">{{ fmtNum(data.price_data?.close) }}</el-descriptions-item>
            <el-descriptions-item label="昨收">{{ fmtNum(data.price_data?.prev_close) }}</el-descriptions-item>
            <el-descriptions-item label="最高">{{ fmtNum(data.price_data?.high) }}</el-descriptions-item>
            <el-descriptions-item label="最低">{{ fmtNum(data.price_data?.low) }}</el-descriptions-item>
            <el-descriptions-item label="波动幅度">{{ fmtNum(data.technical_analysis?.volatility?.daily_range_percent) }}%</el-descriptions-item>
            <el-descriptions-item label="成交量" :span="2">{{ fmtInt(data.price_data?.volume) }}</el-descriptions-item>
            <el-descriptions-item label="20日均量">{{ fmtInt(data.price_data?.avg_volume_20d) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt16">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="block-card">
          <template #header>
            <div class="card-hd">
              <span>技术面</span>
              <el-tag size="small" type="warning" v-if="data.technical_analysis?.candle?.type">{{ data.technical_analysis?.candle?.type }}</el-tag>
            </div>
          </template>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="短期">{{ data.technical_analysis?.trend?.short_term || '-' }}</el-descriptions-item>
            <el-descriptions-item label="中期">{{ data.technical_analysis?.trend?.medium_term || '-' }}</el-descriptions-item>
            <el-descriptions-item label="长期">{{ data.technical_analysis?.trend?.long_term || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ data.technical_analysis?.trend?.notes || '-' }}</el-descriptions-item>
          </el-descriptions>
          <el-descriptions :column="3" size="small" border class="mt8">
            <el-descriptions-item label="MA5">{{ fmtNum(data.technical_analysis?.indicators?.moving_averages?.ma5) }}</el-descriptions-item>
            <el-descriptions-item label="MA10">{{ fmtNum(data.technical_analysis?.indicators?.moving_averages?.ma10) }}</el-descriptions-item>
            <el-descriptions-item label="MA20">{{ fmtNum(data.technical_analysis?.indicators?.moving_averages?.ma20) }}</el-descriptions-item>
            <el-descriptions-item label="RSI">{{ fmtNum(data.technical_analysis?.indicators?.rsi?.value) }}</el-descriptions-item>
            <el-descriptions-item label="MACD">{{ fmtNum(data.technical_analysis?.indicators?.macd?.macd_line) }}</el-descriptions-item>
            <el-descriptions-item label="BOLL位置">{{ data.technical_analysis?.indicators?.bollinger_bands?.position || '-' }}</el-descriptions-item>
          </el-descriptions>
          <div class="sr-box mt8" v-if="hasSR">
            <div>
              <span class="sr-title">支撑</span>
              <el-tag v-for="(s, i) in (data.technical_analysis?.support_resistance?.support || [])" :key="'s'+i" class="mr8 mb8" type="success" effect="light">
                {{ fmtNum(s.level) }} · {{ s.strength || '-' }}
              </el-tag>
            </div>
            <div class="mt8">
              <span class="sr-title">阻力</span>
              <el-tag v-for="(r, i) in (data.technical_analysis?.support_resistance?.resistance || [])" :key="'r'+i" class="mr8 mb8" type="danger" effect="light">
                {{ fmtNum(r.level) }} · {{ r.strength || '-' }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="block-card">
          <template #header>
            <div class="card-hd">
              <span>基本面</span>
            </div>
          </template>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="市值">{{ data.fundamental_analysis?.market_cap ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="PE">{{ fmtNum(data.fundamental_analysis?.pe_ratio) }}</el-descriptions-item>
            <el-descriptions-item label="EPS">{{ fmtNum(data.fundamental_analysis?.eps) }}</el-descriptions-item>
            <el-descriptions-item label="股息率">{{ fmtNum(data.fundamental_analysis?.dividend_yield) }}%</el-descriptions-item>
            <el-descriptions-item label="利润率">{{ fmtNum(data.fundamental_analysis?.profit_margin) }}%</el-descriptions-item>
            <el-descriptions-item label="收入趋势">{{ data.fundamental_analysis?.revenue_trend || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ data.fundamental_analysis?.notes || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt16">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="block-card">
          <template #header>
            <div class="card-hd"><span>成交量分析</span></div>
          </template>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="当日量">{{ fmtInt(data.volume_analysis?.daily_volume) }}</el-descriptions-item>
            <el-descriptions-item label="20日均量">{{ fmtInt(data.volume_analysis?.avg_volume_20d) }}</el-descriptions-item>
            <el-descriptions-item label="趋势">{{ data.volume_analysis?.volume_trend || '-' }}</el-descriptions-item>
            <el-descriptions-item label="量能异动">{{ data.volume_analysis?.volume_spike ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="解读" :span="2">{{ data.volume_analysis?.interpretation || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="block-card">
          <template #header>
            <div class="card-hd"><span>新闻与情绪</span></div>
          </template>
          <el-descriptions :column="3" size="small" border>
            <el-descriptions-item label="新闻数">{{ data.news_sentiment?.news_count ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="积极">{{ data.news_sentiment?.positive ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="消极">{{ data.news_sentiment?.negative ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="中性" :span="2">{{ data.news_sentiment?.neutral ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="总体">{{ data.news_sentiment?.overall_sentiment || '-' }}</el-descriptions-item>
          </el-descriptions>
          <div class="mt8" v-if="data.news_sentiment?.top_headlines?.length">
            <div class="headline" v-for="(h, i) in data.news_sentiment?.top_headlines" :key="i">
              <el-tag size="small" :type="headlineTagType(h.sentiment)">{{ h.sentiment || 'neutral' }}</el-tag>
              <span class="headline-title">{{ h.title }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt16">
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="block-card">
          <template #header>
            <div class="card-hd"><span>操作建议</span></div>
          </template>
          <div class="advice-group">
            <div class="advice-block">
              <div class="advice-hd">短线</div>
              <div class="advice-body">
                <p>{{ data.investment_advice?.short_term?.strategy || '-' }}</p>
                <div class="tags">
                  <span>买入区间：</span>
                  <el-tag v-for="(v, i) in (data.investment_advice?.short_term?.buy_zone || [])" :key="'sb'+i" class="mr8 mb8">{{ fmtNum(v) }}</el-tag>
                </div>
                <div class="tags">
                  <span>止盈区间：</span>
                  <el-tag v-for="(v, i) in (data.investment_advice?.short_term?.take_profit_zone || [])" :key="'sp'+i" class="mr8 mb8" type="success">{{ fmtNum(v) }}</el-tag>
                </div>
                <div class="tags">
                  <span>止损：</span>
                  <el-tag type="danger">{{ fmtNum(data.investment_advice?.short_term?.stop_loss) }}</el-tag>
                </div>
                <div class="tags">
                  <span>风险：</span>
                  <el-tag type="warning">{{ data.investment_advice?.short_term?.risk_level || '-' }}</el-tag>
                </div>
              </div>
            </div>
            <div class="advice-block">
              <div class="advice-hd">中线</div>
              <div class="advice-body">
                <p>{{ data.investment_advice?.medium_term?.strategy || '-' }}</p>
                <div class="tags">
                  <span>买入区间：</span>
                  <el-tag v-for="(v, i) in (data.investment_advice?.medium_term?.buy_zone || [])" :key="'mb'+i" class="mr8 mb8">{{ fmtNum(v) }}</el-tag>
                </div>
                <div class="tags">
                  <span>止盈区间：</span>
                  <el-tag v-for="(v, i) in (data.investment_advice?.medium_term?.take_profit_zone || [])" :key="'mp'+i" class="mr8 mb8" type="success">{{ fmtNum(v) }}</el-tag>
                </div>
                <div class="tags">
                  <span>止损：</span>
                  <el-tag type="danger">{{ fmtNum(data.investment_advice?.medium_term?.stop_loss) }}</el-tag>
                </div>
                <div class="tags">
                  <span>风险：</span>
                  <el-tag type="warning">{{ data.investment_advice?.medium_term?.risk_level || '-' }}</el-tag>
                </div>
              </div>
            </div>
            <div class="advice-block">
              <div class="advice-hd">长线</div>
              <div class="advice-body">
                <p>{{ data.investment_advice?.long_term?.strategy || '-' }}</p>
                <p class="muted">{{ data.investment_advice?.long_term?.risk_notes || '' }}</p>
              </div>
            </div>
          </div>
          <div class="risk-warning" v-if="data.investment_advice?.risk_warning">
            <el-alert :title="data.investment_advice?.risk_warning" type="warning" show-icon />
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="hover" class="block-card">
          <template #header>
            <div class="card-hd"><span>宏观因素</span></div>
          </template>
          <el-descriptions :column="2" size="small" border>
            <el-descriptions-item label="利率">{{ fmtNum(data.macro_economic_factors?.interest_rate) }}%</el-descriptions-item>
            <el-descriptions-item label="通胀">{{ fmtNum(data.macro_economic_factors?.inflation_rate) }}%</el-descriptions-item>
            <el-descriptions-item label="市场情绪" :span="2">{{ data.macro_economic_factors?.market_sentiment || '-' }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ data.macro_economic_factors?.notes || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { ExtractedJson } from '../types';

interface Props { data: ExtractedJson | null | undefined }
const props = defineProps<Props>();

const changeText = computed(() => {
  const c = props.data?.price_data?.daily_change;
  const p = props.data?.price_data?.daily_change_percent;
  if (c == null && p == null) return '-';
  const sign = (c ?? 0) >= 0 ? '+' : '';
  const cp = p != null ? ` (${sign}${p.toFixed(2)}%)` : '';
  return `${sign}${(c ?? 0).toFixed(2)}${cp}`;
});

const priceChangeTagType = computed(() => {
  const p = props.data?.price_data?.daily_change ?? 0;
  return p > 0 ? 'success' : p < 0 ? 'danger' : 'info';
});

const hasSR = computed(() => (props.data?.technical_analysis?.support_resistance?.support?.length || 0) > 0 || (props.data?.technical_analysis?.support_resistance?.resistance?.length || 0) > 0);

function fmtNum(v?: number) { return typeof v === 'number' ? Number(v).toFixed(2) : '-'; }
function fmtInt(v?: number) { return typeof v === 'number' ? Intl.NumberFormat().format(v) : '-'; }
function formatDate(v?: string) { if (!v) return '-'; const d = new Date(v); return isNaN(d.getTime()) ? v : d.toLocaleString(); }
function headlineTagType(s?: string) { if (!s) return 'info'; const t = s.toLowerCase(); return t.includes('pos') ? 'success' : t.includes('neg') ? 'danger' : 'info'; }
</script>

<style scoped>
.stock-advice-panel { width: 100%; }
.block-card { height: 100%; }
.card-hd { display: flex; justify-content: space-between; align-items: center; font-weight: 600; }
.title { font-size: 18px; margin-bottom: 8px; }
.title .code { font-weight: 700; margin-right: 8px; }
.title .name { color: #606266; }
.sr-box .sr-title { font-weight: 600; margin-right: 8px; }
.mt8 { margin-top: 8px; }
.mt16 { margin-top: 16px; }
.mr8 { margin-right: 8px; }
.mb8 { margin-bottom: 8px; }
.advice-group { display: grid; grid-template-columns: repeat(1, 1fr); gap: 12px; }
@media (min-width: 992px) { .advice-group { grid-template-columns: repeat(3, 1fr); } }
.advice-block { border: 1px solid #ebeef5; border-radius: 8px; overflow: hidden; }
.advice-hd { background: #f5f7fa; padding: 8px 12px; font-weight: 600; }
.advice-body { padding: 8px 12px; }
.tags { margin: 6px 0; display: flex; align-items: center; gap: 4px; flex-wrap: wrap; }
.muted { color: #909399; font-size: 12px; }
.headline { display: flex; align-items: center; gap: 8px; padding: 4px 0; }
.headline-title { white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.risk-warning { margin-top: 12px; }
</style>

