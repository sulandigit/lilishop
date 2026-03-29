package cn.lili.common.exception;

import cn.lili.common.enums.ResultCode;

/**
 * 签名验证异常
 * 当请求签名验证失败时抛出
 * 
 * @author Qoder
 * @since 2026-01-09
 */
public class SignatureVerificationException extends ServiceException {
    
    private static final long serialVersionUID = 1L;
    
    public SignatureVerificationException(String message) {
        super(ResultCode.SIGNATURE_ERROR, message);
    }
    
    public SignatureVerificationException() {
        super(ResultCode.SIGNATURE_ERROR);
    }
}
