package com.na.article.model;

import com.na.article.repository.ArticleRepository;
import com.na.article.repository.AuthorRepository;
import com.na.article.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CategoryEntityTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    void shouldPersistCategoryWithNameAndDescription() {
        Category category = categoryRepository.save(new Category("Technology", "Articles about tech"));

        assertThat(category.getId()).isNotNull();
        assertThat(category.getName()).isEqualTo("Technology");
        assertThat(category.getDescription()).isEqualTo("Articles about tech");
    }

    @Test
    void shouldPersistCategoryWithNullDescription() {
        Category category = categoryRepository.save(new Category("Science"));

        assertThat(category.getId()).isNotNull();
        assertThat(category.getName()).isEqualTo("Science");
        assertThat(category.getDescription()).isNull();
    }

    @Test
    void shouldPersistArticleWithCategory() {
        Author author = authorRepository.save(new Author("Jane"));
        Category category = categoryRepository.save(new Category("Tech"));

        Article article = new Article();
        article.setTitle("Test");
        article.setContent("Content");
        article.setAuthor(author);
        article.setCategory(category);
        articleRepository.save(article);

        Article found = articleRepository.findById(article.getId()).orElseThrow();
        assertThat(found.getCategory()).isNotNull();
        assertThat(found.getCategory().getId()).isEqualTo(category.getId());
        assertThat(found.getCategory().getName()).isEqualTo("Tech");
    }

    @Test
    void shouldPersistArticleWithoutCategory() {
        Author author = authorRepository.save(new Author("Jane"));

        Article article = new Article();
        article.setTitle("Test");
        article.setContent("Content");
        article.setAuthor(author);
        articleRepository.save(article);

        Article found = articleRepository.findById(article.getId()).orElseThrow();
        assertThat(found.getCategory()).isNull();
    }
}
