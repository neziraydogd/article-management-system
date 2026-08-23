package com.na.article.controller;

import java.util.List;
import java.util.Optional;

import com.na.article.dto.CreateCategoryRequest;
import com.na.article.model.Category;
import com.na.article.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void shouldCreateCategory() throws Exception {
        Category category = createCategory(1L, "Tech", "Technology articles");
        when(categoryService.create("Tech", "Technology articles")).thenReturn(category);

        CreateCategoryRequest request = new CreateCategoryRequest("Tech", "Technology articles");

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/categories/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Tech"))
                .andExpect(jsonPath("$.description").value("Technology articles"));
    }

    @Test
    void shouldCreateCategoryWithNullDescription() throws Exception {
        Category category = createCategory(1L, "Tech", null);
        when(categoryService.create("Tech", null)).thenReturn(category);

        CreateCategoryRequest request = new CreateCategoryRequest("Tech", null);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Tech"))
                .andExpect(jsonPath("$.description").doesNotExist());
    }

    @Test
    void shouldFindCategoryById() throws Exception {
        Category category = createCategory(1L, "Tech", "Technology articles");
        when(categoryService.findById(1L)).thenReturn(Optional.of(category));

        mockMvc.perform(get("/api/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Tech"))
                .andExpect(jsonPath("$.description").value("Technology articles"));
    }

    @Test
    void shouldReturn404WhenCategoryNotFound() throws Exception {
        when(categoryService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/categories/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindAllCategories() throws Exception {
        Category cat1 = createCategory(1L, "Tech", "Technology");
        Category cat2 = createCategory(2L, "Science", null);
        when(categoryService.findAll()).thenReturn(List.of(cat1, cat2));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Tech"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Science"));
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        Category category = createCategory(1L, "Tech", null);
        when(categoryService.findById(1L)).thenReturn(Optional.of(category));

        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryService).deleteById(1L);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentCategory() throws Exception {
        when(categoryService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/categories/999"))
                .andExpect(status().isNotFound());

        verify(categoryService, never()).deleteById(any());
    }

    private Category createCategory(Long id, String name, String description) {
        Category category = new Category(name, description);
        category.setId(id);
        return category;
    }
}
