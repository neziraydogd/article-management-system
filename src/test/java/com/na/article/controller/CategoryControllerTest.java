package com.na.article.controller;

import com.na.article.model.Author;
import com.na.article.model.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @PersistenceContext
    private EntityManager entityManager;

    private Author persistAuthor(String name) {
        Author author = new Author(name);
        entityManager.persist(author);
        entityManager.flush();
        return author;
    }

    private Category persistCategory(String name, String description) {
        Category category = new Category(name, description);
        entityManager.persist(category);
        entityManager.flush();
        return category;
    }

    private String createCategoryJson(String name, String description) {
        if (description != null) {
            return """
                    {"name":"%s","description":"%s"}""".formatted(name, description);
        }
        return """
                {"name":"%s"}""".formatted(name);
    }

    private String createArticleJson(String title, String content, Long authorId) {
        return """
                {"title":"%s","content":"%s","authorId":%d}""".formatted(title, content, authorId);
    }

    private String assignCategoryJson(Long categoryId) {
        return """
                {"categoryId":%d}""".formatted(categoryId);
    }

    @Test
    void shouldCreateCategory() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createCategoryJson("Technology", "Tech articles")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Technology"))
                .andExpect(jsonPath("$.description").value("Tech articles"));
    }

    @Test
    void shouldCreateCategoryWithNullDescription() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createCategoryJson("Science", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Science"))
                .andExpect(jsonPath("$.description").isEmpty());
    }

    @Test
    void shouldFindAllCategories() throws Exception {
        persistCategory("Tech", null);
        persistCategory("News", "News articles");

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnEmptyListWhenNoCategories() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldFindCategoryById() throws Exception {
        Category category = persistCategory("Technology", "Tech articles");

        mockMvc.perform(get("/api/categories/{id}", category.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Technology"))
                .andExpect(jsonPath("$.description").value("Tech articles"));
    }

    @Test
    void shouldReturn404WhenCategoryNotFound() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        Category category = persistCategory("ToDelete", null);

        mockMvc.perform(delete("/api/categories/{id}", category.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categories/{id}", category.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldAssignCategoryToArticle() throws Exception {
        Author author = persistAuthor("Alice");
        Category category = persistCategory("Tech", null);

        String articleResponse = mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createArticleJson("Title", "Content", author.getId())))
                .andReturn().getResponse().getContentAsString();

        Long articleId = com.jayway.jsonpath.JsonPath.parse(articleResponse).read("$.id", Long.class);

        mockMvc.perform(put("/api/articles/{id}/category", articleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignCategoryJson(category.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(category.getId()))
                .andExpect(jsonPath("$.categoryName").value("Tech"));
    }

    @Test
    void shouldReturn404WhenAssigningNonexistentCategory() throws Exception {
        Author author = persistAuthor("Bob");

        String articleResponse = mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createArticleJson("Title", "Content", author.getId())))
                .andReturn().getResponse().getContentAsString();

        Long articleId = com.jayway.jsonpath.JsonPath.parse(articleResponse).read("$.id", Long.class);

        mockMvc.perform(put("/api/articles/{id}/category", articleId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignCategoryJson(999L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListArticlesInCategory() throws Exception {
        Author author = persistAuthor("Carol");
        Category category = persistCategory("News", null);

        String a1 = mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createArticleJson("First", "Content 1", author.getId())))
                .andReturn().getResponse().getContentAsString();
        Long id1 = com.jayway.jsonpath.JsonPath.parse(a1).read("$.id", Long.class);

        String a2 = mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createArticleJson("Second", "Content 2", author.getId())))
                .andReturn().getResponse().getContentAsString();
        Long id2 = com.jayway.jsonpath.JsonPath.parse(a2).read("$.id", Long.class);

        mockMvc.perform(put("/api/articles/{id}/category", id1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(assignCategoryJson(category.getId())));
        mockMvc.perform(put("/api/articles/{id}/category", id2)
                .contentType(MediaType.APPLICATION_JSON)
                .content(assignCategoryJson(category.getId())));

        mockMvc.perform(get("/api/categories/{id}/articles", category.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnEmptyListForCategoryWithNoArticles() throws Exception {
        Category category = persistCategory("Empty", null);

        mockMvc.perform(get("/api/categories/{id}/articles", category.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldIncludeCategoryInArticleResponse() throws Exception {
        Author author = persistAuthor("Dave");
        Category category = persistCategory("Tech", "Technology");

        String articleResponse = mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createArticleJson("Title", "Content", author.getId())))
                .andReturn().getResponse().getContentAsString();

        Long articleId = com.jayway.jsonpath.JsonPath.parse(articleResponse).read("$.id", Long.class);

        mockMvc.perform(put("/api/articles/{id}/category", articleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(assignCategoryJson(category.getId())));

        mockMvc.perform(get("/api/articles/{id}", articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").value(category.getId()))
                .andExpect(jsonPath("$.categoryName").value("Tech"));
    }

    @Test
    void shouldReturnNullCategoryWhenArticleHasNone() throws Exception {
        Author author = persistAuthor("Eve");

        String articleResponse = mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createArticleJson("Title", "Content", author.getId())))
                .andReturn().getResponse().getContentAsString();

        Long articleId = com.jayway.jsonpath.JsonPath.parse(articleResponse).read("$.id", Long.class);

        mockMvc.perform(get("/api/articles/{id}", articleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryId").isEmpty())
                .andExpect(jsonPath("$.categoryName").isEmpty());
    }
}
