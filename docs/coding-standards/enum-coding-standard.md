# Java 枚举类统一定义规范

## 一、规范概述

### 1.1 目标
为lilishop项目中的新枚举类提供统一、清晰的编码标准,解决当前存在的方法命名不一致、字段命名不统一等问题。

### 1.2 适用范围
- **强制适用**: 所有新创建的枚举类必须严格遵循本规范
- **建议适用**: 现有枚举在修改时建议逐步迁移到新规范(保持向后兼容)

### 1.3 设计原则
- **一致性优先**: 统一命名和结构模式
- **简单性**: 不引入基础接口或抽象类,保持枚举独立
- **实用性**: 移除不必要的 `value()` 方法
- **可读性**: 使用 `description()` 而非 `getDescription()`

## 二、核心规范

### 2.1 命名规范

#### 枚举类命名
- **格式**: `{业务概念}Enum`
- **示例**: `OrderStatusEnum`, `PaymentMethodEnum`, `UserRoleEnum`
- **位置**: 放在对应模块的 `entity/enums/` 包下

#### 枚举常量命名
- **格式**: 全大写,单词间下划线分隔
- **示例**: `UNPAID`, `BANK_TRANSFER`, `FULL_DISCOUNT`

#### 字段命名
- **描述字段**: 统一使用 `description` (禁止使用 `des`, `desc` 等缩写)
- **其他字段**: 使用小驼峰,语义明确 (如 `plugin`, `paymentName`)

#### 方法命名
- **访问方法**: 字段名 + `()`,**不使用 `get` 前缀**
  - ✅ `description()`
  - ❌ `getDescription()`
  - ✅ `plugin()`
  - ❌ `getPlugin()`
  
- **工具方法**: 遵循标准命名
  - 判断: `isXxx()`
  - 验证: `isValid()`
  - 查找: `findByXxx()`, `xxxOf()`

### 2.2 结构规范

#### 文件结构顺序
```
1. 包声明
2. 导入语句(如果有)
3. 类级别 JavaDoc
4. 枚举类声明
5. 枚举常量声明(每个常量带 JavaDoc)
6. 空行
7. 静态常量字段(如果有,如枚举数组等)
8. 空行
9. 实例字段声明
10. 空行
11. 构造器
12. 空行
13. 访问方法
14. 空行
15. 工具方法(如果有)
```

#### 字段规范
- 所有字段声明为 `private final`
- 字段顺序: 业务主键字段 → 描述字段 → 其他字段

#### 构造器规范
- 访问修饰符: 默认(package-private)
- 参数顺序: 与字段声明顺序一致
- 参数命名: 与字段名一致

### 2.3 JavaDoc 规范

#### 类级别 JavaDoc
```java
/**
 * {枚举用途的一句话描述}
 *
 * @author {作者名}
 * @since {创建日期}
 */
```

#### 枚举常量 JavaDoc
```java
/**
 * {常量含义说明}
 */
CONSTANT_NAME("value");
```

#### 方法 JavaDoc
- 访问方法(`description()` 等)不需要 JavaDoc
- 工具方法需要完整的 JavaDoc (包含 `@param`, `@return`)

## 三、标准模板

本规范提供三种标准模板,位于 `docs/coding-standards/templates/` 目录:

1. **EnumTemplateSimple.java** - 简单枚举模板(单字段)
2. **EnumTemplateComplex.java** - 复杂枚举模板(多字段)
3. **EnumTemplateWithUtils.java** - 带工具方法的枚举模板

### 3.1 简单枚举示例

```java
package cn.lili.modules.goods.entity.enums;

/**
 * 商品审核状态枚举
 *
 * @author developer
 * @since 2024/12/15
 */
public enum GoodsAuditStatusEnum {
    
    /**
     * 待审核
     */
    PENDING("待审核"),
    
    /**
     * 审核通过
     */
    APPROVED("审核通过"),
    
    /**
     * 审核拒绝
     */
    REJECTED("审核拒绝");

    private final String description;

    GoodsAuditStatusEnum(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
```

