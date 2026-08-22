package com.na.article.dto;

import java.time.LocalDateTime;

import com.na.article.model.Article;

public record ArticleResponse(Long id, String title, String content, Long authorId, String authorName,
                               LocalDateTime createdAt) {

    public static ArticleResponse from(Article article) {
        return new ArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getContent(),
                article.getAuthor().getId(),
                article.getAuthor().getName(),
                article.getCreatedAt());
    }
}
