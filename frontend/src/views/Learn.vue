<template>
  <div class="learning-plan-page">
    <div class="learn-container">
      <div class="page-shell">
        <div class="shell-accent"></div>
        <div class="shell-copy">
          <p class="shell-kicker">Learning Center</p>
          <p class="shell-text">
            从路径、进度和题库入口一起推进，比单纯刷题更容易保持连续性。
          </p>
        </div>
      </div>

      <LearningPlanHeader @open-plan="goToMyPlan" />

      <LearningPathHighlights
        :in-progress-paths="inProgressPaths"
        :featured-paths="featuredPaths"
        @open-path="goToPath"
      />

      <LearningPathCatalog
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        v-model:language-filter="languageFilter"
        v-model:direction-filter="directionFilter"
        :learning-paths="learningPaths"
        :paginated-learning-paths="paginatedLearningPaths"
        :total="total"
        :get-language-label="getLanguageLabel"
        :get-direction-label="getDirectionLabel"
        @filter-change="handleFilterChange"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @open-path="goToPath"
      />
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useLearnPageData } from '@/composables/useLearnPageData'
import LearningPlanHeader from '@/components/learn/LearningPlanHeader.vue'
import LearningPathHighlights from '@/components/learn/LearningPathHighlights.vue'
import LearningPathCatalog from '@/components/learn/LearningPathCatalog.vue'

const router = useRouter()

const {
  currentPage,
  directionFilter,
  featuredPaths,
  getDirectionLabel,
  getLanguageLabel,
  handleCurrentChange,
  handleFilterChange,
  handleSizeChange,
  inProgressPaths,
  languageFilter,
  learningPaths,
  pageSize,
  paginatedLearningPaths,
  total
} = useLearnPageData()

const goToPath = (pathId) => {
  router.push(`/learn/path/${pathId}`)
}

const goToMyPlan = () => {
  router.push('/learn/assessment')
}
</script>

<style scoped>
.learning-plan-page {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(0, 102, 255, 0.05), transparent 22%),
    var(--leetcode-bg-secondary, #f7f8fa);
  padding: 24px;
}

.learn-container {
  max-width: 1200px;
  margin: 0 auto;
}

.page-shell {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  margin-bottom: 20px;
  border: 1px solid rgba(0, 102, 255, 0.12);
  border-radius: 18px;
  background:
    linear-gradient(135deg, rgba(0, 102, 255, 0.08), rgba(255, 255, 255, 0.92)),
    var(--leetcode-bg, #ffffff);
}

.shell-accent {
  width: 10px;
  align-self: stretch;
  border-radius: 999px;
  background: linear-gradient(180deg, #0066ff, #66b3ff);
}

.shell-copy {
  min-width: 0;
}

.shell-kicker {
  margin: 0 0 6px;
  color: #1668dc;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.shell-text {
  margin: 0;
  color: var(--leetcode-text-secondary, #6b7280);
  line-height: 1.7;
}

@media (max-width: 768px) {
  .learning-plan-page {
    padding: 16px;
  }

  .page-shell {
    align-items: flex-start;
  }
}
</style>
