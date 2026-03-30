<template>
  <div v-loading="overviewLoading" class="overview-page">
    <ProfileOverviewHero
      :profile="profile"
      :current-path="currentPath"
      :current-path-progress="currentPathProgress"
      :get-language-label="getLanguageLabel"
      :get-direction-label="getDirectionLabel"
      @open-settings="goToSettings"
      @open-wrong-book="goToWrongBook"
      @continue-learning="continueLearning"
      @open-learn-center="goToLearnCenter"
    />

    <ProfileOverviewStats :stat-cards="statCards" />

    <ProfileOverviewPanels
      :current-path="currentPath"
      :recent-submissions="recentSubmissions"
      :format-relative-time="formatRelativeTime"
      :get-result-text="getResultText"
      :get-result-type="getResultType"
      @continue-learning="continueLearning"
      @open-wrong-book="goToWrongBook"
      @open-submissions="goToSubmissions"
      @open-problem="openProblem"
    />
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import ProfileOverviewHero from '@/components/profile/ProfileOverviewHero.vue'
import ProfileOverviewPanels from '@/components/profile/ProfileOverviewPanels.vue'
import ProfileOverviewStats from '@/components/profile/ProfileOverviewStats.vue'
import { useProfileOverviewData } from '@/composables/useProfileOverviewData'

const router = useRouter()
const {
  currentPath,
  currentPathProgress,
  formatRelativeTime,
  getDirectionLabel,
  getLanguageLabel,
  getResultText,
  getResultType,
  overviewLoading,
  profile,
  recentSubmissions,
  statCards
} = useProfileOverviewData()

const continueLearning = () => {
  if (currentPath.value?.id) {
    router.push(`/learn/path/${currentPath.value.id}`)
    return
  }
  router.push('/learn')
}

const goToLearnCenter = () => {
  router.push('/learn')
}

const goToWrongBook = () => {
  router.push('/wrong-book')
}

const goToSettings = () => {
  router.push({ name: 'ProfileSettings' })
}

const goToSubmissions = (submitId) => {
  const query = submitId ? { submitId } : undefined
  router.push({ name: 'ProfileSubmissions', query })
}

const openProblem = (problemId) => {
  if (!problemId) return
  router.push(`/problem/${problemId}`)
}
</script>

<style scoped>
.overview-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
</style>
