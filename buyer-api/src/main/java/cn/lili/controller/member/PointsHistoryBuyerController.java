package cn.lili.controller.member;

import cn.lili.common.security.context.UserContext;
import cn.lili.mybatis.util.PageUtil;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.MemberPointsHistory;
import cn.lili.modules.member.entity.vo.MemberPointsHistoryVO;
import cn.lili.modules.member.service.MemberPointsHistoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 买家端,会员积分历史接口
 *
 * @author Bulbasaur
 * @since 2020-02-25 14:10:16
 */
@RestController
@Api(tags = "买家端,会员积分历史接口")
@RequestMapping("/buyer/member/memberPointsHistory")
public class PointsHistoryBuyerController {
    /**
     * 会员积分历史服务
     */
    @Autowired
    private MemberPointsHistoryService memberPointsHistoryService;

    /**
     * 分页获取会员积分历史记录
     * @param page 分页参数
     * @return 积分历史记录分页数据
     */
    @ApiOperation(value = "分页获取")
    @GetMapping(value = "/getByPage")
    public ResultMessage<IPage<MemberPointsHistory>> getByPage(PageVO page) {
        // 构建查询条件
        LambdaQueryWrapper<MemberPointsHistory> queryWrapper = Wrappers.lambdaQuery();
        // 查询当前登录用户的积分历史
        queryWrapper.eq(MemberPointsHistory::getMemberId, UserContext.getCurrentUser().getId());
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(MemberPointsHistory::getCreateTime);
        // 分页查询并返回结果
        return ResultUtil.data(memberPointsHistoryService.page(PageUtil.initPage(page), queryWrapper));
    }

    /**
     * 获取会员积分统计信息
     * @return 会员积分历史统计VO对象
     */
    @ApiOperation(value = "获取会员积分VO")
    @GetMapping(value = "/getMemberPointsHistoryVO")
    public ResultMessage<MemberPointsHistoryVO> getMemberPointsHistoryVO() {
        // 获取当前登录用户的积分统计信息
        return ResultUtil.data(memberPointsHistoryService.getMemberPointsHistoryVO(UserContext.getCurrentUser().getId()));
    }


}