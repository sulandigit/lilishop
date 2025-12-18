package cn.lili.init;

import cn.lili.modules.system.service.SensitiveWordsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 敏感词初始化类
 * 在应用启动时加载和初始化敏感词库
 *
 * @author Chopper
 * @version v1.0
 * 2021-11-29 11:38
 */
@Slf4j
@Component
public class SensitiveWordsInit implements ApplicationRunner {

    /**
     * 敏感词服务
     */
    @Autowired
    private SensitiveWordsService sensitiveWordsService;

    /**
     * consumer 启动时,实时更新一下过滤词
     * 从数据库加载敏感词并刷新缓存
     *
     * @param args 启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        // 重置敏感词缓存
        sensitiveWordsService.resetCache();
    }

}
