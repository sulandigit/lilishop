package cn.lili.modules.backup.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 备份信息实体
 * 
 * @author Qoder
 * @version v1.0
 * 2026-01-05
 */
@Data
public class BackupInfo {

    /**
     * 备份ID
     */
    private String id;

    /**
     * 备份名称
     */
    private String name;

    /**
     * 备份描述
     */
    private String description;

    /**
     * 备份类型 (DATABASE, FILE, FULL)
     */
    private String type;

    /**
     * 备份文件路径
     */
    private String backupPath;

    /**
     * 备份文件大小
     */
    private Long fileSize;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 备份状态 (SUCCESS, FAILED, PROCESSING)
     */
    private String status;

    /**
     * 备份完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 备份说明
     */
    private String remarks;
}