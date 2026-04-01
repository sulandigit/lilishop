import axios from 'axios'
import { Message } from 'view-design'
import store from '@/store'
import router from '@/router'

const service = axios.create({
  baseURL: '/',
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 添加 uuid 到 header
    if (store.state.uuid) {
      config.headers['uuid'] = store.state.uuid
    }
    // 添加 token 到 header
    const token = store.state.accessToken
    if (token) {
      config.headers['accessToken'] = token
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.success === false) {
      Message.error(res.message || '请求失败')
      // 登录失效
      if (res.code === 'USER_NOT_LOGIN' || res.code === 'USER_AUTH_EXPIRED') {
        store.commit('CLEAR_TOKEN')
        router.push('/login')
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    Message.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service
