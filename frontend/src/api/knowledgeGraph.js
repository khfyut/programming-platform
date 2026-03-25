import request from '@/utils/request'

export function getKnowledgeGraph(params) {
  return request('/api/knowledge-graph', {
    params
  })
}

export function getNodeDetail(nodeId) {
  return request(`/api/knowledge-graph/node/${nodeId}`)
}

export function getKnowledgeMasteryDistribution() {
  return request('/api/knowledge-graph/mastery')
}

export function getWeakKnowledgePoints(threshold = 2) {
  return request('/api/knowledge-graph/weaknesses', {
    params: { threshold }
  })
}
