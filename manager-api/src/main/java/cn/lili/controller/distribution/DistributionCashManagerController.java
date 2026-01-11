package cn.lili.controller.distribution;

import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.context.ThreadContextHolder;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.distribution.entity.dos.DistributionCash;
import cn.lili.modules.distribution.entity.vos.DistributionCashSearchParams;
import cn.lili.modules.distribution.service.DistributionCashService;
import cn.lili.modules.order.order.entity.dto.OrderSearchParams;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * 管理端,分销佣金管理接口
 * 提供分销佣金的查询、审核和导出功能
 *
 * @author pikachu
 * @since 2020-03-14 23:04:56
 */
@RestController
@Api(tags = "管理端,分销佣金管理接口")
@RequestMapping("/manager/distribution/cash")
public class DistributionCashManagerController {

    /**
     * 分销佣金服务
     */
    @Autowired
    private DistributionCashService distributorCashService;

    /**
     * 通过id获取分销佣金详情
     *
     * @param id 分销佣金ID
     * @return 分销佣金详细信息
     */
    @ApiOperation(value = "通过id获取分销佣金详情")
    @GetMapping(value = "/get/{id}")
    public ResultMessage<DistributionCash> get(@PathVariable String id) {
        return ResultUtil.data(distributorCashService.getById(id));
    }

    /**
     * 分页获取分销佣金列表
     *
     * @param distributionCashSearchParams 分销佣金查询参数对象,包含筛选条件和分页信息
     * @return 分页后的分销佣金列表
     */
    @ApiOperation(value = "分页获取")
    @GetMapping(value = "/getByPage")
    public ResultMessage<IPage<DistributionCash>> getByPage(DistributionCashSearchParams distributionCashSearchParams) {

        return ResultUtil.data(distributorCashService.getDistributionCash(distributionCashSearchParams));
    }


    /**
     * 审核分销佣金
     * 对分销商提现申请进行审核处理
     *
     * @param id     分销佣金ID
     * @param result 处理结果,审核通过或拒绝
     * @return 处理后的分销佣金信息
     */
    @PreventDuplicateSubmissions
    @ApiOperation(value = "审核")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "分销佣金ID", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "result", value = "处理结果", required = true, paramType = "query", dataType = "String")
    })
    @PostMapping(value = "/audit/{id}")
    public ResultMessage<DistributionCash> audit(@PathVariable String id, @NotNull String result) {
        return ResultUtil.data(distributorCashService.audit(id, result));
    }


    /**
     * 查询分销提现导出列表
     * 根据查询条件导出分销提现数据到Excel文件
     *
     * @param distributionCashSearchParams 分销佣金查询参数对象
     */
    @ApiOperation(value = "查询分销提现导出列表")
    @GetMapping("/queryExport")
    public void queryExport(DistributionCashSearchParams distributionCashSearchParams) {
        // 获取HTTP响应对象
        HttpServletResponse response = ThreadContextHolder.getHttpResponse();
        // 执行导出操作
        distributorCashService.queryExport(response,distributionCashSearchParams);
    }
}
