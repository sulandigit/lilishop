package cn.lili.modules.backup.quartz;

import cn.lili.modules.backup.service.BackupManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 自动备份定时任务
 * 
 * @author Qoder
 * @version v1.0
 * 2026-01-05
 */
@Slf4j
@Configuration
@EnableScheduling
public class AutoBackupQuartz {

    @Autowired
    private BackupManagerService backupManagerService;

    @Value("${lili.backup.auto.enabled:false}")
    private boolean autoBackupEnabled;

    @Value("${lili.backup.auto.type:FULL}")
    private String backupType;

    /**
     * 每天凌晨2点执行自动备份
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void performAutoBackup() {
        if (!autoBackupEnabled) {
            log.info("自动备份未启用");
            return;
        }

        log.info("开始执行自动备份，类型: {}", backupType);
        
        try {
            switch (backupType.toUpperCase()) {
                case "FULL":
                    backupManagerService.performFullBackup("Auto_Full_Backup_" + System.currentTimeMillis(), "自动完整备份");
                    break;
                case "DATABASE":
                    backupManagerService.performDatabaseBackup("Auto_DB_Backup_" + System.currentTimeMillis(), "自动数据库备份");
                    break;
                case "FILE":
                    backupManagerService.performFileBackup("Auto_File_Backup_" + System.currentTimeMillis(), "自动文件备份");
                    break;
                default:
                    log.warn("未知的备份类型: {}", backupType);
                    break;
            }
            
            log.info("自动备份完成");
        } catch (Exception e) {
            log.error("自动备份执行失败", e);
        }
    }
}