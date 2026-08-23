package com.na.article.controller;

import java.net.URI;
import java.util.List;

import com.na.article.dto.ArticleResponse;
import com.na.article.dto.CreateArticleRequest;
import com.na.article.model.Article;
import com.na.article.model.Author;
import com.na.article.model.Category;
import com.na.article.repository.AuthorRepository;
import com.na.article.repository.CategoryRepository;
import com.na.article.service.ArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
                .orElse(null);
        if (author == null) {
            return ResponseEntity.notFound().build();
        }

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository.findById(request.categoryId())
                    .orElse(null);
            if (category == null) {
                return ResponseEntity.notFound().build();
            }
        }

        Article article = articleService.create(request.title(), request.content(), author, category);
        ArticleResponse response = ArticleResponse.from(article);
        URI location = URI.create("/api/articles/" + article.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponse> findById(@PathVariable Long id) {
        return articleService.findById(id)
                .map(ArticleResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ArticleResponse>> findAll(
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long categoryId) {

        if (authorId != null) {
            Author author = authorRepository.findById(authorId).orElse(null);
            if (author == null) {
                return ResponseEntity.notFound().build();
            }
            List<ArticleResponse> responses = articleService.findByAuthor(author).stream()
                    .map(ArticleResponse::from)
                    .toList();
            return ResponseEntity.ok(responses);
        }

        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category == null) {
                return ResponseEntity.notFound().build();
            }
            List<ArticleResponse> responses = articleService.findByCategory(category).stream()
                    .map(ArticleResponse::from)
                    .toList();
            return ResponseEntity.ok(responses);
        }

        List<ArticleResponse> responses = articleService.findAll().stream()
                .map(ArticleResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (articleService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        articleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
