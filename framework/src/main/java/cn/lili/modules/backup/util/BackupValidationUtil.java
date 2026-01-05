package cn.lili.modules.backup.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 备份验证工具类
 * 
 * @author Qoder
 * @version v1.0
 * 2026-01-05
 */
@Slf4j
public class BackupValidationUtil {

    /**
     * 计算文件的MD5校验和
     * 
     * @param filePath 文件路径
     * @return MD5校验和
     */
    public static String calculateMD5(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = fis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
            
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
        } catch (Exception e) {
            log.error("计算MD5校验和失败: {}", filePath, e);
            return null;
        }
    }

    /**
     * 验证ZIP文件完整性
     * 
     * @param zipFilePath ZIP文件路径
     * @return 验证结果
     */
    public static boolean validateZipIntegrity(String zipFilePath) {
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            // 遍历所有条目并验证
            return zipFile.stream()
                    .allMatch(entry -> {
                        try (InputStream is = zipFile.getInputStream(entry)) {
                            // 读取条目内容以验证完整性
                            byte[] buffer = new byte[8192];
                            while (is.read(buffer) != -1) {
                                // 继续读取直到条目结束
                            }
                            return true;
                        } catch (Exception e) {
                            log.error("验证ZIP条目失败: {}", entry.getName(), e);
                            return false;
                        }
                    });
        } catch (Exception e) {
            log.error("验证ZIP文件完整性失败: {}", zipFilePath, e);
            return false;
        }
    }

    /**
     * 验证SQL文件基本结构
     * 
     * @param sqlFilePath SQL文件路径
     * @return 验证结果
     */
    public static boolean validateSqlStructure(String sqlFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(sqlFilePath))) {
            String line;
            int lineCount = 0;
            boolean hasSqlKeywords = false;
            
            // 检查前100行是否包含SQL关键字
            while ((line = reader.readLine()) != null && lineCount < 100) {
                line = line.trim().toLowerCase();
                
                if (line.contains("create") || line.contains("insert") || 
                    line.contains("update") || line.contains("delete") || 
                    line.contains("drop") || line.contains("alter") ||
                    line.contains("table") || line.contains("database")) {
                    hasSqlKeywords = true;
                    break;
                }
                
                lineCount++;
            }
            
            if (!hasSqlKeywords) {
                log.warn("SQL文件可能不包含有效的SQL语句: {}", sqlFilePath);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("验证SQL文件结构失败: {}", sqlFilePath, e);
            return false;
        }
    }

    /**
     * 验证文件大小是否合理
     * 
     * @param filePath 文件路径
     * @param expectedMinSize 最小预期大小（字节）
     * @return 验证结果
     */
    public static boolean validateFileSize(String filePath, long expectedMinSize) {
        File file = new File(filePath);
        if (!file.exists()) {
            log.error("文件不存在: {}", filePath);
            return false;
        }
        
        long fileSize = file.length();
        if (fileSize == 0) {
            log.error("文件为空: {}", filePath);
            return false;
        }
        
        if (expectedMinSize > 0 && fileSize < expectedMinSize) {
            log.warn("文件大小小于预期: {} (实际: {} 字节, 预期最小: {} 字节)", 
                    filePath, fileSize, expectedMinSize);
            return false;
        }
        
        return true;
    }

    /**
     * 创建备份文件的校验文件
     * 
     * @param backupFilePath 备份文件路径
     * @param checksumFilepath 校验文件路径
     * @return 创建是否成功
     */
    public static boolean createChecksumFile(String backupFilePath, String checksumFilepath) {
        String md5 = calculateMD5(backupFilePath);
        if (md5 == null) {
            return false;
        }
        
        try (FileWriter writer = new FileWriter(checksumFilepath)) {
            writer.write(md5 + "  " + new File(backupFilePath).getName());
            writer.flush();
            return true;
        } catch (IOException e) {
            log.error("创建校验文件失败: {}", checksumFilepath, e);
            return false;
        }
    }

    /**
     * 验证备份文件与校验文件的一致性
     * 
     * @param backupFilePath 备份文件路径
     * @param checksumFilepath 校验文件路径
     * @return 验证结果
     */
    public static boolean verifyChecksum(String backupFilePath, String checksumFilepath) {
        try {
            // 读取校验文件内容
            String storedChecksum;
            try (BufferedReader reader = new BufferedReader(new FileReader(checksumFilepath))) {
                String line = reader.readLine();
                if (line != null) {
                    storedChecksum = line.split("\\s+")[0].toLowerCase();
                } else {
                    log.error("校验文件为空: {}", checksumFilepath);
                    return false;
                }
            }
            
            // 计算当前文件的校验和
            String currentChecksum = calculateMD5(backupFilePath);
            if (currentChecksum == null) {
                return false;
            }
            
            // 比较校验和
            boolean isValid = storedChecksum.equals(currentChecksum.toLowerCase());
            if (!isValid) {
                log.error("校验和不匹配: 文件 {} (当前: {}, 存储: {})", 
                        backupFilePath, currentChecksum, storedChecksum);
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("验证校验和失败: {}", checksumFilepath, e);
            return false;
        }
    }
}