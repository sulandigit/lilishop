package cn.lili.modules.statistics.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 订单时段分布VO
 * 用于分析订单在一天内的时间分布
 *
 * @author lili
 * @since 2024/1/11
 */
@Data
public class OrderTimeDistributionVO {

    @ApiModelProperty(value = "时段（如 09:00-10:00）")
    private String hourRange;

    @ApiModelProperty(value = "小时（0-23）")
    private Integer hour;

    @ApiModelProperty(value = "订单数")
    private Long orderNum;

    @ApiModelProperty(value = "付款订单数")
    private Long paymentOrderNum;

    @ApiModelProperty(value = "订单金额")
    private Double orderAmount;

    @ApiModelProperty(value = "付款金额")
    private Double paymentAmount;

    @ApiModelProperty(value = "该时段转化率")
    private String conversionRate;

    public Long getOrderNum() {
        if (orderNum == null) {
            return 0L;
        }
        return orderNum;
    }

    public Long getPaymentOrderNum() {
        if (paymentOrderNum == null) {
            return 0L;
        }
        return paymentOrderNum;
    }

    public Double getOrderAmount() {
        if (orderAmount == null) {
            return 0D;
        }
        return orderAmount;
    }

    public Double getPaymentAmount() {
        if (paymentAmount == null) {
            return 0D;
        }
        return paymentAmount;
    }

    public Integer getHour() {
        if (hour == null) {
            return 0;
        }
        return hour;
    }
}
