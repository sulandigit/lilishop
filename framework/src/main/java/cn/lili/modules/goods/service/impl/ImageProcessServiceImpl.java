package cn.lili.modules.goods.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import cn.lili.modules.goods.entity.enums.ImagePrecisionEnum;
import cn.lili.modules.goods.entity.vos.GoodsImageVO;
import cn.lili.modules.goods.entity.dos.GoodsGallery;
import cn.lili.modules.goods.service.ImageProcessService;
import cn.lili.modules.file.entity.enums.OssEnum;
import cn.lili.modules.system.entity.dto.GoodsSetting;
import cn.lili.modules.system.entity.dto.OssSetting;
import cn.lili.modules.system.entity.enums.SettingEnum;
import cn.lili.modules.system.entity.dos.Setting;
import cn.lili.modules.system.service.SettingService;
import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 图片处理服务实现
 * 实现图片URL处理、质量参数添加、WebP格式转换等功能
 */
@Service
public class ImageProcessServiceImpl implements ImageProcessService {

    private static final Logger logger = LoggerFactory.getLogger(ImageProcessServiceImpl.class);

    // 默认图片质量
    private static final int DEFAULT_QUALITY = 80;
    // 最小图片质量
    private static final int MIN_QUALITY = 1;
    // 最大图片质量
    private static final int MAX_QUALITY = 100;

    @Autowired
    private SettingService settingService;

    /**
     * 获取OSS设置
     */
    private OssSetting getOssSetting() {
        try {
            Setting setting = settingService.get(SettingEnum.OSS_SETTING.name());
            if (setting != null) {
                return JSONUtil.toBean(setting.getSettingValue(), OssSetting.class);
            }
        } catch (Exception e) {
            logger.error("获取OSS设置失败", e);
        }
        return null;
    }

    /**
     * 获取商品设置
     */
    private GoodsSetting getGoodsSetting() {
        try {
            Setting setting = settingService.get(SettingEnum.GOODS_SETTING.name());
            if (setting != null) {
                return JSONUtil.toBean(setting.getSettingValue(), GoodsSetting.class);
            }
        } catch (Exception e) {
            logger.error("获取商品设置失败", e);
        }
        return null;
    }

    /**
     * 处理图片URL - 统一的URL处理方法
     */
    @Override
    public String processImageUrl(String originalUrl, Integer width, Integer height, Integer quality, Boolean enableWebp) {
        try {
            // 参数校验
            if (CharSequenceUtil.isBlank(originalUrl)) {
                return originalUrl;
            }

            quality = validateQuality(quality);
            enableWebp = enableWebp != null && enableWebp;

            OssSetting ossSetting = getOssSetting();
            if (ossSetting == null) {
                return originalUrl;
            }

            // 清理原URL
            String cleanedUrl = cleanUrl(originalUrl);
            String processedUrl = cleanedUrl;

            try {
                OssEnum ossType = OssEnum.valueOf(ossSetting.getType());

                // 第一步：添加尺寸参数
                if (width != null && height != null && width > 0 && height > 0) {
                    processedUrl = addSizeParameter(processedUrl, width, height, ossType);
                }

                // 第二步：添加质量参数
                if (quality != null && quality > 0) {
                    String qualityParam = getQualityParameter(ossSetting.getType(), quality);
                    processedUrl = appendParameter(processedUrl, qualityParam, ossType);
                }

                // 第三步：添加WebP参数
                if (enableWebp) {
                    String webpParam = getWebpParameter(ossSetting.getType());
                    processedUrl = appendParameter(processedUrl, webpParam, ossType);
                }

                return processedUrl;
            } catch (IllegalArgumentException e) {
                logger.warn("不支持的OSS类型: {}", ossSetting.getType());
                return cleanedUrl;
            }
        } catch (Exception e) {
            logger.error("处理图片URL失败: {}", originalUrl, e);
            return originalUrl;
        }
    }

