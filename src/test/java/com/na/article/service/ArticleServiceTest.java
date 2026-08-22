package com.na.article.service;

import java.util.List;
import java.util.Optional;

import com.na.article.model.Article;
import com.na.article.model.Author;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ArticleServiceTest {

    @Autowired
    private ArticleService articleService;

    @PersistenceContext
    private EntityManager entityManager;

    private Author persistAuthor(String name) {
        Author author = new Author(name);
        entityManager.persist(author);
        entityManager.flush();
        return author;
    }

    @Test
    void shouldCreateArticle() {
        Author author = persistAuthor("Alice");

        Article article = articleService.create("My Title", "My Content", author);

        assertThat(article.getId()).isNotNull();
        assertThat(article.getTitle()).isEqualTo("My Title");
        assertThat(article.getContent()).isEqualTo("My Content");
        assertThat(article.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(article.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindArticleById() {
        Author author = persistAuthor("Bob");
        Article created = articleService.create("Title", "Content", author);

        Optional<Article> found = articleService.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Title");
    }

    @Test
    void shouldReturnEmptyWhenArticleNotFound() {
        Optional<Article> found = articleService.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindArticlesByAuthor() {
        Author author = persistAuthor("Carol");
        articleService.create("First", "Content 1", author);
        articleService.create("Second", "Content 2", author);

        List<Article> articles = articleService.findByAuthor(author.getId());

        assertThat(articles).hasSize(2);
        assertThat(articles).extracting(Article::getTitle).containsExactlyInAnyOrder("First", "Second");
    }

    @Test
    void shouldReturnEmptyListWhenAuthorHasNoArticles() {
        Author author = persistAuthor("Dave");

        List<Article> articles = articleService.findByAuthor(author.getId());

        assertThat(articles).isEmpty();
    }

    @Test
    void shouldNotReturnArticlesFromOtherAuthors() {
        Author alice = persistAuthor("Alice");
        Author bob = persistAuthor("Bob");
        articleService.create("Alice's Article", "Content", alice);
        articleService.create("Bob's Article", "Content", bob);

        List<Article> aliceArticles = articleService.findByAuthor(alice.getId());

        assertThat(aliceArticles).hasSize(1);
        assertThat(aliceArticles.get(0).getTitle()).isEqualTo("Alice's Article");
    }

    @Test
    void shouldDeleteArticleById() {
        Author author = persistAuthor("Eve");
        Article article = articleService.create("To Delete", "Content", author);
        Long id = article.getId();

        articleService.deleteById(id);
        entityManager.flush();
        entityManager.clear();

        Optional<Article> found = articleService.findById(id);
        assertThat(found).isEmpty();
    }
}
