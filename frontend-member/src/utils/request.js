import axios from 'axios'
import { useUserStore } from '../stores/user'
import { showToast } from 'vant'
import router from '../router'

const request = axios.create({
  baseURL: '',
  // 评测提交会调用后端 AI（DeepSeek）评分，首次调用较慢，放宽超时避免“评测超时”
  timeout: 60000,
})

// 请求拦截器：注入 Token
request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.accessToken) {
      config.headers.Authorization = `Bearer ${userStore.accessToken}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 是否正在刷新 Token
let isRefreshing = false
let pendingRequests = []

// 响应拦截器：统一错误处理 + Token 无感刷新
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    // 静默请求不弹提示
    if (!response.config._silent) {
      showToast(res.message || '操作失败')
    }
    return Promise.reject(new Error(res.message))
  },
  async (error) => {
    const originalRequest = error.config
    const status = error.response?.status

    // 401 或 403: Token 无效/过期，尝试刷新
    if ((status === 401 || status === 403) && !originalRequest._retry) {
      const userStore = useUserStore()

      if (!userStore.refreshToken) {
        userStore.logout()
        router.push('/login')
        return Promise.reject(error)
      }

      if (isRefreshing) {
        return new Promise((resolve) => {
          pendingRequests.push((newToken) => {
            originalRequest.headers.Authorization = `Bearer ${newToken}`
            resolve(request(originalRequest))
          })
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const res = await axios.post('/api/auth/refresh', {
          refreshToken: userStore.refreshToken,
        })

        if (res.data.code === 200) {
          const { accessToken, refreshToken: newRefresh } = res.data.data
          userStore.setTokens(accessToken, newRefresh)
          pendingRequests.forEach((cb) => cb(accessToken))
          pendingRequests = []
          originalRequest.headers.Authorization = `Bearer ${accessToken}`
          return request(originalRequest)
        }
      } catch (refreshError) {
        userStore.logout()
        router.push('/login')
        showToast('登录已过期，请重新登录')
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    // 静默请求不弹提示
    if (!originalRequest?._silent) {
      const msg = error.response?.data?.message || error.message || '网络错误'
      showToast(msg)
    }
    return Promise.reject(error)
  }
)

export default request
