package cn.lili.controller.member;

import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dto.EvaluationQueryParams;
import cn.lili.modules.member.entity.vo.MemberEvaluationListVO;
import cn.lili.modules.member.entity.vo.MemberEvaluationVO;
import cn.lili.modules.member.service.MemberEvaluationService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;

/**
 * 管理端,会员商品评价接口
 * 提供商品评价的查询、状态修改和删除功能,用于管理用户对商品的评价
 *
 * @author Bulbasaur
 * @since 2020-02-25 14:10:16
 */
@RestController
@Api(tags = "管理端,会员商品评价接口")
@RequestMapping("/manager/member/evaluation")
public class MemberEvaluationManagerController {
    /**
     * 会员评价服务
     */
    @Autowired
    private MemberEvaluationService memberEvaluationService;

    /**
     * 通过ID获取评论详情
     * 使用防重复提交注解,避免短时间内重复请求
     *
     * @param id 评价ID
     * @return 返回评价详细信息的视图对象
     */
    @PreventDuplicateSubmissions
    @ApiOperation(value = "通过id获取评论")
    @ApiImplicitParam(name = "id", value = "评价ID", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/get/{id}")
    public ResultMessage<MemberEvaluationVO> get(@PathVariable String id) {

        return ResultUtil.data(memberEvaluationService.queryById(id));
    }

    /**
     * 分页获取评价列表
     * 支持多条件筛选,如商品名称、会员名称、评价等级等
     *
     * @param evaluationQueryParams 评价查询参数对象,包含筛选条件
     * @param page 分页参数,包含页码、每页条数等信息
     * @return 返回分页后的评价列表视图对象
     */
    @ApiOperation(value = "获取评价分页")
    @GetMapping(value = "/getByPage")
    public ResultMessage<IPage<MemberEvaluationListVO>> getByPage(EvaluationQueryParams evaluationQueryParams, PageVO page) {

        return ResultUtil.data(memberEvaluationService.queryPage(evaluationQueryParams));
    }

    /**
     * 修改评价显示状态
     * 管理员可以控制评价是否在前台展示,用于处理违规评价
     * 使用防重复提交注解,避免短时间内重复操作
     *
     * @param id 评价ID
     * @param status 显示状态,OPEN表示正常显示,CLOSE表示关闭不显示
     * @return 返回操作结果
     */
    @PreventDuplicateSubmissions
    @ApiOperation(value = "修改评价状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "评价ID", required = true, paramType = "path"),
            @ApiImplicitParam(name = "status", value = "显示状态,OPEN 正常 ,CLOSE 关闭", required = true, paramType = "query")
    })
    @GetMapping(value = "/updateStatus/{id}")
    public ResultMessage<Object> updateStatus(@PathVariable String id, @NotNull String status) {
        memberEvaluationService.updateStatus(id, status);
        return ResultUtil.success();
    }

    /**
     * 删除评论
     * 管理员可以删除违规或不当的评价
     *
     * @param id 评价ID
     * @return 返回删除结果
     */
    @ApiOperation(value = "删除评论")
    @ApiImplicitParam(name = "id", value = "评价ID", required = true, dataType = "String", paramType = "path")
    @PutMapping(value = "/delete/{id}")
    public ResultMessage<IPage<Object>> delete(@PathVariable String id) {
        memberEvaluationService.delete(id);
        return ResultUtil.success();
    }

}