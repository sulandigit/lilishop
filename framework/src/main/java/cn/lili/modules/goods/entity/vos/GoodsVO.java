package cn.lili.modules.goods.entity.vos;

import cn.lili.modules.goods.entity.dos.Goods;
import cn.lili.modules.goods.entity.dos.Wholesale;
import cn.lili.modules.goods.entity.dto.GoodsParamsDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * 商品VO
 *
 * @author pikachu
 * @since 2020-02-26 23:24:13
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsVO extends Goods {

    private static final long serialVersionUID = 6377623919990713567L;

    @ApiModelProperty(value = "分类名称")
    private List<String> categoryName;

    @ApiModelProperty(value = "商品参数")
    private List<GoodsParamsDTO> goodsParamsDTOList;

    @ApiModelProperty(value = "商品图片")
    private List<String> goodsGalleryList;

    @ApiModelProperty(value = "sku列表")
    private List<GoodsSkuVO> skuList;

    @ApiModelProperty(value = "批发商品消费规则列表")
    private List<Wholesale> wholesaleList;

    /**
     * 图片优化相关字段
     */
    @ApiModelProperty(value = "优化后的商品图片列表（包含懒加载信息）")
    private List<GoodsImageVO> goodsImageList;

    @ApiModelProperty(value = "图片元数据（首屏图片数、懒加载阈值等）")
    private Map<String, Object> imageMetadata;
}