### 3.2 复杂枚举示例

```java
package cn.lili.modules.logistics.entity.enums;

/**
 * 物流服务商枚举
 *
 * @author developer
 * @since 2024/12/15
 */
public enum LogisticsProviderEnum {
    
    /**
     * 顺丰速运
     */
    SF_EXPRESS("SF", "顺丰速运", "https://api.sf-express.com"),
    
    /**
     * 圆通速递
     */
    YTO("YTO", "圆通速递", "https://api.yto.net.cn"),
    
    /**
     * 中通快递
     */
    ZTO("ZTO", "中通快递", "https://api.zto.com");

    private final String code;
    private final String description;
    private final String apiUrl;

    LogisticsProviderEnum(String code, String description, String apiUrl) {
        this.code = code;
        this.description = description;
        this.apiUrl = apiUrl;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }

    public String apiUrl() {
        return apiUrl;
    }

    /**
     * 根据物流编码查找枚举
     *
     * @param code 物流编码
     * @return 物流服务商枚举,未找到返回null
     */
    public static LogisticsProviderEnum findByCode(String code) {
        for (LogisticsProviderEnum provider : values()) {
            if (provider.code().equals(code)) {
                return provider;
            }
        }
        return null;
    }

    /**
     * 验证物流编码是否有效
     *
     * @param code 物流编码
     * @return true-有效 false-无效
     */
    public static boolean isValid(String code) {
        return findByCode(code) != null;
    }
}
```

## 四、常见问题和反模式

### 4.1 反模式示例

❌ **错误1: 使用 `get` 前缀**
```java
public String getDescription() {
    return description;
}
```
✅ **正确做法**:
```java
public String description() {
    return description;
}
```

❌ **错误2: 字段名使用缩写**
```java
private final String des;

StoreStatusEnum(String des) {
    this.description = des;
}
```
✅ **正确做法**:
```java
private final String description;

StoreStatusEnum(String description) {
    this.description = description;
}
```

❌ **错误3: 添加不必要的 `value()` 方法**
```java
public String value() {
    return this.name();
}
```
✅ **正确做法**: 直接使用枚举的 `name()` 方法,不需要封装

❌ **错误4: 枚举常量缺少 JavaDoc**
```java
OPEN("开启中"),
CLOSED("店铺关闭");
```
✅ **正确做法**:
```java
/**
 * 开启中
 */
OPEN("开启中"),

/**
 * 店铺关闭
 */
CLOSED("店铺关闭");
```

### 4.2 常见问题

**Q1: 为什么不使用 `getDescription()` 而使用 `description()`?**

A: 
- 枚举不是 JavaBean,不需要遵循 `getXxx()` 规范
- `description()` 更简洁,符合现代 Java 编码风格
- 项目中已有多个枚举使用 `description()` 模式

**Q2: 为什么移除 `value()` 方法?**

A: 
- `value()` 方法仅是返回 `name()`,功能重复
- 直接使用 `name()` 方法更直观
- 减少不必要的方法定义

**Q3: 多字段枚举的字段顺序如何确定?**

A: 建议顺序:
1. 业务主键字段(如 `code`, `plugin`)
2. 描述字段(`description`)
3. 其他业务字段(如 `enabled`, `apiUrl`)

**Q4: 什么时候需要添加工具方法?**

A: 以下场景建议添加工具方法:
- 根据业务字段查找枚举(如 `findByCode()`)
- 验证字符串是否为有效枚举值(`isValid()`)
- 枚举分组判断(如 `hasStock()`, `supportAfterSale()`)
- 枚举类型判断(如 `isTerminal()`)

## 五、现有枚举迁移指南

### 5.1 迁移原则

- **不破坏现有 API**: 所有迁移必须保持向后兼容
- **渐进式改进**: 通过 `@Deprecated` 标记过渡
- **优先新代码**: 新枚举严格执行新规范
- **按需迁移**: 不强制修改稳定的旧枚举

### 5.2 迁移场景

#### 场景1: 枚举同时有 `getDescription()` 和 `description()`

