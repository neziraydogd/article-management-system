package com.na.article.service;

import java.util.List;
import java.util.Optional;

import com.na.article.model.Category;
import com.na.article.repository.CategoryRepository;
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
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void shouldCreateCategory() {
        Category saved = new Category("Tech", "Technology articles");
        saved.setId(1L);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        Category result = categoryService.create("Tech", "Technology articles");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Tech");
        assertThat(result.getDescription()).isEqualTo("Technology articles");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void shouldCreateCategoryWithNullDescription() {
        Category saved = new Category("Tech");
        saved.setId(1L);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        Category result = categoryService.create("Tech", null);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Tech");
        assertThat(result.getDescription()).isNull();
    }

    @Test
    void shouldFindCategoryById() {
        Category category = new Category("Tech");
        category.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Optional<Category> result = categoryService.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Tech");
    }

    @Test
    void shouldReturnEmptyWhenCategoryNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Category> result = categoryService.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindAllCategories() {
        Category cat1 = new Category("Tech");
        cat1.setId(1L);
        Category cat2 = new Category("Science");
        cat2.setId(2L);
        when(categoryRepository.findAll()).thenReturn(List.of(cat1, cat2));

        List<Category> result = categoryService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Category::getName)
                .containsExactly("Tech", "Science");
    }

    @Test
    void shouldDeleteCategoryById() {
        categoryService.deleteById(1L);

        verify(categoryRepository).deleteById(1L);
    }
}
