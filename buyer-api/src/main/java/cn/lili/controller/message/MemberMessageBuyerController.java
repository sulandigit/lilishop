package cn.lili.controller.message;

import cn.lili.common.enums.ResultUtil;
import cn.lili.common.security.context.UserContext;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.message.entity.dos.MemberMessage;
import cn.lili.modules.message.entity.enums.MessageStatusEnum;
import cn.lili.modules.message.entity.vos.MemberMessageQueryVO;
import cn.lili.modules.message.service.MemberMessageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 买家端,会员站内消息接口
 *
 * @author Bulbasaur
 * @since 2020/11/16 10:07 下午
 */
@RestController
@Api(tags = "买家端,会员站内消息接口")
@RequestMapping("/buyer/message/member")
public class MemberMessageBuyerController {

    /**
     * 会员站内消息
     */
    @Autowired
    private MemberMessageService memberMessageService;

    /**
     * 分页获取会员站内消息
     * <p>
     * 根据查询条件分页查询当前登录会员的站内消息列表，
     * 自动设置当前登录用户ID作为查询条件
     * </p>
     *
     * @param memberMessageQueryVO 会员消息查询条件对象，包含消息状态、标题等筛选条件
     * @param page                 分页参数，包含页码和每页数量
     * @return 分页后的会员站内消息列表
     */
    @ApiOperation(value = "分页获取会员站内消息")
    @GetMapping
    public ResultMessage<IPage<MemberMessage>> page(MemberMessageQueryVO memberMessageQueryVO, PageVO page) {
        memberMessageQueryVO.setMemberId(UserContext.getCurrentUser().getId());
        return ResultUtil.data(memberMessageService.getPage(memberMessageQueryVO, page));
    }

    /**
     * 标记消息为已读状态
     * <p>
     * 将指定的会员消息状态更新为已读（ALREADY_READY）
     * </p>
     *
     * @param messageId 会员消息ID
     * @return 操作结果，true表示更新成功，false表示更新失败
     */
    @ApiOperation(value = "消息已读")
    @ApiImplicitParam(name = "messageId", value = "会员消息id", required = true, paramType = "path")
    @PutMapping("/{message_id}")
    public ResultMessage<Boolean> read(@PathVariable("message_id") String messageId) {
        return ResultUtil.data(memberMessageService.editStatus(MessageStatusEnum.ALREADY_READY.name(), messageId));
    }

    /**
     * 将消息放入回收站
     * <p>
     * 将指定的会员消息状态更新为已删除（ALREADY_REMOVE），
     * 实现逻辑删除，消息会被移至回收站而非物理删除
     * </p>
     *
     * @param messageId 会员消息ID
     * @return 操作结果，true表示删除成功，false表示删除失败
     */
    @ApiOperation(value = "消息放入回收站")
    @ApiImplicitParam(name = "messageId", value = "会员消息id", required = true, paramType = "path")
    @DeleteMapping("/{message_id}")
    public ResultMessage<Boolean> deleteMessage(@PathVariable("message_id") String messageId) {
        return ResultUtil.data(memberMessageService.editStatus(MessageStatusEnum.ALREADY_REMOVE.name(), messageId));

    }


}
