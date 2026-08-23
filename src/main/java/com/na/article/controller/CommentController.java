package com.na.article.controller;

import java.net.URI;
import java.util.List;

import com.na.article.dto.CommentResponse;
import com.na.article.dto.CreateCommentRequest;
import com.na.article.model.Article;
import com.na.article.service.ArticleService;
import com.na.article.service.CommentService;
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
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;
    private final ArticleService articleService;

    public CommentController(CommentService commentService, ArticleService articleService) {
        this.commentService = commentService;
        this.articleService = articleService;
    }

    @PostMapping
    public ResponseEntity<CommentResponse> create(@RequestBody CreateCommentRequest request) {
        Article article = articleService.findById(request.articleId()).orElse(null);
        if (article == null) {
            return ResponseEntity.notFound().build();
        }

        var comment = commentService.create(article, request.authorName(), request.body());
        CommentResponse response = CommentResponse.from(comment);
        URI location = URI.create("/api/comments/" + comment.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> findById(@PathVariable Long id) {
        return commentService.findById(id)
                .map(CommentResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> findAll(
            @RequestParam(required = false) Long articleId) {

        if (articleId != null) {
            Article article = articleService.findById(articleId).orElse(null);
            if (article == null) {
                return ResponseEntity.notFound().build();
            }
            List<CommentResponse> responses = commentService.findByArticle(article).stream()
                    .map(CommentResponse::from)
                    .toList();
            return ResponseEntity.ok(responses);
        }

        List<CommentResponse> responses = commentService.findAll().stream()
                .map(CommentResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        if (commentService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        commentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
