package cn.lili.modules.{module}.entity.enums;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * {枚举用途说明} - 示例:促销活动类型枚举
 *
 * 使用说明:
 * 1. 将 {module} 替换为实际的模块名,如 promotion, goods 等
 * 2. 将 {EnumName} 替换为实际的枚举类名,如 PromotionType, ActivityType 等
 * 3. 根据实际业务需求调整字段数量和类型
 * 4. 填充实际的枚举常量,每个常量都需要添加 JavaDoc 注释
 * 5. 根据业务需要定义静态常量(如枚举分组)
 * 6. 根据业务需要添加或修改工具方法
 * 7. 修改 @author 和 @since 信息
 * 8. 删除本使用说明块
 *
 * @author {作者名}
 * @since {创建日期,如 2024/12/15}
 */
public enum {EnumName}Enum {
    
    /**
     * {常量1的含义说明}
     * 示例:秒杀活动
     */
    CONSTANT_ONE("描述1", true, true),
    
    /**
     * {常量2的含义说明}
     * 示例:拼团活动
     */
    CONSTANT_TWO("描述2", true, false),
    
    /**
     * {常量3的含义说明}
     * 示例:优惠券
     */
    CONSTANT_THREE("描述3", false, false),
    
    /**
     * {常量4的含义说明}
     * 示例:满减活动
     */
    CONSTANT_FOUR("描述4", false, false);

    /**
     * 特定分组1 - 根据业务需要定义
     * 示例:有库存的促销类型
     */
    public static final {EnumName}Enum[] GROUP_ONE = 
        new {EnumName}Enum[]{CONSTANT_ONE, CONSTANT_TWO};

    /**
     * 特定分组2 - 根据业务需要定义
     * 示例:有独立库存的促销类型
     */
    public static final {EnumName}Enum[] GROUP_TWO = 
        new {EnumName}Enum[]{CONSTANT_ONE};

    private final String description;
    private final Boolean propertyOne;
    private final Boolean propertyTwo;

    {EnumName}Enum(String description, Boolean propertyOne, Boolean propertyTwo) {
        this.description = description;
        this.propertyOne = propertyOne;
        this.propertyTwo = propertyTwo;
    }

    public String description() {
        return description;
    }

    public Boolean propertyOne() {
        return propertyOne;
    }

    public Boolean propertyTwo() {
        return propertyTwo;
    }

    /**
     * 判断是否属于特定分组1
     * 示例:判断促销类型是否有库存
     *
     * @param name 枚举名称
     * @return true-属于 false-不属于
     */
    public static boolean isInGroupOne(String name) {
        for ({EnumName}Enum item : GROUP_ONE) {
            if (item.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 验证枚举名称是否有效
     *
     * @param name 枚举名称
     * @return true-有效 false-无效
     */
    public static boolean isValid(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return Arrays.stream(values()).anyMatch(item -> item.name().equals(name));
    }

    /**
     * 业务判断方法示例
     * 示例:判断促销类型是否支持售后
     *
     * @param name 枚举名称
     * @return true-支持 false-不支持
     */
    public static boolean supportFeature(String name) {
        if (!isValid(name)) {
            return true;
        }
        // 定义不支持该特性的枚举集合
        EnumSet<{EnumName}Enum> unsupportedSet = 
            EnumSet.of({EnumName}Enum.CONSTANT_ONE);
        return !unsupportedSet.contains({EnumName}Enum.valueOf(name));
    }

    /**
     * 根据描述查找枚举
     *
     * @param description 描述信息
     * @return 枚举对象,未找到返回null
     */
    public static {EnumName}Enum findByDescription(String description) {
        for ({EnumName}Enum item : values()) {
            if (item.description().equals(description)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 获取特定属性为true的所有枚举
     * 示例:获取所有有库存的促销类型
     *
     * @return 枚举数组
     */
    public static {EnumName}Enum[] getItemsWithPropertyOne() {
        return Arrays.stream(values())
                .filter({EnumName}Enum::propertyOne)
                .toArray({EnumName}Enum[]::new);
    }
}
