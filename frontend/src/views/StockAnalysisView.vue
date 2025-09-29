<template>
  <div class="deprecated">
    <el-result icon="info" title="页面已拆分" sub-title="请通过“股票分析”主页进入各功能模块。">
      <template #extra>
        <el-button type="primary" @click="go">前往股票分析主页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
const router = useRouter();
function go() { router.push({ name: 'stocks-home' }); }
</script>

<style scoped>
.deprecated { display:flex; align-items:center; justify-content:center; min-height: 60vh; }
</style>
