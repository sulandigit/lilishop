package cn.lili.common.aop.interceptor;

import cn.lili.common.aop.annotation.ApiEncrypt;
import cn.lili.common.exception.DecryptionException;
import cn.lili.common.exception.ReplayAttackException;
import cn.lili.common.exception.SignatureVerificationException;
import cn.lili.common.properties.ApiEncryptionProperties;
import cn.lili.common.security.crypto.ApiEncryptionUtil;
import cn.lili.common.security.crypto.dto.EncryptedRequest;
import cn.lili.cache.Cache;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * API接口请求解密拦截器
 * 拦截标记@ApiEncrypt注解的接口,对请求体进行解密和防重放验证
 * 
 * @author Qoder
 * @since 2026-01-09
 */
@Slf4j
@ControllerAdvice
public class ApiEncryptionRequestBodyAdvice extends RequestBodyAdviceAdapter {
    
    @Autowired
    private ApiEncryptionProperties properties;
    
    @Autowired
    private Cache<String> cache;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 判断是否支持当前请求
     * 检查方法是否有@ApiEncrypt注解且encryptRequest=true
     */
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                           Class<? extends HttpMessageConverter<?>> converterType) {
        // 检查加密功能是否启用
        if (!properties.getEnabled()) {
            return false;
        }
        
        // 检查方法是否有@ApiEncrypt注解
        if (!methodParameter.hasMethodAnnotation(ApiEncrypt.class)) {
            return false;
        }
        
        ApiEncrypt annotation = methodParameter.getMethodAnnotation(ApiEncrypt.class);
        // 检查是否需要加密请求
        return annotation != null && annotation.encryptRequest();
    }
    
    /**
     * 在请求体读取并转换后执行
     * 对加密的请求体进行解密和验证
     */
    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage,
                               MethodParameter parameter, Type targetType,
                               Class<? extends HttpMessageConverter<?>> converterType) {
        try {
            // 如果body不是EncryptedRequest类型,直接返回
            if (!(body instanceof EncryptedRequest)) {
                log.warn("请求体不是EncryptedRequest类型,跳过解密");
                return body;
            }
            
            EncryptedRequest encRequest = (EncryptedRequest) body;
            ApiEncrypt annotation = parameter.getMethodAnnotation(ApiEncrypt.class);
            
            if (annotation == null) {
                return body;
            }
            
            // 1. 验证防重放攻击
            if (annotation.enableReplayProtection()) {
                verifyReplayProtection(encRequest);
            }
            
            // 2. 验证签名(可选)
            if (annotation.enableSignature()) {
                verifySignature(encRequest);
            }
            
            // 3. 解密数据
            String decryptedJson = decryptData(encRequest.getEncryptedData());
            
            // 4. 反序列化为目标类型
            return objectMapper.readValue(decryptedJson, objectMapper.constructType(targetType));
            
        } catch (DecryptionException | ReplayAttackException | SignatureVerificationException e) {
            // 重新抛出业务异常
            throw e;
        } catch (Exception e) {
            log.error("请求解密失败", e);
            throw new DecryptionException("请求数据格式错误");
        }
    }
    
    /**
     * 验证防重放攻击
     * 检查timestamp和nonce
     */
    private void verifyReplayProtection(EncryptedRequest encRequest) {
        // 验证timestamp
        if (encRequest.getTimestamp() == null) {
            throw new ReplayAttackException("请求时间戳不能为空");
        }
        
        if (!ApiEncryptionUtil.verifyTimestamp(encRequest.getTimestamp(), 
                                               properties.getTimestampTolerance())) {
            log.warn("请求时间戳已过期: {}", encRequest.getTimestamp());
            throw new ReplayAttackException("请求已过期");
        }
        
        // 验证nonce
        if (encRequest.getNonce() == null || encRequest.getNonce().isEmpty()) {
            throw new ReplayAttackException("nonce不能为空");
        }
        
        String nonceKey = "API_NONCE:" + encRequest.getNonce();
        if (cache.hasKey(nonceKey)) {
            log.warn("检测到重复的nonce: {}", encRequest.getNonce());
            throw new ReplayAttackException("请求重复");
        }
        
        // 将nonce存入Redis,设置过期时间
        cache.put(nonceKey, "1", properties.getNonceExpire().longValue());
    }
    
    /**
     * 验证签名
     */
    private void verifySignature(EncryptedRequest encRequest) {
        if (encRequest.getSign() == null || encRequest.getSign().isEmpty()) {
            throw new SignatureVerificationException("签名不能为空");
        }
        
        // 构造待签名数据: encryptedData + timestamp + nonce
        String signData = encRequest.getEncryptedData() 
                        + encRequest.getTimestamp() 
                        + encRequest.getNonce();
        
        if (!ApiEncryptionUtil.verifySign(signData, encRequest.getSign(), 
                                          properties.getSignSecret())) {
            log.warn("签名验证失败");
            throw new SignatureVerificationException("签名验证失败");
        }
    }
    
    /**
     * 解密数据
     */
    private String decryptData(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            throw new DecryptionException("加密数据不能为空");
        }
        
        try {
            return ApiEncryptionUtil.decrypt(encryptedData, properties.getAesKey());
        } catch (Exception e) {
            log.error("数据解密失败", e);
            throw new DecryptionException("数据解密失败");
        }
    }
}
