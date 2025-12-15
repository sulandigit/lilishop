package cn.lili.modules.{module}.entity.enums;

/**
 * {枚举用途说明} - 示例:物流服务商枚举
 *
 * 使用说明:
 * 1. 将 {module} 替换为实际的模块名,如 logistics, payment 等
 * 2. 将 {EnumName} 替换为实际的枚举类名,如 LogisticsProvider, PaymentMethod 等
 * 3. 根据实际业务需求调整字段数量和类型
 * 4. 填充实际的枚举常量,每个常量都需要添加 JavaDoc 注释
 * 5. 修改 @author 和 @since 信息
 * 6. 根据需要添加或删除工具方法(如 findByCode, isValid 等)
 * 7. 删除本使用说明块
 *
 * @author {作者名}
 * @since {创建日期,如 2024/12/15}
 */
public enum {EnumName}Enum {
    
    /**
     * {常量1的含义说明}
     * 示例:顺丰速运
     */
    CONSTANT_ONE("code1", "描述1", "额外属性1"),
    
    /**
     * {常量2的含义说明}
     * 示例:圆通速递
     */
    CONSTANT_TWO("code2", "描述2", "额外属性2"),
    
    /**
     * {常量3的含义说明}
     * 示例:中通快递
     */
    CONSTANT_THREE("code3", "描述3", "额外属性3");

    private final String code;
    private final String description;
    private final String extraProperty;

    {EnumName}Enum(String code, String description, String extraProperty) {
        this.code = code;
        this.description = description;
        this.extraProperty = extraProperty;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }

    public String extraProperty() {
        return extraProperty;
    }

    /**
     * 根据业务编码查找枚举
     *
     * @param code 业务编码
     * @return 枚举对象,未找到返回null
     */
    public static {EnumName}Enum findByCode(String code) {
        for ({EnumName}Enum item : values()) {
            if (item.code().equals(code)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 验证业务编码是否有效
     *
     * @param code 业务编码
     * @return true-有效 false-无效
     */
    public static boolean isValid(String code) {
        return findByCode(code) != null;
    }
}
