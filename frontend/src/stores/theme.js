import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(false)

  const loadTheme = () => {
    const savedTheme = localStorage.getItem('theme')
    if (savedTheme !== null) {
      isDark.value = savedTheme === 'dark'
    } else {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      isDark.value = prefersDark
    }
    applyTheme()
  }

  const toggleTheme = () => {
    isDark.value = !isDark.value
    localStorage.setItem('theme', isDark.value ? 'dark' : 'light')
    applyTheme()
  }

  const setTheme = (dark) => {
    isDark.value = dark
    localStorage.setItem('theme', dark ? 'dark' : 'light')
    applyTheme()
  }

  const applyTheme = () => {
    const root = document.documentElement
    if (isDark.value) {
      root.classList.add('dark')
      root.classList.remove('light')
    } else {
      root.classList.remove('dark')
      root.classList.add('light')
    }
  }

  watch(isDark, () => {
    applyTheme()
  })

  return {
    isDark,
    toggleTheme,
    setTheme,
    loadTheme
  }
})
