package cn.lili.modules.statistics.entity.enums;

/**
 * 对比类型枚举
 *
 * @author lili
 * @since 2024/1/11
 */
public enum CompareTypeEnum {

    /**
     * 同比（去年同期）
     */
    YEAR_ON_YEAR("同比"),

    /**
     * 环比（上一周期）
     */
    MONTH_ON_MONTH("环比");

    private final String description;

    CompareTypeEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
