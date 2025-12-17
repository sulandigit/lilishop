package cn.lili.controller.other;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.system.entity.dos.AppVersion;
import cn.lili.modules.system.service.AppVersionService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 买家端,APP版本控制器
 * <p>提供应用版本检测和版本历史查询功能</p>
 *
 * @author Bulbasaur
 * @since 2021/5/21 11:15 上午
 */
@Slf4j
@RestController
@Api(tags = "买家端,APP版本接口")
@RequestMapping("/buyer/other/appVersion")
@RequiredArgsConstructor
public class AppVersionBuyerController {

    private final AppVersionService appVersionService;

    /**
     * 获取指定应用类型的最新版本信息
     * <p>用于客户端检测更新,返回对应平台(iOS/Android)的最新版本详情</p>
     *
     * @param appType 应用类型,可选值: IOS, ANDROID,参考 {@link cn.lili.modules.system.entity.enums.AppType}
     * @return 最新版本信息,包含版本号、下载地址、是否强制更新等字段
     */
    @ApiOperation(value = "根据应用类型获取最新版本信息")
    @ApiImplicitParam(name = "appType", value = "应用类型(IOS/ANDROID)", required = true, paramType = "path")
    @GetMapping("/{appType}/latest")
    public ResultMessage<AppVersion> getLatestVersion(@PathVariable String appType) {
        log.info("获取{}类型的最新版本", appType);
        return ResultUtil.data(appVersionService.getAppVersion(appType));
    }

    /**
     * 分页获取指定应用类型的版本历史列表
     * <p>用于版本管理后台查看历史版本记录,支持分页和排序</p>
     *
     * @param appType 应用类型,可选值: IOS, ANDROID,参考 {@link cn.lili.modules.system.entity.enums.AppType}
     * @param pageVO  分页参数,包含页码、每页大小、排序字段等
     * @return 版本历史分页列表
     */
    @ApiOperation(value = "根据应用类型分页获取版本历史列表")
    @ApiImplicitParam(name = "appType", value = "应用类型(IOS/ANDROID)", required = true, paramType = "path")
    @GetMapping("/{appType}/list")
    public ResultMessage<IPage<AppVersion>> getAppVersionList(@PathVariable String appType, PageVO pageVO) {
        log.info("分页获取{}类型的版本列表,页码:{},每页:{}", appType, pageVO.getPageNumber(), pageVO.getPageSize());
        IPage<AppVersion> page = appVersionService.page(
            PageUtil.initPage(pageVO), 
            new LambdaQueryWrapper<AppVersion>().eq(AppVersion::getType, appType)
        );
        return ResultUtil.data(page);
    }

    /**
     * 获取版本号列表(已废弃)
     * 
     * @deprecated 该接口路由命名不规范,请使用 {@link #getAppVersionList(String, PageVO)} 替代
     * @param appType 应用类型
     * @param pageVO  分页参数
     * @return 版本历史分页列表
     */
    @Deprecated
    @ApiOperation(value = "获取版本号列表(已废弃,请使用 /{appType}/list)")
    @ApiImplicitParam(name = "appType", value = "应用类型(IOS/ANDROID)", required = true, paramType = "path")
    @GetMapping("/appVersion/{appType}")
    public ResultMessage<IPage<AppVersion>> appVersionDeprecated(@PathVariable String appType, PageVO pageVO) {
        log.warn("调用了已废弃的版本列表接口,请升级到新路由: /{}/list", appType);
        return getAppVersionList(appType, pageVO);
    }
}