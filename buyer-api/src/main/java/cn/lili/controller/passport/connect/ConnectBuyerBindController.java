package cn.lili.controller.passport.connect;


import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.connect.service.ConnectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 买家端,app/小程序 联合登录
 *
 * @author Chopper
 * @since 2020-11-25 19:29
 */
@RestController
@Api(tags = "买家端,app/小程序 联合登录")
@RequestMapping("/buyer/passport/connect/bind")
public class ConnectBuyerBindController {

    private final ConnectService connectService;

    public ConnectBuyerBindController(ConnectService connectService) {
        this.connectService = connectService;
    }

    @ApiOperation(value = "unionID 绑定")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "unionID", value = "unionID", required = true, paramType = "query"),
            @ApiImplicitParam(name = "type", value = "type", required = true, paramType = "query")
    })
    @PostMapping
    public ResultMessage<Void> bindUnionId(@NotBlank(message = "unionID不能为空") @RequestParam String unionID,
                                            @NotBlank(message = "type不能为空") @RequestParam String type) {
        connectService.bind(unionID, type);
        return ResultUtil.success();
    }

    @ApiOperation(value = "unionID 解绑")
    @ApiImplicitParam(name = "type", value = "type", required = true, paramType = "query")
    @PostMapping("/unbind")
    public ResultMessage<Void> unbindUnionId(@NotBlank(message = "type不能为空") @RequestParam String type) {
        connectService.unbind(type);
        return ResultUtil.success();
    }

    @GetMapping("/list")
    @ApiOperation(value = "绑定列表")
    public ResultMessage<List<String>> bindList() {
        return ResultUtil.data(connectService.bindList());
    }

}