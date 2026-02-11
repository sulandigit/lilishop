package cn.lili.common.exception;

import cn.lili.common.enums.ResultCode;

/**
 * 数据加密异常
 * 
 * @author Qoder
 * @since 2026-01-09
 */
public class EncryptionException extends ServiceException {
    
    private static final long serialVersionUID = 1L;
    
    public EncryptionException(String message) {
        super(ResultCode.ENCRYPTION_ERROR, message);
    }
    
    public EncryptionException() {
        super(ResultCode.ENCRYPTION_ERROR);
    }
}
