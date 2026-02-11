package cn.lili.controller.store.seat;

import cn.lili.common.enums.ResultUtil;
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
 * Store Manager Seat Controller
 * Provides seat management functionality for store managers
 * 
 * @author Chopper
 * @version v1.0
 * 2022-02-10 11:50
 */
@RestController
@Api(tags = "店铺端,坐席管理")
@RequestMapping("/manager/seat/setting")
@Transactional(rollbackFor = Exception.class)
public class SeatStoreManagerController {

    /**
     * Seat service for handling seat-related business logic
     */
    @Autowired
    private SeatService seatService;

    /**
     * Get the list of seats for a specific store
     * 
     * @param storeId The ID of the store to query seats for
     * @return ResultMessage containing a list of SeatVO objects
     */
    @ApiOperation(value = "查看店铺坐席列表")
    @GetMapping("/list")
    public ResultMessage<List<SeatVO>> getSeats(String storeId) {
        return ResultUtil.data(seatService.seatVoList(storeId));
    }

}