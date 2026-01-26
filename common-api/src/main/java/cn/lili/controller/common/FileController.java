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
 * File Management Controller.
 * <p>
 * This controller provides RESTful APIs for file management operations including:
 * <ul>
 *     <li>Listing files owned by the authenticated user</li>
 *     <li>Renaming files with permission validation</li>
 *     <li>Batch deleting files with permission validation</li>
 * </ul>
 * <p>
 * Access control is enforced based on user roles:
 * <ul>
 *     <li>MEMBER: Can only access their own files</li>
 *     <li>STORE: Can only access files belonging to their store</li>
 *     <li>MANAGER: Has full access to all files</li>
 * </ul>
 *
 * @author Chopper
 * @since 2020/11/26 15:41
 */
@RestController
@Api(tags = "文件管理接口")
@RequestMapping("/common/common/file")
@RequiredArgsConstructor
public class FileController {

    /**
     * File service for handling file-related business logic.
     */
    private final FileService fileService;

    /**
     * Cache service for retrieving authenticated user information.
     */
    private final Cache cache;

    /**
     * Retrieves a paginated list of files owned by the authenticated user.
     * <p>
     * The owner ID is automatically set based on the user's role:
     * <ul>
     *     <li>MEMBER: Uses the user's ID as owner ID</li>
     *     <li>STORE: Uses the store ID as owner ID</li>
     *     <li>MANAGER: No owner ID restriction (can view all files)</li>
     * </ul>
     *
     * @param accessToken  the JWT access token from request header for authentication
     * @param fileOwnerDTO the query parameters including pagination and optional title filter
     * @return a paginated result containing file records, or null if authentication fails
     */
    @ApiOperation(value = "获取自己的图片资源")
    @GetMapping
    @ApiImplicitParam(name = "title", value = "名称模糊匹配")
    public ResultMessage<IPage<File>> getFileList(@RequestHeader String accessToken, FileOwnerDTO fileOwnerDTO) {
        AuthUser authUser = UserContext.getAuthUser(cache, accessToken);
        if (authUser == null) {
            // Return 403 response directly when user session has expired
            ResponseUtil.output(ThreadContextHolder.getHttpResponse(), 403,
                    ResponseUtil.resultMap(false, 403, "登录已失效，请重新登录"));
            return null;
        }
        // Set owner ID based on user role for data isolation
        if (authUser.getRole().equals(UserEnums.MEMBER)) {
            fileOwnerDTO.setOwnerId(authUser.getId());
        } else if (authUser.getRole().equals(UserEnums.STORE)) {
            fileOwnerDTO.setOwnerId(authUser.getStoreId());
        }
        fileOwnerDTO.setUserEnums(authUser.getRole().name());
        return ResultUtil.data(fileService.customerPageOwner(fileOwnerDTO));
    }

    /**
     * Renames a file with the specified new name.
     * <p>
     * This operation requires the user to have ownership of the file.
     * Permission is validated based on the user's role and file ownership.
     *
     * @param accessToken the JWT access token from request header for authentication
     * @param id          the unique identifier of the file to rename
     * @param newName     the new name to assign to the file
     * @return the updated file entity wrapped in a result message
     * @throws ServiceException with FILE_NOT_EXIST_ERROR if the file does not exist
     * @throws ServiceException with USER_AUTH_EXPIRED if the user is not authenticated
     * @throws ServiceException with USER_AUTHORITY_ERROR if the user lacks permission
     */
    @ApiOperation(value = "文件重命名")
    @PostMapping(value = "/rename")
    public ResultMessage<File> rename(@RequestHeader String accessToken, String id, String newName) {
        AuthUser authUser = getAuthUserOrThrow(accessToken);
        File file = fileService.getById(id);
        if (file == null) {
            throw new ServiceException(ResultCode.FILE_NOT_EXIST_ERROR);
        }
        // Validate user has permission to modify this file
        checkFilePermission(authUser, file);
        file.setName(newName);
        fileService.updateById(file);
        return ResultUtil.data(file);
    }

    /**
     * Deletes multiple files by their IDs.
     * <p>
     * This operation performs batch deletion with permission validation
     * handled by the service layer for each file.
     *
     * @param accessToken the JWT access token from request header for authentication
     * @param ids         the list of file IDs to delete
     * @return a success result message if deletion completes
     * @throws ServiceException with USER_AUTH_EXPIRED if the user is not authenticated
     * @throws ServiceException with USER_AUTHORITY_ERROR if the user lacks permission for any file
     */
    @ApiOperation(value = "文件删除")
    @DeleteMapping(value = "/delete/{ids}")
    public ResultMessage<Object> delete(@RequestHeader String accessToken, @PathVariable List<String> ids) {
        AuthUser authUser = getAuthUserOrThrow(accessToken);
        fileService.batchDelete(ids, authUser);
        return ResultUtil.success();
    }

    /**
     * Retrieves the authenticated user from cache or throws an exception if not found.
     * <p>
     * This is a helper method to centralize authentication validation logic.
     *
     * @param accessToken the JWT access token to look up in cache
     * @return the authenticated user entity
     * @throws ServiceException with USER_AUTH_EXPIRED if the token is invalid or expired
     */
    private AuthUser getAuthUserOrThrow(String accessToken) {
        AuthUser authUser = UserContext.getAuthUser(cache, accessToken);
        if (authUser == null) {
            throw new ServiceException(ResultCode.USER_AUTH_EXPIRED);
        }
        return authUser;
    }

    /**
     * Validates that the authenticated user has permission to operate on the specified file.
     * <p>
     * Permission rules:
     * <ul>
     *     <li>MEMBER: Must own the file (matching user ID and role)</li>
     *     <li>STORE: Must own the file through store association (matching store ID and role)</li>
     *     <li>MANAGER: Always has permission (administrative access)</li>
     * </ul>
     *
     * @param authUser the authenticated user requesting the operation
     * @param file     the file entity to check permissions against
     * @throws ServiceException with USER_AUTHORITY_ERROR if permission check fails
     */
    private void checkFilePermission(AuthUser authUser, File file) {
        switch (authUser.getRole()) {
            case MEMBER:
                // Members can only access files they own
                if (file.getOwnerId().equals(authUser.getId())
                        && file.getUserEnums().equals(authUser.getRole().name())) {
                    return;
                }
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
            case STORE:
                // Store users can only access files belonging to their store
                if (file.getOwnerId().equals(authUser.getStoreId())
                        && file.getUserEnums().equals(authUser.getRole().name())) {
                    return;
                }
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
            case MANAGER:
                // Managers have full access to all files
                return;
            default:
                // Unknown roles are denied access
                throw new ServiceException(ResultCode.USER_AUTHORITY_ERROR);
        }
    }
}
