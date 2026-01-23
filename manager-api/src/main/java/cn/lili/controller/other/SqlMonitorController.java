package cn.lili.controller.other;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.mybatis.druid.SlowSqlMonitorService;
import cn.lili.mybatis.druid.SlowSqlVO;
import cn.lili.mybatis.druid.SqlStatsSummary;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * SQL性能监控控制器
 * 提供慢SQL查询、统计信息查看等功能
 *
 * @author lili
 */
@RestController
@Api(tags = "SQL性能监控")
@RequestMapping("/manager/other/sql-monitor")
public class SqlMonitorController {

    @Autowired
    private SlowSqlMonitorService slowSqlMonitorService;

    @ApiOperation(value = "获取SQL统计摘要")
    @GetMapping("/summary")
    public ResultMessage<SqlStatsSummary> getSummary() {
        return ResultUtil.data(slowSqlMonitorService.getSqlStatsSummary());
    }

    @ApiOperation(value = "获取慢SQL列表")
    @GetMapping("/slow-sql")
    public ResultMessage<List<SlowSqlVO>> getSlowSqlList(
            @ApiParam(value = "返回数量限制", defaultValue = "50")
            @RequestParam(defaultValue = "50") int limit) {
        return ResultUtil.data(slowSqlMonitorService.getSlowSqlList(limit));
    }

    @ApiOperation(value = "获取所有SQL统计信息")
    @GetMapping("/all-sql")
    public ResultMessage<List<SlowSqlVO>> getAllSqlStats() {
        return ResultUtil.data(slowSqlMonitorService.getAllSqlStats());
    }

    @ApiOperation(value = "获取最近记录的慢SQL")
    @GetMapping("/recent-slow-sql")
    public ResultMessage<List<SlowSqlVO>> getRecentSlowSql() {
        return ResultUtil.data(slowSqlMonitorService.getRecentSlowSqlList());
    }

    @ApiOperation(value = "获取数据源统计信息")
    @GetMapping("/datasource")
    public ResultMessage<List<Map<String, Object>>> getDataSourceStats() {
        return ResultUtil.data(slowSqlMonitorService.getDataSourceStats());
    }

    @ApiOperation(value = "获取当前慢SQL阈值")
    @GetMapping("/threshold")
    public ResultMessage<Long> getSlowSqlThreshold() {
        return ResultUtil.data(slowSqlMonitorService.getSlowSqlMillis());
    }

    @ApiOperation(value = "设置慢SQL阈值")
    @PutMapping("/threshold")
    public ResultMessage<String> setSlowSqlThreshold(
            @ApiParam(value = "慢SQL阈值（毫秒）", required = true)
            @RequestParam long millis) {
        if (millis < 100) {
            return ResultUtil.error(400, "慢SQL阈值不能小于100毫秒");
        }
        slowSqlMonitorService.setSlowSqlMillis(millis);
        return ResultUtil.success("慢SQL阈值已更新为: " + millis + "ms");
    }

    @ApiOperation(value = "重置SQL统计数据")
    @DeleteMapping("/reset")
    public ResultMessage<String> resetStats() {
        slowSqlMonitorService.resetStats();
        return ResultUtil.success("SQL统计数据已重置");
    }
}
