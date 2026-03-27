<template>
  <div class="assessment-page">
    <div class="assessment-container">
      <div class="page-header" v-if="!assessmentStarted">
        <h1 class="page-title">能力测评</h1>
        <p class="page-subtitle">通过测评了解您的编程水平，获取个性化学习建议</p>
      </div>

      <div class="assessment-intro" v-if="!assessmentStarted">
        <div class="intro-card">
          <div class="intro-icon">
            <el-icon><TrendCharts /></el-icon>
          </div>
          <h2>开始能力测评</h2>
          <p>测评将根据您的答题情况，评估您的编程能力水平，并为您推荐合适的学习路径。</p>
          
          <div class="assessment-config">
            <div class="config-item">
              <label>编程语言</label>
              <el-select v-model="config.language" placeholder="选择语言">
                <el-option label="Java" value="java" />
                <el-option label="Python" value="python" />
              </el-select>
            </div>
            <div class="config-item">
              <label>测评方向</label>
              <el-select v-model="config.direction" placeholder="选择方向">
                <el-option label="算法基础" value="algorithm" />
                <el-option label="数据结构" value="data-structure" />
                <el-option label="前端开发" value="frontend" />
                <el-option label="后端开发" value="backend" />
              </el-select>
            </div>
            <div class="config-item">
              <label>题目数量</label>
              <el-select v-model="config.limit" placeholder="选择数量">
                <el-option label="10 题" :value="10" />
                <el-option label="20 题" :value="20" />
                <el-option label="30 题" :value="30" />
              </el-select>
            </div>
          </div>

          <el-button
            type="primary"
            size="large"
            class="start-btn"
            @click="startAssessment"
            :loading="loading"
          >
            <el-icon><CaretRight /></el-icon>
            开始测评
          </el-button>
        </div>

        <div class="tips-card">
          <h3>测评说明</h3>
          <ul>
            <li>测评题目均为选择题，请认真作答</li>
            <li>测评过程中请保持专注，避免中断</li>
            <li>测评结果将用于为您推荐个性化学习路径</li>
            <li>您可以随时重新测评以更新能力评估</li>
          </ul>
        </div>
      </div>

      <div class="assessment-content" v-if="assessmentStarted && !assessmentFinished">
        <div class="progress-bar">
          <div class="progress-info">
            <span class="progress-text">答题进度</span>
            <span class="progress-count">{{ currentIndex + 1 }} / {{ questions.length }}</span>
          </div>
          <el-progress
            :percentage="progressPercentage"
            :show-text="false"
            :stroke-width="8"
          />
        </div>

        <div class="question-card" v-if="currentQuestion">
          <div class="question-header">
            <span class="question-number">第 {{ currentIndex + 1 }} 题</span>
            <span class="question-type">单选题</span>
          </div>
          
          <div class="question-content">
            <p>{{ currentQuestion.question }}</p>
          </div>

          <div class="options-list">
            <div
              v-for="(option, index) in currentQuestion.options"
              :key="index"
              :class="['option-item', { selected: answers[currentIndex] === index }]"
              @click="selectAnswer(index)"
            >
              <div class="option-letter">{{ String.fromCharCode(65 + index) }}</div>
              <div class="option-text">{{ option }}</div>
            </div>
          </div>

          <div class="question-actions">
            <el-button
              v-if="currentIndex > 0"
              @click="prevQuestion"
            >
              <el-icon><ArrowLeft /></el-icon>
              上一题
            </el-button>
            <div v-else></div>
            
            <el-button
              v-if="currentIndex < questions.length - 1"
              type="primary"
              @click="nextQuestion"
              :disabled="answers[currentIndex] === undefined"
            >
              下一题
              <el-icon><ArrowRight /></el-icon>
            </el-button>
            <el-button
              v-else
              type="success"
              @click="submitAssessment"
              :loading="submitting"
            >
              <el-icon><Check /></el-icon>
              提交测评
            </el-button>
          </div>
        </div>
      </div>

      <div class="assessment-result" v-if="assessmentFinished">
        <div class="result-header">
          <div class="result-icon">
            <el-icon><Trophy /></el-icon>
          </div>
          <h2>测评完成</h2>
          <p>以下是您的能力评估报告</p>
        </div>

        <div class="result-content">
          <div class="score-section">
            <div class="score-circle">
              <el-progress
                type="circle"
                :percentage="result.score"
                :width="160"
                :stroke-width="12"
                :color="getScoreColor(result.score)"
              >
                <template #default>
                  <div class="score-value">
                    <span class="score-number">{{ result.score }}</span>
                    <span class="score-label">分</span>
                  </div>
                </template>
              </el-progress>
            </div>
            <div class="level-info">
              <div class="level-badge" :class="getLevelClass(result.level)">
                {{ result.level }}
              </div>
              <p class="level-desc">{{ result.levelDescription }}</p>
            </div>
          </div>

          <div class="analysis-section">
            <h3>能力分析</h3>
            <div class="analysis-grid">
              <div
                v-for="item in result.analysis"
                :key="item.name"
                class="analysis-item"
              >
                <div class="analysis-header">
                  <span class="analysis-name">{{ item.name }}</span>
                  <span class="analysis-score">{{ item.score }}%</span>
                </div>
                <el-progress
                  :percentage="item.score"
                  :show-text="false"
                  :color="getAnalysisColor(item.score)"
                />
              </div>
            </div>
          </div>

          <div class="suggestions-section">
            <h3>学习建议</h3>
            <div class="suggestions-list">
              <div
                v-for="(suggestion, index) in result.suggestions"
                :key="index"
                class="suggestion-item"
              >
                <el-icon class="suggestion-icon"><Star /></el-icon>
                <span>{{ suggestion }}</span>
              </div>
            </div>
          </div>

          <div class="path-recommendation" v-if="result.recommendedPath">
            <h3>推荐学习路径</h3>
            <div class="path-card" @click="goToPath(result.recommendedPath.id)">
              <div class="path-icon">
                <el-icon><Guide /></el-icon>
              </div>
              <div class="path-info">
                <div class="path-name">{{ result.recommendedPath.name }}</div>
                <div class="path-desc">{{ result.recommendedPath.description }}</div>
              </div>
              <el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </div>
          </div>
        </div>

        <div class="result-actions">
          <el-button @click="retakeAssessment">
            <el-icon><Refresh /></el-icon>
            重新测评
          </el-button>
          <el-button type="primary" @click="goToLearn">
            <el-icon><Reading /></el-icon>
            开始学习
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  TrendCharts,
  CaretRight,
  ArrowLeft,
  ArrowRight,
  Check,
  Trophy,
  Star,
  Guide,
  Refresh,
  Reading
} from '@element-plus/icons-vue'
import { getAssessment, commitAssessment } from '@/api/learn'

