package cn.lili.modules.order.order.entity.dos;

import cn.lili.mybatis.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("li_export_task")
@ApiModel(value = "导出任务")
@NoArgsConstructor
public class ExportTask extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "导出类型")
    private String exportType;

    @ApiModelProperty(value = "任务状态: PENDING-待处理, PROCESSING-处理中, SUCCESS-成功, FAILED-失败")
    private String status;

    @ApiModelProperty(value = "查询参数JSON")
    private String queryParams;

    @ApiModelProperty(value = "文件下载URL")
    private String downloadUrl;

    @ApiModelProperty(value = "导出记录数")
    private Long totalCount;

    @ApiModelProperty(value = "失败原因")
    private String failReason;

    @ApiModelProperty(value = "操作人ID")
    private String operatorId;

    @ApiModelProperty(value = "操作人类型: MANAGER-管理员, STORE-商家")
    private String operatorType;

    @ApiModelProperty(value = "店铺ID(商家操作时)")
    private String storeId;
}
