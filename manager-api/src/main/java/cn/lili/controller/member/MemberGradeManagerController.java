package cn.lili.controller.member;

import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.MemberGrade;
import cn.lili.modules.member.service.MemberGradeService;
import cn.lili.mybatis.util.PageUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端,会员等级接口
 * 提供会员等级的增删改查功能,用于管理会员等级体系
 *
 * @author Bulbasaur
 * @since 2021/5/16 11:29 下午
 */
@RestController
@Api(tags = "管理端,会员等级接口")
@RequestMapping("/manager/member/memberGrade")
public class MemberGradeManagerController {

    /**
     * 会员等级服务
     */
    @Autowired
    private MemberGradeService memberGradeService;

    /**
     * 通过ID获取会员等级详情
     *
     * @param id 会员等级ID
     * @return 返回会员等级详细信息
     */
    @ApiOperation(value = "通过id获取会员等级")
    @ApiImplicitParam(name = "id", value = "会员等级ID", required = true, dataType = "String", paramType = "path")
    @GetMapping(value = "/get/{id}")
    public ResultMessage<MemberGrade> get(@PathVariable String id) {

        return ResultUtil.data(memberGradeService.getById(id));
    }

    /**
     * 分页获取会员等级列表
     *
     * @param page 分页参数,包含页码、每页条数等信息
     * @return 返回分页后的会员等级列表
     */
    @ApiOperation(value = "获取会员等级分页")
    @GetMapping(value = "/getByPage")
    public ResultMessage<IPage<MemberGrade>> getByPage(PageVO page) {

        return ResultUtil.data(memberGradeService.page(PageUtil.initPage(page)));
    }

    /**
     * 添加会员等级
     * 创建新的会员等级,包括等级名称、成长值要求、折扣等信息
     *
     * @param memberGrade 会员等级实体,需进行校验
     * @return 返回添加结果
     */
    @ApiOperation(value = "添加会员等级")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "会员等级ID", required = true, paramType = "path")
    })
    @PostMapping(value = "/add")
    public ResultMessage<Object> daa(@Validated MemberGrade memberGrade) {
        if (memberGradeService.save(memberGrade)) {
            return ResultUtil.success(ResultCode.SUCCESS);
        }
        return ResultUtil.error(ResultCode.ERROR);
    }

    /**
     * 修改会员等级
     * 更新指定ID的会员等级信息
     *
     * @param id 会员等级ID
     * @param memberGrade 更新的会员等级信息
     * @return 返回修改结果
     */
    @ApiOperation(value = "修改会员等级")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "会员等级ID", required = true, paramType = "path")
    })
    @PutMapping(value = "/update/{id}")
    public ResultMessage<Object> update(@PathVariable String id, MemberGrade memberGrade) {
        if (memberGradeService.updateById(memberGrade)) {
            return ResultUtil.success(ResultCode.SUCCESS);
        }
        return ResultUtil.error(ResultCode.ERROR);
    }


    /**
     * 删除会员等级
     * 根据ID删除指定的会员等级
     * 注意:删除前需确认该等级下没有关联会员
     *
     * @param id 会员等级ID
     * @return 返回删除结果
     */
    @ApiOperation(value = "删除会员等级")
    @ApiImplicitParam(name = "id", value = "会员等级ID", required = true, dataType = "String", paramType = "path")
    @DeleteMapping(value = "/delete/{id}")
    public ResultMessage<IPage<Object>> delete(@PathVariable String id) {
        if (memberGradeService.removeById(id)) {
            return ResultUtil.success(ResultCode.SUCCESS);
        }
        return ResultUtil.error(ResultCode.ERROR);
    }
}