const router = useRouter()

const loading = ref(false)
const submitting = ref(false)
const assessmentStarted = ref(false)
const assessmentFinished = ref(false)

const config = reactive({
  language: 'java',
  direction: 'algorithm',
  limit: 10
})

const questions = ref([])
const answers = ref([])
const currentIndex = ref(0)
const result = ref({
  score: 0,
  level: '',
  levelDescription: '',
  analysis: [],
  suggestions: [],
  recommendedPath: null
})

const currentQuestion = computed(() => {
  return questions.value[currentIndex.value]
})

const progressPercentage = computed(() => {
  if (questions.value.length === 0) return 0
  return Math.round(((currentIndex.value + 1) / questions.value.length) * 100)
})

const startAssessment = async () => {
  loading.value = true
  try {
    const res = await getAssessment({
      language: config.language,
      direction: config.direction,
      limit: config.limit
    })
    
    if (res.code === 200) {
      questions.value = (res.data || []).map(q => ({
        ...q,
        options: typeof q.options === 'string' ? JSON.parse(q.options) : q.options
      }))
      answers.value = new Array(questions.value.length).fill(undefined)
      assessmentStarted.value = true
    } else {
      ElMessage.error(res.msg || '获取测评题目失败')
    }
  } catch (error) {
    console.error('获取测评题目失败:', error)
    ElMessage.error('获取测评题目失败，请重试')
  } finally {
    loading.value = false
  }
}

const selectAnswer = (index) => {
  answers.value[currentIndex.value] = index
}

