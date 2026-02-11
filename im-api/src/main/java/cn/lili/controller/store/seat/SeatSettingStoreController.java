package cn.lili.controller.store.seat;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.im.entity.dos.SeatSetting;
import cn.lili.modules.im.service.SeatSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Store Seat Settings Controller
 * Manages seat configuration settings for stores
 * Handles retrieval and updates of seat-related configuration parameters
 *
 * @author pikachu
 * @since 2020-02-18 15:18:56
 */
@RestController
@Api(tags = "店铺端,坐席设置")
@RequestMapping("/store/seat/setting")
@Transactional(rollbackFor = Exception.class)
public class SeatSettingStoreController {

    /**
     * Seat setting service for handling seat configuration logic
     */
    @Autowired
    private SeatSettingService seatSettingService;

    /**
     * Query seat settings for the current store
     * Retrieves the tenant ID from the current logged-in user's context
     * 
     * @return ResultMessage containing the SeatSetting configuration object
     */
    @ApiOperation(value = "查询坐席设置")
    @GetMapping
    public ResultMessage<SeatSetting> getSetting() {
        return ResultUtil.data(seatSettingService.getSetting(UserContext.getCurrentUser().getTenantId()));
    }

    /**
     * Update seat settings for the current store
     * Automatically sets the tenant ID from the current user context before updating
     * 
     * @param seatSetting The seat setting object containing updated configuration values
     */
    @ApiOperation(value = "更新坐席设置")
    @PutMapping
    public void update(SeatSetting seatSetting) {
        seatSetting.setTenantId(UserContext.getCurrentUser().getTenantId());
        seatSettingService.updateByStore(seatSetting);
    }
}