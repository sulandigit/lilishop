package cn.lili.modules.order.order.entity.enums;

public enum ExportTypeEnum {

    ORDER("订单导出");

    private final String description;

    ExportTypeEnum(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
