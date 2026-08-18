package com.na.article.model;

import java.time.LocalDateTime;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ArticleEntityTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldPersistArticleWithAuthor() {
        Author author = new Author("Jane Doe");
        entityManager.persist(author);

        Article article = new Article();
        article.setTitle("Test Title");
        article.setContent("Test content body.");
        article.setAuthor(author);

        entityManager.persist(article);
        entityManager.flush();

        assertThat(article.getId()).isNotNull();
        assertThat(article.getTitle()).isEqualTo("Test Title");
        assertThat(article.getContent()).isEqualTo("Test content body.");
        assertThat(article.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(article.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldSetCreatedAtAutomatically() {
        LocalDateTime before = LocalDateTime.now();

        Author author = new Author("John Doe");
        entityManager.persist(author);

        Article article = new Article();
        article.setTitle("Timestamp Test");
        article.setContent("Content");
        article.setAuthor(author);

        entityManager.persist(article);
        entityManager.flush();

        assertThat(article.getCreatedAt()).isAfterOrEqualTo(before);
        assertThat(article.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void shouldAllowMultipleArticlesForSameAuthor() {
        Author author = new Author("Shared Author");
        entityManager.persist(author);

        Article first = new Article();
        first.setTitle("First Article");
        first.setContent("First content.");
        first.setAuthor(author);

        Article second = new Article();
        second.setTitle("Second Article");
        second.setContent("Second content.");
        second.setAuthor(author);

        entityManager.persist(first);
        entityManager.persist(second);
        entityManager.flush();

        Article foundFirst = entityManager.find(Article.class, first.getId());
        Article foundSecond = entityManager.find(Article.class, second.getId());

        assertThat(foundFirst.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(foundSecond.getAuthor().getId()).isEqualTo(author.getId());
    }
}
