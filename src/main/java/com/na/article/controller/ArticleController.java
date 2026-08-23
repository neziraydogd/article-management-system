package com.na.article.controller;

import java.util.List;

import com.na.article.model.Article;
import com.na.article.model.Author;
import com.na.article.model.Category;
import com.na.article.repository.AuthorRepository;
import com.na.article.repository.CategoryRepository;
import com.na.article.service.ArticleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    public ArticleController(ArticleService articleService, AuthorRepository authorRepository,
                             CategoryRepository categoryRepository) {
        this.articleService = articleService;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
    }

    @PostMapping
    public ResponseEntity<ArticleResponse> create(@RequestBody CreateArticleRequest request) {
        Author author = authorRepository.findById(request.authorId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
        Article article = articleService.create(request.title(), request.content(), author);
        return ResponseEntity.status(HttpStatus.CREATED).body(ArticleResponse.from(article));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> findById(@PathVariable Long id) {
        return articleService.findById(id)
                .map(article -> ResponseEntity.ok(ArticleResponse.from(article)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-author/{authorId}")
    public ResponseEntity<List<ArticleResponse>> findByAuthor(@PathVariable Long authorId) {
        List<ArticleResponse> articles = articleService.findByAuthor(authorId).stream()
                .map(ArticleResponse::from)
                .toList();
        return ResponseEntity.ok(articles);
    }

    @PutMapping("/{id}/category")
    public ResponseEntity<ArticleResponse> assignCategory(@PathVariable Long id,
                                                          @RequestBody AssignCategoryRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        Article article = articleService.assignCategory(id, category);
        return ResponseEntity.ok(ArticleResponse.from(article));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        articleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    record CreateArticleRequest(String title, String content, Long authorId) {
    }

    record AssignCategoryRequest(Long categoryId) {
    }

    record ArticleResponse(Long id, String title, String content, Long authorId, String authorName,
                           Long categoryId, String categoryName) {
        static ArticleResponse from(Article article) {
            Category category = article.getCategory();
            return new ArticleResponse(
                    article.getId(),
                    article.getTitle(),
                    article.getContent(),
                    article.getAuthor().getId(),
                    article.getAuthor().getName(),
                    category != null ? category.getId() : null,
                    category != null ? category.getName() : null
            );
        }
    }
}
