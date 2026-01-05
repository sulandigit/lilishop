package cn.lili.modules.system.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 商品设置
 *
 * @author Chopper
 * @since 2020/11/17 7:58 下午
 */
@Data
public class GoodsSetting implements Serializable {

    private static final long serialVersionUID = -4132785717179910025L;
    @ApiModelProperty(value = "是否开启商品审核")
    private Boolean goodsCheck;

    @ApiModelProperty(value = "小图宽")
    private Integer smallPictureWidth;

    @ApiModelProperty(value = "小图高")
    private Integer smallPictureHeight;

    @ApiModelProperty(value = "缩略图宽")
    private Integer abbreviationPictureWidth;

    @ApiModelProperty(value = "缩略图高")
    private Integer abbreviationPictureHeight;

    @ApiModelProperty(value = "原图宽")
    private Integer originalPictureWidth;

    @ApiModelProperty(value = "原图高")
    private Integer originalPictureHeight;

    /**
     * 图片优化相关配置字段
     */
    @ApiModelProperty(value = "默认图片质量(1-100), 推荐80")
    private Integer defaultImageQuality;

    @ApiModelProperty(value = "列表页使用的图片精度(THUMBNAIL/SMALL/ORIGINAL), 默认THUMBNAIL")
    private String listPageImagePrecision;

    @ApiModelProperty(value = "是否启用图片压缩")
    private Boolean enableImageCompression;

    @ApiModelProperty(value = "是否启用WebP格式转换")
    private Boolean enableWebpConversion;

    @ApiModelProperty(value = "列表页首屏返回的图片数, 默认6")
    private Integer listPageImageCount;

    @ApiModelProperty(value = "懒加载阈值 (第几张开始懒加载), 默认3")
    private Integer lazyLoadThreshold;

}