package cn.lili.modules.goods.service;

import cn.lili.modules.goods.entity.enums.ImagePrecisionEnum;
import cn.lili.modules.goods.entity.vos.GoodsImageVO;
import cn.lili.modules.goods.entity.dos.GoodsGallery;

import java.util.List;

/**
 * 图片处理服务接口
 * 负责图片URL处理、精度控制、质量参数添加等功能
 */
public interface ImageProcessService {

    /**
     * 处理图片URL - 统一的URL处理方法
     * 根据指定的参数添加尺寸、质量、WebP等参数
     *
     * @param originalUrl   原始图片URL
     * @param width         宽度
     * @param height        高度
     * @param quality       质量 (1-100)
     * @param enableWebp    是否启用WebP
     * @return 处理后的完整URL
     */
    String processImageUrl(String originalUrl, Integer width, Integer height, Integer quality, Boolean enableWebp);

    /**
     * 根据精度返回图片URL列表
     *
     * @param goodsId       商品ID
     * @param precision     图片精度 (THUMBNAIL/SMALL/ORIGINAL)
     * @param quality       质量 (1-100)
     * @param enableWebp    是否启用WebP
     * @return 处理后的URL列表
     */
    List<String> getImageUrlByPrecision(String goodsId, ImagePrecisionEnum precision, Integer quality, Boolean enableWebp);

    /**
     * 构建图片对象 - 包含懒加载信息
     *
     * @param gallery           商品相册对象
     * @param precision         图片精度
     * @param loadIndex         第几张图片 (用于计算加载优先级)
     * @param totalImages       图片总数
     * @param listPageImageCount 列表页首屏图片数
     * @param defaultQuality    默认质量
     * @param enableWebp        是否启用WebP
     * @return 图片对象 (包含懒加载信息)
     */
    GoodsImageVO buildImageObject(GoodsGallery gallery, ImagePrecisionEnum precision, int loadIndex,
                                  int totalImages, int listPageImageCount, int defaultQuality, Boolean enableWebp);

    /**
     * 获取质量参数 - 根据OSS类型返回对应的质量参数字符串
     *
     * @param ossType   OSS类型 (ALI_OSS/TENCENT_COS/HUAWEI_OBS/MINIO)
     * @param quality   质量 (1-100)
     * @return 质量参数字符串，例如: "/quality,Q_80" (阿里云)
     */
    String getQualityParameter(String ossType, Integer quality);

    /**
     * 获取WebP参数 - 根据OSS类型返回对应的WebP参数字符串
     *
     * @param ossType   OSS类型 (ALI_OSS/TENCENT_COS/HUAWEI_OBS/MINIO)
     * @return WebP参数字符串，例如: "/format,webp" (阿里云)
     */
    String getWebpParameter(String ossType);

    /**
     * 构建占位符图片 - 用于懒加载前显示
     *
     * @param originalUrl   原始图片URL
     * @param width         宽度
     * @param height        高度
     * @return 占位符URL (可以是超低分辨率图或Base64编码的GIF)
     */
    String buildPlaceholder(String originalUrl, Integer width, Integer height);

    /**
     * 清理URL - 移除原有的参数
     *
     * @param url   图片URL
     * @return 清理后的URL (不包含参数)
     */
    String cleanUrl(String url);

    /**
     * 根据商品相册列表构建图片对象列表
     *
     * @param galleries         商品相册列表
     * @param precision         图片精度
     * @param listPageImageCount 列表页首屏图片数
     * @param defaultQuality    默认质量
     * @param enableWebp        是否启用WebP
     * @return 图片对象列表
     */
    List<GoodsImageVO> buildImageObjectList(List<GoodsGallery> galleries, ImagePrecisionEnum precision,
                                            int listPageImageCount, int defaultQuality, Boolean enableWebp);
}
