package cn.lili.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * API接口加密配置类
 * 
 * @author Qoder
 * @since 2026-01-09
 */
@Configuration
@ConfigurationProperties(prefix = "lili.api-encryption")
@Data
public class ApiEncryptionProperties {
    
    /**
     * 是否启用加密功能
     * 支持开发环境关闭加密便于调试
     */
    private Boolean enabled = true;
    
    /**
     * AES密钥(32字节,Base64编码)
     * 使用Jasypt加密保护
     * 示例配置: ENC(...)
     */
    private String aesKey;
    
    /**
     * 签名密钥
     * 使用Jasypt加密保护
     * 示例配置: ENC(...)
     */
    private String signSecret;
    
    /**
     * 时间戳容差(分钟)
     * 请求时间戳与服务器时间差不能超过此值
     * 默认5分钟
     */
    private Integer timestampTolerance = 5;
    
    /**
     * nonce有效期(秒)
     * 用于防重放攻击,nonce在Redis中的缓存时间
     * 默认300秒(5分钟)
     */
    private Integer nonceExpire = 300;
}
