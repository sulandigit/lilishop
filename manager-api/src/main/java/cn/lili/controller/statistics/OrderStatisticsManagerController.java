package cn.lili.controller.statistics;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.order.aftersale.entity.dos.AfterSale;
import cn.lili.modules.order.order.entity.vo.OrderSimpleVO;
import cn.lili.modules.statistics.entity.dto.StatisticsQueryParam;
import cn.lili.modules.statistics.entity.vo.OrderAnalysisVO;
import cn.lili.modules.statistics.entity.vo.OrderOverviewVO;
import cn.lili.modules.statistics.entity.vo.OrderSourceAnalysisVO;
import cn.lili.modules.statistics.entity.vo.OrderStatisticsDataVO;
import cn.lili.modules.statistics.entity.vo.OrderTimeDistributionVO;
import cn.lili.modules.statistics.entity.vo.OrderTrendCompareVO;
import cn.lili.modules.statistics.service.AfterSaleStatisticsService;
import cn.lili.modules.statistics.service.OrderStatisticsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端,订单统计接口
 *
 * @author Bulbasaur
 * @since 2020/12/9 19:04
 */
@Slf4j
@Api(tags = "管理端,订单统计接口")
@RestController
@RequestMapping("/manager/statistics/order")
public class OrderStatisticsManagerController {
    @Autowired
    private OrderStatisticsService orderStatisticsService;
    @Autowired
    private AfterSaleStatisticsService afterSaleStatisticsService;

    @ApiOperation(value = "订单概览统计")
    @GetMapping("/overview")
    public ResultMessage<OrderOverviewVO> overview(StatisticsQueryParam statisticsQueryParam) {
        try {
            return ResultUtil.data(orderStatisticsService.overview(statisticsQueryParam));
        } catch (Exception e) {
            log.error("订单概览统计错误",e);
        }
        return null;
    }

    @ApiOperation(value = "订单图表统计")
    @GetMapping
    public ResultMessage<List<OrderStatisticsDataVO>> statisticsChart(StatisticsQueryParam statisticsQueryParam) {
        try {
            return ResultUtil.data(orderStatisticsService.statisticsChart(statisticsQueryParam));
        } catch (Exception e) {
            log.error("订单图表统计",e);
        }
        return null;
    }


    @ApiOperation(value = "订单统计")
    @GetMapping("/order")
    public ResultMessage<IPage<OrderSimpleVO>> order(StatisticsQueryParam statisticsQueryParam, PageVO pageVO) {
        try {
            return ResultUtil.data(orderStatisticsService.getStatistics(statisticsQueryParam, pageVO));
        } catch (Exception e) {
            log.error("订单统计",e);
        }
        return null;
    }


    @ApiOperation(value = "退单统计")
    @GetMapping("/refund")
    public ResultMessage<IPage<AfterSale>> refund(StatisticsQueryParam statisticsQueryParam, PageVO pageVO) {
        return ResultUtil.data(afterSaleStatisticsService.getStatistics(statisticsQueryParam, pageVO));
    }

    @ApiOperation(value = "订单分析增强（客单价、复购率等）")
    @GetMapping("/analysis")
    public ResultMessage<OrderAnalysisVO> analysis(StatisticsQueryParam statisticsQueryParam) {
        try {
            return ResultUtil.data(orderStatisticsService.getOrderAnalysis(statisticsQueryParam));
        } catch (Exception e) {
            log.error("订单分析增强统计错误", e);
        }
        return null;
    }

    @ApiOperation(value = "趋势对比分析（同比/环比）")
    @GetMapping("/trend-compare")
    public ResultMessage<OrderTrendCompareVO> trendCompare(StatisticsQueryParam statisticsQueryParam, String compareType) {
        try {
            return ResultUtil.data(orderStatisticsService.getTrendCompare(statisticsQueryParam, compareType));
        } catch (Exception e) {
            log.error("趋势对比分析错误", e);
        }
        return null;
    }

    @ApiOperation(value = "时段分布统计（24小时）")
    @GetMapping("/time-distribution")
    public ResultMessage<List<OrderTimeDistributionVO>> timeDistribution(StatisticsQueryParam statisticsQueryParam) {
        try {
            return ResultUtil.data(orderStatisticsService.getTimeDistribution(statisticsQueryParam));
        } catch (Exception e) {
            log.error("时段分布统计错误", e);
        }
        return null;
    }

    @ApiOperation(value = "订单来源分析")
    @GetMapping("/source-analysis")
    public ResultMessage<List<OrderSourceAnalysisVO>> sourceAnalysis(StatisticsQueryParam statisticsQueryParam) {
        try {
            return ResultUtil.data(orderStatisticsService.getSourceAnalysis(statisticsQueryParam));
        } catch (Exception e) {
            log.error("订单来源分析错误", e);
        }
        return null;
    }
}