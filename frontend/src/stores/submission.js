import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getMySubmissions, getSubmitDetail } from '@/api/submit'

export const useSubmissionStore = defineStore('submission', () => {
  const loading = ref(false)
  const submissionList = ref([])
  const detailDialogVisible = ref(false)
  const currentSubmission = ref(null)
  const detailLoading = ref(false)
  const sortBy = ref('latest')
  const filters = ref({
    result: null,
    language: null
  })
  const pagination = ref({
    page: 1,
    size: 10,
    total: 0
  })

  const toTimestamp = (value) => {
    if (!value) return 0
    const timestamp = new Date(value).getTime()
    return Number.isNaN(timestamp) ? 0 : timestamp
  }

  const sortSubmissions = (list) => {
    const sortedList = [...list]

    if (sortBy.value === 'oldest') {
      sortedList.sort((a, b) => toTimestamp(a.submitTime) - toTimestamp(b.submitTime))
    } else if (sortBy.value === 'runtime') {
      sortedList.sort((a, b) => Number(a.timeCost || 0) - Number(b.timeCost || 0))
    } else if (sortBy.value === 'memory') {
      sortedList.sort((a, b) => Number(a.memoryCost || 0) - Number(b.memoryCost || 0))
    } else {
      sortedList.sort((a, b) => toTimestamp(b.submitTime) - toTimestamp(a.submitTime))
    }

    return sortedList
  }

  const handleSortChange = () => {
    submissionList.value = sortSubmissions(submissionList.value)
  }

  const fetchSubmissions = async () => {
    loading.value = true

    try {
      const params = {
        page: pagination.value.page,
        size: pagination.value.size
      }

      if (filters.value.result !== null && filters.value.result !== undefined) {
        params.result = filters.value.result
      }

      if (filters.value.language) {
        params.language = filters.value.language
      }

      const res = await getMySubmissions(params)
      if (res.code === 200) {
        const normalizedList = (res.data.list || []).map((item) => ({
          ...item,
          problemTitle: item.problemTitle || item.title || item.problemName,
          difficulty: item.difficulty,
          tags: item.tags || item.tagList || []
        }))

        submissionList.value = sortSubmissions(normalizedList)
        pagination.value.total = res.data.total || 0
      }
    } catch (error) {
      console.error('获取提交记录失败:', error)
    } finally {
      loading.value = false
    }
  }

  const viewDetail = async (submission) => {
    const submitId = Number(submission?.id || submission)
    if (!submitId) return

    detailLoading.value = true
    currentSubmission.value = null
    detailDialogVisible.value = true

    try {
      const res = await getSubmitDetail(submitId)
      if (res.code === 200) {
        const fallbackSubmission = typeof submission === 'object' ? submission : {}
        const data = res.data || {}

        currentSubmission.value = {
          ...fallbackSubmission,
          ...data,
          problemTitle: data.problemTitle || data.title || data.problemName || fallbackSubmission.problemTitle,
          difficulty: data.difficulty || fallbackSubmission.difficulty,
          submitTime: data.submitTime || data.createTime || data.createdAt || fallbackSubmission.submitTime,
          tags: data.tags || data.tagList || fallbackSubmission.tags || []
        }
      } else {
        currentSubmission.value = typeof submission === 'object' ? submission : null
      }
    } catch (error) {
      console.error('获取提交详情失败:', error)
      currentSubmission.value = typeof submission === 'object' ? submission : null
    } finally {
      detailLoading.value = false
    }
  }

  const openDetailFromRoute = async (submitId) => {
    const id = Number(submitId)
    if (!id) return

    const matched = submissionList.value.find((item) => Number(item.id) === id)
    await viewDetail(matched || id)
  }

  const resetFilters = async () => {
    filters.value = {
      result: null,
      language: null
    }
    sortBy.value = 'latest'
    pagination.value.page = 1
    await fetchSubmissions()
  }

  const closeDetail = () => {
    detailDialogVisible.value = false
    currentSubmission.value = null
  }

  return {
    loading,
    submissionList,
    detailDialogVisible,
    currentSubmission,
    detailLoading,
    sortBy,
    filters,
    pagination,
    handleSortChange,
    fetchSubmissions,
    viewDetail,
    openDetailFromRoute,
    resetFilters,
    closeDetail
  }
})
