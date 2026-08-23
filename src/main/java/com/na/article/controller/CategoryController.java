package com.na.article.controller;

import java.net.URI;
import java.util.List;

import com.na.article.dto.CategoryResponse;
import com.na.article.dto.CreateCategoryRequest;
import com.na.article.model.Category;
import com.na.article.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestBody CreateCategoryRequest request) {
        Category category = categoryService.create(request.name(), request.description());
        CategoryResponse response = CategoryResponse.from(category);
        URI location = URI.create("/api/categories/" + category.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(CategoryResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll() {
        List<CategoryResponse> responses = categoryService.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (categoryService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
