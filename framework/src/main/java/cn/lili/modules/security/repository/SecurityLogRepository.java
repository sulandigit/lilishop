package cn.lili.modules.security.repository;

import cn.lili.modules.security.entity.vo.SecurityLogVO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 安全日志存储库
 *
 * @author Qoder
 * @since 2026-01-05
 */
@Repository
public interface SecurityLogRepository extends ElasticsearchRepository<SecurityLogVO, String> {
}