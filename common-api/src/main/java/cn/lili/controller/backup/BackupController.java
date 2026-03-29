package cn.lili.modules.backup.controller;

import cn.lili.common.vo.ResultMessage;
import cn.lili.common.vo.ResultUtil;
import cn.lili.modules.backup.entity.BackupInfo;
import cn.lili.modules.backup.service.BackupManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 备份管理控制器
 * 
 * @author Qoder
 * @version v1.0
 * 2026-01-05
 */
@RestController
@Api(tags = "备份管理接口")
@RequestMapping("/common/backup")
public class BackupController {

    @Autowired
    private BackupManagerService backupManagerService;

    @ApiOperation(value = "执行完整备份")
    @PostMapping("/full")
    public ResultMessage<BackupInfo> performFullBackup(
            @RequestParam String backupName,
            @RequestParam(required = false) String description) {
        BackupInfo backupInfo = backupManagerService.performFullBackup(backupName, description);
        return ResultUtil.data(backupInfo);
    }

    @ApiOperation(value = "执行数据库备份")
    @PostMapping("/database")
    public ResultMessage<BackupInfo> performDatabaseBackup(
            @RequestParam String backupName,
            @RequestParam(required = false) String description) {
        BackupInfo backupInfo = backupManagerService.performDatabaseBackup(backupName, description);
        return ResultUtil.data(backupInfo);
    }

    @ApiOperation(value = "执行文件备份")
    @PostMapping("/file")
    public ResultMessage<BackupInfo> performFileBackup(
            @RequestParam String backupName,
            @RequestParam(required = false) String description) {
        BackupInfo backupInfo = backupManagerService.performFileBackup(backupName, description);
        return ResultUtil.data(backupInfo);
    }

    @ApiOperation(value = "获取备份列表")
    @GetMapping("/list")
    public ResultMessage<List<BackupInfo>> getBackupList() {
        List<BackupInfo> backupList = backupManagerService.getBackupList();
        return ResultUtil.data(backupList);
    }

    @ApiOperation(value = "恢复备份")
    @PostMapping("/restore/{backupId}")
    public ResultMessage<Boolean> restoreBackup(@PathVariable String backupId) {
        boolean result = backupManagerService.restoreBackup(backupId);
        return ResultUtil.data(result);
    }

    @ApiOperation(value = "删除备份")
    @DeleteMapping("/{backupId}")
    public ResultMessage<Boolean> deleteBackup(@PathVariable String backupId) {
        boolean result = backupManagerService.deleteBackup(backupId);
        return ResultUtil.data(result);
    }

    @ApiOperation(value = "验证备份")
    @PostMapping("/validate/{backupId}")
    public ResultMessage<Boolean> validateBackup(@PathVariable String backupId) {
        boolean result = backupManagerService.validateBackup(backupId);
        return ResultUtil.data(result);
    }
}