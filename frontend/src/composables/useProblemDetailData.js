import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getProblemDetail, getSampleTestCases } from '@/api/problem'
import { submitCode as submitCodeApi, getMySubmissions, getSubmitDetail } from '@/api/submit'
import { getHint } from '@/api/referenceSolution'
import { getStudyStats } from '@/api/userProfile'

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

  const codeTemplates = {
    java: `class Solution {
    public int[] twoSum(int[] nums, int target) {
        
    }
}`,
    python: `class Solution:
    def twoSum(self, nums: List[int], target: int) -> List[int]:
        `
  }

  const initializeCode = () => {
    const problemId = route.params.id
    let template = codeTemplates[language.value] || ''

    if (problemId == 1) {
      if (language.value === 'java') {
        template = `class Solution {
    public int[] twoSum(int[] nums, int target) {
        
    }
}`
      } else {
        template = `class Solution:
    def twoSum(self, nums: List[int], target: int) -> List[int]:
        `
      }
    } else if (problemId == 2) {
      if (language.value === 'java') {
        template = `class Solution {
    public int findMax(int[] nums) {
        
    }
}`
      } else {
        template = `class Solution:
    def findMax(self, nums: List[int]) -> int:
        `
      }
    } else if (problemId == 3) {
      if (language.value === 'java') {
        template = `class Solution {
    public int abs(int x) {
        
    }
}`
      } else {
        template = `class Solution:
    def abs(self, x: int) -> int:
        `
      }
    }

    code.value = template
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

  const fetchHints = async () => {
    try {
      const hintsArray = []
      for (let i = 1; i <= 3; i += 1) {
        const res = await getHint(route.params.id, i)
        if (res.code === 200 && res.data) {
          hintsArray.push(res.data)
        }
      }
      hints.value = hintsArray
    } catch (error) {
      console.error('获取提示失败:', error)
    }
  }

  const fetchProblemDetail = async () => {
    loading.value = true
    try {
      const res = await getProblemDetail(route.params.id)
      if (res.code === 200) {
        problem.value = res.data
        initializeCode()
        await Promise.all([fetchTestCases(), fetchHints()])
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

  const handleLanguageChange = () => {
    initializeCode()
  }

  const resetCode = () => {
    initializeCode()
    ElMessage.success('代码已重置')
  }

  const runCode = async () => {
    running.value = true
    activeTestcaseTab.value = 'result'
    try {
      const res = await submitCodeApi({
        problemId: route.params.id,
        code: code.value,
        language: language.value
      })

      if (res.code === 200) {
        result.value = res.data
        ElMessage.success('运行完成')
      } else {
        ElMessage.error(res.msg || '运行失败')
      }
    } catch (error) {
      ElMessage.error('运行失败，请重试')
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
      } else {
        ElMessage.error(res.msg || '提交失败')
      }
    } catch (error) {
      ElMessage.error('提交失败，请重试')
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
    await Promise.all([
      fetchProblemDetail(),
      fetchUserStats(),
      fetchProblemSubmissions()
    ])
  }

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
    fetchProblemSubmissions,
    fetchUserStats,
    handleLanguageChange,
    resetCode,
    runCode,
    submitCode,
    openSubmissionDetail
  }
}
