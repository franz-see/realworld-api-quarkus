package org.example.realworldapi.domain.service;

import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.exception.ArticleNotFoundException;
import org.example.realworldapi.domain.model.article.*;
import org.example.realworldapi.domain.validator.ModelValidator;

import javax.inject.Singleton;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Singleton
@AllArgsConstructor
public class ArticlesService {

    private final ArticleRepository articleRepository;
    private final FavoriteRelationshipRepository favoriteRelationshipRepository;
    private final UserService userService;
    private final SlugService slugService;
    private final TagService tagService;
    private final ArticleModelBuilder articleBuilder;
    private final TagRelationshipService tagRelationshipService;
    private final ModelValidator modelValidator;

    public PageResult<Article> findMostRecentByFilter(ArticleFilter articleFilter) {
        return articleRepository.findMostRecentArticlesByFilter(articleFilter);
    }

    public PageResult<Article> findByFilter(ArticleFilter articleFilter) {
        return articleRepository.findArticlesByFilter(articleFilter);
    }

    public Article create(NewArticleInput newArticleInput) {
        final var author = userService.findById(newArticleInput.getAuthorId());
        final var slug = slugService.createByTitle(newArticleInput.getTitle());
        final var article =
                articleBuilder.build(
                        slug,
                        newArticleInput.getTitle(),
                        newArticleInput.getDescription(),
                        newArticleInput.getBody(),
                        author);
        articleRepository.save(article);
        final var tags = tagService.findByNameCreateIfNotExists(newArticleInput.getTagList());
        tagRelationshipService.createTagRelationship(article, tags);
        return article;
    }

    public Article findBySlug(String slug) {
        return articleRepository.findBySlug(slug).orElseThrow(ArticleNotFoundException::new);
    }

    public Article updateBySlug(UpdateArticleInput updateArticleInput) {
        final var article = findBySlug(updateArticleInput.getSlug());
        if (atLeastOneFieldIsNotBlank(updateArticleInput)) {
            if (isNotBlank(updateArticleInput.getTitle())) {
                article.setSlug(slugService.createByTitle(updateArticleInput.getTitle()));
                article.setTitle(updateArticleInput.getTitle());
            }
            if (isNotBlank(updateArticleInput.getDescription())) {
                article.setDescription(updateArticleInput.getDescription());
            }
            if (isNotBlank(updateArticleInput.getBody())) {
                article.setBody(updateArticleInput.getBody());
            }
            article.setUpdatedAt(LocalDateTime.now());
            articleRepository.update(modelValidator.validate(article));
        }
        return article;
    }

    public Article findByAuthorAndSlug(UUID authorId, String slug) {
        return articleRepository
                .findByAuthorAndSlug(authorId, slug)
                .orElseThrow(ArticleNotFoundException::new);
    }

    public void deleteBySlug(UUID authorId, String slug) {
        final var article = findByAuthorAndSlug(authorId, slug);
        articleRepository.delete(article);
    }

    public FavoriteRelationship favorite(String articleSlug, UUID currentUserId) {
        final var article = findBySlug(articleSlug);
        final var favoriteRelationshipOptional =
                favoriteRelationshipRepository.findByArticleIdAndUserId(article.getId(), currentUserId);
        return favoriteRelationshipOptional.orElse(createFavoriteRelationship(currentUserId, article));
    }

    public void unfavorite(String articleSlug, UUID currentUserId) {
        final var article = findBySlug(articleSlug);
        final var favoriteRelationshipOptional =
                favoriteRelationshipRepository.findByArticleIdAndUserId(article.getId(), currentUserId);
        favoriteRelationshipOptional.ifPresent(favoriteRelationshipRepository::delete);
    }

    public Article findById(UUID id) {
        return articleRepository.findArticleById(id).orElseThrow(ArticleNotFoundException::new);
    }

    public long favoritesCount(UUID articleId) {
        final var article = findById(articleId);
        return favoriteRelationshipRepository.favoritesCount(article);
    }

    public boolean isArticleFavorited(Article article, UUID currentUserId) {
        return favoriteRelationshipRepository.isFavorited(article, currentUserId);
    }

    private boolean atLeastOneFieldIsNotBlank(UpdateArticleInput updateArticleInput) {
        return isNotBlank(updateArticleInput.getTitle())
                || isNotBlank(updateArticleInput.getDescription())
                || isNotBlank(updateArticleInput.getBody());
    }

    private FavoriteRelationship createFavoriteRelationship(UUID currentUserId, Article article) {
        final var user = userService.findById(currentUserId);
        final var favoriteRelationship = new FavoriteRelationship(user, article);
        favoriteRelationshipRepository.save(favoriteRelationship);
        return favoriteRelationship;
    }
}
