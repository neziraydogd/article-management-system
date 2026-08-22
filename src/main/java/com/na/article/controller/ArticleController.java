package com.na.article.controller;

import java.net.URI;
import java.util.List;

import com.na.article.dto.ArticleResponse;
import com.na.article.dto.CreateArticleRequest;
import com.na.article.model.Article;
import com.na.article.model.Author;
import com.na.article.repository.AuthorRepository;
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

    public ArticleController(ArticleService articleService, AuthorRepository authorRepository) {
        this.articleService = articleService;
        this.authorRepository = authorRepository;
    }

    @PostMapping
    public ResponseEntity<ArticleResponse> create(@RequestBody CreateArticleRequest request) {
        Author author = authorRepository.findById(request.authorId())
                .orElse(null);
        if (author == null) {
            return ResponseEntity.notFound().build();
        }

        Article article = articleService.create(request.title(), request.content(), author);
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
    public ResponseEntity<List<ArticleResponse>> findByAuthor(@RequestParam Long authorId) {
        Author author = authorRepository.findById(authorId)
                .orElse(null);
        if (author == null) {
            return ResponseEntity.notFound().build();
        }

        List<ArticleResponse> responses = articleService.findByAuthor(author).stream()
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
