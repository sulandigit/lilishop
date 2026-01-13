package cn.lili.controller.member;
 
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.MemberPointsHistory;
import cn.lili.modules.member.entity.vo.MemberPointsHistoryVO;
import cn.lili.modules.member.service.MemberPointsHistoryService;
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
 * 管理端,会员积分历史接口
 * 提供会员积分历史记录的查询功能,包括分页查询和统计信息
 *
 * @author Bulbasaur
 * @since 2020-02-25 14:10:16
 */
@RestController
@Api(tags = "管理端,会员积分历史接口")
@RequestMapping("/manager/member/memberPointsHistory")
public class MemberPointsHistoryManagerController {
    /**
     * 会员积分历史服务
     */
    @Autowired
    private MemberPointsHistoryService memberPointsHistoryService;

    /**
     * 分页获取会员积分历史记录列表
     * 支持根据会员ID和会员名称进行筛选查询
     *
     * @param page 分页参数,包含页码、每页条数等信息
     * @param memberId 会员ID,用于筛选指定会员的积分历史
     * @param memberName 会员名称,用于模糊查询会员
     * @return 返回分页后的会员积分历史记录列表
     */
    @ApiOperation(value = "分页获取")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberId", value = "会员ID", required = true, paramType = "query"),
            @ApiImplicitParam(name = "memberName", value = "会员名称", required = true, paramType = "query")
    })
    @GetMapping(value = "/getByPage")
    public ResultMessage<IPage<MemberPointsHistory>> getByPage(PageVO page, String memberId, String memberName) {
        return ResultUtil.data(memberPointsHistoryService.MemberPointsHistoryList(page, memberId, memberName));
    }

    /**
     * 获取会员积分历史统计信息VO
     * 返回会员的积分统计数据,如总积分、可用积分、冻结积分等
     *
     * @param memberId 会员ID
     * @return 返回会员积分历史统计视图对象
     */
    @ApiOperation(value = "获取会员积分VO")
    @ApiImplicitParam(name = "memberId", value = "会员ID", paramType = "query")
    @GetMapping(value = "/getMemberPointsHistoryVO")
    public ResultMessage<MemberPointsHistoryVO> getMemberPointsHistoryVO(String memberId) {
        return ResultUtil.data(memberPointsHistoryService.getMemberPointsHistoryVO(memberId));
    }


}