package cn.lili.buyer.test.controller;

import cn.lili.modules.goods.service.GoodsService;
import cn.lili.modules.goods.service.GoodsSkuService;
import cn.lili.modules.search.service.EsGoodsSearchService;
import cn.lili.modules.search.service.HotWordsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("商品接口集成测试")
class GoodsBuyerControllerTest extends BaseControllerTest {

    @MockBean
    private GoodsService goodsService;

    @MockBean
    private GoodsSkuService goodsSkuService;

    @MockBean
    private EsGoodsSearchService esGoodsSearchService;

    @MockBean
    private HotWordsService hotWordsService;

    @Test
    @DisplayName("通过ID获取商品信息")
    void testGetGoodsById() throws Exception {
        when(goodsService.getGoodsVO(anyString())).thenReturn(null);

        mockMvc.perform(get("/buyer/goods/goods/get/{goodsId}", "123456")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("获取商品SKU详情")
    void testGetSkuDetail() throws Exception {
        Map<String, Object> skuDetail = new HashMap<>();
        skuDetail.put("id", "sku123");
        skuDetail.put("goodsId", "goods123");
        when(goodsSkuService.getGoodsSkuDetail(anyString(), anyString())).thenReturn(skuDetail);

        mockMvc.perform(get("/buyer/goods/goods/sku/{goodsId}/{skuId}", "goods123", "sku123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result.id").value("sku123"));
    }

    @Test
    @DisplayName("获取商品分页列表")
    void testGetGoodsPage() throws Exception {
        when(goodsService.queryByParams(any())).thenReturn(null);

        mockMvc.perform(get("/buyer/goods/goods")
                        .param("pageNumber", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("获取商品SKU列表")
    void testGetSkuList() throws Exception {
        when(goodsSkuService.getGoodsSkuByList(any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/buyer/goods/goods/sku")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("获取搜索热词")
    void testGetHotWords() throws Exception {
        when(hotWordsService.getHotWords(anyInt())).thenReturn(Arrays.asList("手机", "电脑", "平板"));

        mockMvc.perform(get("/buyer/goods/goods/hot-words")
                        .param("count", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result[0]").value("手机"));
    }

    @Test
    @DisplayName("从ES中获取商品信息")
    void testGetGoodsFromEs() throws Exception {
        when(esGoodsSearchService.searchGoodsByPage(any(), any())).thenReturn(null);

        mockMvc.perform(get("/buyer/goods/goods/es")
                        .param("pageNumber", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("从ES中获取相关商品信息")
    void testGetGoodsRelatedFromEs() throws Exception {
        when(esGoodsSearchService.getSelector(any(), any())).thenReturn(null);

        mockMvc.perform(get("/buyer/goods/goods/es/related")
                        .param("pageNumber", "1")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
