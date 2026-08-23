package com.na.article.service;

import java.util.List;
import java.util.Optional;

import com.na.article.model.Article;
import com.na.article.model.Author;
import com.na.article.model.Category;
import com.na.article.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public Article create(String title, String content, Author author) {
        Article article = new Article(title, content, author);
        return articleRepository.save(article);
    }

    @Transactional(readOnly = true)
    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Article> findByAuthor(Long authorId) {
        return articleRepository.findByAuthorId(authorId);
    }

    @Transactional(readOnly = true)
    public List<Article> findByCategory(Long categoryId) {
        return articleRepository.findByCategoryId(categoryId);
    }

    public Article assignCategory(Long articleId, Category category) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("Article not found"));
        article.setCategory(category);
        return articleRepository.save(article);
    }

    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }
}
