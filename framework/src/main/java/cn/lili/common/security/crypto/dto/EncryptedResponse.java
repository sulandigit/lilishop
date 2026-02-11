package cn.lili.common.security.crypto.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 加密响应包装类
 * 后端返回加密响应时使用此结构
 * 
 * @author Qoder
 * @since 2026-01-09
 */
@Data
@ApiModel(value = "加密响应包装")
public class EncryptedResponse {
    
    @ApiModelProperty(value = "标识数据已加密", required = true)
    private Boolean encrypted = true;
    
    @ApiModelProperty(value = "加密的响应数据(Base64编码)", required = true)
    private String data;
    
    @ApiModelProperty(value = "随机字符串(16位)", required = true)
    private String nonce;
}
