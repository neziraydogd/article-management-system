package com.na.article.model;

import java.time.LocalDateTime;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ArticleEntityTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldPersistArticleWithAllFields() {
        Author author = new Author("John Doe");
        entityManager.persist(author);

        Article article = new Article("Test Title", "Test Content", author);
        entityManager.persist(article);
        entityManager.flush();
        entityManager.clear();

        Article found = entityManager.find(Article.class, article.getId());

        assertThat(found.getId()).isNotNull();
        assertThat(found.getTitle()).isEqualTo("Test Title");
        assertThat(found.getContent()).isEqualTo("Test Content");
        assertThat(found.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void shouldAutoPopulateCreatedAtOnPersist() {
        LocalDateTime before = LocalDateTime.now();

        Author author = new Author("Jane Doe");
        entityManager.persist(author);

        Article article = new Article("Title", "Content", author);
        entityManager.persist(article);
        entityManager.flush();

        assertThat(article.getCreatedAt()).isAfterOrEqualTo(before);
        assertThat(article.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void shouldNotPersistArticleWithoutTitle() {
        Author author = new Author("Author");
        entityManager.persist(author);

        Article article = new Article(null, "Content", author);

        assertThatThrownBy(() -> {
            entityManager.persist(article);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    void shouldNotPersistArticleWithoutContent() {
        Author author = new Author("Author");
        entityManager.persist(author);

        Article article = new Article("Title", null, author);

        assertThatThrownBy(() -> {
            entityManager.persist(article);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    void shouldNotPersistArticleWithoutAuthor() {
        Article article = new Article("Title", "Content", null);

        assertThatThrownBy(() -> {
            entityManager.persist(article);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    void shouldAllowMultipleArticlesBySameAuthor() {
        Author author = new Author("Prolific Writer");
        entityManager.persist(author);

        Article first = new Article("First", "Content 1", author);
        Article second = new Article("Second", "Content 2", author);
        entityManager.persist(first);
        entityManager.persist(second);
        entityManager.flush();

        assertThat(first.getId()).isNotEqualTo(second.getId());
        assertThat(first.getAuthor().getId()).isEqualTo(second.getAuthor().getId());
    }
}
