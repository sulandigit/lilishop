<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1 class="login-title">Lilishop</h1>
        <p class="login-subtitle">B2B2C 商城管理系统</p>
      </div>
      
      <Form ref="loginForm" :model="formData" :rules="rules" class="login-form">
        <FormItem prop="username">
          <Input 
            v-model="formData.username"
            prefix="ios-person"
            placeholder="请输入用户名"
            size="large"
            @on-enter="handleLogin"
          />
        </FormItem>
        
        <FormItem prop="password">
          <Input 
            v-model="formData.password"
            type="password"
            prefix="ios-lock"
            placeholder="请输入密码"
            size="large"
            password
            @on-enter="handleLogin"
          />
        </FormItem>
        
        <FormItem>
          <Button 
            type="primary" 
            long 
            size="large"
            :loading="loading"
            @click="handleLogin"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </Button>
        </FormItem>
      </Form>
      
      <div class="login-footer">
        <p>默认账号: admin / 123456</p>
      </div>
    </div>
    
    <!-- 滑块验证码弹窗 -->
    <Modal
      v-model="showSlider"
      title="请完成安全验证"
      :closable="true"
      :mask-closable="false"
      width="380"
      footer-hide
    >
      <SliderVerify
        v-if="showSlider"
        :slider-data="sliderData"
        @success="onSliderSuccess"
        @refresh="loadSliderImage"
      />
    </Modal>
  </div>
</template>

<script>
import { login, getSliderImage, verifySlider } from '@/api/auth'
import SliderVerify from '@/components/SliderVerify.vue'

export default {
  name: 'Login',
  components: {
    SliderVerify
  },
  data() {
    return {
      formData: {
        username: '',
        password: ''
      },
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 4, message: '密码长度不能少于4位', trigger: 'blur' }
        ]
      },
      loading: false,
      showSlider: false,
      sliderData: null
    }
  },
  created() {
    // 生成新的 UUID
    this.$store.dispatch('generateNewUUID')
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (valid) {
          // 显示滑块验证
          await this.loadSliderImage()
          this.showSlider = true
        }
      })
    },
    
    async loadSliderImage() {
      try {
        // 每次加载前生成新的 UUID
        await this.$store.dispatch('generateNewUUID')
        const res = await getSliderImage('LOGIN')
        this.sliderData = res.result
      } catch (error) {
        this.$Message.error('获取验证码失败，请重试')
      }
    },
    
    async onSliderSuccess(xPos) {
      try {
        // 验证滑块
        const verifyRes = await verifySlider(xPos, 'LOGIN')
        if (verifyRes.result) {
          // 验证成功，进行登录
          await this.doLogin()
        } else {
          this.$Message.error('验证失败，请重试')
          await this.loadSliderImage()
        }
      } catch (error) {
        this.$Message.error('验证失败，请重试')
        await this.loadSliderImage()
      }
    },
    
    async doLogin() {
      this.loading = true
      try {
        const res = await login(this.formData.username, this.formData.password)
        const { accessToken, refreshToken } = res.result
        
        // 保存 token
        this.$store.dispatch('loginSuccess', { accessToken, refreshToken })
        
        this.$Message.success('登录成功')
        this.showSlider = false
        
        // 跳转到首页（这里暂时跳转到登录页，实际项目中应该跳转到首页）
        this.$router.push('/dashboard')
      } catch (error) {
        // 登录失败，重新获取验证码
        await this.loadSliderImage()
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="less" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-title {
  font-size: 32px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
}

.login-subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.login-form {
  /deep/ .ivu-input {
    height: 42px;
    line-height: 42px;
  }
  
  /deep/ .ivu-input-prefix {
    width: 40px;
    i {
      font-size: 18px;
      color: #999;
    }
  }
  
  /deep/ .ivu-btn-primary {
    height: 42px;
    font-size: 16px;
  }
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  
  p {
    font-size: 12px;
    color: #999;
    margin: 0;
  }
}
</style>
