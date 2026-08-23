package com.na.article.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.na.article.dto.CreateArticleRequest;
import com.na.article.model.Article;
import com.na.article.model.Author;
import com.na.article.model.Category;
import com.na.article.repository.AuthorRepository;
import com.na.article.repository.CategoryRepository;
import com.na.article.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ArticleService articleService;

    @MockitoBean
    private AuthorRepository authorRepository;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @Test
    void shouldCreateArticle() throws Exception {
        Author author = createAuthor(1L, "John");
        Article article = createArticle(1L, "Title", "Content", author, null);

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(articleService.create("Title", "Content", author, null)).thenReturn(article);

        CreateArticleRequest request = new CreateArticleRequest("Title", "Content", 1L, null);

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/articles/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.content").value("Content"))
                .andExpect(jsonPath("$.authorId").value(1))
                .andExpect(jsonPath("$.authorName").value("John"))
                .andExpect(jsonPath("$.categoryId").doesNotExist())
                .andExpect(jsonPath("$.categoryName").doesNotExist());
    }

    @Test
    void shouldCreateArticleWithCategory() throws Exception {
        Author author = createAuthor(1L, "John");
        Category category = createCategory(1L, "Tech");
        Article article = createArticle(1L, "Title", "Content", author, category);

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(articleService.create("Title", "Content", author, category)).thenReturn(article);

        CreateArticleRequest request = new CreateArticleRequest("Title", "Content", 1L, 1L);

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.categoryId").value(1))
                .andExpect(jsonPath("$.categoryName").value("Tech"));
    }

    @Test
    void shouldReturn404WhenCreatingArticleWithNonExistentAuthor() throws Exception {
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        CreateArticleRequest request = new CreateArticleRequest("Title", "Content", 999L, null);

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(articleService, never()).create(any(), any(), any(), any());
    }

    @Test
    void shouldReturn404WhenCreatingArticleWithNonExistentCategory() throws Exception {
        Author author = createAuthor(1L, "John");
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        CreateArticleRequest request = new CreateArticleRequest("Title", "Content", 1L, 999L);

        mockMvc.perform(post("/api/articles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(articleService, never()).create(any(), any(), any(), any());
    }

    @Test
    void shouldFindArticleById() throws Exception {
        Author author = createAuthor(1L, "John");
        Article article = createArticle(1L, "Title", "Content", author, null);

        when(articleService.findById(1L)).thenReturn(Optional.of(article));

        mockMvc.perform(get("/api/articles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.content").value("Content"))
                .andExpect(jsonPath("$.authorId").value(1))
                .andExpect(jsonPath("$.authorName").value("John"));
    }

    @Test
    void shouldReturn404WhenArticleNotFound() throws Exception {
        when(articleService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/articles/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindArticlesByAuthor() throws Exception {
        Author author = createAuthor(1L, "John");
        Article article1 = createArticle(1L, "Title 1", "Content 1", author, null);
        Article article2 = createArticle(2L, "Title 2", "Content 2", author, null);

        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(articleService.findByAuthor(author)).thenReturn(List.of(article1, article2));

        mockMvc.perform(get("/api/articles").param("authorId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Title 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Title 2"));
    }

    @Test
    void shouldReturn404WhenListingArticlesForNonExistentAuthor() throws Exception {
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/articles").param("authorId", "999"))
                .andExpect(status().isNotFound());

        verify(articleService, never()).findByAuthor(any());
    }

    @Test
    void shouldFindArticlesByCategory() throws Exception {
        Author author = createAuthor(1L, "John");
        Category category = createCategory(1L, "Tech");
        Article article1 = createArticle(1L, "Title 1", "Content 1", author, category);
        Article article2 = createArticle(2L, "Title 2", "Content 2", author, category);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(articleService.findByCategory(category)).thenReturn(List.of(article1, article2));

        mockMvc.perform(get("/api/articles").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoryId").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Tech"))
                .andExpect(jsonPath("$[1].categoryId").value(1));
    }

    @Test
    void shouldReturn404WhenListingArticlesForNonExistentCategory() throws Exception {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/articles").param("categoryId", "999"))
                .andExpect(status().isNotFound());

        verify(articleService, never()).findByCategory(any());
    }

    @Test
    void shouldDeleteArticle() throws Exception {
        Author author = createAuthor(1L, "John");
        Article article = createArticle(1L, "Title", "Content", author, null);

        when(articleService.findById(1L)).thenReturn(Optional.of(article));

        mockMvc.perform(delete("/api/articles/1"))
                .andExpect(status().isNoContent());

        verify(articleService).deleteById(1L);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentArticle() throws Exception {
        when(articleService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/articles/999"))
                .andExpect(status().isNotFound());

        verify(articleService, never()).deleteById(any());
    }

    private Author createAuthor(Long id, String name) {
        Author author = new Author(name);
        author.setId(id);
        return author;
    }

    private Category createCategory(Long id, String name) {
        Category category = new Category(name);
        category.setId(id);
        return category;
    }

    private Article createArticle(Long id, String title, String content, Author author, Category category) {
        Article article = new Article();
        article.setId(id);
        article.setTitle(title);
        article.setContent(content);
        article.setAuthor(author);
        article.setCategory(category);
        article.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        return article;
    }
}
