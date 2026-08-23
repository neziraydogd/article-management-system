package com.na.article.dto;

public record CreateCommentRequest(Long articleId, String authorName, String body) {
}
