import Vue from 'vue'
import Vuex from 'vuex'
import Cookies from 'js-cookie'

Vue.use(Vuex)

// 生成 UUID
function generateUUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
    const r = Math.random() * 16 | 0
    const v = c === 'x' ? r : (r & 0x3 | 0x8)
    return v.toString(16)
  })
}

export default new Vuex.Store({
  state: {
    accessToken: Cookies.get('accessToken') || '',
    refreshToken: Cookies.get('refreshToken') || '',
    uuid: Cookies.get('uuid') || generateUUID(),
    userInfo: null
  },
  mutations: {
    SET_TOKEN(state, { accessToken, refreshToken }) {
      state.accessToken = accessToken
      state.refreshToken = refreshToken
      Cookies.set('accessToken', accessToken, { expires: 7 })
      Cookies.set('refreshToken', refreshToken, { expires: 7 })
    },
    CLEAR_TOKEN(state) {
      state.accessToken = ''
      state.refreshToken = ''
      state.userInfo = null
      Cookies.remove('accessToken')
      Cookies.remove('refreshToken')
    },
    SET_UUID(state, uuid) {
      state.uuid = uuid
      Cookies.set('uuid', uuid, { expires: 1 })
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
    }
  },
  actions: {
    // 生成新的 UUID
    generateNewUUID({ commit }) {
      const uuid = generateUUID()
      commit('SET_UUID', uuid)
      return uuid
    },
    // 登录成功后保存 token
    loginSuccess({ commit }, token) {
      commit('SET_TOKEN', token)
    },
    // 退出登录
    logout({ commit }) {
      commit('CLEAR_TOKEN')
    }
  },
  getters: {
    isLoggedIn: state => !!state.accessToken
  }
})
