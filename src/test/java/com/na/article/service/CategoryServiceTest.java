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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

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
    void shouldCreateCategory() {
        Category category = categoryService.create("Technology", "Tech articles");

        assertThat(category.getId()).isNotNull();
        assertThat(category.getName()).isEqualTo("Technology");
        assertThat(category.getDescription()).isEqualTo("Tech articles");
    }

    @Test
    void shouldCreateCategoryWithNullDescription() {
        Category category = categoryService.create("Science", null);

        assertThat(category.getId()).isNotNull();
        assertThat(category.getName()).isEqualTo("Science");
        assertThat(category.getDescription()).isNull();
    }

    @Test
    void shouldFindCategoryById() {
        Category created = categoryService.create("News", "News articles");

        Optional<Category> found = categoryService.findById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("News");
    }

    @Test
    void shouldReturnEmptyWhenCategoryNotFound() {
        Optional<Category> found = categoryService.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAllCategories() {
        categoryService.create("Tech", null);
        categoryService.create("Science", null);
        categoryService.create("News", null);

        List<Category> categories = categoryService.findAll();

        assertThat(categories).hasSize(3);
        assertThat(categories).extracting(Category::getName)
                .containsExactlyInAnyOrder("Tech", "Science", "News");
    }

    @Test
    void shouldReturnEmptyListWhenNoCategories() {
        List<Category> categories = categoryService.findAll();

        assertThat(categories).isEmpty();
    }

    @Test
    void shouldDeleteCategoryById() {
        Category category = categoryService.create("ToDelete", null);
        Long id = category.getId();

        categoryService.deleteById(id);
        entityManager.flush();
        entityManager.clear();

        Optional<Category> found = categoryService.findById(id);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldAssignCategoryToArticle() {
        Author author = persistAuthor("Alice");
        Article article = articleService.create("Title", "Content", author);
        Category category = categoryService.create("Tech", null);

        Article updated = articleService.assignCategory(article.getId(), category);

        assertThat(updated.getCategory()).isNotNull();
        assertThat(updated.getCategory().getId()).isEqualTo(category.getId());
    }

    @Test
    void shouldFindArticlesByCategory() {
        Author author = persistAuthor("Bob");
        Category tech = categoryService.create("Tech", null);
        Category news = categoryService.create("News", null);

        Article a1 = articleService.create("Tech Article", "Content", author);
        articleService.assignCategory(a1.getId(), tech);
        Article a2 = articleService.create("News Article", "Content", author);
        articleService.assignCategory(a2.getId(), news);
        Article a3 = articleService.create("Another Tech", "Content", author);
        articleService.assignCategory(a3.getId(), tech);

        List<Article> techArticles = articleService.findByCategory(tech.getId());

        assertThat(techArticles).hasSize(2);
        assertThat(techArticles).extracting(Article::getTitle)
                .containsExactlyInAnyOrder("Tech Article", "Another Tech");
    }

    @Test
    void shouldReturnEmptyListWhenCategoryHasNoArticles() {
        Category category = categoryService.create("Empty", null);

        List<Article> articles = articleService.findByCategory(category.getId());

        assertThat(articles).isEmpty();
    }

    @Test
    void shouldThrowWhenAssigningCategoryToNonexistentArticle() {
        Category category = categoryService.create("Tech", null);

        assertThatThrownBy(() -> articleService.assignCategory(999L, category))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
