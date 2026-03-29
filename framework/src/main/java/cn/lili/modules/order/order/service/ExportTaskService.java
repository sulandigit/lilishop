package cn.lili.modules.order.order.service;

import cn.lili.modules.order.order.entity.dos.ExportTask;
import cn.lili.modules.order.order.entity.dto.OrderSearchParams;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ExportTaskService extends IService<ExportTask> {

    ExportTask createOrderExportTask(OrderSearchParams orderSearchParams, String operatorId, String operatorType, String storeId);

    ExportTask getTaskById(String taskId);

    IPage<ExportTask> getTaskPage(String operatorId, String operatorType, String storeId, int pageNum, int pageSize);

    void executeExportTask(String taskId);
}
