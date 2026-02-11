package cn.lili.modules.goods.entity.vos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;

/**
 * 商品图片视图对象
 * 用于返回优化后的图片信息，包含懒加载相关数据
 */
@ApiModel(value = "GoodsImageVO", description = "商品图片视图对象")
public class GoodsImageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "处理后的图片URL", required = true)
    private String imageUrl;

    @ApiModelProperty(value = "占位符图片URL (懒加载前显示)", required = false)
    private String placeholderUrl;

    @ApiModelProperty(value = "原始未处理的图片URL", required = false)
    private String originalUrl;

    @ApiModelProperty(value = "推荐显示宽度", required = false)
    private Integer width;

    @ApiModelProperty(value = "推荐显示高度", required = false)
    private Integer height;

    @ApiModelProperty(value = "估计文件大小（字节）", required = false)
    private Long fileSize;

    @ApiModelProperty(value = "是否需要懒加载", required = true)
    private Boolean isLazyLoad;

    @ApiModelProperty(value = "加载优先级 (0=首屏，1-N=后续)", required = true)
    private Integer loadPriority;

    @ApiModelProperty(value = "图片质量 (1-100)", required = true)
    private Integer quality;

    @ApiModelProperty(value = "图片格式 (JPEG/WEBP/PNG)", required = false)
    private String format;

    @ApiModelProperty(value = "是否支持WebP格式", required = false)
    private Boolean supportWebp;

    // Constructors
    public GoodsImageVO() {
    }

    public GoodsImageVO(String imageUrl, String placeholderUrl, Integer width, Integer height,
                        Boolean isLazyLoad, Integer loadPriority, Integer quality) {
        this.imageUrl = imageUrl;
        this.placeholderUrl = placeholderUrl;
        this.width = width;
        this.height = height;
        this.isLazyLoad = isLazyLoad;
        this.loadPriority = loadPriority;
        this.quality = quality;
    }

    // Getters and Setters
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPlaceholderUrl() {
        return placeholderUrl;
    }

    public void setPlaceholderUrl(String placeholderUrl) {
        this.placeholderUrl = placeholderUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Boolean getIsLazyLoad() {
        return isLazyLoad;
    }

    public void setIsLazyLoad(Boolean isLazyLoad) {
        this.isLazyLoad = isLazyLoad;
    }

    public Integer getLoadPriority() {
        return loadPriority;
    }

    public void setLoadPriority(Integer loadPriority) {
        this.loadPriority = loadPriority;
    }

    public Integer getQuality() {
        return quality;
    }

    public void setQuality(Integer quality) {
        this.quality = quality;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Boolean getSupportWebp() {
        return supportWebp;
    }

    public void setSupportWebp(Boolean supportWebp) {
        this.supportWebp = supportWebp;
    }

    @Override
    public String toString() {
        return "GoodsImageVO{" +
                "imageUrl='" + imageUrl + '\'' +
                ", placeholderUrl='" + placeholderUrl + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", isLazyLoad=" + isLazyLoad +
                ", loadPriority=" + loadPriority +
                ", quality=" + quality +
                ", supportWebp=" + supportWebp +
                '}';
    }
}
