package cn.lili.modules.backup.service;

import cn.lili.modules.backup.entity.BackupInfo;
import java.util.List;

/**
 * 备份管理服务接口
 * 
 * @author Qoder
 * @version v1.0
 * 2026-01-05
 */
public interface BackupManagerService {

    /**
     * 执行完整备份（数据库+文件）
     * 
     * @param backupName 备份名称
     * @param description 备份描述
     * @return 备份信息
     */
    BackupInfo performFullBackup(String backupName, String description);

    /**
     * 执行数据库备份
     * 
     * @param backupName 备份名称
     * @param description 备份描述
     * @return 备份信息
     */
    BackupInfo performDatabaseBackup(String backupName, String description);

    /**
     * 执行文件备份
     * 
     * @param backupName 备份名称
     * @param description 备份描述
     * @return 备份信息
     */
    BackupInfo performFileBackup(String backupName, String description);

    /**
     * 获取备份列表
     * 
     * @return 备份信息列表
     */
    List<BackupInfo> getBackupList();

    /**
     * 恢复备份
     * 
     * @param backupId 备份ID
     * @return 恢复是否成功
     */
    boolean restoreBackup(String backupId);

    /**
     * 删除备份
     * 
     * @param backupId 备份ID
     * @return 删除是否成功
     */
    boolean deleteBackup(String backupId);

    /**
     * 验证备份完整性
     * 
     * @param backupId 备份ID
     * @return 验证结果
     */
    boolean validateBackup(String backupId);
}