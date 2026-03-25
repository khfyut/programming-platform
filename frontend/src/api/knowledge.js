import request from '@/utils/request'

// 获取所有知识点
export const getAllKnowledgePoints = () => {
  return request('/api/knowledge-graph')
}

// 获取知识点统计（带题目数量）
export const getKnowledgePointStats = () => {
  return request('/api/knowledge-graph/stats')
}
