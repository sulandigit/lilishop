package cn.lili.modules.goods.serviceimpl;

import cn.hutool.json.JSONUtil;
import cn.lili.modules.file.entity.enums.OssEnum;
import cn.lili.modules.goods.entity.dos.GoodsGallery;
import cn.lili.modules.goods.entity.enums.ImagePrecisionEnum;
import cn.lili.modules.goods.entity.vos.GoodsImageVO;
import cn.lili.modules.goods.mapper.GoodsGalleryMapper;
import cn.lili.modules.goods.service.GoodsGalleryService;
import cn.lili.modules.goods.service.ImageProcessService;
import cn.lili.modules.system.entity.dos.Setting;
import cn.lili.modules.system.entity.dto.GoodsSetting;
import cn.lili.modules.system.entity.dto.OssSetting;
import cn.lili.modules.system.entity.enums.SettingEnum;
import cn.lili.modules.system.service.SettingService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 商品相册接口实现
 *
 * @author pikachu
 * @version v1.0
 * @since v1.0
 * 2020-02-23 15:18:56
 */
@Service
public class GoodsGalleryServiceImpl extends ServiceImpl<GoodsGalleryMapper, GoodsGallery> implements GoodsGalleryService {
    /**
     * 设置
     */
    @Autowired
    private SettingService settingService;

    /**
     * 图片处理服务
     */
    @Autowired
    private ImageProcessService imageProcessService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(List<String> goodsGalleryList, String goodsId) {
        //删除原来商品相册信息
        this.baseMapper.delete(new QueryWrapper<GoodsGallery>().eq("goods_id", goodsId));
        //确定好图片选择器后进行处理
        int i = 0;
        for (String origin : goodsGalleryList) {
            //获取带所有缩略的相册
            GoodsGallery galley = this.getGoodsGallery(origin);
            galley.setGoodsId(goodsId);
            //默认第一个为默认图片
            galley.setIsDefault(i == 0 ? 1 : 0);
            i++;
            this.baseMapper.insert(galley);
        }
    }

    @Override
    public GoodsGallery getGoodsGallery(String origin) {
        GoodsGallery goodsGallery = new GoodsGallery();
        //获取商品系统配置决定是否审核
        Setting setting = settingService.get(SettingEnum.GOODS_SETTING.name());
        GoodsSetting goodsSetting = JSONUtil.toBean(setting.getSettingValue(), GoodsSetting.class);
        //缩略图
        String thumbnail = this.getUrl(origin, goodsSetting.getAbbreviationPictureWidth(), goodsSetting.getAbbreviationPictureHeight());
        //小图
        String small = this.getUrl(origin, goodsSetting.getSmallPictureWidth(), goodsSetting.getSmallPictureHeight());
        //赋值
        goodsGallery.setSmall(small);
        goodsGallery.setThumbnail(thumbnail);
        goodsGallery.setOriginal(origin);
        return goodsGallery;
    }

    @Override
    public List<GoodsGallery> goodsGalleryList(String goodsId) {
        //根据商品id查询商品相册
        return this.baseMapper.selectList(new QueryWrapper<GoodsGallery>().eq("goods_id", goodsId));
    }

    /**
     * 根据商品 id删除商品相册缩略图
     *
     * @param goodsId 商品ID
     */
    @Override
    public void removeByGoodsId(String goodsId) {
        this.baseMapper.delete(new QueryWrapper<GoodsGallery>().eq("goods_id", goodsId));
    }


    /**
     * 根据原图生成规定尺寸的图片
     *
     * @param url    连接
     * @param width  宽
     * @param height 高
     * @return
     */
    private String getUrl(String url, Integer width, Integer height) {
        Setting setting = settingService.get(SettingEnum.OSS_SETTING.name());
        OssSetting ossSetting = JSONUtil.toBean(setting.getSettingValue(), OssSetting.class);
        switch (OssEnum.valueOf(ossSetting.getType())) {
            case MINIO:
                //缩略图全路径
                return url;
            case ALI_OSS:
                //缩略图全路径
                return url + "?x-oss-process=style/" + width + "X" + height;
            case HUAWEI_OBS:
                //缩略图全路径
                return url + "?image/resize,m_fixed,h_" + height + ",w_" + width;
            case TENCENT_COS:
                //缩略图全路径
                return url + "?imageMogr2/thumbnail/" + width + "x" + height;
        }
        return url;
    }

    /**
     * 增强版 getUrl - 支持质量和WebP参数
     *
     * @param url       图片URL
     * @param width     宽度
     * @param height    高度
     * @param quality   质量 (1-100)
     * @param enableWebp 是否启用WebP
     * @return 处理后的URL
     */
    public String getUrl(String url, Integer width, Integer height, Integer quality, Boolean enableWebp) {
        return imageProcessService.processImageUrl(url, width, height, quality, enableWebp);
    }

    /**
     * 获取商品图片列表 - 返回优化后的图片对象
     *
     * @param goodsId           商品ID
     * @param precision         图片精度
     * @param includeLazyLoadInfo 是否包含懒加载信息
     * @return 图片对象列表
     */
    public List<GoodsImageVO> getGoodsImageList(String goodsId, ImagePrecisionEnum precision, Boolean includeLazyLoadInfo) {
        List<GoodsImageVO> result = new ArrayList<>();
        try {
            // 查询商品相册列表
            List<GoodsGallery> galleries = this.goodsGalleryList(goodsId);
            if (galleries.isEmpty()) {
                return result;
            }

            // 获取商品设置
            Setting setting = settingService.get(SettingEnum.GOODS_SETTING.name());
            GoodsSetting goodsSetting = JSONUtil.toBean(setting.getSettingValue(), GoodsSetting.class);

            // 获取默认配置
            int listPageImageCount = goodsSetting.getListPageImageCount() != null ? goodsSetting.getListPageImageCount() : 6;
            int defaultQuality = goodsSetting.getDefaultImageQuality() != null ? goodsSetting.getDefaultImageQuality() : 80;
            boolean enableWebp = goodsSetting.getEnableWebpConversion() != null && goodsSetting.getEnableWebpConversion();

            // 使用ImageProcessService构建图片对象列表
            result = imageProcessService.buildImageObjectList(galleries, precision, listPageImageCount, defaultQuality, enableWebp);
        } catch (Exception e) {
            // 异常处理：返回原始URL列表
            return galleries.stream()
                    .map(g -> {
                        GoodsImageVO vo = new GoodsImageVO();
                        vo.setImageUrl(g.getOriginal());
                        vo.setOriginalUrl(g.getOriginal());
                        vo.setIsLazyLoad(false);
                        vo.setLoadPriority(0);
                        return vo;
                    })
                    .collect(Collectors.toList());
        }
        return result;
    }

}