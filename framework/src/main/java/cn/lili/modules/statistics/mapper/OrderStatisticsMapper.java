package cn.lili.modules.statistics.mapper;

import cn.lili.modules.order.order.entity.dos.Order;
import cn.lili.modules.order.order.entity.dos.OrderItem;
import cn.lili.modules.order.order.entity.vo.OrderSimpleVO;
import cn.lili.modules.statistics.entity.vo.OrderSourceAnalysisVO;
import cn.lili.modules.statistics.entity.vo.OrderStatisticsDataVO;
import cn.lili.modules.statistics.entity.vo.OrderTimeDistributionVO;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

/**
 * 订单统计数据处理层
 *
 * @author Bulbasaur
 * @since 2020/11/17 7:34 下午
 */
public interface OrderStatisticsMapper extends BaseMapper<Order> {

    /**
     * 获取订单统计数据
     *
     * @param queryWrapper 查询条件
     * @return 订单统计列表
     */
    @Select("SELECT DATE_FORMAT(create_time,'%Y-%m-%d') AS create_time,sum(flow_price) AS price FROM li_order " +
            " ${ew.customSqlSegment}")
    List<OrderStatisticsDataVO> getOrderStatisticsData(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 订单数量
     *
     * @param queryWrapper 查询条件
     * @return 订单数量
     */
    @Select("SELECT count(0) FROM li_order ${ew.customSqlSegment}")
    Integer count(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 查询订单简短信息分页
     *
     * @param page         分页
     * @param queryWrapper 查询条件
     * @return 简短订单分页
     */
    @Select("select o.sn,o.flow_price,o.create_time,o.order_status,o.pay_status,o.payment_method,o.payment_time,o.member_name,o.store_name as store_name,o.store_id as store_id,o.client_type,o.order_type,o.deliver_status " +
            ",GROUP_CONCAT(oi.goods_id) as group_goods_id," +
            " GROUP_CONCAT(oi.sku_id) as group_sku_id," +
            " GROUP_CONCAT(oi.num) as group_num" +
            ",GROUP_CONCAT(oi.image) as group_images" +
            ",GROUP_CONCAT(oi.goods_name) as group_name " +
            ",GROUP_CONCAT(oi.after_sale_status) as group_after_sale_status" +
            ",GROUP_CONCAT(oi.complain_status) as group_complain_status" +
            ",GROUP_CONCAT(oi.comment_status) as group_comment_status" +
            ",GROUP_CONCAT(oi.sn) as group_order_items_sn " +
            ",GROUP_CONCAT(oi.goods_price) as group_goods_price " +
            " FROM li_order o INNER JOIN li_order_item AS oi on o.sn = oi.order_sn ${ew.customSqlSegment} ")
    IPage<OrderSimpleVO> queryByParams(IPage<OrderSimpleVO> page, @Param(Constants.WRAPPER) Wrapper<OrderSimpleVO> queryWrapper);


    /**
     * 查询已付款未全部退款的订单数量
     */
    @Select("SELECT COALESCE(COUNT(DISTINCT order_sn), 0)  FROM li_order_item ${ew.customSqlSegment} ")
    Long getPayOrderNum(@Param(Constants.WRAPPER) Wrapper<OrderItem> queryWrapper);
    /**
     * 查询已付款未全部退款的订单金额
     */
    @Select("SELECT COALESCE(SUM( oi.flow_price )- SUM( oi.refund_price ), 0) FROM li_order_item oi INNER JOIN li_order o ON o.sn=oi.order_sn ${ew.customSqlSegment} ")
    Double getPayOrderPrice(@Param(Constants.WRAPPER) Wrapper<OrderItem> queryWrapper);
    
    /**
     * 查询商品价格
     */
    @Select("SELECT COALESCE(SUM(goods_price), 0) FROM li_order_item ${ew.customSqlSegment} ")
    Double getGoodsPrice(@Param(Constants.WRAPPER) Wrapper<OrderItem> queryWrapper);
    
    @Select("SELECT COALESCE(SUM( oi.refund_price ), 0) FROM li_order_item oi INNER JOIN li_order o ON o.sn=oi.order_sn ${ew.customSqlSegment} ")
    Double getRefundPrice(@Param(Constants.WRAPPER) Wrapper<OrderItem> queryWrapper);

    /**
     * 统计复购客户数（购买次数>=2的客户）
     *
     * @param queryWrapper 查询条件
     * @return 复购客户数
     */
    @Select("SELECT COUNT(*) FROM (" +
            "SELECT member_id FROM li_order " +
            "WHERE pay_status = 'PAID' AND delete_flag = 0 ${ew.customSqlSegment} " +
            "GROUP BY member_id HAVING COUNT(id) >= 2) AS repeat_customers")
    Long countRepeatCustomers(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 统计新客户数（在指定时间段内首次购买的客户）
     *
     * @param queryWrapper 查询条件
     * @param startDate    统计开始时间
     * @return 新客户数
     */
    @Select("SELECT COUNT(DISTINCT member_id) FROM li_order " +
            "WHERE pay_status = 'PAID' AND delete_flag = 0 ${ew.customSqlSegment} " +
            "AND member_id NOT IN (" +
            "SELECT DISTINCT member_id FROM li_order " +
            "WHERE pay_status = 'PAID' AND delete_flag = 0 AND create_time < #{startDate})")
    Long countNewCustomers(@Param(Constants.WRAPPER) Wrapper queryWrapper, @Param("startDate") Date startDate);

    /**
     * 统计付款客户数
     *
     * @param queryWrapper 查询条件
     * @return 付款客户数
     */
    @Select("SELECT COUNT(DISTINCT member_id) FROM li_order " +
            "WHERE pay_status = 'PAID' AND delete_flag = 0 ${ew.customSqlSegment}")
    Long countPaymentCustomers(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 统计订单商品总数量
     *
     * @param queryWrapper 查询条件
     * @return 商品总数量
     */
    @Select("SELECT COALESCE(SUM(oi.num), 0) FROM li_order_item oi " +
            "INNER JOIN li_order o ON o.sn = oi.order_sn " +
            "WHERE o.pay_status = 'PAID' AND o.delete_flag = 0 ${ew.customSqlSegment}")
    Long sumGoodsNum(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 按小时统计订单分布
     *
     * @param queryWrapper 查询条件
     * @return 时段分布列表
     */
    @Select("SELECT " +
            "HOUR(create_time) AS hour, " +
            "CONCAT(LPAD(HOUR(create_time), 2, '0'), ':00-', LPAD(HOUR(create_time) + 1, 2, '0'), ':00') AS hour_range, " +
            "COUNT(*) AS order_num, " +
            "SUM(CASE WHEN pay_status = 'PAID' THEN 1 ELSE 0 END) AS payment_order_num, " +
            "COALESCE(SUM(flow_price), 0) AS order_amount, " +
            "COALESCE(SUM(CASE WHEN pay_status = 'PAID' THEN flow_price ELSE 0 END), 0) AS payment_amount " +
            "FROM li_order " +
            "WHERE delete_flag = 0 ${ew.customSqlSegment} " +
            "GROUP BY HOUR(create_time) " +
            "ORDER BY HOUR(create_time)")
    List<OrderTimeDistributionVO> getHourlyDistribution(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 按客户端类型统计订单来源
     *
     * @param queryWrapper 查询条件
     * @return 来源统计列表
     */
    @Select("SELECT " +
            "client_type AS client_type, " +
            "COUNT(*) AS order_num, " +
            "SUM(CASE WHEN pay_status = 'PAID' THEN 1 ELSE 0 END) AS payment_order_num, " +
            "COALESCE(SUM(flow_price), 0) AS order_amount, " +
            "COALESCE(SUM(CASE WHEN pay_status = 'PAID' THEN flow_price ELSE 0 END), 0) AS payment_amount " +
            "FROM li_order " +
            "WHERE delete_flag = 0 ${ew.customSqlSegment} " +
            "GROUP BY client_type " +
            "ORDER BY order_amount DESC")
    List<OrderSourceAnalysisVO> getClientTypeStatistics(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 获取订单总数
     *
     * @param queryWrapper 查询条件
     * @return 订单总数
     */
    @Select("SELECT COUNT(*) FROM li_order WHERE delete_flag = 0 ${ew.customSqlSegment}")
    Long countOrders(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 获取订单总金额
     *
     * @param queryWrapper 查询条件
     * @return 订单总金额
     */
    @Select("SELECT COALESCE(SUM(flow_price), 0) FROM li_order WHERE delete_flag = 0 ${ew.customSqlSegment}")
    Double sumOrderAmount(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 获取付款订单数
     *
     * @param queryWrapper 查询条件
     * @return 付款订单数
     */
    @Select("SELECT COUNT(*) FROM li_order WHERE pay_status = 'PAID' AND delete_flag = 0 ${ew.customSqlSegment}")
    Long countPaymentOrders(@Param(Constants.WRAPPER) Wrapper queryWrapper);

    /**
     * 获取付款订单金额
     *
     * @param queryWrapper 查询条件
     * @return 付款订单金额
     */
    @Select("SELECT COALESCE(SUM(flow_price), 0) FROM li_order WHERE pay_status = 'PAID' AND delete_flag = 0 ${ew.customSqlSegment}")
    Double sumPaymentAmount(@Param(Constants.WRAPPER) Wrapper queryWrapper);
}