const prevQuestion = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

const nextQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value++
  }
}

const submitAssessment = async () => {
  const unanswered = answers.value.filter(a => a === undefined).length
  if (unanswered > 0) {
    try {
      await ElMessageBox.confirm(
        `还有 ${unanswered} 道题未作答，确定要提交吗？`,
        '提示',
        {
          confirmButtonText: '确定提交',
          cancelButtonText: '继续作答',
          type: 'warning'
        }
      )
    } catch {
      return
    }
  }
  
  submitting.value = true
  try {
    // 构建后端期望的答案格式: Map<Long, String>
    const formattedAnswers = {};
    questions.value.forEach((question, index) => {
      if (answers.value[index] !== undefined) {
        formattedAnswers[question.id] = answers.value[index].toString();
      }
    });
    
    const res = await commitAssessment({
      language: config.language,
      direction: config.direction,
      answers: formattedAnswers
    })
    
    if (res.code === 200) {
      result.value = res.data
      assessmentFinished.value = true
    } else {
      ElMessage.error(res.msg || '提交失败')
    }
  } catch (error) {
    console.error('提交测评失败:', error)
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

const getScoreColor = (score) => {
  if (score >= 80) return '#00C853'
  if (score >= 60) return '#FFB300'
  return '#EE4D2E'
}

const getLevelClass = (level) => {
  const classes = {
    '初级': 'level-beginner',
    '中级': 'level-intermediate',
    '高级': 'level-advanced',
    '专家': 'level-expert'
  }
  return classes[level] || 'level-beginner'
}

const getAnalysisColor = (score) => {
  if (score >= 80) return '#00C853'
  if (score >= 60) return '#FFB300'
  if (score >= 40) return '#FF9800'
  return '#EE4D2E'
}

const retakeAssessment = () => {
  assessmentStarted.value = false
  assessmentFinished.value = false
  questions.value = []
  answers.value = []
  currentIndex.value = 0
  result.value = {
    score: 0,
    level: '',
    levelDescription: '',
    analysis: [],
    suggestions: [],
    recommendedPath: null
  }
}

const goToPath = (pathId) => {
  router.push({ name: 'LearningPath', params: { id: pathId } })
}

const goToLearn = () => {
  router.push({ name: 'Learn' })
}
</script>

<style scoped>
.assessment-page {
  width: 100%;
  min-height: 100vh;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  padding: 24px;
  box-sizing: border-box;
}

.assessment-container {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6B7280);
  margin: 0;
}

.assessment-intro {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
}

.intro-card {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  padding: 40px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  text-align: center;
}

.intro-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, #0066FF 0%, #66B3FF 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: white;
}

.intro-card h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 12px 0;
}

.intro-card p {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6B7280);
  margin: 0 0 32px 0;
  line-height: 1.6;
}

.assessment-config {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 32px;
  text-align: left;
}

.config-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.config-item label {
  font-size: 14px;
  font-weight: 500;
  color: var(--leetcode-text, #24292F);
}

.config-item .el-select {
  width: 100%;
}

.start-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
}

.tips-card {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
  height: fit-content;
}

.tips-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 16px 0;
}

.tips-card ul {
  margin: 0;
  padding: 0;
  list-style: none;
}

.tips-card li {
  position: relative;
  padding-left: 20px;
  margin-bottom: 12px;
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6B7280);
  line-height: 1.6;
}

.tips-card li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: var(--leetcode-primary, #0066FF);
  font-weight: bold;
}

.assessment-content {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  padding: 32px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
}

.progress-bar {
  margin-bottom: 32px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.progress-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--leetcode-text, #24292F);
}

