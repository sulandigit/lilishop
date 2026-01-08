package cn.lili.controller.distribution;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.OperationalJudgment;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.distribution.entity.dos.DistributionGoods;
import cn.lili.modules.distribution.entity.dto.DistributionGoodsSearchParams;
import cn.lili.modules.distribution.entity.vos.DistributionGoodsVO;
import cn.lili.modules.distribution.service.DistributionGoodsService;
import cn.lili.modules.distribution.service.DistributionSelectedGoodsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Objects;

/**
 * 店铺端,分销商品接口
 *
 * @author Bulbasaur
 * @since 2020/11/16 10:06 下午
 */
@Validated
@RestController
@Api(tags = "店铺端,分销商品接口")
@RequestMapping("/store/distribution/goods")
@RequiredArgsConstructor
public class DistributionGoodsStoreController {

    /**
     * 分销商品服务
     */
    private final DistributionGoodsService distributionGoodsService;

    /**
     * 已选择分销商品服务
     */
    private final DistributionSelectedGoodsService distributionSelectedGoodsService;

    @ApiOperation(value = "获取分销商商品列表")
    @GetMapping
    public ResultMessage<IPage<DistributionGoodsVO>> distributionGoods(DistributionGoodsSearchParams distributionGoodsSearchParams) {
        return ResultUtil.data(distributionGoodsService.goodsPage(distributionGoodsSearchParams));
    }

    @ApiOperation(value = "选择商品参与分销")
    @ApiImplicitParam(name = "skuId", value = "规格ID", required = true, dataType = "String", paramType = "path")
    @PutMapping(value = "/checked/{skuId}")
    public ResultMessage<DistributionGoods> distributionCheckGoods(
            @NotNull(message = "规格ID不能为空") @PathVariable String skuId,
            @NotNull(message = "佣金金额不能为空") @Positive(message = "佣金金额必须大于0") @RequestParam Double commission) {

        String storeId = Objects.requireNonNull(UserContext.getCurrentUser(), "用户信息不能为空").getStoreId();
        return ResultUtil.data(distributionGoodsService.checked(skuId, commission, storeId));
    }

    @ApiOperation(value = "取消分销商品")
    @ApiImplicitParam(name = "id", value = "分销商商品ID", required = true, paramType = "path")
    @DeleteMapping(value = "/cancel/{id}")
    public ResultMessage<Object> cancel(@NotNull(message = "分销商品ID不能为空") @PathVariable String id) {
        DistributionGoods distributionGoods = distributionGoodsService.getById(id);
        OperationalJudgment.judgment(distributionGoods);
        distributionGoodsService.cancelDistributionGoods(id);
        return ResultUtil.success();
    }

}