import request from '@/utils/request'

export function getAdminStudyCategoryList(params) {
  return request({ url: '/admin/study/category/list', method: 'get', params })
}

export function getAdminStudyCategoryDetail(id) {
  return request({ url: `/admin/study/category/${id}`, method: 'get' })
}

export function createAdminStudyCategory(data) {
  return request({ url: '/admin/study/category', method: 'post', data })
}

export function updateAdminStudyCategory(id, data) {
  return request({ url: `/admin/study/category/${id}`, method: 'put', data })
}

export function getAdminStudyQuestionList(params) {
  return request({ url: '/admin/study/question/list', method: 'get', params })
}

export function getAdminStudyQuestionDetail(id) {
  return request({ url: `/admin/study/question/${id}`, method: 'get' })
}

export function createAdminStudyQuestion(data) {
  return request({ url: '/admin/study/question', method: 'post', data })
}

export function updateAdminStudyQuestion(id, data) {
  return request({ url: `/admin/study/question/${id}`, method: 'put', data })
}

export function deleteAdminStudyQuestion(id) {
  return request({ url: `/admin/study/question/${id}`, method: 'delete' })
}

export function importAdminStudyQuestion(data) {
  return request({ url: '/admin/study/question/import', method: 'post', data })
}

export function getAdminStudyCheckRecordList(params) {
  return request({ url: '/admin/study/check-record/list', method: 'get', params })
}

export function getAdminStudyCheckRecordDetail(id) {
  return request({ url: `/admin/study/check-record/${id}`, method: 'get' })
}
