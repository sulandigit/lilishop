package cn.lili.controller.common;

import cn.lili.cache.Cache;
import cn.lili.common.context.ThreadContextHolder;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.utils.ResponseUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.file.entity.File;
import cn.lili.modules.file.entity.dto.FileOwnerDTO;
import cn.lili.modules.file.service.FileService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件管理接口
 *
 * @author Chopper
 * @since 2020/11/26 15:41
 */
@RestController
@Api(tags = "文件管理接口")
@RequestMapping("/common/common/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    private final Cache cache;

    @ApiOperation(value = "获取自己的图片资源")
    @GetMapping
    @ApiImplicitParam(name = "title", value = "名称模糊匹配")
    public ResultMessage<IPage<File>> getFileList(@RequestHeader String accessToken, FileOwnerDTO fileOwnerDTO) {
        AuthUser authUser = UserContext.getAuthUser(cache, accessToken);
        if (authUser == null) {
            ResponseUtil.output(ThreadContextHolder.getHttpResponse(), 403,
                    ResponseUtil.resultMap(false, 403, "登录已失效，请重新登录"));
            return null;
        }
        // 根据用户角色设置 ownerId
        if (authUser.getRole().equals(UserEnums.MEMBER)) {
            fileOwnerDTO.setOwnerId(authUser.getId());
        } else if (authUser.getRole().equals(UserEnums.STORE)) {
            fileOwnerDTO.setOwnerId(authUser.getStoreId());
        }
        fileOwnerDTO.setUserEnums(authUser.getRole().name());
        return ResultUtil.data(fileService.customerPageOwner(fileOwnerDTO));
    }

    @ApiOperation(value = "文件重命名")
    @PostMapping(value = "/rename")
    public ResultMessage<File> rename(@RequestHeader String accessToken, String id, String newName) {
        AuthUser authUser = getAuthUserOrThrow(accessToken);
        File file = fileService.getById(id);
        if (file == null) {
            throw new ServiceException(ResultCode.FILE_NOT_EXIST_ERROR);
        }
        checkFilePermission(authUser, file);
        file.setName(newName);
        fileService.updateById(file);
        return ResultUtil.data(file);
    }

    @ApiOperation(value = "文件删除")
    @DeleteMapping(value = "/delete/{ids}")
    public ResultMessage<Object> delete(@RequestHeader String accessToken, @PathVariable List<String> ids) {
        AuthUser authUser = getAuthUserOrThrow(accessToken);
        fileService.batchDelete(ids, authUser);
        return ResultUtil.success();
    }

    /**
     * 获取认证用户，若不存在则抛出异常
     */
    private AuthUser getAuthUserOrThrow(String accessToken) {
        AuthUser authUser = UserContext.getAuthUser(cache, accessToken);
        if (authUser == null) {
            throw new ServiceException(ResultCode.USER_AUTH_EXPIRED);
        }
        return authUser;
    }

    /**
     * 校验用户对文件的操作权限
     */
    private void checkFilePermission(AuthUser authUser, File file) {
        switch (authUser.getRole()) {
            case MEMBER:
                if (file.getOwnerId().equals(authUser.getId())
                        && file.getUserEnums().equals(authUser.getRole().name())) {
                    return;
                }
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
            case STORE:
                if (file.getOwnerId().equals(authUser.getStoreId())
                        && file.getUserEnums().equals(authUser.getRole().name())) {
                    return;
                }
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
            case MANAGER:
                return;
            default:
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
    }
}
