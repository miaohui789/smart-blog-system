import request from '@/utils/request'

export function getStudyCategories() {
  return request({ url: '/study/categories', method: 'get' })
}

export function getStudyQuestions(params) {
  return request({ url: '/study/questions', method: 'get', params })
}

export function getStudyQuestionDetail(id) {
  return request({ url: `/study/questions/${id}`, method: 'get' })
}

export function getRandomStudyQuestion(params) {
  return request({ url: '/study/questions/random', method: 'get', params })
}

export function getStudyOverview() {
  return request({ url: '/study/progress/overview', method: 'get' })
}

export function getStudyDashboard() {
  return request({ url: '/study/dashboard', method: 'get' })
}

export function recordStudy(questionId) {
  return request({ url: `/study/questions/${questionId}/study`, method: 'post' })
}

export function toggleStudyFavorite(questionId, favorite) {
  return request({ url: `/study/questions/${questionId}/favorite`, method: 'post', params: { favorite } })
}

export function saveStudyNote(questionId, data) {
  return request({ url: `/study/questions/${questionId}/note`, method: 'post', data })
}

export function updateStudyStatus(questionId, data) {
  return request({ url: `/study/questions/${questionId}/status`, method: 'post', data })
}

export function createStudyCheckTask(data) {
  return request({ url: '/study/check/tasks', method: 'post', data })
}

export function getStudyCheckTask(taskId) {
  return request({ url: `/study/check/tasks/${taskId}`, method: 'get' })
}

export function markStudyAnswerViewed(taskId, itemId) {
  return request({ url: `/study/check/tasks/${taskId}/items/${itemId}/view-answer`, method: 'post' })
}

export function submitStudyAnswer(taskId, itemId, data) {
  return request({
    url: `/study/check/tasks/${taskId}/items/${itemId}/submit`,
    method: 'post',
    data,
    timeout: 180000,
    showError: false
  })
}

export function finishStudyTask(taskId) {
  return request({ url: `/study/check/tasks/${taskId}/finish`, method: 'post' })
}

export function getStudyCheckHistory(params) {
  return request({ url: '/study/check/history', method: 'get', params })
}

export function getStudyCheckStatistics() {
  return request({ url: '/study/check/statistics', method: 'get' })
}
