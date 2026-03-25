<template>
  <div class="reference-solution">
    <!-- 查看按钮 -->
    <el-button 
      type="primary" 
      @click="handleViewSolution"
      :disabled="!canView"
      :loading="loading"
    >
      查看参考答案
    </el-button>
    

    
    <!-- 参考答案对话框 -->
    <el-dialog v-model="showDialog" title="参考答案" width="80%">
      <div class="solution-content">
        <!-- 语言选择 -->
        <div class="language-selector">
          <el-select v-model="selectedLanguage" @change="loadSolution" size="small">
            <el-option 
              v-for="lang in availableLanguages" 
              :key="lang"
              :label="lang"
              :value="lang"
            />
          </el-select>
        </div>
        
        <!-- 解题思路 -->
        <div class="explanation-section">
          <h3>解题思路</h3>
          <div class="explanation-content">{{ solution.explanation }}</div>
        </div>
        
        <!-- 复杂度分析 -->
        <div class="complexity-section">
          <el-tag type="success">时间复杂度: {{ solution.timeComplexity }}</el-tag>
          <el-tag type="warning">空间复杂度: {{ solution.spaceComplexity }}</el-tag>
        </div>
        
        <!-- 代码展示 -->
        <div class="code-section">
          <div class="code-header">
            <h3>参考代码</h3>
            <el-button size="small" @click="copyCode" :loading="copyLoading">
              复制代码
            </el-button>
          </div>
          <MonacoEditor
            v-model="solution.solutionCode"
            :language="getMonacoLanguage(solution.language)"
            height="400px"
            :read-only="true"
          />
        </div>
        
        <!-- 渐进式提示 -->
        <div class="hints-section" v-if="solution.hints">
          <h3>渐进式提示</h3>
          <div class="hints-list">
            <div 
              v-for="(hint, level) in solution.hints" 
              :key="level"
              class="hint-item"
            >
              <span class="hint-level">提示 {{ level }}:</span>
              <span class="hint-content">{{ hint }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getReferenceSolution, getAvailableLanguages } from '@/api/referenceSolution'
import MonacoEditor from '@/components/MonacoEditor.vue'

// Props
const props = defineProps({
  problemId: {
    type: Number,
    required: true
  },
  canView: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['view-solution'])

// State
const showDialog = ref(false)
const loading = ref(false)
const copyLoading = ref(false)
const selectedLanguage = ref('')
const availableLanguages = ref([])

const solution = reactive({
  problemId: props.problemId,
  language: '',
  solutionCode: '',
  timeComplexity: '',
  spaceComplexity: '',
  explanation: '',
  hints: {}
})

// Methods
const handleViewSolution = async () => {
  if (!props.canView) return
  
  loading.value = true
  try {
    // 先获取可用语言
    const languagesRes = await getAvailableLanguages(props.problemId)
    if (languagesRes.code === 200 && languagesRes.data.length > 0) {
      availableLanguages.value = languagesRes.data
      selectedLanguage.value = languagesRes.data[0]
      
      // 加载参考答案
      await loadSolution()
      showDialog.value = true
      emit('view-solution')
    } else {
      ElMessage.error('暂无参考答案')
    }
  } catch (error) {
    ElMessage.error('获取参考答案失败')
  } finally {
    loading.value = false
  }
}

const loadSolution = async () => {
  if (!selectedLanguage.value) return
  
  loading.value = true
  try {
    const res = await getReferenceSolution(props.problemId, selectedLanguage.value)
    if (res.code === 200) {
      Object.assign(solution, res.data)
    } else {
      ElMessage.error(res.message || '获取参考答案失败')
    }
  } catch (error) {
    ElMessage.error('获取参考答案失败')
  } finally {
    loading.value = false
  }
}



const copyCode = async () => {
  copyLoading.value = true
  try {
    await navigator.clipboard.writeText(solution.solutionCode)
    ElMessage.success('代码已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  } finally {
    copyLoading.value = false
  }
}

const getMonacoLanguage = (language) => {
  const languageMap = {
    'Java': 'java',
    'Python': 'python',
    'C++': 'cpp',
    'JavaScript': 'javascript'
  }
  return languageMap[language] || 'text'
}

// 引入ElMessage
import { ElMessage } from 'element-plus'
</script>

<style scoped>
.reference-solution {
  margin: 10px 0;
}

.hint-buttons {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}

.solution-content {
  padding: 10px;
}

.language-selector {
  margin-bottom: 20px;
}

.explanation-section {
  margin-bottom: 20px;
}

.explanation-section h3 {
  margin-bottom: 10px;
  color: #303133;
}

.explanation-content {
  line-height: 1.6;
  color: #606266;
}

.complexity-section {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.code-section {
  margin-bottom: 20px;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.code-header h3 {
  margin: 0;
  color: #303133;
}

.monaco-editor {
  width: 100%;
  height: 400px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

.hints-section {
  margin-top: 20px;
}

.hints-section h3 {
  margin-bottom: 10px;
  color: #303133;
}

.hints-list {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.hint-item {
  margin-bottom: 10px;
  line-height: 1.5;
}

.hint-level {
  font-weight: bold;
  margin-right: 10px;
  color: #409eff;
}

.hint-content {
  color: #606266;
}
</style>