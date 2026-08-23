package com.na.article.repository;

import java.util.List;

import com.na.article.model.Article;
import com.na.article.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByArticle(Article article);
}
