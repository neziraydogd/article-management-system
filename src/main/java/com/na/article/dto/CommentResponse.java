package com.na.article.dto;

import java.time.LocalDateTime;

import com.na.article.model.Comment;

public record CommentResponse(Long id, Long articleId, String authorName, String body, LocalDateTime createdAt) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getArticle().getId(),
                comment.getAuthorName(),
                comment.getBody(),
                comment.getCreatedAt());
    }
}
