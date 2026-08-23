package com.na.article.model;

import com.na.article.repository.ArticleRepository;
import com.na.article.repository.AuthorRepository;
import com.na.article.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CommentEntityTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    void shouldPersistCommentWithArticle() {
        Author author = authorRepository.save(new Author("Jane Doe"));
        Article article = new Article();
        article.setTitle("Test Article");
        article.setContent("Article content.");
        article.setAuthor(author);
        article = articleRepository.save(article);

        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setAuthorName("Commenter");
        comment.setBody("Great article!");
        comment = commentRepository.save(comment);

        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getArticle().getId()).isEqualTo(article.getId());
        assertThat(comment.getAuthorName()).isEqualTo("Commenter");
        assertThat(comment.getBody()).isEqualTo("Great article!");
        assertThat(comment.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldSetCreatedAtAutomatically() {
        Author author = authorRepository.save(new Author("Jane Doe"));
        Article article = new Article();
        article.setTitle("Test Article");
        article.setContent("Content.");
        article.setAuthor(author);
        article = articleRepository.save(article);

        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setAuthorName("Commenter");
        comment.setBody("Nice!");
        comment = commentRepository.save(comment);

        assertThat(comment.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldAllowMultipleCommentsForSameArticle() {
        Author author = authorRepository.save(new Author("Jane Doe"));
        Article article = new Article();
        article.setTitle("Test Article");
        article.setContent("Content.");
        article.setAuthor(author);
        article = articleRepository.save(article);

        Comment first = new Comment();
        first.setArticle(article);
        first.setAuthorName("Alice");
        first.setBody("First comment.");
        commentRepository.save(first);

        Comment second = new Comment();
        second.setArticle(article);
        second.setAuthorName("Bob");
        second.setBody("Second comment.");
        commentRepository.save(second);

        var comments = commentRepository.findByArticle(article);
        assertThat(comments).hasSize(2);
    }
}
