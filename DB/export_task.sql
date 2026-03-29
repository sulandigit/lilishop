-- 导出任务表
CREATE TABLE `li_export_task`
(
    `id`            varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          NOT NULL COMMENT '主键ID',
    `create_time`   datetime                                                       NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          NULL DEFAULT NULL COMMENT '创建者',
    `update_time`   datetime                                                       NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by`     varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          NULL DEFAULT NULL COMMENT '更新者',
    `delete_flag`   tinyint(1)                                                     NULL DEFAULT 0 COMMENT '删除标志 true/false 删除/未删除',
    `task_name`     varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '任务名称',
    `export_type`   varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL DEFAULT NULL COMMENT '导出类型',
    `status`        varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL DEFAULT NULL COMMENT '任务状态: PENDING-待处理, PROCESSING-处理中, SUCCESS-成功, FAILED-失败',
    `query_params`  text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci          NULL COMMENT '查询参数JSON',
    `download_url`  varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '文件下载URL',
    `total_count`   bigint                                                         NULL DEFAULT NULL COMMENT '导出记录数',
    `fail_reason`   varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '失败原因',
    `operator_id`   varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          NULL DEFAULT NULL COMMENT '操作人ID',
    `operator_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci   NULL DEFAULT NULL COMMENT '操作人类型: MANAGER-管理员, STORE-商家',
    `store_id`      varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin          NULL DEFAULT NULL COMMENT '店铺ID(商家操作时)',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `idx_operator` (`operator_id`, `operator_type`) USING BTREE,
    INDEX `idx_status` (`status`) USING BTREE,
    INDEX `idx_create_time` (`create_time`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '导出任务表'
  ROW_FORMAT = DYNAMIC;
