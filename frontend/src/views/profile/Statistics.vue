<template>
  <div class="statistics-page">
    <div class="chart-card">
      <div class="card-header">
        <h3 class="card-title">做题趋势</h3>
        <el-radio-group v-model="trendPeriod" size="small">
          <el-radio-button label="7">近 7 天</el-radio-button>
          <el-radio-button label="30">近 30 天</el-radio-button>
        </el-radio-group>
      </div>
      <div class="chart-container">
        <div ref="trendChartRef" class="chart"></div>
      </div>
    </div>

    <div class="charts-row">
      <div class="chart-card half">
        <div class="card-header">
          <h3 class="card-title">难度分布</h3>
        </div>
        <div class="chart-container">
          <div ref="difficultyChartRef" class="chart"></div>
        </div>
      </div>

      <div class="chart-card half">
        <div class="card-header">
          <h3 class="card-title">知识点掌握度</h3>
        </div>
        <div class="chart-container">
          <div ref="radarChartRef" class="chart"></div>
        </div>
      </div>
    </div>

    <div class="stats-detail">
      <div class="card-header">
        <h3 class="card-title">详细统计</h3>
      </div>
      <el-table :data="detailStats" style="width: 100%">
        <el-table-column prop="name" label="统计项" />
        <el-table-column prop="value" label="数据" align="center" />
        <el-table-column prop="trend" label="较上一周期" align="center">
          <template #default="{ row }">
            <span :class="row.trend > 0 ? 'trend-up' : row.trend < 0 ? 'trend-down' : 'trend-flat'">
              <el-icon v-if="row.trend !== 0"><TrendCharts /></el-icon>
              {{ formatTrend(row.trend, row.unit) }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import { TrendCharts } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getDifficultyStats, getKnowledgeMastery } from '@/api/learn'
import { getNodeDetail } from '@/api/knowledgeGraph'
import { getMySubmissions } from '@/api/submit'
import { getStudyStats } from '@/api/userProfile'

const userStore = useUserStore()
const userId = computed(() => userStore.userInfo?.id)

const trendChartRef = ref(null)
const difficultyChartRef = ref(null)
const radarChartRef = ref(null)

const trendPeriod = ref('7')
const studyStats = ref({
  totalSolved: 0,
  totalSubmissions: 0,
  passRate: 0,
  studyHours: 0,
  studyHoursTrend: 0
})
const submissions = ref([])
const difficultyStats = ref({
  easy: 0,
  medium: 0,
  hard: 0
})
const knowledgeMastery = ref([])

let trendChartInstance = null
let difficultyChartInstance = null
let radarChartInstance = null

const clampPercentage = (value) => Math.min(100, Math.max(0, Math.round(Number(value) || 0)))

