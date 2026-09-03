import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUserStore = defineStore('user', () => {
  const accessToken = ref(localStorage.getItem('adminAccessToken') || '')
  const refreshToken = ref(localStorage.getItem('adminRefreshToken') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('adminUserInfo') || 'null'))

  const isLoggedIn = computed(() => !!accessToken.value)

  function setTokens(access, refresh) {
    accessToken.value = access
    refreshToken.value = refresh
    localStorage.setItem('adminAccessToken', access)
    localStorage.setItem('adminRefreshToken', refresh)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('adminUserInfo', JSON.stringify(info))
  }

  function logout() {
    accessToken.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('adminAccessToken')
    localStorage.removeItem('adminRefreshToken')
    localStorage.removeItem('adminUserInfo')
  }

  return { accessToken, refreshToken, userInfo, isLoggedIn, setTokens, setUserInfo, logout }
})
