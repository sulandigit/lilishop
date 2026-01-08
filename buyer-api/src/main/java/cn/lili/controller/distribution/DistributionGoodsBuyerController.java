package cn.lili.controller.distribution;

import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.distribution.entity.dto.DistributionGoodsSearchParams;
import cn.lili.modules.distribution.entity.vos.DistributionGoodsVO;
import cn.lili.modules.distribution.service.DistributionGoodsService;
import cn.lili.modules.distribution.service.DistributionSelectedGoodsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

/**
 * 买家端,分销商品接口
 * 
 * 该控制器负责处理买家端分销商品相关的功能,
 * 包括分销商品列表查询、选择/取消分销商品等操作。
 * 分销员可以通过该接口浏览可推广的商品,并选择自己要推广的商品。
 *
 * @author Bulbasaur
 * @since 2020/11/16 10:06 下午
 */
@RestController
@Api(tags = "买家端,分销商品接口")
@RequestMapping("/buyer/distribution/goods")
public class DistributionGoodsBuyerController {

    /**
     * 分销商品服务
     * 用于处理分销商品的业务逻辑,包括商品查询、商品信息获取等功能
     */
    @Autowired
    private DistributionGoodsService distributionGoodsService;
    
    /**
     * 选择分销商品服务
     * 用于处理分销员选择或取消推广商品的业务逻辑
     */
    @Autowired
    private DistributionSelectedGoodsService distributionSelectedGoodsService;


    /**
     * 获取分销商品分页列表
     * 
     * 该接口用于查询平台所有可推广的分销商品列表,支持分页和条件筛选。
     * 分销员可以浏览这些商品的佣金比例、价格等信息,
     * 以便决定选择哪些商品进行推广。
     *
     * @param distributionGoodsSearchParams 分销商品查询参数,包含分页信息、商品名称、分类等筛选条件
     * @return 返回分页的分销商品列表,包含商品详情和佣金信息
     */
    @ApiOperation(value = "获取分销商商品列表")
    @GetMapping
    public ResultMessage<IPage<DistributionGoodsVO>> distributionGoods(DistributionGoodsSearchParams distributionGoodsSearchParams) {
        return ResultUtil.data(distributionGoodsService.goodsPage(distributionGoodsSearchParams));
    }

    /**
     * 选择或取消分销商品
     * 
     * 该接口用于分销员选择要推广的商品或取消已选择的商品。
     * 分销员只有选择商品后,才能生成该商品的推广链接进行推广。
     * 使用 @PreventDuplicateSubmissions 注解防止重复提交。
     *
     * @param distributionGoodsId 分销商品ID,标识要操作的商品
     * @param checked 是否选择该商品,true表示选择推广,false表示取消推广
     * @return 返回操作结果,成功返回成功标识,失败抛出异常
     * @throws ServiceException 当操作失败时抛出服务异常
     */
    @PreventDuplicateSubmissions
    @ApiOperation(value = "选择分销商品")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "distributionGoodsId", value = "分销ID", required = true, dataType = "String", paramType = "path"),
            @ApiImplicitParam(name = "checked", value = "是否选择", required = true, dataType = "boolean", paramType = "query")
    })
    @GetMapping(value = "/checked/{distributionGoodsId}")
    public ResultMessage<Object> distributionCheckGoods(
            @NotNull(message = "分销商品不能为空") @PathVariable("distributionGoodsId") String distributionGoodsId, Boolean checked) {
        Boolean result = false;
        //根据checked参数判断是添加还是删除
        if (checked) {
            //选择分销商品,添加到分销员的推广商品列表
            result = distributionSelectedGoodsService.add(distributionGoodsId);
        } else {
            //取消分销商品,从分销员的推广商品列表中删除
            result = distributionSelectedGoodsService.delete(distributionGoodsId);
        }
        //判断操作结果
        if (result) {
            return ResultUtil.success(ResultCode.SUCCESS);
        } else {
            throw new ServiceException(ResultCode.ERROR);
        }

    }
}