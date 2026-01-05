package cn.lili.modules.backup.service;

import java.io.File;
import java.util.List;

/**
 * 文件备份服务接口
 * 
 * @author Qoder
 * @version v1.0
 * 2026-01-05
 */
public interface FileBackupService {

    /**
     * 备份本地文件到指定位置
     * 
     * @param sourcePath 源文件路径
     * @param backupPath 备份路径
     * @param fileName 备份文件名
     * @return 备份文件对象
     */
    File backupLocalFile(String sourcePath, String backupPath, String fileName);

    /**
     * 备份文件目录
     * 
     * @param sourceDir 源目录
     * @param backupPath 备份路径
     * @param archiveName 归档文件名
     * @return 备份文件对象
     */
    File backupDirectory(String sourceDir, String backupPath, String archiveName);

    /**
     * 从备份恢复文件
     * 
     * @param backupFilePath 备份文件路径
     * @param restorePath 恢复路径
     * @return 恢复是否成功
     */
    boolean restoreFile(String backupFilePath, String restorePath);

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