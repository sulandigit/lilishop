package cn.lili.controller.store.seat;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.im.entity.vo.SeatVO;
import cn.lili.modules.im.service.SeatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Store Seat Controller
 * Provides seat management functionality for store operators
 * Automatically retrieves store information from the current logged-in user context
 * 
 * @author Chopper
 * @version v1.0
 * 2022-02-10 11:50
 */
@RestController
@Api(tags = "店铺端,坐席管理")
@RequestMapping("/store/seat/setting")
@Transactional(rollbackFor = Exception.class)
public class SeatStoreController {

    /**
     * Seat service for handling seat-related business logic
     */
    @Autowired
    private SeatService seatService;

    /**
     * Get paginated list of seats for the current store
     * Retrieves the store/tenant ID from the current logged-in user's context
     * 
     * @return ResultMessage containing a list of SeatVO objects for the current store
     */
    @ApiOperation(value = "分页获取坐席")
    @GetMapping("/list")
    public ResultMessage<List<SeatVO>> getSeats() {
        return ResultUtil.data(seatService.seatVoList(UserContext.getCurrentUser().getTenantId()));
    }


}