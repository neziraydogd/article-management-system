package com.na.article.service;

import java.util.List;
import java.util.Optional;

import com.na.article.model.Category;
import com.na.article.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category create(String name, String description) {
        Category category = new Category(name, description);
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public void deleteById(Long id) {
        categoryRepository.deleteById(id);
    }
}
