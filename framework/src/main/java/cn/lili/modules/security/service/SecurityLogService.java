package cn.lili.modules.security.service;

import cn.lili.modules.security.entity.vo.SecurityLogVO;

/**
 * 安全审计日志服务接口
 *
 * @author Qoder
 * @since 2026-01-05
 */
public interface SecurityLogService {

    /**
     * 保存安全审计日志
     * @param securityLogVO 安全审计日志对象
     */
    void saveLog(SecurityLogVO securityLogVO);
}