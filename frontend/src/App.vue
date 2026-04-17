<template>
  <div class="min-h-screen text-slate-100 px-4 py-8 md:px-10">
    <header class="mb-6">
      <h1 class="text-3xl md:text-4xl font-bold tracking-wide text-cyan-300 drop-shadow">网危智鉴</h1>
      <p class="text-slate-400 mt-2">Link Risk Intelligence Platform · 本地离线风险分析引擎</p>
    </header>

    <section class="panel mb-6">
      <el-form @submit.prevent>
        <div class="flex flex-col md:flex-row gap-3">
          <el-input v-model="form.url" size="large" placeholder="请输入或粘贴需要检测的URL，如 http://example.com/login" clearable />
          <el-button type="danger" size="large" :loading="loading" class="!bg-alertRed !border-alertRed hover:!opacity-90" @click="handleAnalyze">
            一键检测
          </el-button>
        </div>
      </el-form>
      <transition name="fade">
        <div v-if="loading" class="mt-4 rounded-xl border border-cyan-400/40 p-4 animate-pulseGlow">
          <el-icon class="is-loading mr-2"><Loading /></el-icon>
          正在分析 URL 特征，构建攻击路径图谱...
        </div>
      </transition>
    </section>

    <section v-if="result" class="grid grid-cols-1 xl:grid-cols-3 gap-6">
      <div class="xl:col-span-2 space-y-6">
        <div class="panel">
          <div class="flex items-center justify-between">
            <h2 class="text-xl font-semibold">风险判定结果</h2>
            <el-tag :type="tagType" size="large">{{ riskLabel }}</el-tag>
          </div>
          <div class="mt-4 grid grid-cols-1 md:grid-cols-3 gap-4">
            <div class="rounded-xl border border-rose-500/40 p-4 bg-rose-950/20 shadow-danger">
              <p class="text-sm text-slate-300">综合风险评分</p>
              <p class="text-4xl font-bold text-rose-300">{{ result.score }}</p>
            </div>
            <div class="rounded-xl border border-cyan-500/40 p-4 bg-cyan-950/20">
              <p class="text-sm text-slate-300">账号被盗概率</p>
              <p class="text-3xl font-semibold">{{ toPercent(result.probabilities.phishing) }}</p>
            </div>
            <div class="rounded-xl border border-yellow-500/40 p-4 bg-yellow-950/20">
              <p class="text-sm text-slate-300">恶意软件风险</p>
              <p class="text-3xl font-semibold">{{ toPercent(result.probabilities.malware) }}</p>
            </div>
          </div>
        </div>

        <div class="panel">
          <h3 class="text-lg font-semibold mb-3">攻击路径模拟</h3>
          <el-steps direction="vertical" finish-status="error" :active="4">
            <el-step title="用户点击链接" />
            <el-step title="跳转仿冒页面" />
            <el-step title="输入账号密码" />
            <el-step title="数据发送至攻击服务器" />
          </el-steps>
        </div>

        <div class="panel">
          <h3 class="text-lg font-semibold mb-3">分析过程透明化</h3>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-3 mb-3 text-sm">
            <div class="rounded-lg border border-slate-700 p-3" v-for="(value, key) in result.features" :key="key">
              <span class="text-slate-400">{{ featureNameMap[key] || key }}：</span>
              <span class="font-semibold">{{ value }}</span>
            </div>
          </div>
          <el-timeline>
            <el-timeline-item v-for="(log, i) in result.logs" :key="i" type="danger">{{ log }}</el-timeline-item>
          </el-timeline>
        </div>
      </div>

      <div class="space-y-6">
        <div class="panel"><div ref="gaugeRef" class="h-[280px]"></div></div>
        <div class="panel"><div ref="radarRef" class="h-[320px]"></div></div>
        <div class="panel">
          <h3 class="text-lg font-semibold mb-3">防护建议</h3>
          <ul class="space-y-2 text-sm text-slate-300 list-disc pl-5">
            <li v-for="(item, idx) in result.advice" :key="idx">{{ item }}</li>
          </ul>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { analyzeUrl } from './api/analyze'

const form = reactive({ url: 'http://secure-login-verify-account.example.net/login?user=guest' })
const loading = ref(false)
const result = ref(null)
const gaugeRef = ref(null)
const radarRef = ref(null)

const featureNameMap = {
  urlLength: 'URL长度',
  containsIp: '是否含IP',
  specialChars: '特殊字符数量',
  subdomainCount: '子域名数量',
  https: 'HTTPS',
  suspiciousKeywordCount: '敏感关键词命中',
  digitRatio: '数字占比'
}

const toPercent = (v) => `${Math.round((v || 0) * 100)}%`

const riskLabel = computed(() => {
  if (!result.value) return ''
  return `⚠️ ${result.value.level.toUpperCase()} 风险（${result.value.score}分）`
})

const tagType = computed(() => {
  if (!result.value) return 'info'
  if (result.value.level === 'high') return 'danger'
  if (result.value.level === 'medium') return 'warning'
  return 'success'
})

const renderCharts = () => {
  if (!result.value) return
  const gauge = echarts.init(gaugeRef.value)
  gauge.setOption({
    backgroundColor: 'transparent',
    title: { text: '风险仪表盘', left: 'center', textStyle: { color: '#bae6fd' } },
    series: [{
      type: 'gauge',
      min: 0,
      max: 100,
      progress: { show: true, width: 12 },
      axisLine: { lineStyle: { width: 12 } },
      detail: { valueAnimation: true, formatter: '{value} 分', color: '#fda4af' },
      data: [{ value: result.value.score }]
    }]
  })

  const radar = echarts.init(radarRef.value)
  const r = result.value.radar
  radar.setOption({
    title: { text: '七维风险雷达', textStyle: { color: '#bae6fd' } },
    radar: {
      indicator: [
        { name: '钓鱼攻击', max: 100 },
        { name: '恶意软件', max: 100 },
        { name: '数据泄露', max: 100 },
        { name: 'XSS', max: 100 },
        { name: 'CSRF', max: 100 },
        { name: '域名欺诈', max: 100 },
        { name: '隐私追踪', max: 100 }
      ],
      axisName: { color: '#cbd5e1' }
    },
    series: [{
      type: 'radar',
      data: [{
        value: [r.phishing, r.malware, r.leak, r.xss, r.csrf, r.domainFraud, r.privacyTracking],
        areaStyle: { color: 'rgba(34, 211, 238, 0.3)' },
        lineStyle: { color: '#22d3ee' }
      }]
    }]
  })
}

const handleAnalyze = async () => {
  if (!form.url) {
    ElMessage.error('请输入URL')
    return
  }
  loading.value = true
  try {
    const data = await analyzeUrl(form.url)
    result.value = data
    await nextTick()
    renderCharts()
  } catch (e) {
    ElMessage.error('分析失败，请检查后端服务是否启动')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.35s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
