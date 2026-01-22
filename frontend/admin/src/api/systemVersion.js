import request from '@/utils/request'

// 分页查询版本列表
export function getSystemVersionPage(params) {
  return request({
    url: '/admin/system-version/page',
    method: 'get',
    params
  })
}

// 获取版本详情
export function getSystemVersionById(id) {
  return request({
    url: `/admin/system-version/${id}`,
    method: 'get'
  })
}

// 新增版本
export function addSystemVersion(data) {
  return request({
    url: '/admin/system-version',
    method: 'post',
    data
  })
}

// 更新版本
export function updateSystemVersion(id, data) {
  return request({
    url: `/admin/system-version/${id}`,
    method: 'put',
    data
  })
}

// 删除版本
export function deleteSystemVersion(id) {
  return request({
    url: `/admin/system-version/${id}`,
    method: 'delete'
  })
}

// 批量删除版本
export function batchDeleteSystemVersion(ids) {
  return request({
    url: '/admin/system-version/batch',
    method: 'delete',
    data: ids
  })
}

// 更新版本状态
export function updateSystemVersionStatus(id, status) {
  return request({
    url: `/admin/system-version/${id}/status`,
    method: 'put',
    params: { status }
  })
}
