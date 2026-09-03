import request from '../utils/request'

// 发送短信验证码
export function sendSmsCode(phone) {
  return request.post('/api/sms/send', { phone })
}

// 注册
export function register(data) {
  return request.post('/api/auth/register', data)
}

// 登录
export function login(data) {
  return request.post('/api/auth/login', data)
}

// 刷新 Token
export function refreshToken(data) {
  return request.post('/api/auth/refresh', data)
}

// 登出
export function logout(data) {
  return request.post('/api/auth/logout', data)
}

// 修改密码
export function changePassword(data) {
  return request.post('/api/auth/change-password', data)
}

// 找回密码
export function resetPassword(data) {
  return request.post('/api/auth/reset-password', data)
}
