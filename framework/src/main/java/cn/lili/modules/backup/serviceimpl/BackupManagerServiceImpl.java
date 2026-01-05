package cn.lili.modules.backup.serviceimpl;

import cn.lili.modules.backup.entity.BackupInfo;
import cn.lili.modules.backup.service.BackupManagerService;
import cn.lili.modules.backup.service.DatabaseBackupService;
import cn.lili.modules.backup.service.FileBackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/**
 * 备份管理服务实现
 * 
 * @author Qoder
 * @version v1.0
 * 2026-01-05
 */
@Slf4j
@Service
public class BackupManagerServiceImpl implements BackupManagerService {

    private final DatabaseBackupService databaseBackupService;
    private final FileBackupService fileBackupService;

    @Value("${lili.backup.path:/opt/lilishop/backup}")
    private String backupPath;

    public BackupManagerServiceImpl(DatabaseBackupService databaseBackupService, 
                                  FileBackupService fileBackupService) {
        this.databaseBackupService = databaseBackupService;
        this.fileBackupService = fileBackupService;
    }

    @Override
    public BackupInfo performFullBackup(String backupName, String description) {
        log.info("开始执行完整备份: {}", backupName);
        
        BackupInfo backupInfo = new BackupInfo();
        backupInfo.setId(UUID.randomUUID().toString());
        backupInfo.setName(backupName);
        backupInfo.setDescription(description);
        backupInfo.setType("FULL");
        backupInfo.setCreateTime(LocalDateTime.now());
        backupInfo.setStatus("PROCESSING");

        try {
            // 创建备份目录
            String backupDir = backupPath + "/" + backupInfo.getId();
            new File(backupDir).mkdirs();

            // 执行数据库备份
            String dbBackupFileName = "database_" + System.currentTimeMillis() + ".sql";
            File dbBackupFile = databaseBackupService.backupDatabase(backupDir, dbBackupFileName);

            // 执行文件备份
            String fileBackupFileName = "files_" + System.currentTimeMillis() + ".zip";
            File fileBackupFile = fileBackupService.backupDirectory("/opt/lilishop/upload", backupDir, fileBackupFileName);

            // 设置备份路径和文件大小
            backupInfo.setBackupPath(backupDir);
            backupInfo.setFileSize(dbBackupFile.length() + fileBackupFile.length());
            backupInfo.setStatus("SUCCESS");
            backupInfo.setCompleteTime(LocalDateTime.now());
            backupInfo.setRemarks("数据库备份: " + dbBackupFileName + ", 文件备份: " + fileBackupFileName);

            log.info("完整备份完成: {}", backupName);
        } catch (Exception e) {
            log.error("完整备份失败: {}", backupName, e);
            backupInfo.setStatus("FAILED");
            backupInfo.setCompleteTime(LocalDateTime.now());
            backupInfo.setRemarks("备份失败: " + e.getMessage());
        }

        return backupInfo;
    }

    @Override
    public BackupInfo performDatabaseBackup(String backupName, String description) {
        log.info("开始执行数据库备份: {}", backupName);
        
        BackupInfo backupInfo = new BackupInfo();
        backupInfo.setId(UUID.randomUUID().toString());
        backupInfo.setName(backupName);
        backupInfo.setDescription(description);
        backupInfo.setType("DATABASE");
        backupInfo.setCreateTime(LocalDateTime.now());
        backupInfo.setStatus("PROCESSING");

        try {
            // 创建备份目录
            String backupDir = backupPath + "/" + backupInfo.getId();
            new File(backupDir).mkdirs();

            // 执行数据库备份
            String dbBackupFileName = "database_" + System.currentTimeMillis() + ".sql";
            File dbBackupFile = databaseBackupService.backupDatabase(backupDir, dbBackupFileName);

            // 设置备份路径和文件大小
            backupInfo.setBackupPath(backupDir);
            backupInfo.setFileSize(dbBackupFile.length());
            backupInfo.setStatus("SUCCESS");
            backupInfo.setCompleteTime(LocalDateTime.now());
            backupInfo.setRemarks("数据库备份: " + dbBackupFileName);

            log.info("数据库备份完成: {}", backupName);
        } catch (Exception e) {
            log.error("数据库备份失败: {}", backupName, e);
            backupInfo.setStatus("FAILED");
            backupInfo.setCompleteTime(LocalDateTime.now());
            backupInfo.setRemarks("备份失败: " + e.getMessage());
        }

        return backupInfo;
    }

    @Override
    public BackupInfo performFileBackup(String backupName, String description) {
        log.info("开始执行文件备份: {}", backupName);
        
        BackupInfo backupInfo = new BackupInfo();
        backupInfo.setId(UUID.randomUUID().toString());
        backupInfo.setName(backupName);
        backupInfo.setDescription(description);
        backupInfo.setType("FILE");
        backupInfo.setCreateTime(LocalDateTime.now());
        backupInfo.setStatus("PROCESSING");

        try {
            // 创建备份目录
            String backupDir = backupPath + "/" + backupInfo.getId();
            new File(backupDir).mkdirs();

            // 执行文件备份 - 备份上传目录
            String fileBackupFileName = "files_" + System.currentTimeMillis() + ".zip";
            File fileBackupFile = fileBackupService.backupDirectory("/opt/lilishop/upload", backupDir, fileBackupFileName);

            // 设置备份路径和文件大小
            backupInfo.setBackupPath(backupDir);
            backupInfo.setFileSize(fileBackupFile.length());
            backupInfo.setStatus("SUCCESS");
            backupInfo.setCompleteTime(LocalDateTime.now());
            backupInfo.setRemarks("文件备份: " + fileBackupFileName);

            log.info("文件备份完成: {}", backupName);
        } catch (Exception e) {
            log.error("文件备份失败: {}", backupName, e);
            backupInfo.setStatus("FAILED");
            backupInfo.setCompleteTime(LocalDateTime.now());
            backupInfo.setRemarks("备份失败: " + e.getMessage());
        }

        return backupInfo;
    }

