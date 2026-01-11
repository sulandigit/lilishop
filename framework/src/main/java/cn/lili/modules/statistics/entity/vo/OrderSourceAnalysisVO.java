package cn.lili.modules.statistics.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 订单来源分析VO
 * 按客户端类型统计订单来源
 *
 * @author lili
 * @since 2024/1/11
 */
@Data
public class OrderSourceAnalysisVO {

    @ApiModelProperty(value = "客户端类型（PC、H5、WECHAT_MP、APP等）")
    private String clientType;

    @ApiModelProperty(value = "客户端类型名称")
    private String clientTypeName;

    @ApiModelProperty(value = "订单数")
    private Long orderNum;

    @ApiModelProperty(value = "付款订单数")
    private Long paymentOrderNum;

    @ApiModelProperty(value = "订单金额")
    private Double orderAmount;

    @ApiModelProperty(value = "付款金额")
    private Double paymentAmount;

    @ApiModelProperty(value = "占比（订单数占比）")
    private String percentage;

    @ApiModelProperty(value = "金额占比")
    private String amountPercentage;

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
}
