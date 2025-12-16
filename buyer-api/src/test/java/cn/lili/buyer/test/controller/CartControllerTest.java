package cn.lili.buyer.test.controller;

import cn.lili.modules.order.cart.entity.dto.TradeDTO;
import cn.lili.modules.order.cart.entity.enums.CartTypeEnum;
import cn.lili.modules.order.cart.entity.vo.TradeParams;
import cn.lili.modules.order.cart.service.CartService;
import cn.lili.modules.order.order.entity.dos.Trade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("购物车接口集成测试")
class CartControllerTest extends BaseControllerTest {

    @MockBean
    private CartService cartService;

    @Test
    @DisplayName("添加商品到购物车")
    void testAddToCart() throws Exception {
        doNothing().when(cartService).add(anyString(), any(), anyString(), anyBoolean());

        mockMvc.perform(post("/buyer/trade/carts")
                        .param("skuId", "sku123")
                        .param("num", "2")
                        .param("cartType", "CART")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("获取购物车详情")
    void testGetCartAll() throws Exception {
        TradeDTO tradeDTO = new TradeDTO();
        when(cartService.getAllTradeDTO()).thenReturn(tradeDTO);

        mockMvc.perform(get("/buyer/trade/carts/all")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("获取购物车数量")
    void testGetCartCount() throws Exception {
        when(cartService.getCartNum(any())).thenReturn(5L);

        mockMvc.perform(get("/buyer/trade/carts/count")
                        .param("checked", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").value(5));
    }

    @Test
    @DisplayName("获取可用优惠券数量")
    void testGetCouponNum() throws Exception {
        when(cartService.getCanUseCoupon(any())).thenReturn(3L);

        mockMvc.perform(get("/buyer/trade/carts/coupon/num")
                        .param("way", "CART")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").value(3));
    }

    @Test
    @DisplayName("更新购物车商品数量")
    void testUpdateSkuNum() throws Exception {
        doNothing().when(cartService).add(anyString(), any(), eq(CartTypeEnum.CART.name()), eq(true));

        mockMvc.perform(post("/buyer/trade/carts/sku/num/{skuId}", "sku123")
                        .param("num", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("更新商品选中状态")
    void testUpdateSkuChecked() throws Exception {
        doNothing().when(cartService).checked(anyString(), anyBoolean());

        mockMvc.perform(post("/buyer/trade/carts/sku/checked/{skuId}", "sku123")
                        .param("checked", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("全选/取消全选购物车")
    void testUpdateAllChecked() throws Exception {
        doNothing().when(cartService).checkedAll(anyBoolean());

        mockMvc.perform(post("/buyer/trade/carts/sku/checked")
                        .param("checked", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("选中/取消选中店铺商品")
    void testUpdateStoreChecked() throws Exception {
        doNothing().when(cartService).checkedStore(anyString(), anyBoolean());

        mockMvc.perform(post("/buyer/trade/carts/store/{storeId}", "store123")
                        .param("checked", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("清空购物车")
    void testCleanCart() throws Exception {
        doNothing().when(cartService).clean();

        mockMvc.perform(delete("/buyer/trade/carts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("删除购物车商品")
    void testDeleteSku() throws Exception {
        doNothing().when(cartService).delete(any(String[].class));

        mockMvc.perform(delete("/buyer/trade/carts/sku/remove")
                        .param("skuIds", "sku1", "sku2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("获取结算页面购物车详情")
    void testGetCheckedCart() throws Exception {
        TradeDTO tradeDTO = new TradeDTO();
        when(cartService.getCheckedTradeDTO(any())).thenReturn(tradeDTO);

        mockMvc.perform(get("/buyer/trade/carts/checked")
                        .param("way", "CART")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("选择收货地址")
    void testSelectShippingAddress() throws Exception {
        doNothing().when(cartService).shippingAddress(anyString(), anyString());

        mockMvc.perform(get("/buyer/trade/carts/shippingAddress")
                        .param("shippingAddressId", "addr123")
                        .param("way", "CART")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("选择自提地址")
    void testSelectStoreAddress() throws Exception {
        doNothing().when(cartService).shippingSelfAddress(anyString(), anyString());

        mockMvc.perform(get("/buyer/trade/carts/storeAddress")
                        .param("storeAddressId", "storeAddr123")
                        .param("way", "CART")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("选择配送方式")
    void testSelectShippingMethod() throws Exception {
        doNothing().when(cartService).shippingMethod(anyString(), anyString());

        mockMvc.perform(put("/buyer/trade/carts/shippingMethod")
                        .param("shippingMethod", "LOGISTICS")
                        .param("way", "CART")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("获取可选物流方式")
    void testGetShippingMethodList() throws Exception {
        when(cartService.shippingMethodList(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/buyer/trade/carts/shippingMethodList")
                        .param("way", "CART")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("选择优惠券")
    void testSelectCoupon() throws Exception {
        doNothing().when(cartService).selectCoupon(anyString(), anyString(), anyBoolean());

        mockMvc.perform(get("/buyer/trade/carts/select/coupon")
                        .param("way", "CART")
                        .param("memberCouponId", "coupon123")
                        .param("used", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("创建交易订单")
    void testCreateTrade() throws Exception {
        Trade trade = new Trade();
        trade.setSn("T202312001");
        when(cartService.createTrade(any(TradeParams.class))).thenReturn(trade);

        TradeParams tradeParams = new TradeParams();

        mockMvc.perform(post("/buyer/trade/carts/create/trade")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tradeParams)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
