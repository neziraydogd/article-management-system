package com.na.article.repository;

import java.util.List;

import com.na.article.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    List<Article> findByAuthorId(Long authorId);
}
