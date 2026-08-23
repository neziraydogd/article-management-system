package com.na.article.controller;

import java.util.List;

import com.na.article.model.Article;
import com.na.article.model.Category;
import com.na.article.service.ArticleService;
import com.na.article.service.CategoryService;
import org.springframework.http.HttpStatus;
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
    private final ArticleService articleService;

    public CategoryController(CategoryService categoryService, ArticleService articleService) {
        this.categoryService = categoryService;
        this.articleService = articleService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestBody CreateCategoryRequest request) {
        Category category = categoryService.create(request.name(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoryResponse.from(category));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> findAll() {
        List<CategoryResponse> categories = categoryService.findAll().stream()
                .map(CategoryResponse::from)
                .toList();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> findById(@PathVariable Long id) {
        return categoryService.findById(id)
                .map(category -> ResponseEntity.ok(CategoryResponse.from(category)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        categoryService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/articles")
    public ResponseEntity<List<ArticleController.ArticleResponse>> findArticlesByCategory(@PathVariable Long id) {
        List<ArticleController.ArticleResponse> articles = articleService.findByCategory(id).stream()
                .map(ArticleController.ArticleResponse::from)
                .toList();
        return ResponseEntity.ok(articles);
    }

    record CreateCategoryRequest(String name, String description) {
    }

    record CategoryResponse(Long id, String name, String description) {
        static CategoryResponse from(Category category) {
            return new CategoryResponse(
                    category.getId(),
                    category.getName(),
                    category.getDescription()
            );
        }
    }
}
