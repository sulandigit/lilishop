package cn.lili.mybatis.druid;

import com.alibaba.druid.stat.DruidStatManagerFacade;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 慢SQL信息VO
 *
 * @author lili
 */
@Data
public class SlowSqlVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SQL语句
     */
    private String sql;

    /**
     * SQL ID（SQL语句的hash值）
     */
    private Long sqlId;

    /**
     * 数据源名称
     */
    private String dataSource;

    /**
     * 执行次数
     */
    private Long executeCount;

    /**
     * 执行错误次数
     */
    private Long errorCount;

    /**
     * 总执行时间（毫秒）
     */
    private Long totalTime;

    /**
     * 最大执行时间（毫秒）
     */
    private Long maxTime;

    /**
     * 最后一次执行时间
     */
    private Date lastTime;

    /**
     * 最后一次执行时长（毫秒）
     */
    private Long lastTimeSpan;

    /**
     * 影响行数
     */
    private Long effectedRowCount;

    /**
     * 读取行数
     */
    private Long fetchRowCount;

    /**
     * 正在运行的数量
     */
    private Integer runningCount;

    /**
     * 并发执行最大数量
     */
    private Integer concurrentMax;

    /**
     * 是否为慢SQL
     */
    private Boolean slowSql;

    /**
     * 从DruidStatManagerFacade的SQL统计数据构建SlowSqlVO
     */
    @SuppressWarnings("unchecked")
    public static SlowSqlVO fromSqlStat(Map<String, Object> sqlStat) {
        SlowSqlVO vo = new SlowSqlVO();
        vo.setSql((String) sqlStat.get("SQL"));
        vo.setSqlId((Long) sqlStat.get("ID"));
        vo.setDataSource((String) sqlStat.get("DataSource"));
        vo.setExecuteCount(getLongValue(sqlStat, "ExecuteCount"));
        vo.setErrorCount(getLongValue(sqlStat, "ErrorCount"));
        vo.setTotalTime(getLongValue(sqlStat, "TotalTime"));
        vo.setMaxTime(getLongValue(sqlStat, "MaxTimespan"));
        vo.setLastTime((Date) sqlStat.get("LastTime"));
        vo.setLastTimeSpan(getLongValue(sqlStat, "LastTimespan"));
        vo.setEffectedRowCount(getLongValue(sqlStat, "EffectedRowCount"));
        vo.setFetchRowCount(getLongValue(sqlStat, "FetchRowCount"));
        vo.setRunningCount(getIntValue(sqlStat, "RunningCount"));
        vo.setConcurrentMax(getIntValue(sqlStat, "ConcurrentMax"));
        
        // 判断是否为慢SQL（最大执行时间超过慢SQL阈值）
        Long maxTime = vo.getMaxTime();
        vo.setSlowSql(maxTime != null && maxTime > 0);
        
        return vo;
    }

    private static Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        return 0L;
    }

    private static Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Long) {
            return ((Long) value).intValue();
        }
        return 0;
    }
}
