package cn.lili.rocketmq.tags;

/**
 * 会员操作枚举
 * 用于定义RocketMQ消息队列中会员相关操作的标签类型
 * 这些标签用于标识不同的会员业务事件,便于消息的分类和处理
 *
 * @author paulG
 * @since 2020/12/9
 **/
public enum MemberTagsEnum {
    /**
     * 会员注册
     */
    MEMBER_REGISTER("会员注册"),
    /**
     * 会员注册
     */
    MEMBER_LOGIN("会员登录"),
    /**
     * 会员签到
     */
    MEMBER_SING("会员签到"),
    /**
     * 会员提现
     */
    MEMBER_WITHDRAWAL("会员提现"),
    /**
     * 会员提现
     */
    DISTRIBUTION_WITHDRAWAL("分销提现"),
    /**
     * 会员信息更改
     */
    MEMBER_INFO_EDIT("会员信息更改"),
    /**
     * 会员积分变动
     */
    MEMBER_POINT_CHANGE("会员积分变动"),
    /**
     * 会员使用联合登录
     */
    MEMBER_CONNECT_LOGIN("会员使用联合登录");

    /**
     * 枚举描述信息
     */
    private final String description;

    /**
     * 构造函数
     *
     * @param description 枚举描述信息
     */
    MemberTagsEnum(String description) {
        this.description = description;
    }

    /**
     * 获取枚举的描述信息
     *
     * @return 描述信息
     */
    public String description() {
        return description;
    }


}
