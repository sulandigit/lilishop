package cn.lili.modules.backup.service;

import java.io.File;
import java.util.List;

/**
 * 数据库备份服务接口
 * 
 * @author Qoder
 * @version v1.0
 * 2026-01-05
 */
public interface DatabaseBackupService {

    /**
     * 执行数据库备份
     * 
     * @param backupPath 备份文件存放路径
     * @param fileName 备份文件名
     * @return 备份文件对象
     */
    File backupDatabase(String backupPath, String fileName);

    /**
     * 执行数据库恢复
     * 
     * @param backupFilePath 备份文件路径
     * @return 恢复是否成功
     */
    boolean restoreDatabase(String backupFilePath);

    /**
     * 获取备份文件列表
     * 
     * @param backupPath 备份目录路径
     * @return 备份文件列表
     */
    List<File> getBackupFiles(String backupPath);

    /**
     * 删除备份文件
     * 
     * @param backupFilePath 备份文件路径
     * @return 删除是否成功
     */
    boolean deleteBackup(String backupFilePath);

    /**
     * 验证备份文件完整性
     * 
     * @param backupFilePath 备份文件路径
     * @return 验证结果
     */
    boolean validateBackup(String backupFilePath);
}