    /**
     * 根据精度返回图片URL列表
     */
    @Override
    public List<String> getImageUrlByPrecision(String goodsId, ImagePrecisionEnum precision, Integer quality, Boolean enableWebp) {
        List<String> urls = new ArrayList<>();
        try {
            quality = validateQuality(quality);
            enableWebp = enableWebp != null && enableWebp;

            GoodsSetting goodsSetting = getGoodsSetting();
            if (goodsSetting == null) {
                return urls;
            }

            // TODO: 从数据库查询该商品的相册信息
            // List<GoodsGallery> galleries = goodsGalleryService.getGalleryByGoodsId(goodsId);
            // 这里暂时返回空列表，实际实现时需要调用GoodsGalleryService
        } catch (Exception e) {
            logger.error("根据精度获取图片URL失败: goodsId={}, precision={}", goodsId, precision, e);
        }
        return urls;
    }

    /**
     * 构建图片对象 - 包含懒加载信息
     */
    @Override
    public GoodsImageVO buildImageObject(GoodsGallery gallery, ImagePrecisionEnum precision, int loadIndex,
                                         int totalImages, int listPageImageCount, int defaultQuality, Boolean enableWebp) {
        try {
            enableWebp = enableWebp != null && enableWebp;
            defaultQuality = validateQuality(defaultQuality);

            GoodsImageVO imageVO = new GoodsImageVO();

            // 根据精度选择图片
            String selectedImageUrl = selectImageUrlByPrecision(gallery, precision);
            imageVO.setOriginalUrl(gallery.getOriginal());

            // 获取商品设置以获取宽高
            GoodsSetting goodsSetting = getGoodsSetting();
            Integer width = null;
            Integer height = null;

            if (goodsSetting != null) {
                switch (precision) {
                    case THUMBNAIL:
                        width = goodsSetting.getAbbreviationPictureWidth();
                        height = goodsSetting.getAbbreviationPictureHeight();
                        break;
                    case SMALL:
                        width = goodsSetting.getSmallPictureWidth();
                        height = goodsSetting.getSmallPictureHeight();
                        break;
                    case ORIGINAL:
                        // 原图不设置宽高
                        break;
                }
            }

            // 处理图片URL
            String processedUrl = processImageUrl(selectedImageUrl, width, height, defaultQuality, enableWebp);
            imageVO.setImageUrl(processedUrl);
            imageVO.setWidth(width);
            imageVO.setHeight(height);
            imageVO.setQuality(defaultQuality);
            imageVO.setSupportWebp(enableWebp);

            // 计算是否需要懒加载
            boolean isLazyLoad = loadIndex >= listPageImageCount;
            imageVO.setIsLazyLoad(isLazyLoad);

            // 计算加载优先级
            int loadPriority = isLazyLoad ? (loadIndex - listPageImageCount) : 0;
            imageVO.setLoadPriority(loadPriority);

            // 生成占位符
            if (isLazyLoad) {
                String placeholder = buildPlaceholder(selectedImageUrl, width, height);
                imageVO.setPlaceholderUrl(placeholder);
            }

            return imageVO;
        } catch (Exception e) {
            logger.error("构建图片对象失败", e);
            return null;
        }
    }

    /**
     * 获取质量参数 - 根据OSS类型返回对应的质量参数字符串
     */
    @Override
    public String getQualityParameter(String ossType, Integer quality) {
        if (quality == null || quality <= 0) {
            return "";
        }

        try {
            OssEnum oss = OssEnum.valueOf(ossType);
            switch (oss) {
                case ALI_OSS:
                    // 阿里云: /quality,Q_80
                    return "/quality,Q_" + quality;
                case TENCENT_COS:
                    // 腾讯云: /quality/80
                    return "/quality/" + quality;
                case HUAWEI_OBS:
                    // 华为云: /quality=80
                    return "/quality=" + quality;
                case MINIO:
                    // MinIO不支持远程处理
                    return "";
                default:
                    return "";
            }
        } catch (IllegalArgumentException e) {
            logger.warn("不支持的OSS类型: {}", ossType);
            return "";
        }
    }

    /**
     * 获取WebP参数 - 根据OSS类型返回对应的WebP参数字符串
     */
    @Override
    public String getWebpParameter(String ossType) {
        try {
            OssEnum oss = OssEnum.valueOf(ossType);
            switch (oss) {
                case ALI_OSS:
                    // 阿里云: /format,webp
                    return "/format,webp";
                case TENCENT_COS:
                    // 腾讯云: /format/webp
                    return "/format/webp";
                case HUAWEI_OBS:
                    // 华为云: /format=webp
                    return "/format=webp";
                case MINIO:
                    // MinIO不支持远程处理
                    return "";
                default:
                    return "";
            }
        } catch (IllegalArgumentException e) {
            logger.warn("不支持的OSS类型: {}", ossType);
            return "";
        }
    }

