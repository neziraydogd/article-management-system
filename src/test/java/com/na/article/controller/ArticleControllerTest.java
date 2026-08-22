package com.na.article.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.na.article.model.Article;
import com.na.article.model.Author;
import com.na.article.repository.AuthorRepository;
import com.na.article.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleService articleService;

    @MockitoBean
    private AuthorRepository authorRepository;

    @Test
    void shouldCreateArticle() throws Exception {
        Author author = new Author("Jane Doe");
        author.setId(1L);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        Article article = createArticle(10L, "New Article", "Article content.", author);
        when(articleService.create(eq("New Article"), eq("Article content."), any(Author.class)))
                .thenReturn(article);

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorId": 1, "title": "New Article", "content": "Article content."}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("New Article"))
                .andExpect(jsonPath("$.content").value("Article content."))
                .andExpect(jsonPath("$.authorId").value(1))
                .andExpect(jsonPath("$.authorName").value("Jane Doe"));
    }

    @Test
    void shouldReturnNotFoundWhenAuthorDoesNotExistOnCreate() throws Exception {
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorId": 999, "title": "Title", "content": "Content"}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnArticleById() throws Exception {
        Author author = new Author("Jane Doe");
        author.setId(1L);
        Article article = createArticle(10L, "Found Article", "Found content.", author);
        when(articleService.findById(10L)).thenReturn(Optional.of(article));

        mockMvc.perform(get("/api/articles/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Found Article"))
                .andExpect(jsonPath("$.content").value("Found content."))
                .andExpect(jsonPath("$.authorId").value(1))
                .andExpect(jsonPath("$.authorName").value("Jane Doe"));
    }

    @Test
    void shouldReturnNotFoundWhenArticleDoesNotExist() throws Exception {
        when(articleService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/articles/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnArticlesByAuthor() throws Exception {
        Author author = new Author("Jane Doe");
        author.setId(1L);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        Article first = createArticle(10L, "First", "First content.", author);
        Article second = createArticle(11L, "Second", "Second content.", author);
        when(articleService.findByAuthor(author)).thenReturn(List.of(first, second));

        mockMvc.perform(get("/api/articles").param("authorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("First"))
                .andExpect(jsonPath("$[1].title").value("Second"));
    }

    @Test
    void shouldReturnNotFoundWhenAuthorDoesNotExistOnList() throws Exception {
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/articles").param("authorId", "999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteArticle() throws Exception {
        mockMvc.perform(delete("/api/articles/10"))
                .andExpect(status().isNoContent());

        verify(articleService).deleteById(10L);
    }

    private Article createArticle(Long id, String title, String content, Author author) {
        Article article = new Article();
        article.setId(id);
        article.setTitle(title);
        article.setContent(content);
        article.setAuthor(author);
        article.setCreatedAt(LocalDateTime.now());
        return article;
    }
}
