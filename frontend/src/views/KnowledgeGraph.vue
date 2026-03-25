<template>
  <div class="knowledge-graph-page">
    <div class="page-header">
      <h1>知识图谱</h1>
      <p>查看知识点关系、掌握度分布与节点详情。</p>
    </div>

    <div class="toolbar">
      <el-select
        v-model="selectedDomain"
        placeholder="选择知识域"
        size="small"
        class="toolbar-item"
        @change="loadGraphData"
      >
        <el-option label="全部" value="" />
        <el-option label="数据结构" value="数据结构" />
        <el-option label="算法技巧" value="算法技巧" />
        <el-option label="排序算法" value="排序算法" />
        <el-option label="动态规划" value="动态规划" />
        <el-option label="其他" value="其他" />
      </el-select>

      <div class="slider-wrapper">
        <el-slider
          v-model="masteryRange"
          range
          :min="0"
          :max="100"
          @change="loadGraphData"
        />
        <span class="mastery-range-text">
          掌握度 {{ masteryRange[0] }}% - {{ masteryRange[1] }}%
        </span>
      </div>

      <el-button type="primary" size="small" :loading="loading" @click="refreshGraph">
        刷新
      </el-button>
    </div>

    <div ref="chartContainer" class="chart-container" v-loading="loading" />

    <div class="legend">
      <div class="legend-item">
        <span class="legend-color" style="background-color: #67c23a"></span>
        <span>掌握度 >= 80%</span>
      </div>
      <div class="legend-item">
        <span class="legend-color" style="background-color: #e6a23c"></span>
        <span>掌握度 60% - 79%</span>
      </div>
      <div class="legend-item">
        <span class="legend-color" style="background-color: #f56c6c"></span>
        <span>掌握度 40% - 59%</span>
      </div>
      <div class="legend-item">
        <span class="legend-color" style="background-color: #909399"></span>
        <span>掌握度 &lt; 40%</span>
      </div>
    </div>

    <el-dialog v-model="showNodeDialog" title="知识点详情" width="640px">
      <div v-if="selectedNode" class="node-detail">
        <h3>{{ selectedNode.name }}</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="描述">
            {{ selectedNode.description || '暂无描述' }}
          </el-descriptions-item>
          <el-descriptions-item label="类别">
            {{ selectedNode.category || '其他' }}
          </el-descriptions-item>
          <el-descriptions-item label="难度">
            {{ getDifficultyText(selectedNode.difficulty) }}
          </el-descriptions-item>
          <el-descriptions-item label="练习次数">
            {{ selectedNode.practiceCount || 0 }}
          </el-descriptions-item>
          <el-descriptions-item label="准确率">
            {{ formatAccuracy(selectedNode.accuracy) }}
          </el-descriptions-item>
          <el-descriptions-item label="掌握度">
            <el-progress
              :percentage="Math.round((selectedNode.mastery || 0) * 100)"
              :color="getMasteryColor(selectedNode.mastery || 0)"
              :stroke-width="10"
            />
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <el-empty v-else description="暂无节点详情" />
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import { getKnowledgeGraph, getNodeDetail } from '@/api/knowledgeGraph'

const chartContainer = ref(null)
const chartInstance = ref(null)
const selectedDomain = ref('')
const masteryRange = ref([0, 100])
const showNodeDialog = ref(false)
const selectedNode = ref(null)
const loading = ref(false)

const chartHeight = computed(() => Math.max(window.innerHeight - 260, 420))

const normalizeGraphData = (data = {}) => ({
  nodes: Array.isArray(data.nodes) ? data.nodes : [],
  edges: Array.isArray(data.edges) ? data.edges : []
})

const formatAccuracy = (accuracy) => {
  if (accuracy === undefined || accuracy === null) {
    return '0%'
  }

  return `${(accuracy * 100).toFixed(1)}%`
}

const getMasteryColor = (mastery) => {
  if (mastery >= 0.8) return '#67c23a'
  if (mastery >= 0.6) return '#e6a23c'
  if (mastery >= 0.4) return '#f56c6c'
  return '#909399'
}

const getDifficultyText = (difficulty) => {
  const levels = ['简单', '中等', '困难', '专家']
  return levels[difficulty] || '未知'
}

