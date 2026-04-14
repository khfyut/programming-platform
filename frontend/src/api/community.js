import request from '@/utils/request'

export const getPosts = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/community/posts${query ? '?' + query : ''}`)
}

export const getPostById = (postId) => {
  return request(`/api/community/post/${postId}`)
}

export const createPost = (data) => {
  return request('/api/community/post', {
    method: 'POST',
    data: data
  })
}

export const updatePost = (postId, data) => {
  return request(`/api/community/post/${postId}`, {
    method: 'PUT',
    data: data
  })
}

export const deletePost = (postId) => {
  return request(`/api/community/post/${postId}`, {
    method: 'DELETE'
  })
}

export const likePost = (postId) => {
  return request(`/api/community/post/${postId}/like`, {
    method: 'POST'
  })
}

export const getCommentsByPostId = (postId) => {
  return request(`/api/community/post/${postId}/comments`)
}

export const createComment = (postId, data) => {
  return request(`/api/community/post/${postId}/comment`, {
    method: 'POST',
    data: data
  })
}

export const updateComment = (commentId, data) => {
  return request(`/api/community/comment/${commentId}`, {
    method: 'PUT',
    data: data
  })
}

export const deleteComment = (commentId) => {
  return request(`/api/community/comment/${commentId}`, {
    method: 'DELETE'
  })
}

export const likeComment = (commentId) => {
  return request(`/api/community/comment/${commentId}/like`, {
    method: 'POST'
  })
}

export const getUserPosts = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/community/user/posts${query ? '?' + query : ''}`)
}

export const getMyPosts = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/community/user/posts${query ? '?' + query : ''}`)
}

export const getUserComments = (params) => {
  const query = params ? new URLSearchParams(params).toString() : ''
  return request(`/api/community/user/comments${query ? '?' + query : ''}`)
}

export const getCommunityStatistics = () => {
  return request('/api/community/statistics')
}

export const getHotTopics = (limit = 10) => {
  return request(`/api/community/hot-topics?limit=${limit}`)
}
