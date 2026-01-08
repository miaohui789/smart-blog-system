import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/user'
import { getToken, setToken, removeToken, getUser, setUser, removeUser } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(getUser()) // 从 localStorage 恢复用户信息

  const isLoggedIn = computed(() => !!token.value)

  async function login(loginForm) {
    const res = await loginApi(loginForm)
    token.value = res.data.token
    setToken(res.data.token)
    await fetchUserInfo()
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
      token.value = ''
      userInfo.value = null
      removeToken()
      removeUser()
    }
  }

  function resetState() {
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

  // 初始化时，如果有 token 但没有用户信息，尝试获取
  if (token.value && !userInfo.value) {
    fetchUserInfo()
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
