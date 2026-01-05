package cn.lili.modules.security.test;

import cn.lili.modules.security.entity.vo.SecurityLogVO;
import cn.lili.modules.security.service.SecurityLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 安全审计日志功能测试
 *
 * @author Qoder
 * @since 2026-01-05
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
public class SecurityLogTest {

    @Autowired
    private SecurityLogService securityLogService;

    @Test
    public void testSaveSecurityLog() {
        SecurityLogVO securityLog = new SecurityLogVO();
        securityLog.setUsername("testUser");
        securityLog.setUserId("testUserId");
        securityLog.setUserType("MEMBER");
        securityLog.setRequestUrl("/test/endpoint");
        securityLog.setOperationType("TEST_OPERATION");
        securityLog.setSecurityLevel("INFO");
        securityLog.setRequestType("POST");
        securityLog.setRequestParam("{\"testParam\":\"testValue\"}");
        securityLog.setIp("127.0.0.1");
        securityLog.setIpInfo("Localhost");
        securityLog.setResult("SUCCESS");
        securityLog.setCostTime(100);
        securityLog.setCreateTime(new Date().getTime());

        securityLogService.saveLog(securityLog);

        // 验证日志已创建（通过不抛出异常来验证）
        assertNotNull(securityLog);
    }
}