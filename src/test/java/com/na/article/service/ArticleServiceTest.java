package com.na.article.service;

import java.util.List;
import java.util.Optional;

import com.na.article.model.Article;
import com.na.article.model.Author;
import com.na.article.model.Category;
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

    @Test
    void shouldCreateArticleForAuthor() {
        Author author = new Author("Jane Doe");
        entityManager.persist(author);
        entityManager.flush();

        Article article = articleService.create("Test Title", "Test content.", author, null);

        assertThat(article.getId()).isNotNull();
        assertThat(article.getTitle()).isEqualTo("Test Title");
        assertThat(article.getContent()).isEqualTo("Test content.");
        assertThat(article.getAuthor().getId()).isEqualTo(author.getId());
        assertThat(article.getCategory()).isNull();
        assertThat(article.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldCreateArticleWithCategory() {
        Author author = new Author("Jane Doe");
        entityManager.persist(author);
        Category category = new Category("Tech", "Technology articles");
        entityManager.persist(category);
        entityManager.flush();

        Article article = articleService.create("Test Title", "Test content.", author, category);

        assertThat(article.getId()).isNotNull();
        assertThat(article.getCategory()).isNotNull();
        assertThat(article.getCategory().getId()).isEqualTo(category.getId());
        assertThat(article.getCategory().getName()).isEqualTo("Tech");
    }

    @Test
    void shouldFindArticleById() {
        Author author = new Author("Jane Doe");
        entityManager.persist(author);

        Article article = new Article();
        article.setTitle("Findable Article");
        article.setContent("Some content.");
        article.setAuthor(author);
        entityManager.persist(article);
        entityManager.flush();

        Optional<Article> found = articleService.findById(article.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Findable Article");
    }

    @Test
    void shouldReturnEmptyWhenArticleNotFound() {
        Optional<Article> found = articleService.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllArticlesByAuthor() {
        Author author = new Author("Jane Doe");
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

        List<Article> articles = articleService.findByAuthor(author);

        assertThat(articles).hasSize(2);
        assertThat(articles).extracting(Article::getTitle)
                .containsExactlyInAnyOrder("First Article", "Second Article");
    }

    @Test
    void shouldReturnEmptyListWhenAuthorHasNoArticles() {
        Author author = new Author("No Articles Author");
        entityManager.persist(author);
        entityManager.flush();

        List<Article> articles = articleService.findByAuthor(author);

        assertThat(articles).isEmpty();
    }

    @Test
    void shouldFindArticlesByCategory() {
        Author author = new Author("Jane Doe");
        entityManager.persist(author);
        Category category = new Category("Tech");
        entityManager.persist(category);

        Article first = new Article();
        first.setTitle("First");
        first.setContent("Content 1");
        first.setAuthor(author);
        first.setCategory(category);

        Article second = new Article();
        second.setTitle("Second");
        second.setContent("Content 2");
        second.setAuthor(author);
        second.setCategory(category);

        entityManager.persist(first);
        entityManager.persist(second);
        entityManager.flush();

        List<Article> articles = articleService.findByCategory(category);

        assertThat(articles).hasSize(2);
        assertThat(articles).extracting(Article::getTitle)
                .containsExactlyInAnyOrder("First", "Second");
    }

    @Test
    void shouldReturnEmptyListWhenCategoryHasNoArticles() {
        Category category = new Category("Empty");
        entityManager.persist(category);
        entityManager.flush();

        List<Article> articles = articleService.findByCategory(category);

        assertThat(articles).isEmpty();
    }

    @Test
    void shouldDeleteArticleById() {
        Author author = new Author("Jane Doe");
        entityManager.persist(author);

        Article article = new Article();
        article.setTitle("To Be Deleted");
        article.setContent("Delete me.");
        article.setAuthor(author);
        entityManager.persist(article);
        entityManager.flush();

        Long articleId = article.getId();
        articleService.deleteById(articleId);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(Article.class, articleId)).isNull();
    }

    @Test
    void shouldNotAffectOtherArticlesWhenDeleting() {
        Author author = new Author("Jane Doe");
        entityManager.persist(author);

        Article keep = new Article();
        keep.setTitle("Keep This");
        keep.setContent("Keep content.");
        keep.setAuthor(author);

        Article delete = new Article();
        delete.setTitle("Delete This");
        delete.setContent("Delete content.");
        delete.setAuthor(author);

        entityManager.persist(keep);
        entityManager.persist(delete);
        entityManager.flush();

        articleService.deleteById(delete.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(Article.class, keep.getId())).isNotNull();
        assertThat(entityManager.find(Article.class, delete.getId())).isNull();
    }
}
