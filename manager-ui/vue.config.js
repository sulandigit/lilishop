const { defineConfig } = require('@vue/cli-service')

module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  devServer: {
    port: 8080,
    proxy: {
      '/manager': {
        target: 'http://localhost:8887',
        changeOrigin: true
      },
      '/common': {
        target: 'http://localhost:8888',
        changeOrigin: true
      }
    }
  }
})
