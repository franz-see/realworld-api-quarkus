package org.example.realworldapi.domain.service;

import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.exception.CommentNotFoundException;
import org.example.realworldapi.domain.model.comment.*;

import javax.inject.Singleton;
import java.util.List;
import java.util.UUID;

@Singleton
@AllArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentBuilder commentBuilder;
    private final UserService userService;
    private final ArticlesService articlesService;

    public Comment create(NewCommentInput newCommentInput) {
        final var author = userService.findById(newCommentInput.getAuthorId());
        final var article = articlesService.findBySlug(newCommentInput.getArticleSlug());
        final var comment = commentBuilder.build(author, article, newCommentInput.getBody());
        commentRepository.save(comment);
        return comment;
    }

    public void delete(DeleteCommentInput deleteCommentInput) {
        final var comment =
                findByIdAndAuthor(
                        deleteCommentInput.getCommentId(), deleteCommentInput.getAuthorId());
        commentRepository.delete(comment);
    }

    public Comment findByIdAndAuthor(UUID commentId, UUID authorId) {
        return commentRepository
                .findByIdAndAuthor(commentId, authorId)
                .orElseThrow(CommentNotFoundException::new);
    }

    public List<Comment> findByArticleSlug(String slug) {
        final var article = articlesService.findBySlug(slug);
        return commentRepository.findCommentsByArticle(article);
    }
}
