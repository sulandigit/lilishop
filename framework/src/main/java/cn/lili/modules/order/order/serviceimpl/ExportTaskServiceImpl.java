package cn.lili.modules.order.order.serviceimpl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import cn.lili.common.utils.CurrencyUtil;
import cn.lili.modules.file.plugin.FilePlugin;
import cn.lili.modules.file.plugin.FilePluginFactory;
import cn.lili.modules.order.order.entity.dos.ExportTask;
import cn.lili.modules.order.order.entity.dto.OrderExportDTO;
import cn.lili.modules.order.order.entity.dto.OrderExportDetailDTO;
import cn.lili.modules.order.order.entity.dto.OrderSearchParams;
import cn.lili.modules.order.order.entity.dto.PriceDetailDTO;
import cn.lili.modules.order.order.entity.enums.ExportTaskStatusEnum;
import cn.lili.modules.order.order.entity.enums.ExportTypeEnum;
import cn.lili.modules.order.order.entity.enums.OrderItemAfterSaleStatusEnum;
import cn.lili.modules.order.order.entity.enums.OrderStatusEnum;
import cn.lili.modules.order.order.entity.enums.OrderTypeEnum;
import cn.lili.modules.order.order.mapper.ExportTaskMapper;
import cn.lili.modules.order.order.mapper.OrderMapper;
import cn.lili.modules.order.order.service.ExportTaskService;
import cn.lili.common.enums.ClientTypeEnum;
import cn.lili.modules.payment.entity.enums.PaymentMethodEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ExportTaskServiceImpl extends ServiceImpl<ExportTaskMapper, ExportTask> implements ExportTaskService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private FilePluginFactory filePluginFactory;

    @Override
    public ExportTask createOrderExportTask(OrderSearchParams orderSearchParams, String operatorId, String operatorType, String storeId) {
        ExportTask task = new ExportTask();
        task.setTaskName("订单导出_" + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss"));
        task.setExportType(ExportTypeEnum.ORDER.name());
        task.setStatus(ExportTaskStatusEnum.PENDING.name());
        task.setQueryParams(JSONUtil.toJsonStr(orderSearchParams));
        task.setOperatorId(operatorId);
        task.setOperatorType(operatorType);
        task.setStoreId(storeId);
        this.save(task);

        this.executeExportTask(task.getId());

        return task;
    }

    @Override
    public ExportTask getTaskById(String taskId) {
        return this.getById(taskId);
    }

    @Override
    public IPage<ExportTask> getTaskPage(String operatorId, String operatorType, String storeId, int pageNum, int pageSize) {
        LambdaQueryWrapper<ExportTask> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExportTask::getOperatorId, operatorId);
        queryWrapper.eq(ExportTask::getOperatorType, operatorType);
        if (CharSequenceUtil.isNotEmpty(storeId)) {
            queryWrapper.eq(ExportTask::getStoreId, storeId);
        }
        queryWrapper.orderByDesc(ExportTask::getCreateTime);
        return this.page(new Page<>(pageNum, pageSize), queryWrapper);
    }

    @Override
    @Async
    public void executeExportTask(String taskId) {
        ExportTask task = this.getById(taskId);
        if (task == null) {
            return;
        }

        task.setStatus(ExportTaskStatusEnum.PROCESSING.name());
        this.updateById(task);

        try {
            OrderSearchParams searchParams = JSONUtil.toBean(task.getQueryParams(), OrderSearchParams.class);
            List<OrderExportDTO> orderExportDTOList = orderMapper.queryExportOrder(searchParams.queryWrapper());

            task.setTotalCount((long) orderExportDTOList.size());

            XSSFWorkbook workbook = initOrderExportData(orderExportDTOList);

            String fileName = "export/order/" + DateUtil.format(DateUtil.date(), "yyyyMMdd") + "/" + IdUtil.fastSimpleUUID() + ".xlsx";

            File tempFile = File.createTempFile("order_export_", ".xlsx");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                workbook.write(fos);
            }
            workbook.close();

            FilePlugin filePlugin = filePluginFactory.filePlugin();
            String downloadUrl = filePlugin.pathUpload(tempFile.getAbsolutePath(), fileName);

            tempFile.delete();

            task.setDownloadUrl(downloadUrl);
            task.setStatus(ExportTaskStatusEnum.SUCCESS.name());
            this.updateById(task);

        } catch (Exception e) {
            log.error("导出任务执行失败, taskId: {}", taskId, e);
            task.setStatus(ExportTaskStatusEnum.FAILED.name());
            task.setFailReason(e.getMessage());
            this.updateById(task);
        }
    }

    private XSSFWorkbook initOrderExportData(List<OrderExportDTO> orderExportDTOList) {
        List<OrderExportDetailDTO> orderExportDetailDTOList = new ArrayList<>();
        for (OrderExportDTO orderExportDTO : orderExportDTOList) {
            OrderExportDetailDTO orderExportDetailDTO = new OrderExportDetailDTO();
            BeanUtil.copyProperties(orderExportDTO, orderExportDetailDTO);
            PriceDetailDTO priceDetailDTO = JSONUtil.toBean(orderExportDTO.getPriceDetail(), PriceDetailDTO.class);
            orderExportDetailDTO.setFreightPrice(priceDetailDTO.getFreightPrice());
            orderExportDetailDTO.setDiscountPrice(CurrencyUtil.add(priceDetailDTO.getDiscountPrice(), priceDetailDTO.getCouponPrice()));
            orderExportDetailDTO.setUpdatePrice(priceDetailDTO.getUpdatePrice());
            orderExportDetailDTO.setStoreMarketingCost(priceDetailDTO.getSiteCouponCommission());
            orderExportDetailDTO.setSiteMarketingCost(CurrencyUtil.sub(orderExportDetailDTO.getDiscountPrice(), orderExportDetailDTO.getStoreMarketingCost()));
            if (CharSequenceUtil.isNotBlank(orderExportDTO.getConsigneeAddressPath())) {
                String[] receiveAddress = orderExportDTO.getConsigneeAddressPath().split(",");
                orderExportDetailDTO.setProvince(receiveAddress[0]);
                orderExportDetailDTO.setCity(receiveAddress.length > 1 ? receiveAddress[1] : "");
                orderExportDetailDTO.setDistrict(receiveAddress.length > 2 ? receiveAddress[2] : "");
                orderExportDetailDTO.setStreet(receiveAddress.length > 3 ? receiveAddress[3] : "");
            }
            orderExportDetailDTO.setOrderStatus(OrderStatusEnum.valueOf(orderExportDTO.getOrderStatus()).description());
            orderExportDetailDTO.setPaymentMethod(CharSequenceUtil.isNotBlank(orderExportDTO.getPaymentMethod()) ? PaymentMethodEnum.valueOf(orderExportDTO.getPaymentMethod()).paymentName() : "");
            orderExportDetailDTO.setClientType(ClientTypeEnum.valueOf(orderExportDTO.getClientType()).value());
            orderExportDetailDTO.setOrderType(orderExportDTO.getOrderType().equals(OrderTypeEnum.NORMAL.name()) ? "普通订单" : "虚拟订单");
            orderExportDetailDTO.setAfterSaleStatus(OrderItemAfterSaleStatusEnum.valueOf(orderExportDTO.getAfterSaleStatus()).description());
            orderExportDetailDTO.setCreateTime(DateUtil.formatDateTime(orderExportDTO.getCreateTime()));
            orderExportDetailDTO.setPaymentTime(DateUtil.formatDateTime(orderExportDTO.getPaymentTime()));
            orderExportDetailDTO.setLogisticsTime(DateUtil.formatDateTime(orderExportDTO.getLogisticsTime()));
            orderExportDetailDTO.setCompleteTime(DateUtil.formatDateTime(orderExportDTO.getCompleteTime()));
            orderExportDetailDTOList.add(orderExportDetailDTO);
        }

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("订单列表");

        String[] headers = {"主订单编号", "子订单编号", "商品名称", "商品数量", "商品ID", "商品单价", "应付金额",
                "运费", "优惠金额", "改价金额", "平台营销成本", "店铺营销成本",
                "支付方式", "收件人", "收件人手机", "省", "市", "区", "街道", "详细地址", "买家留言",
                "下单时间", "支付时间", "来源", "订单状态", "订单类型", "售后状态", "发货时间", "完成时间", "店铺"};

        XSSFRow headerRow = sheet.createRow(0);
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < headers.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (OrderExportDetailDTO dto : orderExportDetailDTOList) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(dto.getOrderSn());
            row.createCell(1).setCellValue(dto.getOrderItemSn());
            row.createCell(2).setCellValue(dto.getGoodsName());
            row.createCell(3).setCellValue(dto.getNum() != null ? dto.getNum() : 0);
            row.createCell(4).setCellValue(dto.getGoodsId());
            row.createCell(5).setCellValue(dto.getUnitPrice() != null ? dto.getUnitPrice() : 0);
            row.createCell(6).setCellValue(dto.getFlowPrice() != null ? dto.getFlowPrice() : 0);
            row.createCell(7).setCellValue(dto.getFreightPrice() != null ? dto.getFreightPrice() : 0);
            row.createCell(8).setCellValue(dto.getDiscountPrice() != null ? dto.getDiscountPrice() : 0);
            row.createCell(9).setCellValue(dto.getUpdatePrice() != null ? dto.getUpdatePrice() : 0);
            row.createCell(10).setCellValue(dto.getSiteMarketingCost() != null ? dto.getSiteMarketingCost() : 0);
            row.createCell(11).setCellValue(dto.getStoreMarketingCost() != null ? dto.getStoreMarketingCost() : 0);
            row.createCell(12).setCellValue(dto.getPaymentMethod());
            row.createCell(13).setCellValue(dto.getConsigneeName());
            row.createCell(14).setCellValue(dto.getConsigneeMobile());
            row.createCell(15).setCellValue(dto.getProvince());
            row.createCell(16).setCellValue(dto.getCity());
            row.createCell(17).setCellValue(dto.getDistrict());
            row.createCell(18).setCellValue(dto.getStreet());
            row.createCell(19).setCellValue(dto.getConsigneeDetail());
            row.createCell(20).setCellValue(dto.getRemark());
            row.createCell(21).setCellValue(dto.getCreateTime());
            row.createCell(22).setCellValue(dto.getPaymentTime());
            row.createCell(23).setCellValue(dto.getClientType());
            row.createCell(24).setCellValue(dto.getOrderStatus());
            row.createCell(25).setCellValue(dto.getOrderType());
            row.createCell(26).setCellValue(dto.getAfterSaleStatus());
            row.createCell(27).setCellValue(dto.getLogisticsTime());
            row.createCell(28).setCellValue(dto.getCompleteTime());
            row.createCell(29).setCellValue(dto.getStoreName());
        }

        return workbook;
    }
}
