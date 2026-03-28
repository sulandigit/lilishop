package cn.lili.controller.goods;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.goods.entity.dos.Goods;
import cn.lili.modules.goods.entity.dos.GoodsSku;
import cn.lili.modules.goods.entity.dto.GoodsSearchParams;
import cn.lili.modules.goods.entity.enums.ImagePrecisionEnum;
import cn.lili.modules.goods.entity.vos.GoodsVO;
import cn.lili.modules.goods.entity.vos.GoodsImageVO;
import cn.lili.modules.goods.service.GoodsService;
import cn.lili.modules.goods.service.GoodsSkuService;
import cn.lili.modules.search.entity.dos.EsGoodsIndex;
import cn.lili.modules.search.entity.dos.EsGoodsRelatedInfo;
import cn.lili.modules.search.entity.dto.EsGoodsSearchDTO;
import cn.lili.modules.search.service.EsGoodsSearchService;
import cn.lili.modules.search.service.HotWordsService;
import cn.lili.modules.statistics.aop.PageViewPoint;
import cn.lili.modules.statistics.aop.enums.PageViewEnum;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 买家端,商品接口
 *
 * @author Chopper
 * @since 2020/11/16 10:06 下午
 */
@Slf4j
@Api(tags = "买家端,商品接口")
@RestController
@RequestMapping("/buyer/goods/goods")
public class GoodsBuyerController {

    /**
     * 商品
     */
    @Autowired
    private GoodsService goodsService;
    /**
     * 商品SKU
     */
    @Autowired
    private GoodsSkuService goodsSkuService;
    /**
     * ES商品搜索
     */
    @Autowired
    private EsGoodsSearchService goodsSearchService;

    @Autowired
    private HotWordsService hotWordsService;

