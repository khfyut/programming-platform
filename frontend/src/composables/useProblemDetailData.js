import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { runCode as runCodeApi } from '@/api/code'
import { getProblemDetail, getProblemSupportedLanguages, getSampleTestCases } from '@/api/problem'
import { getHint } from '@/api/referenceSolution'
import { submitCode as submitCodeApi, getMySubmissions, getSubmitDetail } from '@/api/submit'
import { getStudyStats } from '@/api/userProfile'
import {
  getRuntimeDefaultStarterCode,
  getRuntimeLanguageLabel,
  normalizeRuntimeLanguage
} from '@/utils/runtimeLanguage'

export function useProblemDetailData(route, userStore) {
  const loading = ref(false)
  const submitting = ref(false)
  const running = ref(false)
  const problem = ref(null)
  const code = ref('')
  const language = ref(userStore.userInfo?.language || 'java')
  const result = ref(null)
  const submissionsLoading = ref(false)
  const submissionList = ref([])
  const detailDialogVisible = ref(false)
  const currentSubmission = ref(null)
  const detailLoading = ref(false)
  const canViewSolution = ref(false)
  const hints = ref([])
  const supportedLanguages = ref([])
  const codeDrafts = ref({})
  const currentLanguageRef = ref(language.value)
  const userStats = ref({
    solved: 0,
    submissions: 0,
    passRate: 0,
    streak: 0
  })

  const tabs = [
    { key: 'description', label: '题目描述' },
    { key: 'solution', label: '题解' },
    { key: 'submissions', label: '提交记录' }
  ]
  const activeTab = ref('description')

  const testcaseTabs = [
    { key: 'case', label: '测试用例' },
    { key: 'result', label: '测试结果' }
  ]
  const activeTestcaseTab = ref('case')
  const testCases = ref([])
  const selectedTestCase = ref(0)

  const languageOptions = computed(() =>
    supportedLanguages.value.map((item) => ({
      ...item,
      code: normalizeRuntimeLanguage(item.languageCode),
      label: getRuntimeLanguageLabel(item.languageCode)
    }))
  )

  const currentLanguageConfig = computed(() =>
    supportedLanguages.value.find(
      (item) => normalizeRuntimeLanguage(item.languageCode) === normalizeRuntimeLanguage(language.value)
    ) || null
  )

  const currentFileName = computed(() => currentLanguageConfig.value?.starterFilename || 'main.txt')

  const pickInitialLanguage = (languages) => {
    const normalizedUserLanguage = normalizeRuntimeLanguage(userStore.userInfo?.language)
    const enabledCodes = languages.map((item) => normalizeRuntimeLanguage(item.languageCode))
    if (enabledCodes.includes(normalizedUserLanguage)) {
      return normalizedUserLanguage
    }
    const defaultLanguage = languages.find((item) => Number(item.isDefault) === 1)
    if (defaultLanguage) {
      return normalizeRuntimeLanguage(defaultLanguage.languageCode)
    }
    return enabledCodes[0] || 'java'
  }

  const loadCodeForLanguage = (nextLanguage) => {
    const normalizedLanguage = normalizeRuntimeLanguage(nextLanguage)
    const languageConfig = supportedLanguages.value.find(
      (item) => normalizeRuntimeLanguage(item.languageCode) === normalizedLanguage
    )
    if (!languageConfig) {
      return
    }
    code.value =
      codeDrafts.value[normalizedLanguage] ??
      languageConfig.starterCode ??
      getRuntimeDefaultStarterCode(normalizedLanguage)
  }

  const switchLanguage = async (nextLanguage, { preserveCurrent = true, reloadHints = true } = {}) => {
    const normalizedLanguage = normalizeRuntimeLanguage(nextLanguage)
    if (!normalizedLanguage) {
      return
    }

    if (preserveCurrent && currentLanguageRef.value) {
      codeDrafts.value[currentLanguageRef.value] = code.value
    }

    currentLanguageRef.value = normalizedLanguage
    language.value = normalizedLanguage
    loadCodeForLanguage(normalizedLanguage)

    if (reloadHints) {
      await fetchHints(normalizedLanguage)
    }
  }

  const fetchUserStats = async () => {
    try {
      const userId = userStore.userInfo?.id
      if (!userId) return

      const res = await getStudyStats(userId)
      const stats = res?.data || {}
      const passRate = Number(stats.passRate || 0)

      userStats.value = {
        solved: stats.totalSolved || 0,
        submissions: stats.totalSubmissions || 0,
        passRate: Math.round(passRate * 100),
        streak: stats.streak || 0
      }
    } catch (error) {
      console.error('获取用户统计失败:', error)
    }
  }

  const fetchTestCases = async () => {
    try {
      const res = await getSampleTestCases(route.params.id)
      if (res.code === 200 && Array.isArray(res.data)) {
        testCases.value = res.data.map((tc) => ({
          input: tc.input,
          output: tc.output,
          result: null
        }))
        if (testCases.value.length > 0) {
          selectedTestCase.value = 0
        }
      }
    } catch (error) {
      console.error('获取测试用例失败:', error)
    }
  }

  const fetchHints = async (targetLanguage = language.value) => {
    try {
      const hintsArray = []
      for (let i = 1; i <= 3; i += 1) {
        const res = await getHint(route.params.id, i, targetLanguage)
        if (res.code === 200 && res.data) {
          hintsArray.push(res.data)
        }
      }
      hints.value = hintsArray
    } catch (error) {
      console.error('获取提示失败:', error)
    }
  }

  const fetchSupportedLanguages = async () => {
    const res = await getProblemSupportedLanguages(route.params.id)
    if (res.code !== 200) {
      throw new Error(res.msg || '获取题目语言失败')
    }

    supportedLanguages.value = (res.data || []).map((item, index) => ({
      ...item,
      languageCode: normalizeRuntimeLanguage(item.languageCode),
      starterCode: item.starterCode || getRuntimeDefaultStarterCode(item.languageCode),
      starterFilename: item.starterFilename || 'main.txt',
      sortOrder: item.sortOrder ?? index
    }))

    codeDrafts.value = supportedLanguages.value.reduce((acc, item) => {
      acc[item.languageCode] = item.starterCode || getRuntimeDefaultStarterCode(item.languageCode)
      return acc
    }, {})
  }

  const fetchProblemDetail = async () => {
    loading.value = true
    try {
      const [detailRes] = await Promise.all([
        getProblemDetail(route.params.id),
        fetchSupportedLanguages(),
        fetchTestCases()
      ])

      if (detailRes.code === 200) {
        problem.value = detailRes.data
        const initialLanguage = pickInitialLanguage(supportedLanguages.value)
        await switchLanguage(initialLanguage, { preserveCurrent: false, reloadHints: false })
        await fetchHints(initialLanguage)
      }
    } catch (error) {
      console.error('获取题目详情失败:', error)
      ElMessage.error('获取题目详情失败')
    } finally {
      loading.value = false
    }
  }

  const fetchProblemSubmissions = async () => {
    submissionsLoading.value = true
    try {
      const res = await getMySubmissions({
        problemId: route.params.id,
        page: 1,
        size: 10
      })

      if (res.code === 200) {
        submissionList.value = (res.data?.list || []).map((item) => ({
          ...item,
          submitTime: item.submitTime || item.createTime || item.createdAt
        }))
      }
    } catch (error) {
      console.error('获取当前题目提交记录失败:', error)
    } finally {
      submissionsLoading.value = false
    }
  }

  const handleLanguageChange = async () => {
    if (!supportedLanguages.value.some((item) => item.languageCode === normalizeRuntimeLanguage(language.value))) {
      const fallbackLanguage = pickInitialLanguage(supportedLanguages.value)
      await switchLanguage(fallbackLanguage, { preserveCurrent: false, reloadHints: true })
      ElMessage.warning('当前题目未开放该语言，已切回可用语言')
      return
    }
    await switchLanguage(language.value)
  }

  const resetCode = () => {
    const nextCode =
      currentLanguageConfig.value?.starterCode || getRuntimeDefaultStarterCode(language.value)
    codeDrafts.value[normalizeRuntimeLanguage(language.value)] = nextCode
    code.value = nextCode
    ElMessage.success('代码已重置')
  }

  const buildRunResult = (executionResult, currentCase) => {
    const input = currentCase?.input || ''
    const expectedOutput = currentCase?.output || ''
    const actualOutput = executionResult?.output || ''
    const timeCost = executionResult?.timeCost || 0
    const memoryCost = executionResult?.memoryCost || 0

    if (executionResult?.status === 'COMPILE_ERROR') {
      const compileError = executionResult.compileOutput || executionResult.errorMessage || '编译失败'
      return {
        result: 2,
        output: '',
        compileError,
        runtimeError: null,
        errorMessage: null,
        timeCost,
        memoryCost,
        passedCount: 0,
        totalCount: 1,
        testCaseResults: [
          {
            result: 2,
            input,
            expectedOutput,
            actualOutput: '',
            errorMessage: compileError,
            timeCost,
            memoryCost
          }
        ]
      }
    }

    if (executionResult?.status === 'RUNTIME_ERROR') {
      const runtimeError = executionResult.errorMessage || '运行失败'
      return {
        result: 3,
        output: '',
        compileError: null,
        runtimeError,
        errorMessage: null,
        timeCost,
        memoryCost,
        passedCount: 0,
        totalCount: 1,
        testCaseResults: [
          {
            result: 3,
            input,
            expectedOutput,
            actualOutput: '',
            errorMessage: runtimeError,
            timeCost,
            memoryCost
          }
        ]
      }
    }

    if (executionResult?.status && executionResult.status !== 'SUCCESS') {
      const errorMessage = executionResult.errorMessage || executionResult.compileOutput || '执行失败'
      return {
        result: 4,
        output: '',
        compileError: null,
        runtimeError: null,
        errorMessage,
        timeCost,
        memoryCost,
        passedCount: 0,
        totalCount: 1,
        testCaseResults: [
          {
            result: 4,
            input,
            expectedOutput,
            actualOutput: '',
            errorMessage,
            timeCost,
            memoryCost
          }
        ]
      }
    }

    const passed = expectedOutput ? actualOutput.trim() === expectedOutput.trim() : true
    return {
      result: passed ? 0 : 1,
      output: actualOutput,
      compileError: null,
      runtimeError: null,
      errorMessage: null,
      timeCost,
      memoryCost,
      passedCount: passed ? 1 : 0,
      totalCount: 1,
      testCaseResults: [
        {
          result: passed ? 0 : 1,
          input,
          expectedOutput,
          actualOutput,
          errorMessage: null,
          timeCost,
          memoryCost
        }
      ]
    }
  }

  const runCode = async () => {
    running.value = true
    activeTestcaseTab.value = 'result'
    try {
      const currentCase = testCases.value[selectedTestCase.value] || testCases.value[0] || { input: '', output: '' }
      const res = await runCodeApi({
        problemId: Number(route.params.id),
        code: code.value,
        language: language.value,
        input: currentCase.input || ''
      })

      if (res.code === 200) {
        result.value = buildRunResult(res.data || {}, currentCase)
        ElMessage.success('运行完成')
        return {
          ok: true,
          failed: result.value?.result !== 0,
          triggerType: 'run_failed',
          result: result.value,
          latestResultCode: result.value?.result,
          errorMessage:
            result.value?.compileError || result.value?.runtimeError || result.value?.errorMessage || '',
          executionOutput: result.value?.output || '',
          code: code.value,
          language: language.value
        }
      } else {
        ElMessage.error(res.msg || '运行失败')
        return {
          ok: false,
          failed: true,
          triggerType: 'run_failed',
          latestResultCode: 4,
          errorMessage: res.msg || '运行失败',
          executionOutput: '',
          code: code.value,
          language: language.value
        }
      }
    } catch (error) {
      ElMessage.error('运行失败，请重试')
      return {
        ok: false,
        failed: true,
        triggerType: 'run_failed',
        latestResultCode: 4,
        errorMessage: '运行失败，请重试',
        executionOutput: '',
        code: code.value,
        language: language.value
      }
    } finally {
      running.value = false
    }
  }

  const submitCode = async () => {
    submitting.value = true
    try {
      const res = await submitCodeApi({
        problemId: route.params.id,
        code: code.value,
        language: language.value
      })

      if (res.code === 200) {
        result.value = res.data
        activeTestcaseTab.value = 'result'
        ElMessage.success('提交成功')
        canViewSolution.value = true
        await Promise.all([fetchProblemSubmissions(), fetchUserStats()])
        return {
          ok: true,
          failed: result.value?.result !== 0,
          triggerType: 'submit_failed',
          result: result.value,
          submitId: result.value?.submitId,
          latestResultCode: result.value?.result,
          errorMessage:
            result.value?.compileError || result.value?.runtimeError || result.value?.errorMessage || '',
          executionOutput: result.value?.output || '',
          code: code.value,
          language: language.value
        }
      } else {
        ElMessage.error(res.msg || '提交失败')
        return {
          ok: false,
          failed: true,
          triggerType: 'submit_failed',
          latestResultCode: 4,
          errorMessage: res.msg || '提交失败',
          executionOutput: '',
          code: code.value,
          language: language.value
        }
      }
    } catch (error) {
      ElMessage.error('提交失败，请重试')
      return {
        ok: false,
        failed: true,
        triggerType: 'submit_failed',
        latestResultCode: 4,
        errorMessage: '提交失败，请重试',
        executionOutput: '',
        code: code.value,
        language: language.value
      }
    } finally {
      submitting.value = false
    }
  }

  const openSubmissionDetail = async (submission) => {
    const submitId = Number(submission?.id || submission)
    if (!submitId) return

    detailLoading.value = true
    currentSubmission.value = null
    detailDialogVisible.value = true

    try {
      const res = await getSubmitDetail(submitId)
      if (res.code === 200) {
        const data = res.data || {}
        data.problemTitle = data.problemTitle || data.title || problem.value?.title
        data.submitTime = data.submitTime || data.createTime || data.createdAt || submission?.submitTime
        currentSubmission.value = data
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

  const resetSubmissionDetail = () => {
    detailDialogVisible.value = false
    currentSubmission.value = null
  }

  const refreshProblemPage = async () => {
    await Promise.all([fetchProblemDetail(), fetchUserStats(), fetchProblemSubmissions()])
  }

  watch(code, (nextCode) => {
    const currentLanguage = normalizeRuntimeLanguage(language.value)
    if (currentLanguage) {
      codeDrafts.value[currentLanguage] = nextCode
    }
  })

  onMounted(() => {
    refreshProblemPage()
  })

  watch(activeTab, (tab) => {
    if (tab === 'submissions') {
      fetchProblemSubmissions()
    }
  })

  watch(
    () => route.params.id,
    () => {
      refreshProblemPage()
      resetSubmissionDetail()
    }
  )

  return {
    loading,
    submitting,
    running,
    problem,
    code,
    language,
    result,
    submissionsLoading,
    submissionList,
    detailDialogVisible,
    currentSubmission,
    detailLoading,
    canViewSolution,
    hints,
    userStats,
    tabs,
    activeTab,
    testcaseTabs,
    activeTestcaseTab,
    testCases,
    selectedTestCase,
    supportedLanguages,
    languageOptions,
    currentFileName,
    fetchProblemSubmissions,
    fetchUserStats,
    handleLanguageChange,
    resetCode,
    runCode,
    submitCode,
    openSubmissionDetail
  }
}
