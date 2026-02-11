package cn.lili.controller.distribution;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.distribution.entity.dto.DistributionGoodsSearchParams;
import cn.lili.modules.distribution.entity.vos.DistributionGoodsVO;
import cn.lili.modules.distribution.service.DistributionGoodsService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端,分销商品管理接口
 * 提供分销商品的查询和删除功能
 *
 * @author pikachu
 * @since 2020-03-14 23:04:56
 */
@RestController
@Api(tags = "管理端,分销商品管理接口")
@RequestMapping("/manager/distribution/goods")
public class DistributionGoodsManagerController {

    /**
     * 分销商品服务
     */
    @Autowired
    private DistributionGoodsService distributionGoodsService;

    /**
     * 分页获取分销商品列表
     *
     * @param distributionGoodsSearchParams 分销商品查询参数对象,包含筛选条件和分页信息
     * @return 分页后的分销商品列表
     */
    @GetMapping(value = "/getByPage")
    @ApiOperation(value = "分页获取")
    public ResultMessage<IPage<DistributionGoodsVO>> getByPage(DistributionGoodsSearchParams distributionGoodsSearchParams) {
        return ResultUtil.data(distributionGoodsService.goodsPage(distributionGoodsSearchParams));
    }


    /**
     * 批量删除分销商品
     * 根据ID列表批量删除指定的分销商品
     *
     * @param ids 商品ID列表
     * @return 操作结果
     */
    @DeleteMapping(value = "/delByIds/{ids}")
    @ApiOperation(value = "批量删除")
    public ResultMessage<Object> delAllByIds(@PathVariable List ids) {
        // 批量删除分销商品
        distributionGoodsService.removeByIds(ids);
        return ResultUtil.success();
    }
}