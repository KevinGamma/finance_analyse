<template>
  <div class="kline-chart-wrapper">
    <div ref="chartRef" class="kline-chart" :style="{ height: chartHeight }"></div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';
import type { CandlestickPoint } from '../types';

const props = withDefaults(defineProps<{
  data: CandlestickPoint[];
  height?: string;
}>(), {
  height: '320px'
});

const chartRef = ref<HTMLDivElement>();
let chart: echarts.ECharts | null = null;

const chartHeight = computed(() => props.height || '320px');

function initChart() {
  if (!chartRef.value) {
    return;
  }
  if (!chart) {
    chart = echarts.init(chartRef.value);
  }
  renderChart();
}

function disposeChart() {
  if (chart) {
    chart.dispose();
    chart = null;
  }
}

function renderChart() {
  if (!chart) {
    return;
  }
  if (!props.data.length) {
    chart.clear();
    return;
  }
  const categories = props.data.map((item) => item.label);
  const candleData = props.data.map((item) => [item.open, item.close, item.low, item.high]);

  chart.setOption({
    animation: false,
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    grid: {
      left: 40,
      right: 20,
      top: 20,
      bottom: 48
    },
    xAxis: {
      type: 'category',
      data: categories,
      boundaryGap: true,
      axisLine: {
        lineStyle: { color: '#cfd8dc' }
      },
      axisLabel: {
        color: '#607d8b'
      }
    },
    yAxis: {
      type: 'value',
      scale: true,
      splitArea: { show: false },
      axisLine: { lineStyle: { color: '#cfd8dc' } },
      axisLabel: { color: '#607d8b' }
    },
    dataZoom: [
      { type: 'inside', start: 65, end: 100 },
      { type: 'slider', start: 65, end: 100 }
    ],
    series: [
      {
        name: 'K-Line',
        type: 'candlestick',
        data: candleData,
        itemStyle: {
          color: '#ef5350',
          color0: '#26a69a',
          borderColor: '#ef5350',
          borderColor0: '#26a69a'
        }
      }
    ]
  }, { notMerge: true });
  chart.resize();
}

function handleResize() {
  if (chart) {
    chart.resize();
  }
}

onMounted(() => {
  initChart();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  disposeChart();
});

watch(() => props.data, () => {
  if (!chart) {
    initChart();
  } else {
    renderChart();
  }
}, { deep: true });

watch(() => props.height, () => {
  if (chart) {
    chart.resize();
  }
});
</script>

<style scoped>
.kline-chart-wrapper {
  width: 100%;
}

.kline-chart {
  width: 100%;
}
</style>
