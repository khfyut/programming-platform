import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo, updateLanguage as updateLanguageApi } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const clearToken = () => {
    token.value = ''
    localStorage.removeItem('token')
    userInfo.value = null
  }

  const login = async (username, password) => {
    const res = await loginApi({ username, password })
    if (res.code === 200) {
      const tokenValue = res.data?.token || res.data
      setToken(tokenValue)
      await fetchUserInfo()
      return true
    }
    return false
  }

  const register = async (username, password) => {
    const res = await registerApi({ username, password })
    return res.code === 200
  }

  const fetchUserInfo = async () => {
    if (!token.value) return
    const res = await getUserInfo()
    if (res.code === 200) {
      userInfo.value = res.data
    }
  }

  const updateLanguage = async (language) => {
    const res = await updateLanguageApi(language)
    if (res.code === 200 && userInfo.value) {
      userInfo.value.language = language
    }
    return res.code === 200
  }

  return {
    token,
    userInfo,
    setToken,
    clearToken,
    login,
    register,
    fetchUserInfo,
    updateLanguage
  }
})