const formatDay = (date) => {
  const target = new Date(date)
  const year = target.getFullYear()
  const month = String(target.getMonth() + 1).padStart(2, '0')
  const day = String(target.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const formatShortDay = (date) => {
  const target = new Date(date)
  const month = String(target.getMonth() + 1).padStart(2, '0')
  const day = String(target.getDate()).padStart(2, '0')
  return `${month}-${day}`
}

const normalizeKnowledgeItem = (item) => {
  const practiceCount = Number(item.practiceCount) || 0
  const correctCount = Number(item.correctCount) || 0
  const score = item.score !== undefined && item.score !== null
    ? clampPercentage(item.score)
    : clampPercentage((Number(item.masteryLevel) || 0) / 3 * 100)

  return {
    ...item,
    knowledgeName: item.knowledgePoint?.name || item.knowledgeName || `知识点 ${item.knowledgeId}`,
    masteryPercent: score,
    practiceCount,
    correctCount,
    accuracy: practiceCount > 0 ? clampPercentage((correctCount / practiceCount) * 100) : 0
  }
}

const hydrateKnowledgeNames = async (items) => {
  const missingIds = [
    ...new Set(
      items
        .filter((item) => !item.knowledgePoint?.name && !item.knowledgeName && item.knowledgeId)
        .map((item) => item.knowledgeId)
    )
  ]

  if (missingIds.length === 0) {
    return items
  }

  const results = await Promise.allSettled(missingIds.map((id) => getNodeDetail(id)))
  const nameMap = {}

  results.forEach((result, index) => {
    if (result.status === 'fulfilled' && result.value?.code === 200) {
      nameMap[missingIds[index]] = result.value.data?.name
    }
  })

  return items.map((item) => ({
    ...item,
    knowledgeName: item.knowledgePoint?.name || item.knowledgeName || nameMap[item.knowledgeId] || `知识点 ${item.knowledgeId}`
  }))
}

const getPeriodRange = (days, periodOffset = 0) => {
  const end = new Date()
  end.setHours(23, 59, 59, 999)
  end.setDate(end.getDate() - periodOffset * days)

  const start = new Date(end)
  start.setHours(0, 0, 0, 0)
  start.setDate(start.getDate() - days + 1)

  return { start, end }
}

const getSubmissionsInRange = (days, periodOffset = 0) => {
  const { start, end } = getPeriodRange(days, periodOffset)

  return submissions.value.filter((item) => {
    if (!item.createTime) {
      return false
    }

    const createTime = new Date(item.createTime)
    return createTime >= start && createTime <= end
  })
}

const buildSummary = (days, periodOffset = 0) => {
  const list = getSubmissionsInRange(days, periodOffset)
  const passedList = list.filter((item) => Number(item.result) === 0)
  const activeDays = new Set(list.map((item) => formatDay(item.createTime))).size
  const avgRuntime = passedList.length
    ? Math.round(passedList.reduce((total, item) => total + (Number(item.timeCost) || 0), 0) / passedList.length)
    : 0

  return {
    submissions: list.length,
    solved: passedList.length,
    activeDays,
    accuracy: list.length ? clampPercentage((passedList.length / list.length) * 100) : 0,
    avgRuntime
  }
}

const trendChartData = computed(() => {
  const days = Number(trendPeriod.value)
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  const labels = []
  const submitValues = []
  const passedValues = []

  for (let offset = days - 1; offset >= 0; offset -= 1) {
    const current = new Date(today)
    current.setDate(today.getDate() - offset)
    const dayKey = formatDay(current)
    const dayItems = submissions.value.filter((item) => item.createTime && formatDay(item.createTime) === dayKey)

    labels.push(formatShortDay(current))
    submitValues.push(dayItems.length)
    passedValues.push(dayItems.filter((item) => Number(item.result) === 0).length)
  }

  return {
    labels,
    submitValues,
    passedValues
  }
})

const normalizedKnowledgeMastery = computed(() =>
  knowledgeMastery.value
    .map(normalizeKnowledgeItem)
    .sort((a, b) => b.masteryPercent - a.masteryPercent)
)

const detailStats = computed(() => {
  const current = buildSummary(Number(trendPeriod.value))
  const previous = buildSummary(Number(trendPeriod.value), 1)

  return [
    {
      name: '总提交次数',
      value: studyStats.value.totalSubmissions || 0,
      trend: current.submissions - previous.submissions,
      unit: ''
    },
    {
      name: '通过题目数',
      value: studyStats.value.totalSolved || 0,
      trend: current.solved - previous.solved,
      unit: ''
    },
    {
      name: '通过率',
      value: `${clampPercentage((studyStats.value.passRate || 0) * 100)}%`,
      trend: current.accuracy - previous.accuracy,
      unit: '%'
    },
    {
      name: '活跃学习天数',
      value: current.activeDays,
      trend: current.activeDays - previous.activeDays,
      unit: '天'
    },
    {
      name: '累计学习时长',
      value: `${studyStats.value.studyHours || 0}h`,
      trend: studyStats.value.studyHoursTrend || 0,
      unit: 'h'
    },
    {
      name: '平均运行耗时',
      value: `${current.avgRuntime}ms`,
      trend: 0,
      unit: 'ms'
    }
  ]
})

const ensureChart = (chartRef, chartInstance) => {
  if (!chartRef.value) {
    return chartInstance
  }

  if (chartInstance) {
    return chartInstance
  }

  return echarts.init(chartRef.value)
}

const renderTrendChart = () => {
  trendChartInstance = ensureChart(trendChartRef, trendChartInstance)
  if (!trendChartInstance) {
    return
  }

  trendChartInstance.setOption(
    {
      tooltip: { trigger: 'axis' },
      legend: {
        data: ['提交次数', '通过次数'],
        top: 0
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: trendChartData.value.labels
      },
      yAxis: {
        type: 'value',
        minInterval: 1
      },
      series: [
        {
          name: '提交次数',
          type: 'line',
          smooth: true,
          data: trendChartData.value.submitValues,
          lineStyle: { color: '#409eff', width: 3 },
          itemStyle: { color: '#409eff' },
          areaStyle: {
            color: 'rgba(64, 158, 255, 0.12)'
          }
        },
        {
          name: '通过次数',
          type: 'line',
          smooth: true,
          data: trendChartData.value.passedValues,
          lineStyle: { color: '#67c23a', width: 3 },
          itemStyle: { color: '#67c23a' }
        }
      ]
    },
    true
  )
}

const renderDifficultyChart = () => {
  difficultyChartInstance = ensureChart(difficultyChartRef, difficultyChartInstance)
  if (!difficultyChartInstance) {
    return
  }

  difficultyChartInstance.setOption(
    {
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)'
      },
      legend: {
        orient: 'vertical',
        right: '8%',
        top: 'center'
      },
      series: [
        {
          type: 'pie',
          radius: ['50%', '72%'],
          center: ['35%', '50%'],
          label: { show: false },
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2
          },
          data: [
            { value: Number(difficultyStats.value.easy) || 0, name: '简单', itemStyle: { color: '#67c23a' } },
            { value: Number(difficultyStats.value.medium) || 0, name: '中等', itemStyle: { color: '#e6a23c' } },
            { value: Number(difficultyStats.value.hard) || 0, name: '困难', itemStyle: { color: '#f56c6c' } }
          ]
        }
      ]
    },
    true
  )
}

