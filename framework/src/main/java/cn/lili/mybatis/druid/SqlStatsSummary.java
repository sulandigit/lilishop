package cn.lili.mybatis.druid;

import lombok.Data;

import java.io.Serializable;

/**
 * SQL统计摘要
 *
 * @author lili
 */
@Data
public class SqlStatsSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SQL语句总数
     */
    private int totalSqlCount;

    /**
     * 慢SQL数量
     */
    private long slowSqlCount;

    /**
     * SQL执行总次数
     */
    private long totalExecuteCount;

    /**
     * SQL执行错误总次数
     */
    private long totalErrorCount;

    /**
     * 最大执行时间（毫秒）
     */
    private long maxExecuteTime;

    /**
     * 慢SQL阈值（毫秒）
     */
    private long slowSqlThreshold;
}
