package cn.lili.controller.member;

import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.member.entity.dos.MemberAddress;
import cn.lili.modules.member.service.MemberAddressService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 管理端,会员地址API
 * 提供会员收货地址的增删改查功能,用于管理员查看和维护会员的收货地址信息
 *
 * @author Bulbasaur
 * @since 2020-02-25 14:10:16
 */
@RestController
@Api(tags = "管理端,会员地址API")
@RequestMapping("/manager/member/address")
public class MemberAddressManagerController {
    /**
     * 会员地址服务
     */
    @Autowired
    private MemberAddressService memberAddressService;

    /**
     * 分页获取指定会员的地址列表
     *
     * @param page 分页参数,包含页码、每页条数等信息
     * @param memberId 会员ID,用于查询该会员的所有收货地址
     * @return 返回分页后的会员地址列表
     */
    @ApiOperation(value = "会员地址分页列表")
    @GetMapping("/{memberId}")
    public ResultMessage<IPage<MemberAddress>> getByPage(PageVO page, @PathVariable("memberId") String memberId) {
        return ResultUtil.data(memberAddressService.getAddressByMember(page, memberId));
    }

    /**
     * 删除会员收件地址
     * 使用防重复提交注解,避免短时间内重复删除操作
     *
     * @param id 会员地址ID
     * @return 返回删除结果
     */
    @PreventDuplicateSubmissions
    @ApiOperation(value = "删除会员收件地址")
    @ApiImplicitParam(name = "id", value = "会员地址ID", dataType = "String", paramType = "path")
    @DeleteMapping(value = "/delById/{id}")
    public ResultMessage<Object> delShippingAddressById(@PathVariable String id) {
        memberAddressService.removeMemberAddress(id);
        return ResultUtil.success();
    }

    /**
     * 修改会员收件地址
     * 更新会员地址信息,包括收件人、联系电话、详细地址等
     *
     * @param shippingAddress 会员地址对象,需进行数据校验
     * @return 返回更新后的会员地址信息
     */
    @ApiOperation(value = "修改会员收件地址")
    @PutMapping
    public ResultMessage<MemberAddress> editShippingAddress(@Valid MemberAddress shippingAddress) {
        //修改会员地址
        return ResultUtil.data(memberAddressService.updateMemberAddress(shippingAddress));
    }

    /**
     * 新增会员收件地址
     * 为会员添加新的收货地址
     * 使用防重复提交注解,避免短时间内重复添加相同地址
     *
     * @param shippingAddress 会员地址对象,需进行数据校验
     * @return 返回新增的会员地址信息
     */
    @PreventDuplicateSubmissions
    @ApiOperation(value = "新增会员收件地址")
    @PostMapping
    public ResultMessage<MemberAddress> addShippingAddress(@Valid MemberAddress shippingAddress) {
        //添加会员地址
        return ResultUtil.data(memberAddressService.saveMemberAddress(shippingAddress));
    }


}