package cn.lili.controller.other.broadcast;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.goods.entity.vos.StudioVO;
import cn.lili.modules.goods.service.StudioService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 买家端,直播间接口
 * 提供买家端直播间相关功能,包括直播间列表查询和直播回放信息获取
 *
 * @author Bulbasaur
 * @since 2021/5/20 12:03 下午
 */
@RestController
@Api(tags = "买家端,直播间接口")
@RequestMapping("/buyer/broadcast/studio")
public class StudioController {

    /**
     * 直播间服务
     */
    @Autowired
    private StudioService studioService;

    /**
     * 获取店铺直播间列表
     * 支持按推荐状态和直播间状态进行筛选
     *
     * @param pageVO 分页参数对象,包含当前页码和每页大小
     * @param recommend 是否推荐(可选):用于筛选推荐的直播间
     * @param status 直播间状态(可选):用于筛选特定状态的直播间,如进行中、已结束等
     * @return 返回分页的直播间列表数据
     */
    @ApiOperation(value = "获取店铺直播间列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "recommend", value = "是否推荐", paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "status", value = "直播间状态", paramType = "query", dataType = "String")
    })
    @GetMapping
    public ResultMessage<IPage<StudioVO>> page(PageVO pageVO, Integer recommend, String status) {
        return ResultUtil.data(studioService.studioList(pageVO, recommend, status));
    }

    /**
     * 获取店铺直播间回放地址
     * 根据房间ID获取直播回放的详细信息
     *
     * @param roomId 直播间房间ID
     * @return 返回直播回放信息,包括回放地址等相关数据
     */
    @ApiOperation(value = "获取店铺直播间回放地址")
    @GetMapping("/getLiveInfo/{roomId}")
    public ResultMessage<Object> getLiveInfo(Integer roomId) {
        return ResultUtil.data(studioService.getLiveInfo(roomId));
    }

}