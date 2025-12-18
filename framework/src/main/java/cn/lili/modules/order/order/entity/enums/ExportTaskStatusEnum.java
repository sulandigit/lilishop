package cn.lili.modules.order.order.entity.enums;

public enum ExportTaskStatusEnum {

    PENDING("待处理"),
    PROCESSING("处理中"),
    SUCCESS("成功"),
    FAILED("失败");

    private final String description;

    ExportTaskStatusEnum(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