    /**
     * 构建占位符图片 - 用于懒加载前显示
     */
    @Override
    public String buildPlaceholder(String originalUrl, Integer width, Integer height) {
        try {
            // 使用1x1像素的透明GIF作为占位符
            // 这是一个Base64编码的透明GIF图片
            String placeholderGif = "data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7";
            return placeholderGif;
        } catch (Exception e) {
            logger.error("构建占位符失败", e);
            return null;
        }
    }

    /**
     * 清理URL - 移除原有的参数
     */
    @Override
    public String cleanUrl(String url) {
        try {
            if (CharSequenceUtil.isBlank(url)) {
                return url;
            }
            // 移除URL中已有的OSS处理参数
            int queryIndex = url.indexOf('?');
            if (queryIndex > 0) {
                return url.substring(0, queryIndex);
            }
            return url;
        } catch (Exception e) {
            logger.error("清理URL失败", e);
            return url;
        }
    }

    /**
     * 根据商品相册列表构建图片对象列表
     */
    @Override
    public List<GoodsImageVO> buildImageObjectList(List<GoodsGallery> galleries, ImagePrecisionEnum precision,
                                                   int listPageImageCount, int defaultQuality, Boolean enableWebp) {
        List<GoodsImageVO> result = new ArrayList<>();
        try {
            if (galleries == null || galleries.isEmpty()) {
                return result;
            }

            for (int i = 0; i < galleries.size(); i++) {
                GoodsImageVO imageVO = buildImageObject(galleries.get(i), precision, i, galleries.size(),
                        listPageImageCount, defaultQuality, enableWebp);
                if (imageVO != null) {
                    result.add(imageVO);
                }
            }
        } catch (Exception e) {
            logger.error("构建图片对象列表失败", e);
        }
        return result;
    }

    /**
     * 根据精度选择图片URL
     */
    private String selectImageUrlByPrecision(GoodsGallery gallery, ImagePrecisionEnum precision) {
        if (gallery == null) {
            return null;
        }
        switch (precision) {
            case THUMBNAIL:
                return gallery.getThumbnail();
            case SMALL:
                return gallery.getSmall();
            case ORIGINAL:
                return gallery.getOriginal();
            default:
                return gallery.getOriginal();
        }
    }

    /**
     * 添加尺寸参数
     */
    private String addSizeParameter(String url, Integer width, Integer height, OssEnum ossType) {
        switch (ossType) {
            case ALI_OSS:
                // 阿里云OSS需要配置样式: /style/300X300
                return url + "?x-oss-process=style/" + width + "X" + height;
            case TENCENT_COS:
                // 腾讯云COS: /imageMogr2/thumbnail/300x300
                return url + "?imageMogr2/thumbnail/" + width + "x" + height;
            case HUAWEI_OBS:
                // 华为云OBS: ?image/resize,m_fixed,h_300,w_300
                return url + "?image/resize,m_fixed,h_" + height + ",w_" + width;
            case MINIO:
                // MinIO不支持远程处理，直接返回原URL
                return url;
            default:
                return url;
        }
    }

    /**
     * 追加参数到URL
     */
    private String appendParameter(String url, String parameter, OssEnum ossType) {
        if (CharSequenceUtil.isBlank(parameter)) {
            return url;
        }

        switch (ossType) {
            case ALI_OSS:
                // 阿里云：直接拼接
                return url + parameter;
            case TENCENT_COS:
                // 腾讯云：直接拼接
                return url + parameter;
            case HUAWEI_OBS:
                // 华为云：用&拼接
                return url + parameter;
            case MINIO:
                // MinIO不支持参数
                return url;
            default:
                return url;
        }
    }

    /**
     * 验证质量参数
     */
    private int validateQuality(Integer quality) {
        if (quality == null) {
            return DEFAULT_QUALITY;
        }
        if (quality < MIN_QUALITY) {
            return MIN_QUALITY;
        }
        if (quality > MAX_QUALITY) {
            return MAX_QUALITY;
        }
        return quality;
    }
}