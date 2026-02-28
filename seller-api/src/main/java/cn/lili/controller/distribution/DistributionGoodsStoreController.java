package cn.lili.controller.distribution;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.distribution.entity.dos.DistributionGoods;
import cn.lili.modules.distribution.entity.dto.DistributionGoodsSearchParams;
import cn.lili.modules.distribution.entity.vos.DistributionGoodsVO;
import cn.lili.modules.distribution.service.DistributionGoodsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * 店铺端,分销商品接口
 *
 * @author Bulbasaur
 * @since 2020/11/16 10:06 下午
 */
@RestController
@Api(tags = "店铺端,分销商品接口")
@RequestMapping("/store/distribution/goods")
@RequiredArgsConstructor
public class DistributionGoodsStoreController {

    private final DistributionGoodsService distributionGoodsService;

    @ApiOperation(value = "获取分销商商品列表")
    @GetMapping
    public ResultMessage<IPage<DistributionGoodsVO>> distributionGoods(DistributionGoodsSearchParams distributionGoodsSearchParams) {
        return ResultUtil.data(distributionGoodsService.goodsPage(distributionGoodsSearchParams));
    }

    @ApiOperation(value = "选择商品参与分销")
    @ApiImplicitParam(name = "skuId", value = "规格ID", required = true, dataType = "String", paramType = "path")
    @PutMapping(value = "/checked/{skuId}")
    public ResultMessage<DistributionGoods> distributionCheckGoods(@NotNull(message = "规格ID不能为空") @PathVariable String skuId,
                                                                   @NotNull(message = "佣金金额不能为空") @RequestParam Double commission) {
        String storeId = Optional.ofNullable(UserContext.getCurrentUser())
                .orElseThrow(() -> new IllegalStateException("用户未登录"))
                .getStoreId();
        return ResultUtil.data(distributionGoodsService.checked(skuId, commission, storeId));
    }

    @ApiOperation(value = "取消分销商品")
    @ApiImplicitParam(name = "id", value = "分销商商品ID", required = true, paramType = "path")
    @DeleteMapping(value = "/cancel/{id}")
    public ResultMessage<Object> cancel(@NotNull(message = "分销商品ID不能为空") @PathVariable String id) {
        distributionGoodsService.cancelDistributionGoods(id);
        return ResultUtil.success();
    }

}