const renderRadarChart = () => {
  radarChartInstance = ensureChart(radarChartRef, radarChartInstance)
  if (!radarChartInstance) {
    return
  }

  const radarItems = normalizedKnowledgeMastery.value.slice(0, 6)
  const indicators = radarItems.length > 0
    ? radarItems.map((item) => ({ name: item.knowledgeName, max: 100 }))
    : [{ name: '暂无数据', max: 100 }]
  const values = radarItems.length > 0 ? radarItems.map((item) => item.masteryPercent) : [0]

  radarChartInstance.setOption(
    {
      tooltip: {},
      radar: {
        indicator: indicators,
        radius: '62%',
        splitNumber: 4
      },
      series: [
        {
          type: 'radar',
          data: [
            {
              value: values,
              name: '掌握度',
              areaStyle: {
                color: 'rgba(64, 158, 255, 0.24)'
              },
              lineStyle: {
                color: '#409eff',
                width: 2
              },
              itemStyle: {
                color: '#409eff'
              }
            }
          ]
        }
      ]
    },
    true
  )
}

const renderCharts = async () => {
  await nextTick()
  renderTrendChart()
  renderDifficultyChart()
  renderRadarChart()
}

const fetchData = async () => {
  if (!userId.value) {
    return
  }

  try {
    const [statsRes, submissionsRes, difficultyRes, knowledgeRes] = await Promise.all([
      getStudyStats(userId.value),
      getMySubmissions({ page: 1, size: 500 }),
      getDifficultyStats(userId.value),
      getKnowledgeMastery()
    ])

    if (statsRes?.code === 200) {
      studyStats.value = {
        ...studyStats.value,
        ...statsRes.data
      }
    }

    if (submissionsRes?.code === 200) {
      submissions.value = submissionsRes.data?.list || []
    }

    if (difficultyRes?.code === 200) {
      difficultyStats.value = {
        ...difficultyStats.value,
        ...difficultyRes.data
      }
    }

    if (knowledgeRes?.code === 200) {
      knowledgeMastery.value = await hydrateKnowledgeNames(knowledgeRes.data || [])
    } else {
      knowledgeMastery.value = []
    }

    renderCharts()
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const formatTrend = (value, unit) => {
  if (value === 0) {
    return '持平'
  }

  return `${value > 0 ? '+' : ''}${value}${unit}`
}

const handleResize = () => {
  trendChartInstance?.resize()
  difficultyChartInstance?.resize()
  radarChartInstance?.resize()
}

watch(
  userId,
  (id) => {
    if (id) {
      fetchData()
    }
  },
  { immediate: true }
)

watch(trendPeriod, () => {
  renderCharts()
})

onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChartInstance?.dispose()
  difficultyChartInstance?.dispose()
  radarChartInstance?.dispose()
})
</script>

<style scoped>
.statistics-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.chart-card,
.stats-detail {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.chart-card.half {
  flex: 1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.chart-container {
  height: 320px;
}

.chart {
  width: 100%;
  height: 100%;
}

.charts-row {
  display: flex;
  gap: 24px;
}

.trend-up,
.trend-down,
.trend-flat {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

.trend-up {
  color: #67c23a;
}

.trend-down {
  color: #f56c6c;
}

.trend-flat {
  color: #909399;
}

@media (max-width: 768px) {
  .charts-row {
    flex-direction: column;
  }

  .chart-container {
    height: 260px;
  }
}
</style>
