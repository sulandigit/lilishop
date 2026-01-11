import request from './request'

/**
 * 管理员登录
 * @param {string} username 用户名
 * @param {string} password 密码
 */
export function login(username, password) {
  const params = new URLSearchParams()
  params.append('username', username)
  params.append('password', password)
  return request({
    url: '/manager/passport/user/login',
    method: 'post',
    data: params
  })
}

/**
 * 获取当前登录用户信息
 */
export function getUserInfo() {
  return request({
    url: '/manager/passport/user/info',
    method: 'get'
  })
}

/**
 * 退出登录
 */
export function logout() {
  return request({
    url: '/manager/passport/user/logout',
    method: 'post'
  })
}

/**
 * 刷新 token
 * @param {string} refreshToken
 */
export function refreshToken(refreshToken) {
  return request({
    url: `/manager/passport/user/refresh/${refreshToken}`,
    method: 'get'
  })
}

/**
 * 获取滑块验证码
 * @param {string} type 验证类型
 */
export function getSliderImage(type = 'LOGIN') {
  return request({
    url: `/common/common/slider/${type}`,
    method: 'get'
  })
}

/**
 * 验证滑块验证码
 * @param {number} xPos 滑块位置
 * @param {string} type 验证类型
 */
export function verifySlider(xPos, type = 'LOGIN') {
  return request({
    url: `/common/common/slider/${type}`,
    method: 'post',
    params: { xPos }
  })
}
