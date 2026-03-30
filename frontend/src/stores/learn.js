import { computed, markRaw, ref } from 'vue'
import { defineStore } from 'pinia'
import {
  ArrowRight,
  Coffee,
  Collection,
  Connection,
  Cpu,
  DataLine,
  Document,
  Grid,
  Histogram,
  Menu,
  OfficeBuilding,
  Opportunity,
  Platform,
  Reading,
  School,
  TrendCharts
} from '@element-plus/icons-vue'
import { getActivePaths, getAvailablePaths } from '@/api/learn'

const iconMap = {
  Coffee: markRaw(Coffee),
  DataLine: markRaw(DataLine),
  Document: markRaw(Document),
  Cpu: markRaw(Cpu),
  Connection: markRaw(Connection),
  Collection: markRaw(Collection),
  Grid: markRaw(Grid),
  Histogram: markRaw(Histogram),
  Menu: markRaw(Menu),
  OfficeBuilding: markRaw(OfficeBuilding),
  Opportunity: markRaw(Opportunity),
  Platform: markRaw(Platform),
  Reading: markRaw(Reading),
  School: markRaw(School),
  TrendCharts: markRaw(TrendCharts),
  ArrowRight: markRaw(ArrowRight)
}

const getIconComponent = (iconName) => iconMap[iconName] || markRaw(Document)

const normalizePathList = (data) => {
  if (!data) return []
  return Array.isArray(data) ? data : data.paths || []
}

const decoratePaths = (paths) =>
  paths.map((path) => ({
    ...path,
    icon: getIconComponent(path.icon || 'Document'),
    gradient: path.gradient || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    progress: Number(path.progress || 0),
    completedLevels: Number(path.completedLevels || path.completed || 0),
    totalLevels: Number(path.totalLevels || path.total || 0)
  }))

export const useLearnStore = defineStore('learn', () => {
  const inProgressPaths = ref([])
  const featuredPaths = ref([])
  const learningPaths = ref([])
  const currentPage = ref(1)
  const pageSize = ref(8)
  const total = ref(0)
  const languageFilter = ref('all')
  const directionFilter = ref('all')
  const initialized = ref(false)

  const paginatedLearningPaths = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return learningPaths.value.slice(start, end)
  })

  const getLanguageLabel = (language) => {
    const labels = {
      java: 'Java',
      python: 'Python',
      cpp: 'C++',
      all: '通用'
    }

    return labels[language] || language || '未分类'
  }

  const getDirectionLabel = (direction) => {
    const labels = {
      language: '语言基础',
      algorithm: '算法与数据结构',
      backend: '后端开发',
      frontend: '前端开发',
      database: '数据库',
      system: '系统设计'
    }

    return labels[direction] || direction || '未分类'
  }

  const fetchInProgressPaths = async () => {
    try {
      const res = await getActivePaths()
      if (res.code === 200) {
        inProgressPaths.value = decoratePaths(normalizePathList(res.data))
      } else {
        inProgressPaths.value = []
      }
    } catch (error) {
      console.error('获取进行中路径失败:', error)
      inProgressPaths.value = []
    }
  }

  const fetchFeaturedPaths = async () => {
    try {
      const res = await getAvailablePaths()
      if (res.code === 200) {
        featuredPaths.value = decoratePaths(normalizePathList(res.data)).slice(0, 3)
      } else {
        featuredPaths.value = []
      }
    } catch (error) {
      console.error('获取精选路径失败:', error)
      featuredPaths.value = []
    }
  }

  const fetchLearningPaths = async () => {
    try {
      const params = {}

      if (languageFilter.value !== 'all') {
        params.language = languageFilter.value
      }

      if (directionFilter.value !== 'all') {
        params.direction = directionFilter.value
      }

      const res = await getAvailablePaths(params)
      if (res.code === 200) {
        learningPaths.value = decoratePaths(normalizePathList(res.data))
        total.value = learningPaths.value.length
      } else {
        learningPaths.value = []
        total.value = 0
      }
    } catch (error) {
      console.error('获取学习路径失败:', error)
      learningPaths.value = []
      total.value = 0
    }
  }

  const initialize = async (force = false) => {
    if (initialized.value && !force) {
      return
    }

    await Promise.all([
      fetchInProgressPaths(),
      fetchFeaturedPaths(),
      fetchLearningPaths()
    ])

    initialized.value = true
  }

  const handleFilterChange = async () => {
    currentPage.value = 1
    await fetchLearningPaths()
  }

  const handleSizeChange = (size) => {
    pageSize.value = size
    currentPage.value = 1
  }

  const handleCurrentChange = (page) => {
    currentPage.value = page
  }

  return {
    inProgressPaths,
    featuredPaths,
    learningPaths,
    currentPage,
    pageSize,
    total,
    languageFilter,
    directionFilter,
    paginatedLearningPaths,
    getLanguageLabel,
    getDirectionLabel,
    fetchInProgressPaths,
    fetchFeaturedPaths,
    fetchLearningPaths,
    initialize,
    handleFilterChange,
    handleSizeChange,
    handleCurrentChange
  }
})
