package com.na.article.service;

import java.util.List;
import java.util.Optional;

import com.na.article.model.Article;
import com.na.article.model.Comment;
import com.na.article.repository.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public Comment create(Article article, String authorName, String body) {
        Comment comment = new Comment();
        comment.setArticle(article);
        comment.setAuthorName(authorName);
        comment.setBody(body);
        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Comment> findByArticle(Article article) {
        return commentRepository.findByArticle(article);
    }

    public void deleteById(Long id) {
        commentRepository.deleteById(id);
    }
}
