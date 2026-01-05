package cn.lili.modules.goods.entity.enums;

/**
 * 图片精度枚举
 * 定义商品图片的三种精度等级
 */
public enum ImagePrecisionEnum {

    /**
     * 缩略图 - 最小尺寸，最快加载，用于列表页
     * 特点: 最小文件、最快加载、带宽节省 80-90%
     */
    THUMBNAIL("缩略图"),

    /**
     * 小图 - 中等尺寸，平衡质量
     * 特点: 中等文件、平衡质量、带宽节省 50-60%
     */
    SMALL("小图"),

    /**
     * 原图 - 完整尺寸，全质量
     * 特点: 完整质量、原始尺寸、无压缩
     */
    ORIGINAL("原图");

    private final String description;

    ImagePrecisionEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
