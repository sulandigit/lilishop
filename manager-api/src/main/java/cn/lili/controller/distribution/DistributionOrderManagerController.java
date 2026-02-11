package cn.lili.controller.distribution;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.distribution.entity.dos.DistributionOrder;
import cn.lili.modules.distribution.entity.vos.DistributionOrderSearchParams;
import cn.lili.modules.distribution.service.DistributionOrderService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理端,分销订单管理接口
 * 提供分销订单的查询功能
 *
 * @author pikachu
 * @since 2020-03-14 23:04:56
 */
@RestController
@Api(tags = "管理端,分销订单管理接口")
@RequestMapping("/manager/distribution/order")
public class DistributionOrderManagerController {

    /**
     * 分销订单服务
     */
    @Autowired
    private DistributionOrderService distributionOrderService;

    /**
     * 通过id获取分销订单详情
     *
     * @param id 分销订单ID
     * @return 分销订单详细信息
     */
    @ApiOperation(value = "通过id获取分销订单")
    @GetMapping(value = "/get/{id}")
    public ResultMessage<DistributionOrder> get(@PathVariable String id) {
        // 根据ID查询分销订单信息
        return ResultUtil.data(distributionOrderService.getById(id));
    }


    /**
     * 分页获取分销订单列表
     *
     * @param distributionOrderSearchParams 分销订单查询参数对象,包含筛选条件和分页信息
     * @return 分页后的分销订单列表
     */
    @ApiOperation(value = "分页获取分销订单")
    @GetMapping(value = "/getByPage")
    public ResultMessage<IPage<DistributionOrder>> getByPage(DistributionOrderSearchParams distributionOrderSearchParams) {
        // 根据查询条件分页获取分销订单列表
        return ResultUtil.data(distributionOrderService.getDistributionOrderPage(distributionOrderSearchParams));
    }
}