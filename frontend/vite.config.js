import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules') && !id.includes('/src/components/MonacoEditor.vue') && !id.includes('/src/components/ContextMenu.vue')) {
            return
          }

          if (id.includes('monaco-editor') || id.includes('/src/components/MonacoEditor.vue') || id.includes('/src/components/ContextMenu.vue')) {
            return 'monaco'
          }

          if (id.includes('echarts') || id.includes('zrender')) {
            return 'echarts'
          }

          if (id.includes('element-plus') || id.includes('@element-plus')) {
            return 'element-plus'
          }

          if (id.includes('/vue-router/') || id.includes('/pinia/') || id.includes('/vue/')) {
            return 'vue-core'
          }

          if (id.includes('/axios/')) {
            return 'network'
          }

          return 'vendor'
        }
      }
    }
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
