package cn.lili.modules.statistics.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 订单分析增强VO
 * 包含客单价、复购率等高级分析指标
 *
 * @author lili
 * @since 2024/1/11
 */
@Data
public class OrderAnalysisVO {

    /**
     * 客单价相关
     */
    @ApiModelProperty(value = "平均订单金额（订单总额/订单数）")
    private Double avgOrderAmount;

    @ApiModelProperty(value = "平均付款金额（付款总额/付款订单数）")
    private Double avgPaymentAmount;

    @ApiModelProperty(value = "平均客户价值（付款总额/付款人数）")
    private Double avgCustomerValue;

    /**
     * 复购率相关
     */
    @ApiModelProperty(value = "复购率（复购人数/总购买人数）")
    private String repeatPurchaseRate;

    @ApiModelProperty(value = "复购客户数")
    private Long repeatCustomerNum;

    @ApiModelProperty(value = "新客户数")
    private Long newCustomerNum;

    @ApiModelProperty(value = "复购订单数")
    private Long repeatOrderNum;

    /**
     * 商品相关
     */
    @ApiModelProperty(value = "平均每单商品件数")
    private Double avgGoodsNumPerOrder;

    @ApiModelProperty(value = "平均每单SKU数")
    private Double avgSkuNumPerOrder;

    /**
     * 基础统计数据（继承自OrderOverviewVO的核心指标）
     */
    @ApiModelProperty(value = "订单总数")
    private Long orderNum;

    @ApiModelProperty(value = "付款订单数")
    private Long paymentOrderNum;

    @ApiModelProperty(value = "订单总金额")
    private Double orderAmount;

    @ApiModelProperty(value = "付款总金额")
    private Double paymentAmount;

    @ApiModelProperty(value = "付款人数")
    private Long paymentsNum;

    public Double getAvgOrderAmount() {
        if (avgOrderAmount == null) {
            return 0D;
        }
        return avgOrderAmount;
    }

    public Double getAvgPaymentAmount() {
        if (avgPaymentAmount == null) {
            return 0D;
        }
        return avgPaymentAmount;
    }

    public Double getAvgCustomerValue() {
        if (avgCustomerValue == null) {
            return 0D;
        }
        return avgCustomerValue;
    }

    public Long getRepeatCustomerNum() {
        if (repeatCustomerNum == null) {
            return 0L;
        }
        return repeatCustomerNum;
    }

    public Long getNewCustomerNum() {
        if (newCustomerNum == null) {
            return 0L;
        }
        return newCustomerNum;
    }

    public Long getRepeatOrderNum() {
        if (repeatOrderNum == null) {
            return 0L;
        }
        return repeatOrderNum;
    }

    public Double getAvgGoodsNumPerOrder() {
        if (avgGoodsNumPerOrder == null) {
            return 0D;
        }
        return avgGoodsNumPerOrder;
    }

    public Double getAvgSkuNumPerOrder() {
        if (avgSkuNumPerOrder == null) {
            return 0D;
        }
        return avgSkuNumPerOrder;
    }

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

    public Long getPaymentsNum() {
        if (paymentsNum == null) {
            return 0L;
        }
        return paymentsNum;
    }
}
