package cn.lili.controller.store.seat;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.im.service.SeatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Seat Login Controller
 * Handles authentication and login operations for customer service seats
 * Provides multiple login methods including username/password and quick login
 *
 * @author Chopper
 * @version v1.0
 * 2022-02-10 16:40
 */
@Slf4j
@RestController
@Api(tags = "坐席端")
@RequestMapping("/seat/login")
public class SeatLogin {

    /**
     * Seat service for handling seat authentication and management
     */
    @Autowired
    private SeatService seatService;

    /**
     * Standard login interface for seats using username and password
     * 
     * @param username The username of the seat account
     * @param password The password of the seat account
     * @return ResultMessage containing the authentication token and user information
     */
    @ApiOperation(value = "登录接口")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "username", value = "用户名", required = true, paramType = "query"),
            @ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "query")
    })
    @PostMapping("/userLogin")
    public ResultMessage<Object> userLogin(String username, String password) {
        return ResultUtil.data(this.seatService.usernameLogin(username, password));
    }

    /**
     * Quick login interface for merchants to access customer service
     * Allows store owners/managers to quickly log in as a customer service seat
     * 
     * @param code The quick login verification code
     * @return ResultMessage containing the authentication token and user information
     */
    @ApiOperation(value = "商家快捷登录客服")
    @PostMapping("/quicklogin")
    public ResultMessage<Object> quickLogin(String code) {
        return ResultUtil.data(this.seatService.quickLogin(code));
    }

    /**
     * Logout interface for seats
     * Terminates the current seat session and invalidates the authentication token
     * TODO: Implement actual logout logic with user context cleanup
     * 
     * @return ResultMessage indicating successful logout
     */
    @ApiOperation(value = "登出")
    @PostMapping("/logout")
    public ResultMessage<Object> logout() {
        //todo
//        UserContext.getCurrentUser().getId()
//        verificationServiceClient.check(uuid);
        return ResultUtil.success();
    }


}