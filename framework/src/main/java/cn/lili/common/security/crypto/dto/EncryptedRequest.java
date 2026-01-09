package cn.lili.common.security.crypto.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 加密请求包装类
 * 前端发送加密请求时使用此结构
 * 
 * @author Qoder
 * @since 2026-01-09
 */
@Data
@ApiModel(value = "加密请求包装")
public class EncryptedRequest {
    
    @ApiModelProperty(value = "加密的请求数据(Base64编码)", required = true)
    private String encryptedData;
    
    @ApiModelProperty(value = "请求时间戳(毫秒)", required = true)
    private Long timestamp;
    
    @ApiModelProperty(value = "随机字符串(16位)", required = true)
    private String nonce;
    
    @ApiModelProperty(value = "HMAC-SHA256签名(可选)")
    private String sign;
}