**现状**: 如 `OrderStatusEnum.java`
```java
public String getDescription() {
    return description;
}

public String description() {
    return this.description;
}
```

**迁移方案**: 弃用 `getDescription()`
```java
/**
 * 获取状态描述
 * 
 * @deprecated 使用 {@link #description()} 替代
 * @return 状态描述
 */
@Deprecated
public String getDescription() {
    return description();
}

public String description() {
    return description;
}
```

#### 场景2: 枚举有 `value()` 方法

**现状**: 如 `StoreStatusEnum.java`
```java
public String value() {
    return this.name();
}
```

**迁移方案**: 标记弃用
```java
/**
 * 获取枚举名称
 * 
 * @deprecated 直接使用 {@link #name()} 替代
 * @return 枚举名称
 */
@Deprecated
public String value() {
    return this.name();
}
```

#### 场景3: 构造器参数名不规范

**现状**: 参数名为 `des`
```java
StoreStatusEnum(String des) {
    this.description = des;
}
```

**迁移方案**: 修改参数名为 `description`
```java
StoreStatusEnum(String description) {
    this.description = description;
}
```

### 5.3 迁移检查清单

在迁移现有枚举时,按以下顺序检查:

- [ ] 字段名为 `description` (非 `des`, `desc`)
- [ ] 构造器参数名与字段名一致
- [ ] 存在 `description()` 方法
- [ ] 如有 `getDescription()`,已标记 `@Deprecated`
- [ ] 如有 `value()`,已标记 `@Deprecated`
- [ ] 访问方法不使用 `get` 前缀
- [ ] 类级别 JavaDoc 完整
- [ ] 每个枚举常量有 JavaDoc
- [ ] 工具方法有完整的参数和返回值说明
- [ ] 弃用方法的 JavaDoc 指向新方法

## 六、实施要点

### 6.1 新枚举创建流程

1. 确定枚举类型(简单/复杂/带工具方法)
2. 从 `docs/coding-standards/templates/` 复制对应模板
3. 填充业务内容
4. 编写完整 JavaDoc
5. Code Review 检查是否符合规范

### 6.2 Code Review 检查要点

在代码审查中,针对枚举类检查:
- 命名是否符合规范
- 是否包含必要的 JavaDoc
- 方法命名是否一致(`description()` 而非 `getDescription()`)
- 是否避免了不必要的 `value()` 方法
- 多字段枚举的字段顺序是否合理
- 工具方法是否有完整的文档

## 七、关键决策总结

| 决策项 | 选择 | 理由 |
|--------|------|------|
| 方法命名 | `description()` | 简洁,符合现代 Java 风格 |
| 字段命名 | `description` | 统一标准,避免缩写混乱 |
| `value()` 方法 | 不使用 | 与 `name()` 重复,无实际价值 |
| 基础接口 | 不创建 | 避免过度设计,保持简单 |
| 迁移策略 | `@Deprecated` 过渡 | 平滑迁移,不破坏现有代码 |

## 八、参考文件

### 8.1 优秀示例(可作为参考)

- **简单枚举**: `cn.lili.modules.store.entity.enums.StoreStatusEnum`
  - 文件路径: `framework/src/main/java/cn/lili/modules/store/entity/enums/StoreStatusEnum.java`
  - 优点: 结构清晰,有完整 JavaDoc
  - 需改进: 移除 `value()` 方法,统一构造器参数名

- **带工具方法枚举**: `cn.lili.common.enums.PromotionTypeEnum`
  - 文件路径: `framework/src/main/java/cn/lili/common/enums/PromotionTypeEnum.java`
  - 优点: 工具方法丰富,有静态常量分组
  - 已符合新规范

- **多字段枚举**: `cn.lili.modules.payment.entity.enums.PaymentMethodEnum`
  - 文件路径: `framework/src/main/java/cn/lili/modules/payment/entity/enums/PaymentMethodEnum.java`
  - 需改进: `getPlugin()` 改为 `plugin()`

---

**文档版本**: 1.0  
**最后更新**: 2024/12/15  
**维护者**: 开发团队
