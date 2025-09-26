<template>
  <div class="auth-wrapper">
    <el-card class="auth-card">
      <div class="auth-header">
        <h2>{{ activeTab === 'login' ? '欢迎回来' : '创建新账户' }}</h2>
        <p>{{ activeTab === 'login' ? '登录后继续' : '注册后开始使用分析功能' }}</p>
      </div>
      <el-tabs v-model="activeTab" class="auth-tabs">
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-position="top">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" @keyup.enter="submitLogin" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                show-password
                placeholder="请输入密码"
                @keyup.enter="submitLogin"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="loginLoading" @click="submitLogin">登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-position="top">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="registerForm.username" placeholder="请设置用户名" @keyup.enter="submitRegister" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                show-password
                placeholder="请设置安全的密码"
                @keyup.enter="submitRegister"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" :loading="registerLoading" @click="submitRegister">注册并登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules } from 'element-plus';

import { useAuthState } from '../services/auth';
import type { LoginPayload, RegisterPayload } from '../types';
import { extractErrorMessage } from '../utils/errors';

const route = useRoute();
const router = useRouter();

const { login, register } = useAuthState();

const activeTab = ref<'login' | 'register'>('login');

const loginFormRef = ref<FormInstance>();
const registerFormRef = ref<FormInstance>();

const loginForm = reactive<LoginPayload>({ username: '', password: '' });
const registerForm = reactive<RegisterPayload>({ username: '', password: '' });

const loginRules = reactive<FormRules<LoginPayload>>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 64, message: '用户名长度需在 3 到 64 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' }
  ]
});

const registerRules = reactive<FormRules<RegisterPayload>>({
  username: [
    { required: true, message: '请设置用户名', trigger: 'blur' },
    { min: 3, max: 64, message: '用户名长度需在 3 到 64 个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 8, message: '密码长度至少为 8 个字符', trigger: 'blur' }
  ]
});

const loginLoading = ref(false);
const registerLoading = ref(false);

async function submitLogin() {
  if (!loginFormRef.value) {
    return;
  }
  try {
    await loginFormRef.value.validate();
  } catch {
    return;
  }
  loginLoading.value = true;
  try {
    const user = await login({ ...loginForm });
    ElMessage.success(`欢迎回来，${user.username}！`);
    redirectAfterAuth();
  } catch (error) {
    ElMessage.error(extractErrorMessage(error));
  } finally {
    loginLoading.value = false;
  }
}

async function submitRegister() {
  if (!registerFormRef.value) {
    return;
  }
  try {
    await registerFormRef.value.validate();
  } catch {
    return;
  }
  registerLoading.value = true;
  try {
    const user = await register({ ...registerForm });
    ElMessage.success(`账户已创建，欢迎你，${user.username}！`);
    redirectAfterAuth();
  } catch (error) {
    ElMessage.error(extractErrorMessage(error));
  } finally {
    registerLoading.value = false;
  }
}

function redirectAfterAuth() {
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/';
  router.replace(redirect || '/');
}
</script>

<style scoped>
.auth-wrapper {
  min-height: calc(100vh - 120px);
  display: flex;
  justify-content: center;
  align-items: center;
}

.auth-card {
  width: 420px;
  max-width: 90%;
  border-radius: 16px;
  box-shadow: 0 16px 40px rgba(50, 71, 91, 0.18);
}

.auth-header {
  text-align: center;
  margin-bottom: 16px;
}

.auth-header h2 {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: #32475b;
}

.auth-header p {
  margin: 8px 0 0;
  color: #6b7a90;
}

.auth-tabs :deep(.el-tabs__nav) {
  width: 100%;
  display: flex;
  justify-content: space-around;
}

.auth-tabs :deep(.el-tabs__item) {
  font-weight: 600;
  letter-spacing: 0.4px;
}

.el-form-item {
  margin-bottom: 18px;
}

.el-button {
  width: 100%;
}
</style>