.progress-count {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.question-card {
  margin-bottom: 24px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.question-number {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-primary, #0066FF);
}

.question-type {
  font-size: 12px;
  padding: 4px 12px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 12px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.question-content {
  margin-bottom: 24px;
}

.question-content p {
  font-size: 16px;
  color: var(--leetcode-text, #24292F);
  line-height: 1.8;
  margin: 0;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 32px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.option-item:hover {
  background: #FFFFFF;
  border-color: var(--leetcode-primary, #0066FF);
}

.option-item.selected {
  background: rgba(0, 102, 255, 0.05);
  border-color: var(--leetcode-primary, #0066FF);
}

.option-letter {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--leetcode-bg, #FFFFFF);
  border: 1px solid var(--leetcode-border, #E5E7EB);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  color: var(--leetcode-text-secondary, #6B7280);
  flex-shrink: 0;
}

.option-item.selected .option-letter {
  background: var(--leetcode-primary, #0066FF);
  border-color: var(--leetcode-primary, #0066FF);
  color: white;
}

.option-text {
  font-size: 14px;
  color: var(--leetcode-text, #24292F);
  line-height: 1.6;
}

.question-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 24px;
  border-top: 1px solid var(--leetcode-border, #E5E7EB);
}

.assessment-result {
  background: var(--leetcode-bg, #FFFFFF);
  border-radius: 12px;
  padding: 40px;
  border: 1px solid var(--leetcode-border, #E5E7EB);
}

.result-header {
  text-align: center;
  margin-bottom: 40px;
}

.result-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, #FFB300 0%, #FFD54F 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: white;
}

.result-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 8px 0;
}

.result-header p {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6B7280);
  margin: 0;
}

.result-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.score-section {
  display: flex;
  align-items: center;
  gap: 40px;
  padding: 24px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 12px;
}

.score-circle {
  flex-shrink: 0;
}

.score-value {
  text-align: center;
}

.score-number {
  font-size: 36px;
  font-weight: 700;
  color: var(--leetcode-text, #24292F);
}

.score-label {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6B7280);
  margin-left: 4px;
}

.level-info {
  flex: 1;
}

.level-badge {
  display: inline-block;
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.level-badge.level-beginner {
  background: rgba(0, 102, 255, 0.1);
  color: var(--leetcode-primary, #0066FF);
}

.level-badge.level-intermediate {
  background: rgba(0, 200, 83, 0.1);
  color: var(--leetcode-success, #00C853);
}

.level-badge.level-advanced {
  background: rgba(255, 179, 0, 0.1);
  color: var(--leetcode-warning, #FFB300);
}

.level-badge.level-expert {
  background: rgba(238, 77, 46, 0.1);
  color: var(--leetcode-danger, #EE4D2E);
}

.level-desc {
  font-size: 14px;
  color: var(--leetcode-text-secondary, #6B7280);
  line-height: 1.6;
  margin: 0;
}

.analysis-section h3,
.suggestions-section h3,
.path-recommendation h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin: 0 0 16px 0;
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.analysis-item {
  padding: 16px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 8px;
}

.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.analysis-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--leetcode-text, #24292F);
}

.analysis-score {
  font-size: 14px;
  font-weight: 600;
  color: var(--leetcode-text-secondary, #6B7280);
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px 16px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 8px;
}

.suggestion-icon {
  color: var(--leetcode-warning, #FFB300);
  font-size: 18px;
  flex-shrink: 0;
  margin-top: 2px;
}

.suggestion-item span {
  font-size: 14px;
  color: var(--leetcode-text, #24292F);
  line-height: 1.6;
}

.path-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--leetcode-bg-secondary, #F7F8FA);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.path-card:hover {
  background: #FFFFFF;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.path-icon {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #0066FF 0%, #66B3FF 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  flex-shrink: 0;
}

.path-info {
  flex: 1;
}

.path-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--leetcode-text, #24292F);
  margin-bottom: 4px;
}

.path-desc {
  font-size: 13px;
  color: var(--leetcode-text-secondary, #6B7280);
}

.arrow-icon {
  color: var(--leetcode-text-secondary, #6B7280);
  font-size: 20px;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
  padding-top: 32px;
  border-top: 1px solid var(--leetcode-border, #E5E7EB);
}

@media (max-width: 768px) {
  .assessment-page {
    padding: 16px;
  }
  
  .assessment-intro {
    grid-template-columns: 1fr;
  }
  
  .intro-card {
    padding: 24px;
  }
  
  .assessment-content {
    padding: 20px;
  }
  
  .score-section {
    flex-direction: column;
    text-align: center;
  }
  
  .analysis-grid {
    grid-template-columns: 1fr;
  }
  
  .result-actions {
    flex-direction: column;
  }
}
</style>
