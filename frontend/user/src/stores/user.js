import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, loginByEmailCode, logout as logoutApi, getUserInfo } from '@/api/user'
import { getToken, setToken, removeToken, getUser, setUser, removeUser } from '@/utils/auth'
import { initWebSocket, closeWebSocket } from '@/utils/websocket'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(getUser())

  const isLoggedIn = computed(() => !!token.value)

  async function login(loginForm, loginType = 'username') {
    let res
    if (loginType === 'emailCode') {
      // 邮箱验证码登录
      res = await loginByEmailCode(loginForm.email, loginForm.code)
    } else {
      // 用户名密码登录或邮箱密码登录
      res = await loginApi(loginForm)
    }
    token.value = res.data.token
    setToken(res.data.token)
    await fetchUserInfo()
    // 登录成功后建立WebSocket连接
    if (userInfo.value?.id) {
      initWebSocket(userInfo.value.id)
    }
    return res
  }

  async function fetchUserInfo() {
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      setUser(res.data)
      return res.data
    } catch (e) {
      console.error('获取用户信息失败:', e)
      return null
    }
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      closeWebSocket()
      token.value = ''
      userInfo.value = null
      removeToken()
      removeUser()
    }
  }

  function resetState() {
    closeWebSocket()
    token.value = ''
    userInfo.value = null
    removeToken()
    removeUser()
  }

  function updateUserInfo(data) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...data }
      setUser(userInfo.value)
    }
  }

  // 初始化时，如果有token和用户信息，建立WebSocket连接
  if (token.value && userInfo.value?.id) {
    initWebSocket(userInfo.value.id)
  } else if (token.value && !userInfo.value) {
    fetchUserInfo().then(user => {
      if (user?.id) {
        initWebSocket(user.id)
      }
    })
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    login,
    logout,
    fetchUserInfo,
    updateUserInfo,
    resetState
  }
})
