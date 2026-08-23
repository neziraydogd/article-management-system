package com.na.article.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CategoryEntityTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void shouldPersistCategoryWithAllFields() {
        Category category = new Category("Technology", "Articles about tech");
        entityManager.persist(category);
        entityManager.flush();
        entityManager.clear();

        Category found = entityManager.find(Category.class, category.getId());

        assertThat(found.getId()).isNotNull();
        assertThat(found.getName()).isEqualTo("Technology");
        assertThat(found.getDescription()).isEqualTo("Articles about tech");
    }

    @Test
    void shouldPersistCategoryWithNullDescription() {
        Category category = new Category("Science", null);
        entityManager.persist(category);
        entityManager.flush();
        entityManager.clear();

        Category found = entityManager.find(Category.class, category.getId());

        assertThat(found.getName()).isEqualTo("Science");
        assertThat(found.getDescription()).isNull();
    }

    @Test
    void shouldNotPersistCategoryWithoutName() {
        Category category = new Category(null, "Some description");

        assertThatThrownBy(() -> {
            entityManager.persist(category);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    void shouldNotAllowDuplicateCategoryNames() {
        Category first = new Category("Unique", "First");
        entityManager.persist(first);
        entityManager.flush();

        Category duplicate = new Category("Unique", "Second");

        assertThatThrownBy(() -> {
            entityManager.persist(duplicate);
            entityManager.flush();
        }).isInstanceOf(Exception.class);
    }

    @Test
    void shouldAssignCategoryToArticle() {
        Author author = new Author("Writer");
        entityManager.persist(author);

        Category category = new Category("News", "News articles");
        entityManager.persist(category);

        Article article = new Article("Breaking News", "Content", author);
        article.setCategory(category);
        entityManager.persist(article);
        entityManager.flush();
        entityManager.clear();

        Article found = entityManager.find(Article.class, article.getId());

        assertThat(found.getCategory()).isNotNull();
        assertThat(found.getCategory().getId()).isEqualTo(category.getId());
        assertThat(found.getCategory().getName()).isEqualTo("News");
    }

    @Test
    void shouldAllowArticleWithoutCategory() {
        Author author = new Author("Writer");
        entityManager.persist(author);

        Article article = new Article("No Category", "Content", author);
        entityManager.persist(article);
        entityManager.flush();
        entityManager.clear();

        Article found = entityManager.find(Article.class, article.getId());

        assertThat(found.getCategory()).isNull();
    }
}
