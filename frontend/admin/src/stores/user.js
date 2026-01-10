import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, getAdminInfo, getMenus } from '@/api/user'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(null)
  const menus = ref([])
  const permissions = ref([])
  const roles = ref([])

  const isLoggedIn = computed(() => !!token.value)

  async function login(loginForm) {
    const res = await loginApi(loginForm)
    token.value = res.data.token
    setToken(res.data.token)
    return res
  }

  async function fetchUserInfo() {
    const res = await getAdminInfo()
    userInfo.value = res.data
    roles.value = res.data.roles || []
    permissions.value = res.data.permissions || []
  }

  async function fetchMenus() {
    const res = await getMenus()
    menus.value = res.data || []
  }

  function updateAvatar(avatar) {
    if (userInfo.value) {
      userInfo.value.avatar = avatar
    }
  }

  function updateNickname(nickname) {
    if (userInfo.value) {
      userInfo.value.nickname = nickname
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    menus.value = []
    removeToken()
  }

  return { token, userInfo, menus, permissions, roles, isLoggedIn, login, fetchUserInfo, fetchMenus, updateAvatar, updateNickname, logout }
})
