import axios from 'axios'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({ baseURL: '', timeout: 60000 })

// 请求拦截器：注入 Token
request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.accessToken) {
    config.headers.Authorization = `Bearer ${userStore.accessToken}`
  }
  return config
}, (error) => Promise.reject(error))

// 响应拦截器
let isRefreshing = false
let pendingRequests = []

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) return res
    ElMessage.error(res.message || '操作失败')
    return Promise.reject(new Error(res.message))
  },
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      const userStore = useUserStore()
      if (!userStore.refreshToken) {
        userStore.logout()
        router.push('/')
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
          accessToken: userStore.accessToken,
        })
        if (res.data.code === 200) {
          const { accessToken, refreshToken: newRefresh } = res.data.data
          userStore.setTokens(accessToken, newRefresh)
          pendingRequests.forEach((cb) => cb(accessToken))
          pendingRequests = []
          originalRequest.headers.Authorization = `Bearer ${accessToken}`
          return request(originalRequest)
        }
      } catch (e) {
        userStore.logout()
        router.push('/')
        ElMessage.error('登录已过期，请重新登录')
        return Promise.reject(e)
      } finally {
        isRefreshing = false
      }
    }

    const msg = error.response?.data?.message || error.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
