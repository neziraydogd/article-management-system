package com.na.article.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.na.article.dto.CreateCommentRequest;
import com.na.article.model.Article;
import com.na.article.model.Author;
import com.na.article.model.Comment;
import com.na.article.service.ArticleService;
import com.na.article.service.CommentService;
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

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private ArticleService articleService;

    @Test
    void shouldCreateComment() throws Exception {
        Article article = createArticle(1L);
        Comment comment = createComment(1L, article, "Alice", "Great article!");

        when(articleService.findById(1L)).thenReturn(Optional.of(article));
        when(commentService.create(article, "Alice", "Great article!")).thenReturn(comment);

        CreateCommentRequest request = new CreateCommentRequest(1L, "Alice", "Great article!");

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/comments/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.articleId").value(1))
                .andExpect(jsonPath("$.authorName").value("Alice"))
                .andExpect(jsonPath("$.body").value("Great article!"));
    }

    @Test
    void shouldReturn404WhenCreatingCommentForNonExistentArticle() throws Exception {
        when(articleService.findById(999L)).thenReturn(Optional.empty());

        CreateCommentRequest request = new CreateCommentRequest(999L, "Alice", "Comment");

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(commentService, never()).create(any(), any(), any());
    }

    @Test
    void shouldFindCommentById() throws Exception {
        Article article = createArticle(1L);
        Comment comment = createComment(1L, article, "Alice", "Nice!");

        when(commentService.findById(1L)).thenReturn(Optional.of(comment));

        mockMvc.perform(get("/api/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.articleId").value(1))
                .andExpect(jsonPath("$.authorName").value("Alice"))
                .andExpect(jsonPath("$.body").value("Nice!"));
    }

    @Test
    void shouldReturn404WhenCommentNotFound() throws Exception {
        when(commentService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/comments/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFindAllComments() throws Exception {
        Article article = createArticle(1L);
        Comment c1 = createComment(1L, article, "Alice", "First");
        Comment c2 = createComment(2L, article, "Bob", "Second");

        when(commentService.findAll()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].authorName").value("Alice"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].authorName").value("Bob"));
    }

    @Test
    void shouldFindCommentsByArticle() throws Exception {
        Article article = createArticle(1L);
        Comment c1 = createComment(1L, article, "Alice", "First");
        Comment c2 = createComment(2L, article, "Bob", "Second");

        when(articleService.findById(1L)).thenReturn(Optional.of(article));
        when(commentService.findByArticle(article)).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/api/comments").param("articleId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].articleId").value(1))
                .andExpect(jsonPath("$[1].articleId").value(1));
    }

    @Test
    void shouldReturn404WhenListingCommentsForNonExistentArticle() throws Exception {
        when(articleService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/comments").param("articleId", "999"))
                .andExpect(status().isNotFound());

        verify(commentService, never()).findByArticle(any());
    }

    @Test
    void shouldDeleteComment() throws Exception {
        Article article = createArticle(1L);
        Comment comment = createComment(1L, article, "Alice", "Delete me");

        when(commentService.findById(1L)).thenReturn(Optional.of(comment));

        mockMvc.perform(delete("/api/comments/1"))
                .andExpect(status().isNoContent());

        verify(commentService).deleteById(1L);
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentComment() throws Exception {
        when(commentService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/comments/999"))
                .andExpect(status().isNotFound());

        verify(commentService, never()).deleteById(any());
    }

    private Article createArticle(Long id) {
        Author author = new Author("John");
        author.setId(1L);
        Article article = new Article();
        article.setId(id);
        article.setTitle("Test Article");
        article.setContent("Content");
        article.setAuthor(author);
        article.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        return article;
    }

    private Comment createComment(Long id, Article article, String authorName, String body) {
        Comment comment = new Comment();
        comment.setId(id);
        comment.setArticle(article);
        comment.setAuthorName(authorName);
        comment.setBody(body);
        comment.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        return comment;
    }
}
