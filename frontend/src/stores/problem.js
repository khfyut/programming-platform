import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getProblemList, getLanguages } from '@/api/problem'
import { getKnowledgePointStats } from '@/api/knowledge'

const fallbackLanguageTypes = [
  { value: 'algorithm', label: '算法', icon: 'Cpu', color: '#F59E0B' },
  { value: 'database', label: '数据库', icon: 'DataLine', color: '#3B82F6' },
  { value: 'java', label: 'Java', icon: 'Document', color: '#EA2D2E' },
  { value: 'python', label: 'Python', icon: 'Coin', color: '#3776AB' }
]

const fallbackKnowledgeTags = [
  { name: '数组', count: 2306 },
  { name: '字符串', count: 938 },
  { name: '哈希表', count: 870 },
  { name: '数学', count: 721 },
  { name: '动态规划', count: 718 },
  { name: '排序', count: 549 },
  { name: '贪心', count: 467 },
  { name: '深度优先搜索', count: 395 },
  { name: '二分查找', count: 361 },
  { name: '位运算', count: 310 }
]

export const useProblemStore = defineStore('problem', () => {
  const loading = ref(false)
  const problemList = ref([])
  const knowledgeTags = ref([])
  const languageTypes = ref([])
  const searchKeyword = ref('')
  const selectedKnowledge = ref('')
  const selectedLang = ref('all')
  const solvedCount = ref(0)
  const showAllKnowledge = ref(false)
  const sortMode = ref('default')
  const knowledgeDisplayLimit = 12
  const pagination = ref({
    page: 1,
    size: 20,
    total: 0
  })

  const visibleKnowledgeTags = computed(() => {
    if (showAllKnowledge.value) {
      return knowledgeTags.value
    }

    return knowledgeTags.value.slice(0, knowledgeDisplayLimit)
  })

  const sortLabel = computed(() => {
    const labels = {
      default: '默认排序',
      passRate: '通过率优先',
      difficulty: '难度优先'
    }

    return labels[sortMode.value] || labels.default
  })

  const hasActiveFilters = computed(() => {
    return Boolean(
      searchKeyword.value.trim() ||
      selectedKnowledge.value ||
      selectedLang.value !== 'all'
    )
  })

  const normalizeLanguageTypes = (items) => {
    if (!Array.isArray(items) || items.length === 0) {
      return fallbackLanguageTypes
    }

    return items.map((item) => ({
      value: item.value || item.language || item.key,
      label: item.label || item.name || item.value || item.language,
      icon: item.icon || 'Document',
      color: item.color || '#3B82F6'
    }))
  }

  const getSortedProblems = (items) => {
    const list = [...items]

    if (sortMode.value === 'passRate') {
      list.sort((a, b) => {
        const passRateDiff = Number(b.passRate || 0) - Number(a.passRate || 0)
        if (passRateDiff !== 0) return passRateDiff
        return Number(a.id || 0) - Number(b.id || 0)
      })
      return list
    }

    if (sortMode.value === 'difficulty') {
      list.sort((a, b) => {
        const difficultyDiff = Number(a.difficulty ?? 99) - Number(b.difficulty ?? 99)
        if (difficultyDiff !== 0) return difficultyDiff
        return Number(a.id || 0) - Number(b.id || 0)
      })
    }

    return list
  }

  const applySort = () => {
    problemList.value = getSortedProblems(problemList.value)
  }

  const fetchProblems = async () => {
    loading.value = true

    try {
      const params = {
        page: pagination.value.page,
        size: pagination.value.size
      }

      if (searchKeyword.value.trim()) {
        params.keyword = searchKeyword.value.trim()
      }

      if (selectedLang.value !== 'all') {
        params.language = selectedLang.value
      }

      if (selectedKnowledge.value.trim()) {
        params.knowledge = selectedKnowledge.value.trim()
      }

      const res = await getProblemList(params)
      if (res.code === 200) {
        const list = (res.data.list || []).map((item) => ({
          ...item,
          isLocked: false,
          isPremium: false
        }))

        problemList.value = getSortedProblems(list)
        pagination.value.total = res.data.total || 0
        solvedCount.value = res.data.solvedCount || 0
      }
    } catch (error) {
      console.error('获取题目列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  const fetchKnowledgeTags = async () => {
    try {
      const res = await getKnowledgePointStats()
      if (res.code === 200 && Array.isArray(res.data)) {
        knowledgeTags.value = res.data
          .filter((item) => item.problemCount > 0)
          .map((item) => ({
            name: item.name,
            count: item.problemCount
          }))
        return
      }
    } catch (error) {
      console.error('获取知识点分类失败:', error)
    }

    knowledgeTags.value = fallbackKnowledgeTags
  }

  const fetchLanguageTypes = async () => {
    try {
      const res = await getLanguages()
      if (res.code === 200) {
        languageTypes.value = normalizeLanguageTypes(res.data)
        return
      }
    } catch (error) {
      console.error('获取语言分类失败:', error)
    }

    languageTypes.value = fallbackLanguageTypes
  }

  const initialize = async () => {
    await Promise.all([
      fetchProblems(),
      fetchKnowledgeTags(),
      fetchLanguageTypes()
    ])
  }

  const selectKnowledge = async (knowledge) => {
    selectedKnowledge.value = selectedKnowledge.value === knowledge ? '' : knowledge
    selectedLang.value = 'all'
    pagination.value.page = 1
    await fetchProblems()
  }

  const selectLang = async (lang) => {
    selectedLang.value = lang
    selectedKnowledge.value = ''
    pagination.value.page = 1
    await fetchProblems()
  }

  const toggleKnowledgeExpand = () => {
    showAllKnowledge.value = !showAllKnowledge.value
  }

  const toggleSort = () => {
    const nextSortMode = {
      default: 'passRate',
      passRate: 'difficulty',
      difficulty: 'default'
    }

    sortMode.value = nextSortMode[sortMode.value] || 'default'
    applySort()
  }

  const handleSearch = async () => {
    pagination.value.page = 1
    await fetchProblems()
  }

  const clearFilters = async () => {
    searchKeyword.value = ''
    selectedKnowledge.value = ''
    selectedLang.value = 'all'
    pagination.value.page = 1
    await fetchProblems()
  }

  const toggleFavorite = (item) => {
    item.isFavorited = !item.isFavorited
  }

  return {
    loading,
    problemList,
    knowledgeTags,
    languageTypes,
    searchKeyword,
    selectedKnowledge,
    selectedLang,
    solvedCount,
    showAllKnowledge,
    sortMode,
    pagination,
    visibleKnowledgeTags,
    sortLabel,
    hasActiveFilters,
    initialize,
    fetchProblems,
    selectKnowledge,
    selectLang,
    toggleKnowledgeExpand,
    toggleSort,
    handleSearch,
    clearFilters,
    toggleFavorite
  }
})
