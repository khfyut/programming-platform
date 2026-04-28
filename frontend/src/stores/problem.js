import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getProblemList, getLanguages, getProblemCategories } from '@/api/problem'

const fallbackLanguageTypes = [
  { value: 'java', label: 'Java', icon: 'Document', color: '#EA2D2E' },
  { value: 'python', label: 'Python', icon: 'Coin', color: '#3776AB' }
]

const fallbackProblemCategories = [
  { id: 1, name: '基础语法', count: 0 },
  { id: 2, name: '算法', count: 0 },
  { id: 3, name: '数据结构', count: 0 },
  { id: 4, name: '面向对象', count: 0 },
  { id: 5, name: '网络编程', count: 0 },
  { id: 6, name: '数据库', count: 0 },
  { id: 7, name: '项目实战', count: 0 }
]

export const useProblemStore = defineStore('problem', () => {
  const loading = ref(false)
  const problemList = ref([])
  const problemCategories = ref([])
  const languageTypes = ref([])
  const selectedCategoryId = ref('all')
  const selectedLang = ref('all')
  const solvedCount = ref(0)
  const showAllCategories = ref(false)
  const sortMode = ref('default')
  const categoryDisplayLimit = 12
  const pagination = ref({
    page: 1,
    size: 20,
    total: 0
  })

  const visibleProblemCategories = computed(() => {
    if (showAllCategories.value) {
      return problemCategories.value
    }

    return problemCategories.value.slice(0, categoryDisplayLimit)
  })

  const allCategoryCount = computed(() => {
    return problemCategories.value.reduce((sum, item) => sum + Number(item.count || 0), 0)
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
      selectedCategoryId.value !== 'all' ||
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

  const normalizeProblemCategories = (items) => {
    if (!Array.isArray(items) || items.length === 0) {
      return fallbackProblemCategories
    }

    return items.map((item) => ({
      id: String(item.id),
      name: item.name || item.label || `分类 ${item.id}`,
      count: Number(item.problemCount ?? item.count ?? 0)
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

      if (selectedLang.value !== 'all') {
        params.language = selectedLang.value
      }

      if (selectedCategoryId.value !== 'all') {
        params.categoryId = selectedCategoryId.value
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

  const fetchProblemCategories = async () => {
    try {
      const res = await getProblemCategories()
      if (res.code === 200) {
        problemCategories.value = normalizeProblemCategories(res.data)
        return
      }
    } catch (error) {
      console.error('获取题目分类失败:', error)
    }

    problemCategories.value = fallbackProblemCategories
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
      fetchProblemCategories(),
      fetchLanguageTypes()
    ])
  }

  const selectCategory = async (categoryId) => {
    const nextCategoryId = categoryId == null ? 'all' : String(categoryId)
    selectedCategoryId.value = selectedCategoryId.value === nextCategoryId ? 'all' : nextCategoryId
    pagination.value.page = 1
    await fetchProblems()
  }

  const selectLang = async (lang) => {
    selectedLang.value = lang
    pagination.value.page = 1
    await fetchProblems()
  }

  const toggleCategoryExpand = () => {
    showAllCategories.value = !showAllCategories.value
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

  const clearFilters = async () => {
    selectedCategoryId.value = 'all'
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
    problemCategories,
    languageTypes,
    selectedCategoryId,
    selectedLang,
    solvedCount,
    showAllCategories,
    sortMode,
    pagination,
    visibleProblemCategories,
    allCategoryCount,
    sortLabel,
    hasActiveFilters,
    initialize,
    fetchProblems,
    selectCategory,
    selectLang,
    toggleCategoryExpand,
    toggleSort,
    clearFilters,
    toggleFavorite
  }
})
