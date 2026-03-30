import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useLearnStore } from '@/stores/learn'

export const useLearnPageData = () => {
  const learnStore = useLearnStore()

  const {
    currentPage,
    directionFilter,
    featuredPaths,
    inProgressPaths,
    languageFilter,
    learningPaths,
    pageSize,
    paginatedLearningPaths,
    total
  } = storeToRefs(learnStore)

  onMounted(() => {
    learnStore.initialize()
  })

  return {
    currentPage,
    directionFilter,
    featuredPaths,
    getDirectionLabel: learnStore.getDirectionLabel,
    getLanguageLabel: learnStore.getLanguageLabel,
    handleCurrentChange: learnStore.handleCurrentChange,
    handleFilterChange: learnStore.handleFilterChange,
    handleSizeChange: learnStore.handleSizeChange,
    inProgressPaths,
    languageFilter,
    learningPaths,
    pageSize,
    paginatedLearningPaths,
    total
  }
}
