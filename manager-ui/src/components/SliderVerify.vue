<template>
  <div class="slider-verify">
    <div class="slider-image-container">
      <div class="slider-bg" v-if="sliderData">
        <img :src="bgImage" class="bg-img" />
        <img 
          :src="sliderImage" 
          class="slider-block"
          :style="{ left: sliderLeft + 'px', top: sliderData.yHeight + 'px' }"
        />
      </div>
      <div class="slider-loading" v-else>
        <Icon type="ios-loading" size="24" class="loading-icon" />
        <span>加载中...</span>
      </div>
    </div>
    
    <div class="slider-bar">
      <div class="slider-track" :class="{ 'is-success': isSuccess, 'is-fail': isFail }">
        <div class="slider-progress" :style="{ width: sliderLeft + 'px' }"></div>
        <div 
          class="slider-button"
          :style="{ left: sliderLeft + 'px' }"
          @mousedown="onMouseDown"
          @touchstart="onTouchStart"
        >
          <Icon :type="buttonIcon" size="18" />
        </div>
        <span class="slider-tip" v-show="sliderLeft === 0">{{ tipText }}</span>
      </div>
    </div>
    
    <div class="slider-actions">
      <Button type="text" size="small" @click="refresh">
        <Icon type="ios-refresh" />
        刷新
      </Button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SliderVerify',
  props: {
    sliderData: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      sliderLeft: 0,
      isDragging: false,
      startX: 0,
      isSuccess: false,
      isFail: false
    }
  },
  computed: {
    bgImage() {
      if (!this.sliderData) return ''
      return 'data:image/png;base64,' + this.sliderData.originalImageBase64
    },
    sliderImage() {
      if (!this.sliderData) return ''
      return 'data:image/png;base64,' + this.sliderData.slidingImageBase64
    },
    buttonIcon() {
      if (this.isSuccess) return 'ios-checkmark'
      if (this.isFail) return 'ios-close'
      return 'ios-arrow-forward'
    },
    tipText() {
      if (this.isSuccess) return '验证成功'
      if (this.isFail) return '验证失败，请重试'
      return '向右拖动滑块完成验证'
    }
  },
  mounted() {
    document.addEventListener('mousemove', this.onMouseMove)
    document.addEventListener('mouseup', this.onMouseUp)
    document.addEventListener('touchmove', this.onTouchMove)
    document.addEventListener('touchend', this.onTouchEnd)
  },
  beforeDestroy() {
    document.removeEventListener('mousemove', this.onMouseMove)
    document.removeEventListener('mouseup', this.onMouseUp)
    document.removeEventListener('touchmove', this.onTouchMove)
    document.removeEventListener('touchend', this.onTouchEnd)
  },
  methods: {
    onMouseDown(e) {
      if (this.isSuccess) return
      this.isDragging = true
      this.startX = e.clientX - this.sliderLeft
      this.isFail = false
    },
    onTouchStart(e) {
      if (this.isSuccess) return
      this.isDragging = true
      this.startX = e.touches[0].clientX - this.sliderLeft
      this.isFail = false
    },
    onMouseMove(e) {
      if (!this.isDragging) return
      this.moveSlider(e.clientX)
    },
    onTouchMove(e) {
      if (!this.isDragging) return
      this.moveSlider(e.touches[0].clientX)
    },
    moveSlider(clientX) {
      let newLeft = clientX - this.startX
      const maxLeft = 260 // 滑块最大可移动距离
      if (newLeft < 0) newLeft = 0
      if (newLeft > maxLeft) newLeft = maxLeft
      this.sliderLeft = newLeft
    },
    onMouseUp() {
      if (!this.isDragging) return
      this.isDragging = false
      this.verifySlider()
    },
    onTouchEnd() {
      if (!this.isDragging) return
      this.isDragging = false
      this.verifySlider()
    },
    verifySlider() {
      if (this.sliderLeft > 10) {
        // 传递滑块位置给父组件进行验证
        this.$emit('success', Math.round(this.sliderLeft))
      }
    },
    refresh() {
      this.reset()
      this.$emit('refresh')
    },
    reset() {
      this.sliderLeft = 0
      this.isSuccess = false
      this.isFail = false
    },
    setSuccess() {
      this.isSuccess = true
    },
    setFail() {
      this.isFail = true
      setTimeout(() => {
        this.reset()
      }, 1000)
    }
  }
}
</script>

<style lang="less" scoped>
.slider-verify {
  width: 100%;
}

.slider-image-container {
  width: 100%;
  height: 160px;
  background: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
  position: relative;
}

.slider-bg {
  position: relative;
  width: 100%;
  height: 100%;
}

.bg-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.slider-block {
  position: absolute;
  height: 40px;
  width: 40px;
  z-index: 10;
}

.slider-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
  
  .loading-icon {
    animation: spin 1s linear infinite;
    margin-bottom: 8px;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.slider-bar {
  margin-top: 12px;
}

.slider-track {
  position: relative;
  height: 40px;
  background: #f5f5f5;
  border-radius: 20px;
  border: 1px solid #e0e0e0;
  
  &.is-success {
    background: #e8f5e9;
    border-color: #4caf50;
    
    .slider-progress {
      background: #4caf50;
    }
    
    .slider-button {
      background: #4caf50;
      color: #fff;
    }
  }
  
  &.is-fail {
    background: #ffebee;
    border-color: #f44336;
    
    .slider-progress {
      background: #f44336;
    }
    
    .slider-button {
      background: #f44336;
      color: #fff;
    }
  }
}

.slider-progress {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: #e3f2fd;
  border-radius: 20px 0 0 20px;
}

.slider-button {
  position: absolute;
  top: -1px;
  width: 40px;
  height: 40px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  user-select: none;
  transition: background 0.3s;
  color: #2196f3;
  
  &:hover {
    background: #f5f5f5;
  }
}

.slider-tip {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  font-size: 13px;
  color: #999;
  white-space: nowrap;
  pointer-events: none;
}

.slider-actions {
  margin-top: 8px;
  text-align: right;
}
</style>
