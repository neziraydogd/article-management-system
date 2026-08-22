package com.na.article.controller;

import com.na.article.model.Author;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ArticleControllerTest {

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

    private String createArticleJson(String title, String content, Long authorId) {
        return """
                {"title":"%s","content":"%s","authorId":%d}""".formatted(title, content, authorId);
    }

    @Test
    void shouldCreateArticle() throws Exception {
        Author author = persistAuthor("Alice");

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createArticleJson("My Title", "My Content", author.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("My Title"))
                .andExpect(jsonPath("$.content").value("My Content"))
                .andExpect(jsonPath("$.authorId").value(author.getId()))
                .andExpect(jsonPath("$.authorName").value("Alice"));
    }

    @Test
    void shouldReturn404WhenAuthorNotFound() throws Exception {
        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createArticleJson("Title", "Content", 999L)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindArticleById() throws Exception {
        Author author = persistAuthor("Bob");
        String response = mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createArticleJson("Title", "Content", author.getId())))
                .andReturn().getResponse().getContentAsString();

        Long id = com.jayway.jsonpath.JsonPath.parse(response).read("$.id", Long.class);

        mockMvc.perform(get("/api/articles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.authorName").value("Bob"));
    }

    @Test
    void shouldReturn404WhenArticleNotFound() throws Exception {
        mockMvc.perform(get("/api/articles/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindArticlesByAuthor() throws Exception {
        Author author = persistAuthor("Carol");
        mockMvc.perform(post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createArticleJson("First", "Content 1", author.getId())));
        mockMvc.perform(post("/api/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createArticleJson("Second", "Content 2", author.getId())));

        mockMvc.perform(get("/api/articles/by-author/{authorId}", author.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].authorId").value(author.getId()));
    }

    @Test
    void shouldReturnEmptyListForUnknownAuthor() throws Exception {
        mockMvc.perform(get("/api/articles/by-author/{authorId}", 999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldDeleteArticle() throws Exception {
        Author author = persistAuthor("Dave");
        String response = mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createArticleJson("To Delete", "Content", author.getId())))
                .andReturn().getResponse().getContentAsString();

        Long id = com.jayway.jsonpath.JsonPath.parse(response).read("$.id", Long.class);

        mockMvc.perform(delete("/api/articles/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/articles/{id}", id))
                .andExpect(status().isNotFound());
    }
}
