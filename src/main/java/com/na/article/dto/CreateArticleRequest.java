package com.na.article.dto;

public record CreateArticleRequest(String title, String content, Long authorId) {
}