    @Override
    public List<BackupInfo> getBackupList() {
        List<BackupInfo> backupList = new ArrayList<>();
        File backupDir = new File(backupPath);
        
        if (!backupDir.exists()) {
            return backupList;
        }

        File[] backupDirs = backupDir.listFiles(File::isDirectory);
        if (backupDirs == null) {
            return backupList;
        }

        for (File dir : backupDirs) {
            // 尝试从目录中读取备份信息
            BackupInfo backupInfo = readBackupInfoFromDir(dir);
            if (backupInfo != null) {
                backupList.add(backupInfo);
            }
        }

        return backupList;
    }

    @Override
    public boolean restoreBackup(String backupId) {
        log.info("开始恢复备份: {}", backupId);
        
        String backupDir = backupPath + "/" + backupId;
        File backupDirFile = new File(backupDir);
        
        if (!backupDirFile.exists()) {
            log.error("备份目录不存在: {}", backupDir);
            return false;
        }

        boolean success = true;
        
        // 查找数据库备份文件
        File[] dbBackupFiles = backupDirFile.listFiles((dir, name) -> 
            name.toLowerCase().startsWith("database_") && name.toLowerCase().endsWith(".sql"));
        
        if (dbBackupFiles != null && dbBackupFiles.length > 0) {
            // 恢复数据库
            success &= databaseBackupService.restoreDatabase(dbBackupFiles[0].getAbsolutePath());
        }

        // 查找文件备份文件
        File[] fileBackupFiles = backupDirFile.listFiles((dir, name) -> 
            name.toLowerCase().startsWith("files_") && name.toLowerCase().endsWith(".zip"));
        
        if (fileBackupFiles != null && fileBackupFiles.length > 0) {
            // 恢复文件
            success &= fileBackupService.restoreFile(fileBackupFiles[0].getAbsolutePath(), "/opt/lilishop/upload");
        }

        log.info("备份恢复完成: {}, 结果: {}", backupId, success);
        return success;
    }

    @Override
    public boolean deleteBackup(String backupId) {
        log.info("删除备份: {}", backupId);
        
        String backupDir = backupPath + "/" + backupId;
        File backupDirFile = new File(backupDir);
        
        if (!backupDirFile.exists()) {
            log.warn("备份目录不存在: {}", backupDir);
            return false;
        }

        return deleteDirectory(backupDirFile);
    }

    @Override
    public boolean validateBackup(String backupId) {
        log.info("验证备份: {}", backupId);
        
        String backupDir = backupPath + "/" + backupId;
        File backupDirFile = new File(backupDir);
        
        if (!backupDirFile.exists()) {
            log.error("备份目录不存在: {}", backupDir);
            return false;
        }

        boolean isValid = true;
        
        // 查找并验证数据库备份文件
        File[] dbBackupFiles = backupDirFile.listFiles((dir, name) -> 
            name.toLowerCase().startsWith("database_") && name.toLowerCase().endsWith(".sql"));
        
        if (dbBackupFiles != null) {
            for (File dbFile : dbBackupFiles) {
                isValid &= databaseBackupService.validateBackup(dbFile.getAbsolutePath());
            }
        }

        // 查找并验证文件备份文件
        File[] fileBackupFiles = backupDirFile.listFiles((dir, name) -> 
            name.toLowerCase().startsWith("files_") && name.toLowerCase().endsWith(".zip"));
        
        if (fileBackupFiles != null) {
            for (File file : fileBackupFiles) {
                isValid &= fileBackupService.validateBackup(file.getAbsolutePath());
            }
        }

        log.info("备份验证完成: {}, 结果: {}", backupId, isValid);
        return isValid;
    }

    /**
     * 从备份目录读取备份信息
     * 
     * @param backupDir 备份目录
     * @return 备份信息
     */
    private BackupInfo readBackupInfoFromDir(File backupDir) {
        BackupInfo backupInfo = new BackupInfo();
        backupInfo.setId(backupDir.getName());
        
        // 设置基本属性
        backupInfo.setName("Backup_" + backupDir.getName().substring(0, Math.min(8, backupDir.getName().length())));
        backupInfo.setCreateTime(LocalDateTime.now()); // 实际应该从文件属性或元数据中读取
        
        // 检查备份类型
        File[] files = backupDir.listFiles();
        if (files != null) {
            boolean hasDbBackup = false;
            boolean hasFileBackup = false;
            
            for (File file : files) {
                if (file.getName().toLowerCase().startsWith("database_")) {
                    hasDbBackup = true;
                } else if (file.getName().toLowerCase().startsWith("files_")) {
                    hasFileBackup = true;
                }
            }
            
            if (hasDbBackup && hasFileBackup) {
                backupInfo.setType("FULL");
            } else if (hasDbBackup) {
                backupInfo.setType("DATABASE");
            } else if (hasFileBackup) {
                backupInfo.setType("FILE");
            } else {
                backupInfo.setType("UNKNOWN");
            }
            
            // 计算总大小
            long totalSize = 0;
            for (File file : files) {
                totalSize += file.length();
            }
            backupInfo.setFileSize(totalSize);
        }
        
        backupInfo.setStatus("VALID");
        return backupInfo;
    }

    /**
     * 删除目录及其内容
     * 
     * @param directory 要删除的目录
     * @return 删除是否成功
     */
    private boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        return directory.delete();
    }
}