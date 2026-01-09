package cn.lili.common.exception;

import cn.lili.common.enums.ResultCode;

/**
 * 重放攻击异常
 * 当检测到timestamp过期或nonce重复时抛出
 * 
 * @author Qoder
 * @since 2026-01-09
 */
public class ReplayAttackException extends ServiceException {
    
    private static final long serialVersionUID = 1L;
    
    public ReplayAttackException(String message) {
        super(ResultCode.REPLAY_ATTACK, message);
    }
    
    public ReplayAttackException() {
        super(ResultCode.REPLAY_ATTACK);
    }
}
