package cn.lili.common.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 参数校验错误响应VO
 *
 * @author Chopper
 */
@Data
public class ValidationErrorResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字段错误列表
     */
    private List<FieldError> fieldErrors;

    /**
     * 字段错误详情
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldError implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 字段名称
         */
        private String field;

        /**
         * 错误的值
         */
        private Object rejectedValue;

        /**
         * 错误消息
         */
        private String message;
    }
}
