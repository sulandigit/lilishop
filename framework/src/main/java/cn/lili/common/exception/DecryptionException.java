package cn.lili.common.exception;

import cn.lili.common.enums.ResultCode;

/**
 * 数据解密异常
 * 
 * @author Qoder
 * @since 2026-01-09
 */
public class DecryptionException extends ServiceException {
    
    private static final long serialVersionUID = 1L;
    
    public DecryptionException(String message) {
        super(ResultCode.DECRYPTION_ERROR, message);
    }
    
    public DecryptionException() {
        super(ResultCode.DECRYPTION_ERROR);
    }
}
