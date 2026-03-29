package cn.lili.modules.backup.serviceimpl;

import cn.lili.modules.backup.service.DatabaseBackupService;
import cn.lili.modules.backup.util.BackupValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 数据库备份服务实现
 * 
 * @author Qoder
 * @version v1.0
 * 2026-01-05
 */
@Slf4j
@Service
public class DatabaseBackupServiceImpl implements DatabaseBackupService {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Override
    public File backupDatabase(String backupPath, String fileName) {
        try {
            // 解析数据库连接信息
            String dbHost = getDbHostFromUrl(dbUrl);
            String dbName = getDbNameFromUrl(dbUrl);

            // 创建备份目录
            File backupDir = new File(backupPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            // 构建备份命令
            String backupFilePath = backupPath + "/" + fileName;
            String[] command = {
                "mysqldump",
                "--host=" + dbHost,
                "--user=" + dbUsername,
                "--password=" + dbPassword,
                "--single-transaction",  // 保证备份一致性
                "--routines",           // 包含存储过程和函数
                "--triggers",           // 包含触发器
                "--events",             // 包含事件调度器
                "--all-databases",      // 备份所有数据库
                "--result-file=" + backupFilePath
            };

            // 执行备份命令
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("数据库备份成功: {}", backupFilePath);
                return new File(backupFilePath);
            } else {
                log.error("数据库备份失败，退出码: {}", exitCode);
                throw new RuntimeException("数据库备份失败");
            }
        } catch (Exception e) {
            log.error("数据库备份过程中发生异常", e);
            throw new RuntimeException("数据库备份失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean restoreDatabase(String backupFilePath) {
        try {
            // 解析数据库连接信息
            String dbHost = getDbHostFromUrl(dbUrl);
            String dbName = getDbNameFromUrl(dbUrl);

            // 检查备份文件是否存在
            File backupFile = new File(backupFilePath);
            if (!backupFile.exists()) {
                log.error("备份文件不存在: {}", backupFilePath);
                return false;
            }

            // 构建恢复命令
            String[] command = {
                "mysql",
                "--host=" + dbHost,
                "--user=" + dbUsername,
                "--password=" + dbPassword,
                "--execute=source " + backupFilePath
            };

            // 执行恢复命令
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("数据库恢复成功: {}", backupFilePath);
                return true;
            } else {
                log.error("数据库恢复失败，退出码: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("数据库恢复过程中发生异常", e);
            return false;
        }
    }

    @Override
    public List<File> getBackupFiles(String backupPath) {
        File backupDir = new File(backupPath);
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            return new ArrayList<>();
        }

        File[] files = backupDir.listFiles((dir, name) -> 
            name.toLowerCase().endsWith(".sql") || name.toLowerCase().endsWith(".sql.gz"));
        
        if (files == null) {
            return new ArrayList<>();
        }

        return Arrays.asList(files);
    }

    @Override
    public boolean deleteBackup(String backupFilePath) {
        try {
            File backupFile = new File(backupFilePath);
            if (backupFile.exists()) {
                boolean deleted = backupFile.delete();
                if (deleted) {
                    log.info("备份文件删除成功: {}", backupFilePath);
                } else {
                    log.error("备份文件删除失败: {}", backupFilePath);
                }
                return deleted;
            } else {
                log.warn("备份文件不存在: {}", backupFilePath);
                return false;
            }
        } catch (Exception e) {
            log.error("删除备份文件时发生异常: {}", backupFilePath, e);
            return false;
        }
    }

    @Override
    public boolean validateBackup(String backupFilePath) {
        try {
            File backupFile = new File(backupFilePath);
            if (!backupFile.exists()) {
                log.error("备份文件不存在: {}", backupFilePath);
                return false;
            }

            // 使用工具类验证文件大小
            if (!BackupValidationUtil.validateFileSize(backupFilePath, 1024)) { // 最小1KB
                return false;
            }

            // 检查文件是否包含基本的SQL结构
            if (!BackupValidationUtil.validateSqlStructure(backupFilePath)) {
                log.error("SQL文件结构验证失败: {}", backupFilePath);
                return false;
            }

            log.info("数据库备份文件验证通过: {}", backupFilePath);
            return true;
        } catch (Exception e) {
            log.error("验证备份文件时发生异常: {}", backupFilePath, e);
            return false;
        }
    }

    /**
     * 从数据库URL中提取主机名
     * 
     * @param url 数据库URL
     * @return 主机名
     */
    private String getDbHostFromUrl(String url) {
        try {
            // 假设URL格式为 jdbc:mysql://host:port/database
            String hostPart = url.substring("jdbc:mysql://".length());
            return hostPart.split("/")[0].split(":")[0];
        } catch (Exception e) {
            log.error("解析数据库主机失败: {}", url, e);
            return "localhost";
        }
    }

    /**
     * 从数据库URL中提取数据库名
     * 
     * @param url 数据库URL
     * @return 数据库名
     */
    private String getDbNameFromUrl(String url) {
        try {
            // 假设URL格式为 jdbc:mysql://host:port/database
            String[] parts = url.split("/");
            return parts[parts.length - 1].split("\\?")[0];
        } catch (Exception e) {
            log.error("解析数据库名失败: {}", url, e);
            return "";
        }
    }
}