package cn.lili.mybatis.druid;

import com.alibaba.druid.stat.DruidStatManagerFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 慢SQL监控服务
 * 提供慢SQL统计、查询和告警功能
 *
 * @author lili
 */
@Slf4j
@Service
public class SlowSqlMonitorService {

    /**
     * 慢SQL阈值（毫秒），默认3000ms
     */
    @Value("${druid.slow-sql-millis:3000}")
    private long slowSqlMillis;

    /**
     * 是否启用慢SQL日志记录
     */
    @Value("${druid.slow-sql-log-enabled:true}")
    private boolean slowSqlLogEnabled;

    /**
     * 最近记录的慢SQL列表（用于告警和展示）
     */
    private final CopyOnWriteArrayList<SlowSqlVO> recentSlowSqlList = new CopyOnWriteArrayList<>();

    /**
     * 最大保留的慢SQL记录数
     */
    private static final int MAX_SLOW_SQL_RECORDS = 100;

    /**
     * 获取DruidStatManagerFacade实例
     */
    private DruidStatManagerFacade getDruidStatManagerFacade() {
        return DruidStatManagerFacade.getInstance();
    }

    /**
     * 获取所有SQL统计信息
     */
    public List<SlowSqlVO> getAllSqlStats() {
        List<Map<String, Object>> sqlStatDataList = getDruidStatManagerFacade().getSqlStatDataList(null);
        return sqlStatDataList.stream()
                .map(SlowSqlVO::fromSqlStat)
                .collect(Collectors.toList());
    }

    /**
     * 获取慢SQL列表
     *
     * @param limit 返回数量限制
     * @return 慢SQL列表，按最大执行时间降序排列
     */
    public List<SlowSqlVO> getSlowSqlList(int limit) {
        List<Map<String, Object>> sqlStatDataList = getDruidStatManagerFacade().getSqlStatDataList(null);
        
        return sqlStatDataList.stream()
                .map(SlowSqlVO::fromSqlStat)
                .filter(vo -> vo.getMaxTime() != null && vo.getMaxTime() >= slowSqlMillis)
                .sorted(Comparator.comparing(SlowSqlVO::getMaxTime).reversed())
                .limit(limit > 0 ? limit : 50)
                .collect(Collectors.toList());
    }

    /**
     * 获取最近记录的慢SQL列表
     */
    public List<SlowSqlVO> getRecentSlowSqlList() {
        return new ArrayList<>(recentSlowSqlList);
    }

    /**
     * 获取SQL执行统计摘要
     */
    public SqlStatsSummary getSqlStatsSummary() {
        List<Map<String, Object>> sqlStatDataList = getDruidStatManagerFacade().getSqlStatDataList(null);
        
        SqlStatsSummary summary = new SqlStatsSummary();
        summary.setTotalSqlCount(sqlStatDataList.size());
        
        long slowSqlCount = 0;
        long totalExecuteCount = 0;
        long totalErrorCount = 0;
        long maxExecuteTime = 0;
        
        for (Map<String, Object> stat : sqlStatDataList) {
            SlowSqlVO vo = SlowSqlVO.fromSqlStat(stat);
            
            totalExecuteCount += vo.getExecuteCount() != null ? vo.getExecuteCount() : 0;
            totalErrorCount += vo.getErrorCount() != null ? vo.getErrorCount() : 0;
            
            Long maxTime = vo.getMaxTime();
            if (maxTime != null) {
                if (maxTime >= slowSqlMillis) {
                    slowSqlCount++;
                }
                if (maxTime > maxExecuteTime) {
                    maxExecuteTime = maxTime;
                }
            }
        }
        
        summary.setSlowSqlCount(slowSqlCount);
        summary.setTotalExecuteCount(totalExecuteCount);
        summary.setTotalErrorCount(totalErrorCount);
        summary.setMaxExecuteTime(maxExecuteTime);
        summary.setSlowSqlThreshold(slowSqlMillis);
        
        return summary;
    }

    /**
     * 获取数据源统计信息
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getDataSourceStats() {
        return getDruidStatManagerFacade().getDataSourceStatDataList();
    }

    /**
     * 重置所有统计数据
     */
    public void resetStats() {
        getDruidStatManagerFacade().resetAll();
        recentSlowSqlList.clear();
        log.info("Druid统计数据已重置");
    }

    /**
     * 定时检查慢SQL并记录日志
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000)
    public void checkSlowSql() {
        if (!slowSqlLogEnabled) {
            return;
        }

        List<SlowSqlVO> slowSqlList = getSlowSqlList(20);
        
        if (!slowSqlList.isEmpty()) {
            log.warn("===== 慢SQL监控报告 =====");
            log.warn("发现 {} 条慢SQL（阈值: {}ms）", slowSqlList.size(), slowSqlMillis);
            
            for (SlowSqlVO vo : slowSqlList) {
                log.warn("慢SQL - 最大耗时: {}ms, 执行次数: {}, SQL: {}",
                        vo.getMaxTime(), vo.getExecuteCount(),
                        truncateSql(vo.getSql(), 200));
                
                // 添加到最近慢SQL列表
                addToRecentSlowSqlList(vo);
            }
            log.warn("===== 慢SQL监控报告结束 =====");
        }
    }

    /**
     * 添加到最近慢SQL列表
     */
    private void addToRecentSlowSqlList(SlowSqlVO vo) {
        // 检查是否已存在相同SQL
        boolean exists = recentSlowSqlList.stream()
                .anyMatch(existing -> existing.getSqlId() != null && existing.getSqlId().equals(vo.getSqlId()));
        
        if (!exists) {
            if (recentSlowSqlList.size() >= MAX_SLOW_SQL_RECORDS) {
                recentSlowSqlList.remove(0);
            }
            recentSlowSqlList.add(vo);
        }
    }

    /**
     * 截断SQL语句用于日志显示
     */
    private String truncateSql(String sql, int maxLength) {
        if (sql == null) {
            return "";
        }
        sql = sql.replaceAll("\\s+", " ").trim();
        if (sql.length() > maxLength) {
            return sql.substring(0, maxLength) + "...";
        }
        return sql;
    }

    /**
     * 获取慢SQL阈值
     */
    public long getSlowSqlMillis() {
        return slowSqlMillis;
    }

    /**
     * 动态设置慢SQL阈值
     */
    public void setSlowSqlMillis(long slowSqlMillis) {
        this.slowSqlMillis = slowSqlMillis;
        log.info("慢SQL阈值已更新为: {}ms", slowSqlMillis);
    }
}
