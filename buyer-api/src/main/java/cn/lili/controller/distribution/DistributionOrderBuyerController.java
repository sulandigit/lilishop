package cn.lili.controller.distribution;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.distribution.entity.dos.DistributionOrder;
import cn.lili.modules.distribution.entity.vos.DistributionOrderSearchParams;
import cn.lili.modules.distribution.service.DistributionOrderService;
import cn.lili.modules.distribution.service.DistributionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 买家端,分销订单接口
 * 
 * 该控制器负责处理买家端分销员的订单查询相关功能,
 * 允许分销员查看自己推广的订单信息及佣金明细。
 *
 * @author pikachu
 * @since 2020/11/16 10:03 下午
 */
@RestController
@Api(tags = "买家端,分销订单接口")
@RequestMapping("/buyer/distribution/order")
public class DistributionOrderBuyerController {

    /**
     * 分销订单服务
     * 用于处理分销订单的业务逻辑,包括订单查询、统计等功能
     */
    @Autowired
    private DistributionOrderService distributionOrderService;
    
    /**
     * 分销员服务
     * 用于获取当前登录的分销员信息
     */
    @Autowired
    private DistributionService distributionService;


    /**
     * 查询分销员订单分页列表
     * 
     * 该接口用于获取当前登录分销员的推广订单列表,支持分页查询。
     * 分销员可以通过此接口查看自己推广产生的所有订单信息,
     * 包括订单状态、佣金金额、下单时间等详细信息。
     *
     * @param distributionOrderSearchParams 分销订单查询参数对象,包含分页信息、订单状态等筛选条件
     * @return 返回分页的分销订单列表,包含订单详细信息和佣金数据
     */
    @ApiOperation(value = "分销员订单")
    @GetMapping
    public ResultMessage<IPage<DistributionOrder>> casHistory(DistributionOrderSearchParams distributionOrderSearchParams) {
        //获取当前登录的分销员ID,确保只能查询自己的订单
        distributionOrderSearchParams.setDistributionId(distributionService.getDistribution().getId());
        //调用服务层进行分页查询
        return ResultUtil.data(distributionOrderService.getDistributionOrderPage(distributionOrderSearchParams));
    }


}