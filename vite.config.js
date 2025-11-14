import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      // 将所有以'/api'开头的请求代理到后端服务
      '/api': {
        target: 'http://localhost:8080', // 后端服务地址
        changeOrigin: true, // 允许跨域
        secure: false // 如果后端使用https，但证书无效时设置为false
      }
    }
  }
})
