# Hystrix熔断降级集成使用说明

## 概述

本项目已成功集成Hystrix熔断降级机制，用于保护外部HTTP服务调用，防止级联故障。当外部服务不可用时，系统会自动降级并返回缓存数据或默认响应。

## 配置说明

### application.yml配置

```yaml
hystrix:
  enabled: true
  command:
    default:
      execution:
        timeout:
          enabled: true
        isolation:
          thread:
            timeoutInMilliseconds: 10000
            interruptOnTimeout: true
      circuitBreaker:
        enabled: true
        errorThresholdPercentage: 50
        requestVolumeThreshold: 20
        sleepWindowInMilliseconds: 5000
      metrics:
        rollingStats:
          timeInMilliseconds: 10000
        healthSnapshot:
          intervalInMilliseconds: 500
    # 微信支付特殊配置
    WechatCommand:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 15000
      circuitBreaker:
        errorThresholdPercentage: 40
    # 支付宝支付特殊配置
    AlipayCommand:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 12000
      circuitBreaker:
        errorThresholdPercentage: 40
    # 物流查询特殊配置
    LogisticsCommand:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 8000
      circuitBreaker:
        errorThresholdPercentage: 60
    # 地理数据特殊配置
    GeoCommand:
      execution:
        isolation:
          thread:
            timeoutInMilliseconds: 5000
      circuitBreaker:
        errorThresholdPercentage: 70
  threadpool:
    default:
      coreSize: 20
      maximumSize: 50
      maxQueueSize: 100
      keepAliveTimeMinutes: 2
      allowMaximumSizeToDivergeFromCoreSize: true
  dashboard:
    enabled: true
```

## 使用方法

### 1. 使用增强的HTTP工具类

#### HttpClientUtils使用
```java
// 使用Hystrix保护的GET请求
String response = HttpClientUtils.doGetWithHystrix("https://api.weixin.qq.com/xxx", paramMap);

// 系统会自动根据URL识别服务类型（如wechat, alipay, logistics等）
// 并应用相应的Hystrix配置
```

#### AbstractHttpDelegate使用
```java
AbstractHttpDelegate delegate = new YourHttpDelegateImpl();

// 使用Hystrix保护的POST请求
String response = delegate.postWithHystrix("https://api.alipay.com/xxx", data);

// 使用Hystrix保护的带证书POST请求
String response = delegate.postWithHystrix("https://api.mch.weixin.qq.com/xxx", data, certPath, certPass);
```

### 2. 手动使用HystrixCommandHttpExecutor

```java
@Autowired
private HystrixCommandHttpExecutor hystrixExecutor;

// 执行GET请求
String response = hystrixExecutor.executeGetRequest(url, headers, "wechat");

// 执行POST请求
String response = hystrixExecutor.executePostRequest(url, body, headers, "alipay");

// 执行SSL请求
String response = hystrixExecutor.executeWithCert(url, certPath, certPass, body, "wechat");
```

## 服务类型识别

系统会根据URL自动识别服务类型：

- **wechat**: 包含 `weixin`, `wechat`, `wxpay` 的URL
- **alipay**: 包含 `alipay`, `alipayapi` 的URL
- **logistics**: 包含 `kuaidi`, `logistics`, `express` 的URL
- **geo**: 包含 `amap`, `gaode`, `lbs` 的URL
- **http**: 其他通用HTTP服务

## 降级策略

1. **缓存数据降级**: 当服务熔断时，优先返回缓存的旧数据
2. **默认值降级**: 当没有缓存时，返回对应服务类型的默认降级响应
3. **业务异常**: 关键服务（如支付）可能拒绝降级并返回异常

## 监控

Hystrix Dashboard已启用，可通过以下地址访问：
```
http://your-server/hystrix
```

## 启动类配置

所有服务启动类已添加`@EnableHystrix`注解：

- BuyerApiApplication
- ManagerApiApplication
- StoreApiApplication
- CommonApiApplication
- ConsumerApplication
- ImApiApplication

## 异常处理

Hystrix相关异常会被`GlobalControllerExceptionHandler`捕获并处理：

- HystrixException: Hystrix自定义异常
- HystrixRuntimeException: Hystrix运行时异常

系统会根据熔断状态和缓存命中情况返回适当的错误信息。