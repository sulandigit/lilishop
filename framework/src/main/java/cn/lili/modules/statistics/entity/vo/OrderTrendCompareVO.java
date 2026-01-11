package cn.lili.modules.statistics.entity.vo;

import cn.lili.modules.statistics.entity.enums.CompareTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 订单趋势对比VO
 * 用于同比/环比分析
 *
 * @author lili
 * @since 2024/1/11
 */
@Data
public class OrderTrendCompareVO {

    /**
     * 当前期数据
     */
    @ApiModelProperty(value = "当前期订单数")
    private Long currentPeriodOrderNum;

    @ApiModelProperty(value = "当前期付款订单数")
    private Long currentPeriodPaymentOrderNum;

    @ApiModelProperty(value = "当前期下单金额")
    private Double currentPeriodOrderAmount;

    @ApiModelProperty(value = "当前期付款金额")
    private Double currentPeriodPaymentAmount;

    /**
     * 对比期数据
     */
    @ApiModelProperty(value = "对比期订单数")
    private Long comparePeriodOrderNum;

    @ApiModelProperty(value = "对比期付款订单数")
    private Long comparePeriodPaymentOrderNum;

    @ApiModelProperty(value = "对比期下单金额")
    private Double comparePeriodOrderAmount;

    @ApiModelProperty(value = "对比期付款金额")
    private Double comparePeriodPaymentAmount;

    /**
     * 增长指标
     */
    @ApiModelProperty(value = "订单数增长率")
    private String orderNumGrowthRate;

    @ApiModelProperty(value = "付款订单数增长率")
    private String paymentOrderNumGrowthRate;

    @ApiModelProperty(value = "下单金额增长率")
    private String orderAmountGrowthRate;

    @ApiModelProperty(value = "付款金额增长率")
    private String paymentAmountGrowthRate;

    /**
     * 元数据
     */
    @ApiModelProperty(value = "对比类型")
    private CompareTypeEnum compareType;

    @ApiModelProperty(value = "当前期开始时间")
    private Date currentPeriodStart;

    @ApiModelProperty(value = "当前期结束时间")
    private Date currentPeriodEnd;

    @ApiModelProperty(value = "对比期开始时间")
    private Date comparePeriodStart;

    @ApiModelProperty(value = "对比期结束时间")
    private Date comparePeriodEnd;

    public Long getCurrentPeriodOrderNum() {
        if (currentPeriodOrderNum == null) {
            return 0L;
        }
        return currentPeriodOrderNum;
    }

    public Long getCurrentPeriodPaymentOrderNum() {
        if (currentPeriodPaymentOrderNum == null) {
            return 0L;
        }
        return currentPeriodPaymentOrderNum;
    }

    public Double getCurrentPeriodOrderAmount() {
        if (currentPeriodOrderAmount == null) {
            return 0D;
        }
        return currentPeriodOrderAmount;
    }

    public Double getCurrentPeriodPaymentAmount() {
        if (currentPeriodPaymentAmount == null) {
            return 0D;
        }
        return currentPeriodPaymentAmount;
    }

    public Long getComparePeriodOrderNum() {
        if (comparePeriodOrderNum == null) {
            return 0L;
        }
        return comparePeriodOrderNum;
    }

    public Long getComparePeriodPaymentOrderNum() {
        if (comparePeriodPaymentOrderNum == null) {
            return 0L;
        }
        return comparePeriodPaymentOrderNum;
    }

    public Double getComparePeriodOrderAmount() {
        if (comparePeriodOrderAmount == null) {
            return 0D;
        }
        return comparePeriodOrderAmount;
    }

    public Double getComparePeriodPaymentAmount() {
        if (comparePeriodPaymentAmount == null) {
            return 0D;
        }
        return comparePeriodPaymentAmount;
    }
}
