package cn.lili.modules.security.aspect.interceptor;

import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.utils.IpHelper;
import cn.lili.common.utils.IpUtils;
import cn.lili.common.utils.SpelUtil;
import cn.lili.common.utils.ThreadPoolUtil;
import cn.lili.modules.security.entity.vo.SecurityLogVO;
import cn.lili.modules.security.service.SecurityLogService;
import cn.lili.modules.security.aspect.annotation.SecurityLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NamedThreadLocal;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring AOP实现安全审计日志管理
 *
 * @author Qoder
 * @since 2026-01-05
 */
@Aspect
@Component
@Slf4j
public class SecurityLogAspect {

    /**
     * 记录方法开始时间
     */
    private static final ThreadLocal<Date> BEGIN_TIME_THREAD_LOCAL = new NamedThreadLocal<>("SECURITY-LOG");

    @Autowired
    private SecurityLogService securityLogService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private IpHelper ipHelper;

    /**
     * Controller层切点,注解方式
     */
    @Pointcut("@annotation(cn.lili.modules.security.aspect.annotation.SecurityLog)")
    public void controllerAspect() {
    }

    /**
     * 前置通知 (在方法执行之前返回)用于拦截Controller层记录用户的操作的开始时间
     */
    @Before("controllerAspect()")
    public void doBefore() {
        BEGIN_TIME_THREAD_LOCAL.set(new Date());
    }

    /**
     * 后置通知(在方法执行之后) 用于拦截Controller层操作
     *
     * @param joinPoint 切点
     */
    @After("controllerAspect()")
    public void after(JoinPoint joinPoint) {
        try {
            if (request == null) {
                return;
            }

            Map<String, String> securityLogInfo = getSecurityLogInfo(joinPoint);
            String operationType = securityLogInfo.get("operationType");
            String securityLevel = securityLogInfo.get("securityLevel");
            String description = securityLogInfo.get("description");

            // 从request获取参数
            Map<String, String[]> logParams = request.getParameterMap();
            AuthUser authUser = UserContext.getCurrentUser();
            SecurityLogVO securityLogVO = new SecurityLogVO();

            if (authUser != null) {
                // 用户信息
                securityLogVO.setUserId(authUser.getId());
                securityLogVO.setUsername(authUser.getUsername());
                securityLogVO.setUserType(authUser.getRole().name());
                // 如果是商家则记录商家id，否则记录-1，代表平台id
                securityLogVO.setStoreId(authUser.getRole().equals(cn.lili.common.security.enums.UserEnums.STORE) 
                    ? Long.parseLong(authUser.getStoreId()) : -1L);
            } else {
                securityLogVO.setUsername("游客");
                securityLogVO.setUserId("-1");
                securityLogVO.setUserType("GUEST");
                securityLogVO.setStoreId(-2L);
            }

            // 日志信息
            securityLogVO.setOperationType(operationType);
            securityLogVO.setSecurityLevel(securityLevel);
            securityLogVO.setRequestUrl(request.getRequestURI());
            securityLogVO.setRequestType(request.getMethod());
            securityLogVO.setRequestParam(logParams.toString());
            securityLogVO.setIp(IpUtils.getIpAddress(request));
            securityLogVO.setIpInfo(ipHelper.getIpCity(request));
            securityLogVO.setResult("SUCCESS"); // 默认成功，如果出现异常会在异常处理器中更新

            // 请求开始时间
            long beginTime = BEGIN_TIME_THREAD_LOCAL.get().getTime();
            long endTime = System.currentTimeMillis();
            // 请求耗时
            Long usedTime = endTime - beginTime;
            securityLogVO.setCostTime(usedTime.intValue());

            // 调用线程保存
            ThreadPoolUtil.getPool().execute(new SaveSecurityLogThread(securityLogVO, securityLogService));

            BEGIN_TIME_THREAD_LOCAL.remove();
        } catch (Exception e) {
            log.error("安全审计日志保存异常", e);
        }
    }

    /**
     * 获取注解中对方法的描述信息
     *
     * @param joinPoint 切点
     * @return 方法描述
     */
    private static Map<String, String> getSecurityLogInfo(JoinPoint joinPoint) {
        Map<String, String> result = new HashMap<>(3);
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        SecurityLog securityLog = signature.getMethod().getAnnotation(SecurityLog.class);
        
        String operationType = securityLog.operationType();
        String securityLevel = securityLog.securityLevel();
        String description = securityLog.description();

        result.put("operationType", operationType);
        result.put("securityLevel", securityLevel);
        result.put("description", description);
        return result;
    }

    /**
     * 保存安全日志
     */
    private static class SaveSecurityLogThread implements Runnable {
        private final SecurityLogVO securityLogVO;
        private final SecurityLogService securityLogService;

        public SaveSecurityLogThread(SecurityLogVO securityLogVO, SecurityLogService securityLogService) {
            this.securityLogVO = securityLogVO;
            this.securityLogService = securityLogService;
        }

        @Override
        public void run() {
            try {
                securityLogService.saveLog(securityLogVO);
            } catch (Exception e) {
                log.error("安全审计日志保存异常,内容{}：", securityLogVO, e);
            }
        }
    }
}