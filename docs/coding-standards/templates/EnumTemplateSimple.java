package cn.lili.modules.{module}.entity.enums;

/**
 * {枚举用途说明} - 示例:商品状态枚举
 *
 * 使用说明:
 * 1. 将 {module} 替换为实际的模块名,如 goods, order, store 等
 * 2. 将 {EnumName} 替换为实际的枚举类名,如 GoodsStatus, OrderStatus 等
 * 3. 填充实际的枚举常量,每个常量都需要添加 JavaDoc 注释
 * 4. 修改 @author 和 @since 信息
 * 5. 删除本使用说明块
 *
 * @author {作者名}
 * @since {创建日期,如 2024/12/15}
 */
public enum {EnumName}Enum {
    
    /**
     * {常量1的含义说明}
     * 示例:待审核
     */
    CONSTANT_ONE("描述1"),
    
    /**
     * {常量2的含义说明}
     * 示例:审核通过
     */
    CONSTANT_TWO("描述2"),
    
    /**
     * {常量3的含义说明}
     * 示例:审核拒绝
     */
    CONSTANT_THREE("描述3");

    private final String description;

    {EnumName}Enum(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
