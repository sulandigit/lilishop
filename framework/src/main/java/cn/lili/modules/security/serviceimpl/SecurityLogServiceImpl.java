package cn.lili.modules.security.serviceimpl;

import cn.lili.modules.security.entity.vo.SecurityLogVO;
import cn.lili.modules.security.repository.SecurityLogRepository;
import cn.lili.modules.security.service.SecurityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 安全审计日志服务实现
 *
 * @author Qoder
 * @since 2026-01-05
 */
@Service
public class SecurityLogServiceImpl implements SecurityLogService {

    @Autowired
    private SecurityLogRepository securityLogRepository;

    @Override
    public void saveLog(SecurityLogVO securityLogVO) {
        securityLogRepository.save(securityLogVO);
    }
}