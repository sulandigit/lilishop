package cn.lili.buyer.test.controller;

import cn.lili.modules.goods.entity.vos.CategoryVO;
import cn.lili.modules.goods.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("商品分类接口集成测试")
class CategoryBuyerControllerTest extends BaseControllerTest {

    @MockBean
    private CategoryService categoryService;

    @Test
    @DisplayName("获取全部分类列表")
    void testListAllCategories() throws Exception {
        List<CategoryVO> categories = new ArrayList<>();
        CategoryVO category = new CategoryVO();
        category.setId("1");
        category.setName("电子产品");
        categories.add(category);

        when(categoryService.listAllChildren(anyString())).thenReturn(categories);

        mockMvc.perform(get("/buyer/goods/category/get/{parentId}", "0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result[0].name").value("电子产品"));
    }

    @Test
    @DisplayName("获取子分类列表")
    void testListChildCategories() throws Exception {
        List<CategoryVO> childCategories = new ArrayList<>();
        CategoryVO child = new CategoryVO();
        child.setId("2");
        child.setName("手机");
        child.setParentId("1");
        childCategories.add(child);

        when(categoryService.listAllChildren("1")).thenReturn(childCategories);

        mockMvc.perform(get("/buyer/goods/category/get/{parentId}", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result[0].name").value("手机"))
                .andExpect(jsonPath("$.result[0].parentId").value("1"));
    }

    @Test
    @DisplayName("获取空分类列表")
    void testListEmptyCategories() throws Exception {
        when(categoryService.listAllChildren(anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/buyer/goods/category/get/{parentId}", "999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").isEmpty());
    }
}
