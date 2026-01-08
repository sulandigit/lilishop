package cn.lili.controller.distribution;

import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.distribution.entity.dos.DistributionCash;
import cn.lili.modules.distribution.service.DistributionCashService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;


/**
 * 买家端,分销商品佣金提现接口
 * 
 * 该控制器负责处理买家端分销员的佣金提现相关功能,
 * 包括发起提现申请和查询提现历史记录。
 * 分销员可以通过该接口将已结算的佣金提现到自己的账户。
 *
 * @author pikachu
 * @since 2020/11/16 10:03 下午
 */
@RestController
@Api(tags = "买家端,分销商品佣金提现接口")
@RequestMapping("/buyer/distribution/cash")
@Validated
public class DistributionCashBuyerController {

    /**
     * 分销佣金服务
     * 用于处理分销员佣金提现的业务逻辑
     */
    @Autowired
    private DistributionCashService distributionCashService;
    
    /**
     * 分销员提现服务
     * 用于查询分销员的提现历史记录
     */
    @Autowired
    private DistributionCashService distributorCashService;


    /**
     * 分销员申请提现
     * 
     * 该接口用于分销员发起佣金提现申请。
     * 分销员只能提现已结算且未冻结的佣金金额。
     * 提现金额有单次限制:最少1元,最多9999元。
     * 使用 @PreventDuplicateSubmissions 注解防止重复提交。
     *
     * @param price 申请提现的金额,必须在1-9999元之间
     * @return 返回提现申请结果,成功返回成功标识,失败抛出异常
     * @throws ServiceException 当提现申请失败时(如余额不足、金额不合法等)抛出服务异常
     */
    @PreventDuplicateSubmissions
    @ApiOperation(value = "分销员提现")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "price", value = "申请金额", required = true, paramType = "query", dataType = "double")
    })
    @PostMapping
    public ResultMessage<Object> cash(@Validated @Max(value = 9999, message = "提现金额单次最多允许提现9999元")
                                          @Min(value = 1, message = "提现金额单次最少提现金额为1元")
                                          @NotNull @ApiIgnore Double price) {
        //调用服务层处理提现申请
        if (Boolean.TRUE.equals(distributionCashService.cash(price))) {
            return ResultUtil.success();
        }
        //提现失败抛出异常
        throw new ServiceException(ResultCode.ERROR);
    }

    /**
     * 查询分销员提现历史记录
     * 
     * 该接口用于分页查询当前登录分销员的提现申请历史记录。
     * 返回的记录包括提现金额、申请时间、审核状态、审核时间等信息,
     * 方便分销员跟踪自己的提现申请进度。
     *
     * @param page 分页参数对象,包含页码和每页记录数
     * @return 返回分页的提现历史记录列表,包含提现申请的详细信息
     */
    @ApiOperation(value = "分销员提现历史")
    @GetMapping
    public ResultMessage<IPage<DistributionCash>> casHistory(PageVO page) {
        return ResultUtil.data(distributorCashService.getDistributionCash(page));
    }


}