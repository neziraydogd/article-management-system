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

    public Article create(String title, String content, Author author, Category category) {
        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setAuthor(author);
        article.setCategory(category);
        return articleRepository.save(article);
    }

    @Transactional(readOnly = true)
    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Article> findByAuthor(Author author) {
        return articleRepository.findByAuthor(author);
    }

    @Transactional(readOnly = true)
    public List<Article> findByCategory(Category category) {
        return articleRepository.findByCategory(category);
    }

    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }
}
