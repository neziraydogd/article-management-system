package com.na.article.service;

import java.util.List;
import java.util.Optional;

import com.na.article.model.Article;
import com.na.article.model.Author;
import com.na.article.model.Comment;
import com.na.article.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void shouldCreateComment() {
        Article article = createArticle(1L);
        Comment saved = new Comment();
        saved.setId(1L);
        saved.setArticle(article);
        saved.setAuthorName("Alice");
        saved.setBody("Great article!");
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        Comment result = commentService.create(article, "Alice", "Great article!");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getArticle().getId()).isEqualTo(1L);
        assertThat(result.getAuthorName()).isEqualTo("Alice");
        assertThat(result.getBody()).isEqualTo("Great article!");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void shouldFindCommentById() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setAuthorName("Alice");
        comment.setBody("Nice!");
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        Optional<Comment> result = commentService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getAuthorName()).isEqualTo("Alice");
    }

    @Test
    void shouldReturnEmptyWhenCommentNotFound() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Comment> result = commentService.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllComments() {
        Comment c1 = new Comment();
        c1.setId(1L);
        c1.setAuthorName("Alice");
        Comment c2 = new Comment();
        c2.setId(2L);
        c2.setAuthorName("Bob");
        when(commentRepository.findAll()).thenReturn(List.of(c1, c2));

        List<Comment> result = commentService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Comment::getAuthorName)
                .containsExactly("Alice", "Bob");
    }

    @Test
    void shouldFindCommentsByArticle() {
        Article article = createArticle(1L);
        Comment c1 = new Comment();
        c1.setId(1L);
        c1.setArticle(article);
        Comment c2 = new Comment();
        c2.setId(2L);
        c2.setArticle(article);
        when(commentRepository.findByArticle(article)).thenReturn(List.of(c1, c2));

        List<Comment> result = commentService.findByArticle(article);

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldDeleteCommentById() {
        commentService.deleteById(1L);

        verify(commentRepository).deleteById(1L);
    }

    private Article createArticle(Long id) {
        Author author = new Author("Jane");
        author.setId(1L);
        Article article = new Article();
        article.setId(id);
        article.setTitle("Test");
        article.setContent("Content");
        article.setAuthor(author);
        return article;
    }
}
