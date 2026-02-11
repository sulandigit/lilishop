package cn.lili.controller.distribution;

import cn.lili.common.aop.annotation.PreventDuplicateSubmissions;
import cn.lili.common.enums.ResultCode;
import cn.lili.common.enums.ResultUtil;
import cn.lili.common.exception.ServiceException;
import cn.lili.common.vo.PageVO;
import cn.lili.common.vo.ResultMessage;
import cn.lili.modules.distribution.entity.dos.Distribution;
import cn.lili.modules.distribution.entity.dto.DistributionSearchParams;
import cn.lili.modules.distribution.service.DistributionService;
import cn.lili.modules.goods.entity.vos.BrandVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 管理端,分销员管理接口
 * 提供分销员的查询、审核、清退、恢复和更新功能
 *
 * @author pikachu
 * @since 2020-03-14 23:04:56
 */
@RestController
@Api(tags = "管理端,分销员管理接口")
@RequestMapping("/manager/distribution/distribution")
public class DistributionManagerController {

    /**
     * 分销员服务
     */
    @Autowired
    private DistributionService distributionService;

    /**
     * 分页获取分销员列表
     *
     * @param distributionSearchParams 分销员查询参数对象,包含筛选条件
     * @param page                     分页参数对象
     * @return 分页后的分销员列表
     */
    @ApiOperation(value = "分页获取")
    @GetMapping(value = "/getByPage")
    public ResultMessage<IPage<Distribution>> getByPage(DistributionSearchParams distributionSearchParams, PageVO page) {
        return ResultUtil.data(distributionService.distributionPage(distributionSearchParams, page));
    }


    /**
     * 清退分销商
     * 将指定的分销商设置为清退状态
     *
     * @param id 分销商ID
     * @return 操作结果
     */
    @PreventDuplicateSubmissions
    @ApiOperation(value = "清退分销商")
    @PutMapping(value = "/retreat/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "分销商id", required = true, paramType = "path", dataType = "String")
    })
    public ResultMessage<Object> retreat(@PathVariable String id) {
        // 执行清退操作
        if (distributionService.retreat(id)) {
            return ResultUtil.success();
        } else {
            throw new ServiceException(ResultCode.DISTRIBUTION_RETREAT_ERROR);
        }

    }

    /**
     * 恢复分销商
     * 将已清退的分销商恢复为正常状态
     *
     * @param id 分销商ID
     * @return 操作结果
     */
    @PreventDuplicateSubmissions
    @ApiOperation(value = "恢复分销商")
    @PutMapping(value = "/resume/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "分销商id", required = true, paramType = "path", dataType = "String")
    })
    public ResultMessage<Object> resume(@PathVariable String id) {
        // 执行恢复操作
        if (distributionService.resume(id)) {
            return ResultUtil.success();
        } else {
            throw new ServiceException(ResultCode.DISTRIBUTION_RETREAT_ERROR);
        }

    }

    /**
     * 审核分销商
     * 对分销商申请进行审核,通过或拒绝
     *
     * @param id     分销商ID
     * @param status 审核结果,PASS 通过,REFUSE 拒绝
     * @return 操作结果
     */
    @PreventDuplicateSubmissions
    @ApiOperation(value = "审核分销商")
    @PutMapping(value = "/audit/{id}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "分销商id", required = true, paramType = "path", dataType = "String"),
            @ApiImplicitParam(name = "status", value = "审核结果，PASS 通过  REFUSE 拒绝", required = true, paramType = "query", dataType = "String")
    })
    public ResultMessage<Object> audit(@NotNull @PathVariable String id, @NotNull String status) {
        // 执行审核操作
        if (distributionService.audit(id, status)) {
            return ResultUtil.success();
        } else {
            throw new ServiceException(ResultCode.DISTRIBUTION_AUDIT_ERROR);
        }
    }


    /**
     * 更新分销商数据
     * 更新指定分销商的信息
     *
     * @param id           分销商ID
     * @param distribution 分销商对象,包含需要更新的字段信息
     * @return 更新后的分销商对象
     */
    @ApiOperation(value = "更新数据")
    @ApiImplicitParam(name = "id", value = "品牌ID", required = true, dataType = "String", paramType = "path")
    @PutMapping("/{id}")
    public ResultMessage<Distribution> update(@PathVariable String id, @Valid Distribution distribution) {
        // 设置ID并执行更新
        distribution.setId(id);
        if (distributionService.updateById(distribution)) {
            return ResultUtil.data(distribution);
        }
        throw new ServiceException(ResultCode.DISTRIBUTION_EDIT_ERROR);
    }
}