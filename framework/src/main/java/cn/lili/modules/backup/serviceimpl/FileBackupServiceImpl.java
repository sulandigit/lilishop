package cn.lili.modules.backup.serviceimpl;

import cn.lili.modules.backup.service.FileBackupService;
import cn.lili.modules.backup.util.BackupValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 文件备份服务实现
 * 
 * @author Qoder
 * @version v1.0
 * 2026-01-05
 */
@Slf4j
@Service
public class FileBackupServiceImpl implements FileBackupService {

    @Override
    public File backupLocalFile(String sourcePath, String backupPath, String fileName) {
        try {
            File sourceFile = new File(sourcePath);
            if (!sourceFile.exists()) {
                throw new RuntimeException("源文件不存在: " + sourcePath);
            }

            // 创建备份目录
            File backupDir = new File(backupPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            // 复制文件
            File backupFile = new File(backupPath, fileName);
            try (FileInputStream fis = new FileInputStream(sourceFile);
                 FileOutputStream fos = new FileOutputStream(backupFile)) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
            }

            log.info("文件备份成功: {} -> {}", sourcePath, backupFile.getAbsolutePath());
            return backupFile;
        } catch (Exception e) {
            log.error("文件备份失败: {}", sourcePath, e);
            throw new RuntimeException("文件备份失败: " + e.getMessage(), e);
        }
    }

    @Override
    public File backupDirectory(String sourceDir, String backupPath, String archiveName) {
        try {
            File sourceDirectory = new File(sourceDir);
            if (!sourceDirectory.exists() || !sourceDirectory.isDirectory()) {
                throw new RuntimeException("源目录不存在或不是目录: " + sourceDir);
            }

            // 创建备份目录
            File backupDir = new File(backupPath);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }

            // 创建归档文件
            File archiveFile = new File(backupPath, archiveName);
            try (FileOutputStream fos = new FileOutputStream(archiveFile);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                // 递归添加目录中的文件到归档
                addToZip(sourceDirectory, sourceDirectory.getName(), zos);
            }

            log.info("目录备份成功: {} -> {}", sourceDir, archiveFile.getAbsolutePath());
            return archiveFile;
        } catch (Exception e) {
            log.error("目录备份失败: {}", sourceDir, e);
            throw new RuntimeException("目录备份失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean restoreFile(String backupFilePath, String restorePath) {
        try {
            File backupFile = new File(backupFilePath);
            if (!backupFile.exists()) {
                log.error("备份文件不存在: {}", backupFilePath);
                return false;
            }

            // 创建恢复目录
            File restoreDir = new File(restorePath);
            if (!restoreDir.exists()) {
                restoreDir.mkdirs();
            }

            // 检查是否为ZIP文件
            if (backupFilePath.toLowerCase().endsWith(".zip")) {
                return extractZip(backupFilePath, restorePath);
            } else {
                // 直接复制文件
                File sourceFile = new File(backupFilePath);
                File destFile = new File(restorePath, sourceFile.getName());
                
                Files.copy(Paths.get(backupFilePath), Paths.get(destFile.getAbsolutePath()));
                log.info("文件恢复成功: {} -> {}", backupFilePath, destFile.getAbsolutePath());
                return true;
            }
        } catch (Exception e) {
            log.error("文件恢复失败: {}", backupFilePath, e);
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
            name.toLowerCase().endsWith(".zip") || 
            name.toLowerCase().endsWith(".tar") || 
            name.toLowerCase().endsWith(".tar.gz") || 
            name.toLowerCase().endsWith(".sql") ||
            name.toLowerCase().endsWith(".sql.gz"));
        
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

            // 验证ZIP文件
            if (backupFilePath.toLowerCase().endsWith(".zip")) {
                return BackupValidationUtil.validateZipIntegrity(backupFilePath);
            }

            log.info("文件备份验证通过: {}", backupFilePath);
            return true;
        } catch (Exception e) {
            log.error("验证备份文件时发生异常: {}", backupFilePath, e);
            return false;
        }
    }

    /**
     * 将目录内容添加到ZIP归档
     * 
     * @param sourceDir 源目录
     * @param fileName 文件名
     * @param zos ZIP输出流
     * @throws IOException
     */
    private void addToZip(File sourceDir, String fileName, ZipOutputStream zos) throws IOException {
        File[] files = sourceDir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                addToZip(file, fileName + "/" + file.getName(), zos);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    zos.putNextEntry(new ZipEntry(fileName + "/" + file.getName()));
                    byte[] buffer = new byte[8192];
                    int length;
                    while ((length = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    /**
     * 提取ZIP文件
     * 
     * @param zipFilePath ZIP文件路径
     * @param destDir 目标目录
     * @return 提取是否成功
     */
    private boolean extractZip(String zipFilePath, String destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(destDir, zipEntry.getName());
                
                // 确保目录存在
                File parent = newFile.getParentFile();
                if (parent != null && !parent.exists()) {
                    parent.mkdirs();
                }

                if (!zipEntry.isDirectory()) {
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[8192];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
        log.info("ZIP文件解压成功: {} -> {}", zipFilePath, destDir);
        return true;
    }

    /**
     * 验证ZIP文件完整性
     * 
     * @param zipFilePath ZIP文件路径
     * @return 验证结果
     */
    private boolean validateZipFile(String zipFilePath) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // 读取条目内容以验证完整性
                byte[] buffer = new byte[1024];
                while (zis.read(buffer) != -1) {
                    // 继续读取，直到条目结束
                }
                zis.closeEntry();
            }
            return true;
        } catch (Exception e) {
            log.error("ZIP文件验证失败: {}", zipFilePath, e);
            return false;
        }
    }
}