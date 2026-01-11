package cn.lili.controller.passport;


import cn.lili.common.aop.annotation.DemoSite;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.security.AuthUser;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.security.enums.UserEnums;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.Member;
import cn.lili.modules.member.service.MemberService;
import cn.lili.modules.sms.SmsUtil;
import cn.lili.modules.verification.entity.enums.VerificationEnums;
import cn.lili.modules.verification.service.VerificationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

/**
 * Store Passport Controller
 * Handles store/seller authentication operations including login, logout, and password management.
 *
 * @author Chopper
 * @since 2020/12/22 15:02
 */

@RestController
@Api(tags = "店铺端,商家登录接口 ")
@RequestMapping("/store/passport/login")
public class StorePassportController {

    /**
     * Member service for handling member-related operations
     */
    @Autowired
    private MemberService memberService;

    /**
     * SMS utility for sending and verifying SMS codes
     */
    @Autowired
    private SmsUtil smsUtil;

    /**
     * Verification service for handling verification code operations
     */
    @Autowired
    private VerificationService verificationService;

    /**
     * User login with username and password
     *
     * @param username the username for login
     * @param password the password for login
     * @param uuid     the verification code UUID from request header
     * @return login token information
     */
    @ApiOperation(value = "登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, paramType = "query"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "query")
    })
    @PostMapping("/userLogin")
    public ResultMessage<Object> userLogin(@NotNull(message = "用户名不能为空") @RequestParam String username,
                                           @NotNull(message = "密码不能为空") @RequestParam String password, @RequestHeader String uuid) {
        if (verificationService.check(uuid, VerificationEnums.LOGIN)) {
            return ResultUtil.data(this.memberService.usernameStoreLogin(username, password));
        } else {
            throw new ServiceException(ResultCode.VERIFICATION_ERROR);
        }
    }

    /**
     * User login with SMS verification code
     *
     * @param mobile the mobile phone number
     * @param code   the SMS verification code
     * @param uuid   the verification code UUID from request header
     * @return login token information
     */
    @ApiOperation(value = "短信登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mobile", value = "手机号", required = true, paramType = "query"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, paramType = "query")
    })
    @PostMapping("/smsLogin")
    public ResultMessage<Object> smsLogin(@NotNull(message = "手机号为空") @RequestParam String mobile,
                                          @NotNull(message = "验证码为空") @RequestParam String code,
                                          @RequestHeader String uuid) {
        if (smsUtil.verifyCode(mobile, VerificationEnums.LOGIN, uuid, code)) {
            return ResultUtil.data(memberService.mobilePhoneStoreLogin(mobile));
        } else {
            throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
        }
    }

    /**
     * User logout
     *
     * @return success result
     */
    @ApiOperation(value = "注销接口")
    @PostMapping("/logout")
    public ResultMessage<Object> logout() {
        this.memberService.logout(UserEnums.STORE);
        return ResultUtil.success();
    }

    /**
     * Reset password via SMS verification
     * Verifies SMS code and caches member info for password reset (valid for 3 minutes)
     *
     * @param mobile the mobile phone number
     * @param code   the SMS verification code
     * @param uuid   the verification code UUID from request header
     * @return success result
     */
    @ApiOperation(value = "通过短信重置密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mobile", value = "手机号", required = true, paramType = "query"),
            @ApiImplicitParam(name = "password", value = "是否保存登录", required = true, paramType = "query")
    })
    @PostMapping("/resetByMobile")
    public ResultMessage<Member> resetByMobile(@NotNull(message = "手机号为空") @RequestParam String mobile,
                                               @NotNull(message = "验证码为空") @RequestParam String code,
                                               @RequestHeader String uuid) {
        // Verify if SMS verification code is correct
        if (smsUtil.verifyCode(mobile, VerificationEnums.FIND_USER, uuid, code)) {
            // Verify if member exists by mobile, cache member info for 3 minutes if exists
            memberService.findByMobile(uuid, mobile);
            return ResultUtil.success();
        } else {
            throw new ServiceException(ResultCode.VERIFICATION_SMS_CHECKED_ERROR);
        }
    }
    /**
     * Reset password after SMS verification
     *
     * @param password the new password to set
     * @param uuid     the verification code UUID from request header
     * @return updated member information
     */
    @ApiOperation(value = "修改密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "query")
    })
    @PostMapping("/resetPassword")
    public ResultMessage<Object> resetByMobile(@NotNull(message = "密码为空") @RequestParam String password, @RequestHeader String uuid) {
        return ResultUtil.data(memberService.resetByMobile(uuid, password));
    }

    /**
     * Modify password with old password verification
     *
     * @param password    the current password for verification
     * @param newPassword the new password to set
     * @return updated member information
     */
    @ApiOperation(value = "修改密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "password", value = "旧密码", required = true, paramType = "query"),
            @ApiImplicitParam(name = "newPassword", value = "新密码", required = true, paramType = "query")
    })
    @PostMapping("/modifyPass")
    public ResultMessage<Member> modifyPass(@NotNull(message = "旧密码不能为空") @RequestParam String password,
                                            @NotNull(message = "新密码不能为空") @RequestParam String newPassword) {
        AuthUser tokenUser = UserContext.getCurrentUser();
        if (tokenUser == null) {
            throw new ServiceException(ResultCode.USER_NOT_LOGIN);
        }
        return ResultUtil.data(memberService.modifyPass(password, newPassword));
    }

    /**
     * Refresh access token using refresh token
     *
     * @param refreshToken the refresh token
     * @return new token information
     */
    @ApiOperation(value = "刷新token")
    @GetMapping("/refresh/{refreshToken}")
    public ResultMessage<Object> refreshToken(@NotNull(message = "刷新token不能为空") @PathVariable String refreshToken) {
        return ResultUtil.data(this.memberService.refreshStoreToken(refreshToken));
    }
}