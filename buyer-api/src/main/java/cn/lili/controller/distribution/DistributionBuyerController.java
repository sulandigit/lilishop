package cn.lili.controller.distribution;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.distribution.entity.dos.Distribution;
import cn.lili.modules.distribution.entity.dos.DistributionOrder;
import cn.lili.modules.distribution.entity.dto.DistributionApplyDTO;
import cn.lili.modules.distribution.entity.vos.DistributionOrderSearchParams;
import cn.lili.modules.distribution.service.DistributionOrderService;
import cn.lili.modules.distribution.service.DistributionService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 买家端,分销员接口
 * 
 * 该控制器负责处理买家端分销员相关的核心功能,
 * 包括分销员申请、分销员信息查询、分销员订单查询、绑定上级分销员等功能。
 * 这是分销员管理的主要入口接口。
 *
 * @author pikachu
 * @since 2020/11/16 10:03 下午
 */
@RestController
@Api(tags = "买家端,分销员接口")
@RequestMapping("/buyer/distribution/distribution")
public class DistributionBuyerController {

    /**
     * 分销员服务
     * 用于处理分销员的业务逻辑,包括申请、查询、绑定等核心功能
     */
    @Autowired
    private DistributionService distributionService;
    
    /**
     * 分销员订单服务
     * 用于处理分销员相关的订单查询业务
     */
    @Autowired
    private DistributionOrderService distributionOrderService;

    /**
     * 申请成为分销员
     * 
     * 该接口用于会员申请成为平台的分销员。
     * 需要提交必要的申请信息,如姓名、身份证、联系方式等。
     * 申请提交后需要等待平台审核,审核通过后才能正式成为分销员。
     *
     * @param distributionApplyDTO 分销员申请信息对象,包含申请人的个人信息
     * @return 返回申请结果,包含申请是否成功的标识
     */
    @ApiOperation(value = "申请分销员")
    @PostMapping
    public ResultMessage<Object> applyDistribution(DistributionApplyDTO distributionApplyDTO) {
        return ResultUtil.data(distributionService.applyDistribution(distributionApplyDTO));
    }

    /**
     * 获取分销员的订单分页列表
     * 
     * 该接口用于查询当前登录分销员推广产生的订单列表,支持分页和条件筛选。
     * 分销员可以通过此接口查看自己推广的订单详情,包括订单状态、佣金等信息。
     *
     * @param distributionOrderSearchParams 分销订单查询参数,包含分页信息、订单状态等筛选条件
     * @return 返回分页的分销订单列表,包含订单详情和佣金数据
     */
    @ApiOperation(value = "获取分销员分页订单列表")
    @GetMapping("/distributionOrder")
    public ResultMessage<IPage<DistributionOrder>> distributionOrderPage(DistributionOrderSearchParams distributionOrderSearchParams) {
        //设置查询条件为当前登录分销员的ID
        distributionOrderSearchParams.setDistributionId(distributionService.getDistribution().getId());
        return ResultUtil.data(distributionOrderService.getDistributionOrderPage(distributionOrderSearchParams));
    }

    /**
     * 获取当前会员的分销员信息
     * 
     * 该接口用于查询当前登录会员的分销员详细信息。
     * 返回的信息包括分销员ID、待提现金额、冻结金额、已提现金额、
     * 推广订单数、累计佣金等统计数据,以及分销员的审核状态等信息。
     * 在返回数据前会先检查平台分销功能是否已开启。
     *
     * @return 返回当前会员的分销员信息,如果会员不是分销员则返回空或提示信息
     */
    @ApiOperation(value = "获取当前会员的分销员信息", notes = "可根据分销员信息查询待提现金额以及冻结金额等信息")
    @GetMapping
    public ResultMessage<Distribution> getDistribution() {
        //检查分销开关是否开启
        distributionService.checkDistributionSetting();

        return ResultUtil.data(distributionService.getDistribution());
    }

    /**
     * 绑定上级分销员
     * 
     * 该接口用于会员绑定推荐人(上级分销员)。
     * 会员通过推荐人的分销链接注册或首次购买时,会自动绑定该推荐人为上级。
     * 也可以通过此接口手动绑定上级分销员。
     * 绑定后,该会员产生的订单佣金将归属于上级分销员。
     * 
     * @param distributionId 上级分销员的ID
     * @return 返回绑定结果,成功返回成功标识
     */
    @ApiOperation(value = "绑定分销员")
    @ApiImplicitParam(name = "distributionId", value = "分销员ID", required = true, paramType = "path")
    @GetMapping("/bindingDistribution/{distributionId}")
    public ResultMessage<Object> bindingDistribution(@PathVariable String distributionId){
        //执行绑定操作
        distributionService.bindingDistribution(distributionId);
        return ResultUtil.success();
    }
}