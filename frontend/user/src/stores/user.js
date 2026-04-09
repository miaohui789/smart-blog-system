import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, loginByEmailCode, logout as logoutApi, getUserInfo } from '@/api/user'
import { getToken, setToken, removeToken, getUser, setUser, removeUser } from '@/utils/auth'
import { initWebSocket, closeWebSocket } from '@/utils/websocket'
import { getExpSummary } from '@/api/exp'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(getUser())
  const expSummary = ref(null)

  const isLoggedIn = computed(() => !!token.value)

  async function login(loginForm, loginType = 'username') {
    let res
    if (loginType === 'emailCode') {
      res = await loginByEmailCode(loginForm.email, loginForm.code)
    } else {
      res = await loginApi(loginForm)
    }
    token.value = res.data.token
    setToken(res.data.token)
    const user = await fetchUserInfo({ noAuthRedirect: true })
    if (!user?.id) {
      resetState()
      throw new Error('登录状态初始化失败，请重新登录')
    }
    if (userInfo.value?.id) {
      initWebSocket(userInfo.value.id)
      fetchExpSummary()
    }
    return res
  }

  async function fetchUserInfo({ noAuthRedirect = false } = {}) {
    try {
      const previousUser = userInfo.value
      const res = await getUserInfo(noAuthRedirect ? { noAuthRedirect: true } : {})
      userInfo.value = res.data
      setUser(res.data)
      emitUserDisplayRefresh(previousUser, res.data)
      // 获取用户经验概览
      fetchExpSummary()
      return res.data
    } catch (e) {
      console.error('获取用户信息失败:', e)
      return null
    }
  }

  async function fetchExpSummary() {
    try {
      const res = await getExpSummary()
      expSummary.value = res.data
      if (userInfo.value && res.data?.userLevel && userInfo.value.userLevel !== res.data.userLevel) {
        updateUserInfo({ userLevel: res.data.userLevel })
      }
      return res.data
    } catch (e) {
      console.error('获取用户经验概览失败:', e)
      return null
    }
  }

  function sleep(ms) {
    return new Promise(resolve => window.setTimeout(resolve, ms))
  }

  async function refreshExpSummaryWithRetry(options = {}) {
    const {
      retryCount = 5,
      retryDelay = 600
    } = options

    if (!token.value) return null

    const previousTotalExp = expSummary.value?.totalExp
    let latest = null

    for (let index = 0; index < retryCount; index++) {
      latest = await fetchExpSummary()
      if (!latest) {
        if (index < retryCount - 1) {
          await sleep(retryDelay)
        }
        continue
      }
      if (previousTotalExp == null || latest.totalExp !== previousTotalExp) {
        return latest
      }
      if (index < retryCount - 1) {
        await sleep(retryDelay)
      }
    }

    return latest
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      closeWebSocket()
      token.value = ''
      userInfo.value = null
      expSummary.value = null
      removeToken()
      removeUser()
    }
  }

  function resetState() {
    closeWebSocket()
    token.value = ''
    userInfo.value = null
    expSummary.value = null
    removeToken()
    removeUser()
  }

  function updateUserInfo(data) {
    if (userInfo.value) {
      const previousUser = userInfo.value
      userInfo.value = { ...userInfo.value, ...data }
      setUser(userInfo.value)
      emitUserDisplayRefresh(previousUser, userInfo.value)
    }
  }

  function emitUserDisplayRefresh(previousUser, nextUser) {
    if (!nextUser?.id || typeof window === 'undefined') {
      return
    }
    if (
      previousUser?.userLevel === nextUser.userLevel &&
      previousUser?.vipLevel === nextUser.vipLevel &&
      previousUser?.nickname === nextUser.nickname &&
      previousUser?.username === nextUser.username &&
      previousUser?.avatar === nextUser.avatar
    ) {
      return
    }
    window.dispatchEvent(new CustomEvent('user-display-refresh', {
      detail: {
        userId: nextUser.id,
        userLevel: nextUser.userLevel,
        vipLevel: nextUser.vipLevel
      }
    }))
  }

  return {
    token,
    userInfo,
    expSummary,
    isLoggedIn,
    login,
    logout,
    fetchUserInfo,
    fetchExpSummary,
    refreshExpSummaryWithRetry,
    updateUserInfo,
    resetState
  }
})
