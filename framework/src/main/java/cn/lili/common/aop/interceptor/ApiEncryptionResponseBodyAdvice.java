package cn.lili.common.aop.interceptor;

import cn.lili.common.aop.annotation.ApiEncrypt;
import cn.lili.common.exception.EncryptionException;
import cn.lili.common.properties.ApiEncryptionProperties;
import cn.lili.common.security.crypto.ApiEncryptionUtil;
import cn.lili.common.security.crypto.dto.EncryptedResponse;
import cn.lili.common.vo.ResultMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * API接口响应加密拦截器
 * 拦截标记@ApiEncrypt注解的接口,对响应体进行加密
 * 
 * @author Qoder
 * @since 2026-01-09
 */
@Slf4j
@ControllerAdvice
public class ApiEncryptionResponseBodyAdvice implements ResponseBodyAdvice<ResultMessage> {
    
    @Autowired
    private ApiEncryptionProperties properties;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 判断是否支持当前响应
     * 检查方法是否有@ApiEncrypt注解且encryptResponse=true
     */
    @Override
    public boolean supports(MethodParameter returnType,
                           Class<? extends HttpMessageConverter<?>> converterType) {
        // 检查加密功能是否启用
        if (!properties.getEnabled()) {
            return false;
        }
        
        // 检查方法是否有@ApiEncrypt注解
        if (!returnType.hasMethodAnnotation(ApiEncrypt.class)) {
            return false;
        }
        
        ApiEncrypt annotation = returnType.getMethodAnnotation(ApiEncrypt.class);
        if (annotation == null || !annotation.encryptResponse()) {
            return false;
        }
        
        // 检查返回值类型是否为ResultMessage
        return ResultMessage.class.isAssignableFrom(returnType.getParameterType());
    }
    
    /**
     * 在响应体写入前执行
     * 对ResultMessage的result字段进行加密
     */
    @Override
    public ResultMessage beforeBodyWrite(ResultMessage body, MethodParameter returnType,
                                        MediaType selectedContentType,
                                        Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                        ServerHttpRequest request, ServerHttpResponse response) {
        // 如果body为空或result为空,直接返回
        if (body == null || body.getResult() == null) {
            return body;
        }
        
        try {
            // 检查是否已经是加密格式
            if (body.getResult() instanceof EncryptedResponse) {
                return body;
            }
            
            // 1. 提取result对象
            Object result = body.getResult();
            
            // 2. 序列化为JSON
            String resultJson = objectMapper.writeValueAsString(result);
            
            // 3. 加密
            String encryptedData = encryptData(resultJson);
            
            // 4. 生成nonce
            String nonce = ApiEncryptionUtil.generateNonce();
            
            // 5. 构造EncryptedResponse
            EncryptedResponse encResponse = new EncryptedResponse();
            encResponse.setEncrypted(true);
            encResponse.setData(encryptedData);
            encResponse.setNonce(nonce);
            
            // 6. 替换result字段
            body.setResult(encResponse);
            
            return body;
            
        } catch (Exception e) {
            log.error("响应加密失败", e);
            throw new EncryptionException("数据加密失败");
        }
    }
    
    /**
     * 加密数据
     */
    private String encryptData(String plainText) {
        try {
            return ApiEncryptionUtil.encrypt(plainText, properties.getAesKey());
        } catch (Exception e) {
            log.error("数据加密失败", e);
            throw new EncryptionException("数据加密失败");
        }
    }
}