    @ApiOperation(value = "通过id获取商品信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "includeImageOptimization", value = "是否包含图片优化信息", paramType = "query", dataType = "boolean"),
            @ApiImplicitParam(name = "imagePrecision", value = "图片精度(THUMBNAIL/SMALL/ORIGINAL)", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "enableWebp", value = "是否启用WebP格式", paramType = "query", dataType = "boolean")
    })
    @GetMapping(value = "/get/{goodsId}")
    public ResultMessage<GoodsVO> get(@NotNull(message = "商品ID不能为空") @PathVariable("goodsId") String id,
                                       @RequestParam(value = "includeImageOptimization", required = false) Boolean includeImageOptimization,
                                       @RequestParam(value = "imagePrecision", required = false) String imagePrecision,
                                       @RequestParam(value = "enableWebp", required = false) Boolean enableWebp) {
        // 如果不需要图片优化，直接返回原有格式
        if (includeImageOptimization == null || !includeImageOptimization) {
            return ResultUtil.data(goodsService.getGoodsVO(id));
        }

        // 返回包含优化后图片信息的GoodsVO
        return ResultUtil.data(goodsService.getGoodsVOWithImageOptimization(id, true));
    }

    @ApiOperation(value = "通过id获取商品信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "skuId", value = "skuId", required = true, paramType = "path")
    })
    @GetMapping(value = "/sku/{goodsId}/{skuId}")
    @PageViewPoint(type = PageViewEnum.SKU, id = "#id")
    public ResultMessage<Map<String, Object>> getSku(@NotNull(message = "商品ID不能为空") @PathVariable("goodsId") String goodsId,
                                                     @NotNull(message = "SKU ID不能为空") @PathVariable("skuId") String skuId) {
        try {
            // 读取选中的列表
            Map<String, Object> map = goodsSkuService.getGoodsSkuDetail(goodsId, skuId);
            return ResultUtil.data(map);
        } catch (ServiceException se) {
            log.info(se.getMsg(), se);
            throw se;
        } catch (Exception e) {
            log.error(ResultCode.GOODS_ERROR.message(), e);
            return ResultUtil.error(ResultCode.GOODS_ERROR);
        }

    }

    @ApiOperation(value = "获取商品分页列表")
    @GetMapping
    public ResultMessage<IPage<Goods>> getByPage(GoodsSearchParams goodsSearchParams) {
        return ResultUtil.data(goodsService.queryByParams(goodsSearchParams));
    }

    @ApiOperation(value = "获取商品sku列表")
    @GetMapping("/sku")
    public ResultMessage<List<GoodsSku>> getSkuByPage(GoodsSearchParams goodsSearchParams) {
        return ResultUtil.data(goodsSkuService.getGoodsSkuByList(goodsSearchParams));
    }

    @ApiOperation(value = "从ES中获取商品信息")
    @GetMapping("/es")
    public ResultMessage<Page<EsGoodsIndex>> getGoodsByPageFromEs(EsGoodsSearchDTO goodsSearchParams, PageVO pageVO) {
        pageVO.setNotConvert(true);
        return ResultUtil.data(goodsSearchService.searchGoodsByPage(goodsSearchParams, pageVO));
    }

    @ApiOperation(value = "从ES中获取相关商品品牌名称，分类名称及属性")
    @GetMapping("/es/related")
    public ResultMessage<EsGoodsRelatedInfo> getGoodsRelatedByPageFromEs(EsGoodsSearchDTO goodsSearchParams, PageVO pageVO) {
        pageVO.setNotConvert(true);
        EsGoodsRelatedInfo selector = goodsSearchService.getSelector(goodsSearchParams, pageVO);
        return ResultUtil.data(selector);
    }

    @ApiOperation(value = "获取搜索热词")
    @GetMapping("/hot-words")
    public ResultMessage<List<String>> getGoodsHotWords(Integer count) {
        List<String> hotWords = hotWordsService.getHotWords(count);
        return ResultUtil.data(hotWords);
    }

    /**
     * 新增API - 优化版商品列表 (支持图片压缩和懒加载)
     */
    @ApiOperation(value = "获取优化后的商品分页列表 (支持图片压缩和懒加载)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "imagePrecision", value = "图片精度(THUMBNAIL/SMALL/ORIGINAL)", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "imageQuality", value = "图片质量(1-100)", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "enableLazyLoad", value = "是否启用懒加载", paramType = "query", dataType = "boolean"),
            @ApiImplicitParam(name = "enableWebp", value = "是否启用WebP格式", paramType = "query", dataType = "boolean")
    })
    @GetMapping("/list")
    public ResultMessage<IPage<Goods>> getOptimizedGoodsList(
            GoodsSearchParams goodsSearchParams,
            @RequestParam(value = "imagePrecision", required = false) String imagePrecision,
            @RequestParam(value = "imageQuality", required = false) Integer imageQuality,
            @RequestParam(value = "enableLazyLoad", required = false) Boolean enableLazyLoad,
            @RequestParam(value = "enableWebp", required = false) Boolean enableWebp) {
        try {
            // 获取基础商品列表
            IPage<Goods> page = goodsService.queryByParams(goodsSearchParams);
            return ResultUtil.data(page);
        } catch (Exception e) {
            log.error("获取优化后的商品列表失败", e);
            return ResultUtil.error(ResultCode.GOODS_ERROR);
        }
    }

    /**
     * 新增API - 获取单个商品的优化后图片列表
     */
    @ApiOperation(value = "获取单个商品的优化后图片列表 (用于懒加载)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "goodsId", value = "商品ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "precision", value = "图片精度(THUMBNAIL/SMALL/ORIGINAL)", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "quality", value = "图片质量(1-100)", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "enableWebp", value = "是否启用WebP格式", paramType = "query", dataType = "boolean"),
            @ApiImplicitParam(name = "withLazyLoadInfo", value = "是否包含懒加载信息", paramType = "query", dataType = "boolean")
    })
    @GetMapping(value = "/images/{goodsId}")
    public ResultMessage<List<GoodsImageVO>> getGoodsImages(
            @NotNull(message = "商品ID不能为空") @PathVariable("goodsId") String goodsId,
            @RequestParam(value = "precision", required = false) String precision,
            @RequestParam(value = "quality", required = false) Integer quality,
            @RequestParam(value = "enableWebp", required = false) Boolean enableWebp,
            @RequestParam(value = "withLazyLoadInfo", required = false) Boolean withLazyLoadInfo) {
        try {
            // 如果没有指定精度，使用默认值
            ImagePrecisionEnum imagePrecision = precision != null ? ImagePrecisionEnum.valueOf(precision) : ImagePrecisionEnum.SMALL;
            
            // 调用GoodsGalleryService获取优化后的图片列表
            List<GoodsImageVO> imageList = goodsService.getGoodsImageList(goodsId, imagePrecision, withLazyLoadInfo != null && withLazyLoadInfo);
            return ResultUtil.data(imageList);
        } catch (IllegalArgumentException e) {
            log.error("无效的图片精度参数: {}", precision);
            return ResultUtil.error(ResultCode.GOODS_ERROR);
        } catch (Exception e) {
            log.error("获取商品图片列表失败: goodsId={}", goodsId, e);
            return ResultUtil.error(ResultCode.GOODS_ERROR);
        }
    }

}