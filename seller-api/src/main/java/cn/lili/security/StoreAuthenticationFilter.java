package cn.lili.security;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.lili.cache.Cache;
import cn.lili.cache.CachePrefix;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.enums.PermissionEnum;
import cn.lili.common.security.enums.SecurityEnum;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.security.token.SecretKeyUtil;
import cn.lili.common.utils.ResponseUtil;
import cn.lili.modules.member.entity.dos.Clerk;
import cn.lili.modules.member.service.ClerkService;
import cn.lili.modules.member.service.StoreMenuRoleService;
import cn.lili.modules.member.token.StoreTokenGenerate;
import com.google.gson.Gson;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.naming.NoPermissionException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 店铺认证过滤器
 * 用于验证店铺端用户的JWT令牌，并进行权限校验
 * 
 * @author Chopper
 */
@Slf4j
public class StoreAuthenticationFilter extends BasicAuthenticationFilter {

    /**
     * 缓存服务
     */
    private final Cache cache;

    /**
     * 店铺Token生成器
     */
    private final StoreTokenGenerate storeTokenGenerate;

    /**
     * 店铺菜单角色服务
     */
    private final StoreMenuRoleService storeMenuRoleService;

    /**
     * 店员服务
     */
    private final ClerkService clerkService;

    /**
     * 构造方法
     *
     * @param authenticationManager 认证管理器
     * @param storeTokenGenerate    店铺Token生成器
     * @param storeMenuRoleService  店铺菜单角色服务
     * @param clerkService          店员服务
     * @param cache                 缓存服务
     */
    public StoreAuthenticationFilter(AuthenticationManager authenticationManager,
                                     StoreTokenGenerate storeTokenGenerate,
                                     StoreMenuRoleService storeMenuRoleService,
                                     ClerkService clerkService,
                                     Cache cache) {
        super(authenticationManager);
        this.storeTokenGenerate = storeTokenGenerate;
        this.storeMenuRoleService = storeMenuRoleService;
        this.clerkService = clerkService;
        this.cache = cache;
    }

    /**
     * 过滤器核心方法
     * 从请求头中获取JWT令牌，验证并将用户信息存入Spring Security上下文
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param chain    过滤器链
     * @throws IOException      IO异常
     * @throws ServletException Servlet异常
     */
    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        //从header中获取jwt
        String jwt = request.getHeader(SecurityEnum.HEADER_TOKEN.getValue());
        //如果没有token 则return
        if (StrUtil.isBlank(jwt)) {
            chain.doFilter(request, response);
            return;
        }
        //获取用户信息，存入context
        UsernamePasswordAuthenticationToken authentication = getAuthentication(jwt, response);
        //自定义权限过滤
        if (authentication != null) {
            customAuthentication(request, response, authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }


    /**
     * 获取token信息并验证
     * 解析JWT令牌，从中获取用户信息，并验证令牌在缓存中是否有效
     *
     * @param jwt      JWT令牌字符串
     * @param response HTTP响应
     * @return 用户认证信息，验证失败返回null
     */
    private UsernamePasswordAuthenticationToken getAuthentication(String jwt, HttpServletResponse response) {

        try {
            Claims claims
                    = Jwts.parserBuilder()
                    .setSigningKey(SecretKeyUtil.generalKeyByDecoders())
                    .build()
                    .parseClaimsJws(jwt).getBody();
            //获取存储在claims中的用户信息
            String json = claims.get(SecurityEnum.USER_CONTEXT.getValue()).toString();
            AuthUser authUser = new Gson().fromJson(json, AuthUser.class);
    
            //校验redis中是否有权限
            if (cache.hasKey(CachePrefix.ACCESS_TOKEN.getPrefix(UserEnums.STORE, authUser.getId()) + jwt)) {
                //用户角色
                List<GrantedAuthority> auths = new ArrayList<>();
                auths.add(new SimpleGrantedAuthority("ROLE_" + authUser.getRole().name()));
                //构造返回信息
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(authUser.getUsername(), null, auths);
                authentication.setDetails(authUser);
                return authentication;
            }
            ResponseUtil.output(response, 403, ResponseUtil.resultMap(false, 403, "登录已失效，请重新登录"));
            return null;
        } catch (ExpiredJwtException e) {
            log.debug("user analysis exception:", e);
        } catch (Exception e) {
            log.error("user analysis exception:", e);
        }
        return null;
    }


    /**
     * 自定义权限过滤
     * 根据用户角色和权限配置，验证用户是否有权限访问请求的URL
     * 超级管理员拥有所有权限，普通用户根据其角色权限进行校验
     *
     * @param request        请求
     * @param response       响应
     * @param authentication 用户信息
     * @throws NoPermissionException 权限不足异常
     */
    private void customAuthentication(HttpServletRequest request, HttpServletResponse response, UsernamePasswordAuthenticationToken authentication) throws NoPermissionException {
        AuthUser authUser = (AuthUser) authentication.getDetails();
        String requestUrl = request.getRequestURI();


        //如果不是超级管理员， 则鉴权
        if (Boolean.FALSE.equals(authUser.getIsSuper())) {

            String permissionCacheKey = CachePrefix.PERMISSION_LIST.getPrefix(UserEnums.STORE) + authUser.getId();
            //获取缓存中的权限
            Map<String, List<String>> permission =
                    (Map<String, List<String>>) cache.get(permissionCacheKey);
            if (permission == null || permission.isEmpty()) {
                //根据会员id查询店员信息
                Clerk clerk = clerkService.getClerkByMemberId(authUser.getId());
                if (clerk != null) {
                    permission = storeTokenGenerate.permissionList(storeMenuRoleService.findAllMenu(clerk.getId(), authUser.getId()));
                    cache.put(permissionCacheKey, permission);
                }
            }
            //获取数据(GET 请求)权限
            if (request.getMethod().equals(RequestMethod.GET.name())) {
                //如果用户的超级权限和查阅权限都不包含当前请求的api
                if (match(permission.get(PermissionEnum.SUPER.name()), requestUrl)
                        || match(permission.get(PermissionEnum.QUERY.name()), requestUrl)) {
                } else {
                    ResponseUtil.output(response, ResponseUtil.resultMap(false, 400, "权限不足"));
                    log.error("当前请求路径：{},所拥有权限：{}", requestUrl, JSONUtil.toJsonStr(permission));
                    throw new NoPermissionException("权限不足");
                }
            }
            //非get请求（数据操作） 判定鉴权
            else {
                if (!match(permission.get(PermissionEnum.SUPER.name()), requestUrl)) {
                    ResponseUtil.output(response, ResponseUtil.resultMap(false, 400, "权限不足"));
                    log.error("当前请求路径：{},所拥有权限：{}", requestUrl, JSONUtil.toJsonStr(permission));
                    throw new NoPermissionException("权限不足");
                }
            }
        }
    }

    /**
     * 校验权限
     * 使用Spring的模式匹配工具，验证请求URL是否匹配权限列表中的任一权限模式
     *
     * @param permissions 权限集合
     * @param url         请求地址
     * @return 是否拥有权限
     */
    boolean match(List<String> permissions, String url) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }
        return PatternMatchUtils.simpleMatch(permissions.toArray(new String[0]), url);
    }

}