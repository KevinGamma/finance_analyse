<template>
  <div ref="chartRef" class="kline-chart" :style="{ height: computedHeight }"></div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, onBeforeUnmount, nextTick, computed } from 'vue';
import * as echarts from 'echarts';
import type { TimeSeriesCandle } from '../types';

interface Props {
  candles: TimeSeriesCandle[];
  height?: number | string; // number => px
}

const props = defineProps<Props>();
const chartRef = ref<HTMLDivElement | null>(null);
let chart: echarts.ECharts | null = null;
let ro: ResizeObserver | null = null;

const computedHeight = computed(() => {
  if (props.height == null) return '360px';
  if (typeof props.height === 'number') return props.height + 'px';
  return props.height;
});

function formatCandles() {
  return props.candles.map(c => [c.open, c.close, c.low, c.high]);
}

function buildOption(): echarts.EChartsOption {
  const dates = props.candles.map(c => c.date);
  const values = formatCandles();
  const volumes = props.candles.map(c => c.volume);
  return {
    animation: false,
    backgroundColor: '#fff',
    grid: [
      { left: 50, right: 16, top: 20, height: '62%' },
      { left: 50, right: 16, top: '74%', height: '18%' }
    ],
    xAxis: [
      {
        type: 'category',
        data: dates,
        boundaryGap: false,
        axisLine: { lineStyle: { color: '#8899aa' } },
        axisLabel: { color: '#556677' },
        min: 'dataMin',
        max: 'dataMax'
      },
      {
        type: 'category',
        gridIndex: 1,
        data: dates,
        boundaryGap: false,
        axisLine: { lineStyle: { color: '#8899aa' } },
        axisLabel: { color: '#556677' },
        min: 'dataMin',
        max: 'dataMax'
      }
    ],
    yAxis: [
      {
        scale: true,
        axisLine: { lineStyle: { color: '#8899aa' } },
        axisLabel: { color: '#556677' },
        splitLine: { lineStyle: { color: 'rgba(0,0,0,0.08)' } }
      },
      {
        gridIndex: 1,
        axisLine: { lineStyle: { color: '#8899aa' } },
        axisLabel: { color: '#556677' },
        splitLine: { show: false }
      }
    ],
    dataZoom: [
      { type: 'inside', xAxisIndex: [0, 1], start: 60, end: 100 },
      { show: true, type: 'slider', xAxisIndex: [0, 1], top: '95%', start: 60, end: 100 }
    ],
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross' },
      backgroundColor: 'rgba(50,50,50,0.85)',
      textStyle: { color: '#fff' },
      valueFormatter: (val) => (typeof val === 'number' ? val.toFixed(2) : val as string)
    },
    toolbox: {
      feature: { saveAsImage: { show: true }, dataZoom: { show: true }, restore: { show: true } }
    },
    series: [
      {
        name: 'K线',
        type: 'candlestick',
        data: values,
        itemStyle: {
          color: '#d64b56', // up
          color0: '#3ba272', // down
          borderColor: '#d64b56',
          borderColor0: '#3ba272'
        }
      },
      {
        name: '成交量',
        type: 'bar',
        xAxisIndex: 1,
        yAxisIndex: 1,
        data: volumes,
        itemStyle: { color: '#5470c6' }
      }
    ]
  };
}

function render() {
  if (!chartRef.value) return;
  if (!chart) {
    chart = echarts.init(chartRef.value);
    window.addEventListener('resize', handleResize);
    setupResizeObserver();
  }
  const option = buildOption();
  chart.setOption(option as any, true);
  chart.resize();
}

function handleResize() {
  chart?.resize();
}

function onFullscreenChange() {
  // Resize a few times to accommodate transitions/animation of fullscreen
  setTimeout(() => chart?.resize(), 50);
  setTimeout(() => chart?.resize(), 200);
}

function setupResizeObserver() {
  if (!chartRef.value || ro) return;
  try {
    ro = new ResizeObserver(() => {
      // Debounce via microtask to avoid thrashing during transitions
      Promise.resolve().then(() => chart?.resize());
    });
    ro.observe(chartRef.value);
  } catch (e) {
    // older browsers: ignore
  }
}

watch(() => props.candles, async (val) => {
  if (!val || !val.length) return;
  await nextTick();
  render();
}, { deep: true });

watch(computedHeight, () => {
  setTimeout(() => chart?.resize(), 50);
});

onMounted(() => {
  if (props.candles.length) {
    render();
  } else {
    // still set up resize observer so future renders adapt
    setupResizeObserver();
  }
  document.addEventListener('fullscreenchange', onFullscreenChange);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  document.removeEventListener('fullscreenchange', onFullscreenChange);
  if (ro) {
    try { ro.disconnect(); } catch {}
    ro = null;
  }
  if (chart) {
    chart.dispose();
    chart = null;
  }
});
</script>

<style scoped>
.kline-chart {
  width: 100%;
}
</style>
