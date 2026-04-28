<template>
  <section ref="rootRef" class="reflection-task-panel">
    <div class="task-head">
      <div>
        <p class="task-kicker">当前复盘任务</p>
        <h2>先做复盘，再继续追问</h2>
      </div>
      <el-tag v-if="allSubmitted" type="success">可标记已复习</el-tag>
    </div>

    <div class="task-list">
      <article
        v-for="(task, index) in REVIEW_TASKS"
        :key="task.key"
        class="task-item"
        :class="{ submitted: taskState.tasks[task.key]?.submitted }"
      >
        <div class="task-title-row">
          <h3>{{ index + 1 }}. {{ task.title }}</h3>
          <el-tag v-if="taskState.tasks[task.key]?.submitted" size="small" type="success">已提交</el-tag>
        </div>
        <div class="task-compose">
          <el-input
            :ref="(el) => setInputRef(task.key, el)"
            v-model="localAnswers[task.key]"
            type="textarea"
            :rows="2"
            resize="none"
            maxlength="300"
            show-word-limit
            :placeholder="task.placeholder"
          />
          <el-button
            type="primary"
            :loading="submittingTask === task.key"
            :disabled="!localAnswers[task.key]?.trim()"
            @click="submitTask(task)"
          >
            提交
          </el-button>
        </div>
      </article>
    </div>

    <div v-if="submittingTask" class="task-working-status">
      <span></span>
      {{ workingStep || '正在提交复盘任务' }}
    </div>
  </section>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { chatWrongBookAgent } from '@/api/wrongBookAgent'
import {
  REVIEW_TASKS,
  createReflectionTaskPayload,
  normalizeWrongBookAgentState
} from '@/utils/wrongBookAgent'
import { getAgentWorkingSteps } from '@/utils/agentWorkingSteps'

const props = defineProps({
  wrongItem: {
    type: Object,
    required: true
  },
  session: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['session-updated'])

const rootRef = ref(null)
const inputRefs = ref({})
const submittingTask = ref('')
const workingStep = ref('')
const workingTimer = ref(null)
const localAnswers = ref({
  summary: '',
  counterexample: '',
  fixIdea: ''
})

const agentState = computed(() => normalizeWrongBookAgentState(props.session || {}, props.wrongItem || {}))
const taskState = computed(() => agentState.value.learningState)
const allSubmitted = computed(() => REVIEW_TASKS.every((task) => taskState.value.tasks[task.key]?.submitted))

const setInputRef = (key, el) => {
  if (el) {
    inputRefs.value[key] = el
  }
}

const syncAnswers = () => {
  localAnswers.value = {
    summary: taskState.value.tasks.summary.answer || '',
    counterexample: taskState.value.tasks.counterexample.answer || '',
    fixIdea: taskState.value.tasks.fixIdea.answer || ''
  }
}

const stopWorkingSteps = () => {
  if (workingTimer.value) {
    window.clearInterval(workingTimer.value)
    workingTimer.value = null
  }
  workingStep.value = ''
}

const startWorkingSteps = () => {
  stopWorkingSteps()
  const steps = getAgentWorkingSteps({
    scene: 'wrong_book',
    actionType: 'reflection_task_submit'
  })
  let index = 0
  workingStep.value = steps[index] || '正在提交复盘任务'
  workingTimer.value = window.setInterval(() => {
    index = Math.min(index + 1, steps.length - 1)
    workingStep.value = steps[index]
  }, 1800)
}

const submitTask = async (task) => {
  if (!props.wrongItem?.id || submittingTask.value) return
  const answer = localAnswers.value[task.key]?.trim()
  if (!answer) {
    ElMessage.warning('先写下你的复盘内容')
    return
  }

  submittingTask.value = task.key
  startWorkingSteps()
  try {
    const res = await chatWrongBookAgent(createReflectionTaskPayload({
      wrongItemId: props.wrongItem.id,
      sessionId: props.session?.sessionId,
      taskType: task.taskType,
      answer
    }))
    if (res?.code === 200 && res.data) {
      emit('session-updated', res.data)
      return
    }
    ElMessage.error(res?.msg || '提交复盘任务失败')
  } catch (error) {
    console.warn('Submit wrong book reflection task failed:', error)
    ElMessage.error('提交复盘任务失败')
  } finally {
    submittingTask.value = ''
    stopWorkingSteps()
  }
}

const focusTask = async (taskKey = 'summary') => {
  await nextTick()
  rootRef.value?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  inputRefs.value[taskKey]?.focus?.()
}

watch(
  () => props.session,
  () => syncAnswers(),
  { immediate: true }
)

onBeforeUnmount(() => {
  stopWorkingSteps()
})

defineExpose({ focusTask })
</script>

<style scoped>
.reflection-task-panel {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #ffffff;
  padding: 18px;
}

.task-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.task-kicker {
  margin: 0 0 5px;
  color: #2563eb;
  font-size: 12px;
  font-weight: 700;
}

.task-head h2 {
  margin: 0;
  color: #172033;
  font-size: 18px;
}

.task-list {
  display: grid;
  gap: 12px;
}

.task-item {
  border: 1px solid #edf1f7;
  border-radius: 8px;
  background: #f8fafc;
  padding: 14px;
}

.task-item.submitted {
  border-color: #bbf7d0;
  background: #f0fdf4;
}

.task-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}

.task-title-row h3 {
  margin: 0;
  color: #243044;
  font-size: 15px;
}

.task-compose {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: end;
}

.task-working-status {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 14px;
  color: #475569;
  font-size: 13px;
  line-height: 1.6;
}

.task-working-status span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #2563eb;
  animation: workingPulse 1.2s infinite;
}

@keyframes workingPulse {
  0%, 100% {
    opacity: 0.4;
    transform: scale(0.9);
  }

  50% {
    opacity: 1;
    transform: scale(1.08);
  }
}

@media (max-width: 640px) {
  .task-head,
  .task-compose {
    grid-template-columns: 1fr;
    display: grid;
  }
}
</style>
