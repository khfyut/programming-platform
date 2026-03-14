import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import router from './router'
import App from './App.vue'
import { setRequestContext } from './utils/request'
import { useUserStore } from './stores/user'
import { useThemeStore } from './stores/theme'
import './styles/theme.css'
import './styles/leetcode-theme.css'
import './styles/element-plus-override.css'
import './styles/enhanced-theme.css'

const app = createApp(App)
const pinia = createPinia()

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus)

app.mount('#app')

const userStore = useUserStore()
const themeStore = useThemeStore()
setRequestContext(router, userStore)
themeStore.loadTheme()