const createChart = () => {
  if (!chartContainer.value) {
    return null
  }

  if (!chartInstance.value) {
    chartInstance.value = echarts.init(chartContainer.value)
  }

  return chartInstance.value
}

const renderGraph = (graphData) => {
  const instance = createChart()
  if (!instance) {
    return
  }

  const data = normalizeGraphData(graphData)

  instance.setOption({
    title: {
      text: '知识图谱',
      left: 'center',
      textStyle: {
        fontSize: 18,
        fontWeight: 'bold'
      }
    },
    tooltip: {
      formatter: (params) => {
        if (params.dataType !== 'node') {
          return ''
        }

        const mastery = Math.round((params.data.mastery || 0) * 100)
        return [
          `<div style="font-weight:700;margin-bottom:4px;">${params.data.name}</div>`,
          `<div>掌握度: ${mastery}%</div>`,
          `<div>难度: ${getDifficultyText(params.data.difficulty)}</div>`,
          `<div>练习次数: ${params.data.practiceCount || 0}</div>`
        ].join('')
      }
    },
    series: [
      {
        type: 'graph',
        layout: 'force',
        roam: true,
        data: data.nodes.map((node) => ({
          ...node,
          id: node.id,
          name: node.name,
          symbolSize: (node.importance || 1) * 10 + 18,
          category: node.category || '其他',
          itemStyle: {
            color: getMasteryColor(node.mastery || 0)
          },
          label: {
            show: true,
            position: 'right',
            fontSize: 12
          }
        })),
        links: data.edges.map((edge) => ({
          source: edge.source,
          target: edge.target,
          value: edge.strength || 1,
          lineStyle: {
            width: edge.strength || 1,
            color: '#999'
          }
        })),
        force: {
          repulsion: 900,
          edgeLength: 140,
          gravity: 0.08
        },
        emphasis: {
          focus: 'adjacency',
          lineStyle: {
            width: 4
          }
        }
      }
    ]
  })

  instance.off('click')
  instance.on('click', async (params) => {
    if (params.dataType !== 'node') {
      return
    }

    try {
      const res = await getNodeDetail(params.data.id)
      selectedNode.value = res.code === 200 ? res.data : params.data
    } catch {
      selectedNode.value = params.data
    } finally {
      showNodeDialog.value = true
    }
  })
}

const loadGraphData = async () => {
  loading.value = true

  try {
    const res = await getKnowledgeGraph({
      domain: selectedDomain.value,
      minMastery: masteryRange.value[0],
      maxMastery: masteryRange.value[1]
    })

    if (res.code === 200) {
      renderGraph(res.data)
    } else {
      ElMessage.error(res.msg || '获取知识图谱失败')
    }
  } catch (error) {
    ElMessage.error('获取知识图谱失败')
  } finally {
    loading.value = false
  }
}

const refreshGraph = () => {
  loadGraphData()
}

const handleResize = () => {
  if (chartInstance.value) {
    chartInstance.value.resize()
  }
}

onMounted(() => {
  if (chartContainer.value) {
    chartContainer.value.style.height = `${chartHeight.value}px`
  }
  loadGraphData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (chartInstance.value) {
    chartInstance.value.dispose()
    chartInstance.value = null
  }
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.knowledge-graph-page {
  min-height: 100vh;
  padding: 24px;
  background: #f5f7fa;
}

.page-header {
  margin-bottom: 24px;
  text-align: center;
}

.page-header h1 {
  margin: 0 0 8px;
  font-size: 28px;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #606266;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
}

.toolbar-item {
  width: 180px;
}

.slider-wrapper {
  flex: 1;
}

.mastery-range-text {
  display: inline-block;
  margin-top: 8px;
  font-size: 13px;
  color: #606266;
}

.chart-container {
  height: 520px;
  margin-bottom: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
}

.legend {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.06);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.legend-color {
  width: 14px;
  height: 14px;
  border-radius: 50%;
}

.node-detail h3 {
  margin: 0 0 16px;
}

@media (max-width: 768px) {
  .knowledge-graph-page {
    padding: 16px;
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-item {
    width: 100%;
  }

  .chart-container {
    height: 420px;
  }
}
</style>
